package com.mel.expensetracker.bff.security;

import com.mel.expensetracker.bff.support.AbstractBffIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

/**
 * [FEATURE D2] Acceptance criterion 3: a cross-origin POST without the CSRF
 * token is rejected. {@code CsrfFilter} runs ahead of the authorization
 * check and ahead of authentication state entirely -- a forged POST from a
 * different origin has no way to have obtained the token in the first place,
 * so it never carries a valid one, whether or not a session is authenticated.
 */
class CsrfProtectionTest extends AbstractBffIntegrationTest {

    @Test
    void postWithoutCsrfTokenIsRejected() {
        client().post()
                .uri("/api/reports")
                .header("Origin", "https://attacker.example")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"title\":\"forged\",\"currency\":\"EUR\"}")
                .exchange()
                .expectStatus()
                .isForbidden();
    }
}
