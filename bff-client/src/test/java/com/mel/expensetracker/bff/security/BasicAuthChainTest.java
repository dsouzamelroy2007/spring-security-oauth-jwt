package com.mel.expensetracker.bff.security;

import com.mel.expensetracker.bff.support.AbstractBffIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

/** [FEATURE A2] HTTP Basic on its own ordered chain. */
class BasicAuthChainTest extends AbstractBffIntegrationTest {

    @Value("${app.security.basic-user}")
    private String basicUser;

    @Value("${app.security.basic-password}")
    private String basicPassword;

    @Test
    void rejectsMissingCredentials() {
        client().get().uri("/internal/basic/whoami").exchange().expectStatus().isUnauthorized();
    }

    @Test
    void acceptsCorrectCredentials() {
        client().get()
                .uri("/internal/basic/whoami")
                .headers(headers -> headers.setBasicAuth(basicUser, basicPassword))
                .exchange()
                .expectStatus()
                .isOk();
    }
}
