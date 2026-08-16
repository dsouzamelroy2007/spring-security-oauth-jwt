package com.mel.expensetracker.authserver.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.mel.expensetracker.authserver.support.AbstractIntegrationTest;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.test.web.servlet.client.ExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;

/** [FEATURE A4] Proves DelegatingPasswordEncoder's upgrade-on-login actually fires. */
class PasswordUpgradeOnLoginTest extends AbstractIntegrationTest {

    private static final Pattern CSRF_PATTERN =
            Pattern.compile("name=\"_csrf\"\\s+value=\"([^\"]+)\"");

    @Autowired
    private UserRepository userRepository;

    @Test
    void bcryptHashIsRewrittenToArgon2OnSuccessfulLogin() {
        // Bob, not Alice: AuthorizationCodeGrantFlowTest also logs in as Alice
        // against this same shared database, which would upgrade her hash
        // before this test runs and make "originalHash starts with {bcrypt}"
        // order-dependent. Bob is seeded {bcrypt} and used nowhere else.
        String originalHash =
                userRepository.findByUsername("bob").orElseThrow().getPasswordHash();
        assertThat(originalHash).startsWith("{bcrypt}");

        login("bob", "bob-demo-pw");

        String upgradedHash =
                userRepository.findByUsername("bob").orElseThrow().getPasswordHash();
        assertThat(upgradedHash).startsWith("{argon2}");
        assertThat(upgradedHash).isNotEqualTo(originalHash);
    }

    @Test
    void alreadyArgon2HashIsUnchangedAfterLogin() {
        String originalHash =
                userRepository.findByUsername("carol").orElseThrow().getPasswordHash();
        assertThat(originalHash).startsWith("{argon2}");

        login("carol", "carol-demo-pw");

        String afterLoginHash =
                userRepository.findByUsername("carol").orElseThrow().getPasswordHash();
        assertThat(afterLoginHash).isEqualTo(originalHash);
    }

    private void login(String username, String password) {
        RestTestClient client = client();

        ExchangeResult loginPage =
                client.get().uri("/login").exchange().expectStatus().isOk().returnResult();
        String sessionCookie = sessionCookie(loginPage);
        String csrf = csrfToken(new String(loginPage.getResponseBodyContent(), StandardCharsets.UTF_8));

        client.post()
                .uri("/login")
                .cookie("JSESSIONID", sessionCookie)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("username=%s&password=%s&_csrf=%s".formatted(username, password, csrf))
                .exchange()
                .expectStatus()
                .is3xxRedirection();
    }

    private static String sessionCookie(ExchangeResult result) {
        List<ResponseCookie> cookies = result.getResponseCookies().get("JSESSIONID");
        return cookies.get(0).getValue();
    }

    private static String csrfToken(String html) {
        Matcher matcher = CSRF_PATTERN.matcher(html);
        if (!matcher.find()) {
            throw new IllegalStateException("No CSRF token found in response body:\n" + html);
        }
        return matcher.group(1);
    }
}
