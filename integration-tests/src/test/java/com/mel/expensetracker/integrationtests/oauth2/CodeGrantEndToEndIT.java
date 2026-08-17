package com.mel.expensetracker.integrationtests.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import com.mel.expensetracker.integrationtests.support.CrossModuleTestSupport;
import com.mel.expensetracker.integrationtests.support.JwtClaims;
import com.mel.expensetracker.integrationtests.support.Pkce;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.client.ExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Drives a genuine authorization_code + PKCE grant across two real,
 * independently running processes -- authorization-server (port 9000) and
 * resource-server (port 8082) -- proving M5's acceptance bar: a real JWT,
 * from a real grant, accepted by the real API. Also proves two negative
 * paths that only a live authorization-server can demonstrate: revoked-
 * refresh-token rejection and one-time authorization-code reuse rejection.
 * Both are Spring Authorization Server's own default behavior (JDBC-backed
 * revocation, single-use codes) -- nothing here is bespoke project code, the
 * test exists to prove the default is actually wired up end to end.
 *
 * <p>Adapted from authorization-server's own (same-module)
 * {@code AuthorizationCodeGrantFlowTest}, against the cross-module ports
 * {@link CrossModuleTestSupport} boots instead of a single random port.
 */
class CodeGrantEndToEndIT extends CrossModuleTestSupport {

    private static final String REDIRECT_URI = "http://127.0.0.1:8080/login/oauth2/code/bff-client";
    private static final Pattern CSRF_PATTERN = Pattern.compile("name=\"_csrf\"\\s+value=\"([^\"]+)\"");

    private record CodeGrantResult(String code, Pkce pkce) {}

    @Test
    @SuppressWarnings("unchecked")
    void genuineCodePlusPkceFlowYieldsRealJwtAcceptedByTheRealResourceServer() {
        RestTestClient authServerClient = authServerClient();
        CodeGrantResult grant = obtainAuthorizationCode(authServerClient, "alice", "alice-demo-pw");

        Map<String, Object> tokenResponse = exchangeCodeForTokens(authServerClient, grant);
        assertThat(tokenResponse).containsKeys("access_token", "id_token", "refresh_token");
        String accessToken = (String) tokenResponse.get("access_token");

        Map<String, Object> accessClaims = JwtClaims.decode(accessToken);
        assertThat(accessClaims).containsEntry("org", "acme");
        assertThat((List<String>) accessClaims.get("roles")).contains("EMPLOYEE");

        Map<String, Object> whoAmI = resourceServerClient()
                .get()
                .uri("/api/v1/whoami")
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(whoAmI).containsEntry("orgSlug", "acme");
    }

    @Test
    void revokedRefreshTokenIsRejectedOnReuse() {
        RestTestClient authServerClient = authServerClient();
        CodeGrantResult grant = obtainAuthorizationCode(authServerClient, "bob", "bob-demo-pw");
        Map<String, Object> tokenResponse = exchangeCodeForTokens(authServerClient, grant);
        String refreshToken = (String) tokenResponse.get("refresh_token");
        assertThat(refreshToken).isNotBlank();

        authServerClient
                .post()
                .uri("/oauth2/revoke")
                .header("Authorization", basicAuthHeader())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("token=%s&token_type_hint=refresh_token".formatted(refreshToken))
                .exchange()
                .expectStatus()
                .isOk();

        authServerClient
                .post()
                .uri("/oauth2/token")
                .header("Authorization", basicAuthHeader())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=refresh_token&refresh_token=%s".formatted(refreshToken))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void replayedAuthorizationCodeIsRejectedOnSecondUse() {
        RestTestClient authServerClient = authServerClient();
        CodeGrantResult grant = obtainAuthorizationCode(authServerClient, "carol", "carol-demo-pw");

        // First use succeeds and consumes the code.
        exchangeCodeForTokens(authServerClient, grant);

        // Second use of the same, now-consumed code must fail.
        authServerClient
                .post()
                .uri("/oauth2/token")
                .header("Authorization", basicAuthHeader())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=authorization_code&code=%s&redirect_uri=%s&code_verifier=%s"
                        .formatted(grant.code(), REDIRECT_URI, grant.pkce().verifier()))
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    private CodeGrantResult obtainAuthorizationCode(
            RestTestClient authServerClient, String username, String password) {
        Pkce pkce = Pkce.generate();
        String[] sessionCookie = new String[1];

        // A fresh state per call, not a fixed literal: this authorization-server
        // is shared (same DB) across every @Test method in this class, and Spring
        // Authorization Server keys authorization rows partly by state -- reusing
        // one would collide a later flow's "pending" lookup with an earlier,
        // already token-issued row for the same state.
        URI authorizeUri = UriComponentsBuilder.fromPath("/oauth2/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", "bff-client")
                .queryParam("scope", "openid expenses.read")
                .queryParam("redirect_uri", REDIRECT_URI)
                .queryParam("state", UUID.randomUUID().toString())
                .queryParam("code_challenge", pkce.challenge())
                .queryParam("code_challenge_method", "S256")
                .build()
                .toUri();

        // 1. Unauthenticated hit on /oauth2/authorize -> redirected to the login page.
        ExchangeResult toLogin = authServerClient
                .get()
                .uri(authorizeUri)
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .returnResult();
        rememberSessionCookie(toLogin, sessionCookie);
        URI loginUri = toLogin.getResponseHeaders().getLocation();
        assertThat(loginUri.getPath()).startsWith("/login");

        // 2. GET the login page to obtain a CSRF token bound to this session.
        String loginHtml = bodyAsString(authServerClient
                .get()
                .uri(loginUri)
                .cookie("JSESSIONID", sessionCookie[0])
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult());
        String loginCsrf = csrfToken(loginHtml);

        // 3. Submit credentials -> redirected back to the originally saved /oauth2/authorize request.
        ExchangeResult afterLogin = authServerClient
                .post()
                .uri("/login")
                .cookie("JSESSIONID", sessionCookie[0])
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("username=%s&password=%s&_csrf=%s".formatted(username, password, loginCsrf))
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .returnResult();
        rememberSessionCookie(afterLogin, sessionCookie);
        URI backToAuthorize = afterLogin.getResponseHeaders().getLocation();

        // 4. Re-request /oauth2/authorize, now authenticated.
        ExchangeResult afterAuthenticated = authServerClient
                .get()
                .uri(backToAuthorize)
                .accept(MediaType.TEXT_HTML)
                .cookie("JSESSIONID", sessionCookie[0])
                .exchange()
                .expectStatus()
                .is3xxRedirection()
                .returnResult();
        rememberSessionCookie(afterAuthenticated, sessionCookie);
        URI redirectAfterAuth = afterAuthenticated.getResponseHeaders().getLocation();

        URI redirectWithCode;
        if (redirectAfterAuth.getPath().startsWith("/oauth2/consent")) {
            // Consent not yet on file for this user+client+scope combination --
            // drive the real consent screen, same as a first-time browser user.
            String consentHtml = bodyAsString(authServerClient
                    .get()
                    .uri(redirectAfterAuth)
                    .cookie("JSESSIONID", sessionCookie[0])
                    .exchange()
                    .expectStatus()
                    .isOk()
                    .returnResult());
            String consentCsrf = csrfToken(consentHtml);

            Map<String, String> consentParams = UriComponentsBuilder.newInstance()
                    .query(redirectAfterAuth.getRawQuery())
                    .build()
                    .getQueryParams()
                    .toSingleValueMap();

            ExchangeResult withCode = authServerClient
                    .post()
                    .uri("/oauth2/authorize")
                    .cookie("JSESSIONID", sessionCookie[0])
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body("client_id=%s&state=%s&scope=expenses.read&_csrf=%s"
                            .formatted(consentParams.get("client_id"), consentParams.get("state"), consentCsrf))
                    .exchange()
                    .expectStatus()
                    .is3xxRedirection()
                    .returnResult();
            redirectWithCode = withCode.getResponseHeaders().getLocation();
        } else {
            // Consent already on file from an earlier test in this class (same
            // shared authorization-server, same underlying DB) -- Spring
            // Authorization Server skips straight to the code on a repeat
            // grant for an already-approved scope set. Real default behavior,
            // not a shortcut this test is taking.
            redirectWithCode = redirectAfterAuth;
        }

        assertThat(redirectWithCode.toString()).startsWith(REDIRECT_URI);
        String code = UriComponentsBuilder.newInstance()
                .query(redirectWithCode.getRawQuery())
                .build()
                .getQueryParams()
                .getFirst("code");
        assertThat(code).isNotBlank();

        return new CodeGrantResult(code, pkce);
    }

    private Map<String, Object> exchangeCodeForTokens(RestTestClient authServerClient, CodeGrantResult grant) {
        Map<String, Object> tokenResponse = authServerClient
                .post()
                .uri("/oauth2/token")
                .header("Authorization", basicAuthHeader())
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=authorization_code&code=%s&redirect_uri=%s&code_verifier=%s"
                        .formatted(grant.code(), REDIRECT_URI, grant.pkce().verifier()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        assertThat(tokenResponse).containsKey("access_token");
        return tokenResponse;
    }

    private static String basicAuthHeader() {
        return "Basic "
                + Base64.getEncoder()
                        .encodeToString("bff-client:bff-client-secret".getBytes(StandardCharsets.UTF_8));
    }

    private static RestTestClient authServerClient() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:9000").build();
    }

    private static RestTestClient resourceServerClient() {
        return RestTestClient.bindToServer().baseUrl("http://localhost:8082").build();
    }

    private static void rememberSessionCookie(ExchangeResult result, String[] sessionCookie) {
        List<ResponseCookie> cookies = result.getResponseCookies().get("JSESSIONID");
        if (cookies != null && !cookies.isEmpty()) {
            sessionCookie[0] = cookies.get(0).getValue();
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
