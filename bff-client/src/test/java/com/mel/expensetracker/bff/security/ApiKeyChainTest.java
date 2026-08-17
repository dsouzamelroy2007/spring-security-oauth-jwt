package com.mel.expensetracker.bff.security;

import com.mel.expensetracker.bff.support.AbstractBffIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

/** [FEATURE A5] Pre-authenticated API-key filter on its own ordered chain. */
class ApiKeyChainTest extends AbstractBffIntegrationTest {

    @Value("${app.security.api-key}")
    private String apiKey;

    @Test
    void rejectsMissingKey() {
        client().get().uri("/internal/api-key/whoami").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void rejectsWrongKey() {
        client().get()
                .uri("/internal/api-key/whoami")
                .header("X-API-Key", "not-the-right-key")
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }

    @Test
    void acceptsCorrectKey() {
        client().get()
                .uri("/internal/api-key/whoami")
                .header("X-API-Key", apiKey)
                .exchange()
                .expectStatus()
                .isOk();
    }
}
