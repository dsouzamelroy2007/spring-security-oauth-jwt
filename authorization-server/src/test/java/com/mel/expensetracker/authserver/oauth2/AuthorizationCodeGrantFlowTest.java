package com.mel.expensetracker.authserver.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import com.mel.expensetracker.authserver.support.AbstractIntegrationTest;
import com.mel.expensetracker.authserver.support.JwtTestSupport;
import com.mel.expensetracker.authserver.support.Pkce;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.client.ExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Drives a real authorization_code + PKCE run against every hop a browser would
 * take -- login, consent, token exchange -- proving acceptance criteria 3 and 4
 * end to end rather than unit-testing any single piece in isolation.
 */
class AuthorizationCodeGrantFlowTest extends AbstractIntegrationTest {

    private static final String REDIRECT_URI = "http://127.0.0.1:8080/login/oauth2/code/bff-client";
    private static final Pattern CSRF_PATTERN =
            Pattern.compile("name=\"_csrf\"\\s+value=\"([^\"]+)\"");

    private String sessionCookie;

    @Test
    void authorizationCodeWithPkceYieldsAccessAndIdTokenWithOrgAndRoleClaims() {
        RestTestClient client = client();
        Pkce pkce = Pkce.generate();

        URI authorizeUri = UriComponentsBuilder.fromPath("/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", "bff-client")
                .queryParam("scope", "openid expenses.read")
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("state", "test-state")
                .queryParam("code_challenge", pkce.challenge())
                .queryParam("code_challenge_method", "S256")
                .build()
                .toUri();

        // 1. Unauthenticated hit on /oauth2/authorize -> redirected to the login page.
        ExchangeResult toLogin = client.get()
                .uri(authorizeUri)
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .returnResult();
        rememberSessionCookie(toLogin);
        URI loginUri = toLogin.getResponseHeaders().getLocation();
        // Path may carry a ";jsessionid=..." matrix param: the very first
        // redirect happens before any cookie has round-tripped, so Tomcat
        // falls back to URL-based session tracking until it does.
        assertThat(loginUri.getPath()).startsWith("/login");

        // 2. GET the login page to obtain a CSRF token bound to this session.
        String loginHtml = bodyAsString(client.get().uri(loginUri).cookie("JSESSIONID", sessionCookie).exchange()
                .expectStatus()
                .isOk()
                .returnResult());
        String loginCsrf = csrfToken(loginHtml);

        // 3. Submit credentials -> redirected back to the originally saved /oauth2/authorize request.
        ExchangeResult afterLogin = client.post()
                .uri("/login")
                .cookie("JSESSIONID", sessionCookie)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("username=alice&password=alice-demo-pw&_csrf=" + loginCsrf)
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .returnResult();
        rememberSessionCookie(afterLogin);
        URI backToAuthorize = afterLogin.getResponseHeaders().getLocation();

        // 4. Re-request /oauth2/authorize, now authenticated -> consent required (not yet granted).
        ExchangeResult toConsent = client.get()
                .uri(backToAuthorize)
                .accept(MediaType.TEXT_HTML)
                .cookie("JSESSIONID", sessionCookie)
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .returnResult();
        rememberSessionCookie(toConsent);
        URI consentUri = toConsent.getResponseHeaders().getLocation();
        assertThat(consentUri.getPath()).startsWith("/oauth2/consent");

        // 5. GET the consent page (rendered by ConsentController) for its CSRF token.
        String consentHtml = bodyAsString(client.get().uri(consentUri).cookie("JSESSIONID", sessionCookie).exchange()
                .expectStatus()
                .isOk()
                .returnResult());
        String consentCsrf = csrfToken(consentHtml);

        Map<String, String> consentParams = UriComponentsBuilder.newInstance()
                .query(consentUri.getRawQuery())
                .build()
                .getQueryParams()
                .toSingleValueMap();

        // 6. Approve consent -> posts back to /oauth2/authorize, which issues the code.
        ExchangeResult withCode = client.post()
                .uri("/oauth2/authorize")
                .cookie("JSESSIONID", sessionCookie)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("client_id=%s&state=%s&scope=expenses.read&_csrf=%s"
                        .formatted(consentParams.get("client_id"), consentParams.get("state"), consentCsrf))
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .returnResult();
        URI redirectWithCode = withCode.getResponseHeaders().getLocation();
        assertThat(redirectWithCode.toString()).startsWith(REDIRECT_URI);
        String code = UriComponentsBuilder.newInstance()
                .query(redirectWithCode.getRawQuery())
                .build()
                .getQueryParams()
                .getFirst("code");
        assertThat(code).isNotBlank();

        // 7. Exchange the code (+ PKCE verifier) for tokens.
        String basicAuth = Base64.getEncoder()
                .encodeToString("bff-client:bff-client-secret".getBytes(StandardCharsets.UTF_8));
        Map<String, Object> tokenResponse = client.post()
                .uri("/oauth2/token")
                .header("Authorization", "Basic " + basicAuth)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=authorization_code&code=%s&redirect_uri=%s&code_verifier=%s"
                        .formatted(code, REDIRECT_URI, pkce.verifier()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(tokenResponse).containsKeys("access_token", "id_token");

        Map<String, Object> accessClaims =
                JwtTestSupport.decodeClaims((String) tokenResponse.get("access_token"));
        assertThat(accessClaims).containsEntry("org", "acme");
        assertThat((List<String>) accessClaims.get("roles")).contains("EMPLOYEE");

        Map<String, Object> idClaims = JwtTestSupport.decodeClaims((String) tokenResponse.get("id_token"));
        assertThat(idClaims).containsEntry("org", "acme");
    }

    private void rememberSessionCookie(ExchangeResult result) {
        List<ResponseCookie> cookies = result.getResponseCookies().get("JSESSIONID");
        if (cookies != null && !cookies.isEmpty()) {
            sessionCookie = cookies.get(0).getValue();
        }
    }

    private static String bodyAsString(ExchangeResult result) {
        return new String(result.getResponseBodyContent(), StandardCharsets.UTF_8);
    }

    private static String csrfToken(String html) {
        Matcher matcher = CSRF_PATTERN.matcher(html);
        if (!matcher.find()) {
            throw new IllegalStateException("No CSRF token found in response body:\n" + html);
        }
        return matcher.group(1);
    }
}
