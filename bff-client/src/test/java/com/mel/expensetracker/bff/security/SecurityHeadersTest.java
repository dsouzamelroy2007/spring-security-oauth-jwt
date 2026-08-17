package com.mel.expensetracker.bff.security;

import com.mel.expensetracker.bff.support.AbstractBffIntegrationTest;
import org.junit.jupiter.api.Test;

/** [FEATURE D3] Acceptance criterion 5: {@code curl -I} shows the full header set. */
class SecurityHeadersTest extends AbstractBffIntegrationTest {

    @Test
    void rootResponseCarriesTheFullSecurityHeaderSet() {
        client().get()
                .uri("/")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .exists("Content-Security-Policy")
                .expectHeader()
                .valueEquals("Referrer-Policy", "strict-origin-when-cross-origin")
                .expectHeader()
                .exists("Strict-Transport-Security")
                .expectHeader()
                .exists("X-Content-Type-Options");
    }
}
