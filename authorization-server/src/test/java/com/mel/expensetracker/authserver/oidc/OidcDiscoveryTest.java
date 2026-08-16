package com.mel.expensetracker.authserver.oidc;

import static org.assertj.core.api.Assertions.assertThat;

import com.mel.expensetracker.authserver.support.AbstractIntegrationTest;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OidcDiscoveryTest extends AbstractIntegrationTest {

    @Test
    void discoveryDocumentAdvertisesEndpoints() {
        Map<String, Object> metadata = client()
                .get()
                .uri("/.well-known/openid-configuration")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(metadata).containsEntry("issuer", "http://localhost:9000");
        assertThat(metadata).containsKeys(
                "authorization_endpoint", "token_endpoint", "jwks_uri", "userinfo_endpoint");
    }

    @Test
    @SuppressWarnings("unchecked")
    void jwksServesAPublicSigningKey() {
        Map<String, Object> jwks = client()
                .get()
                .uri("/oauth2/jwks")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
        assertThat(keys).isNotEmpty();
        assertThat(keys.get(0)).containsEntry("kty", "RSA").containsEntry("use", "sig");
        // Private key material must never leave this endpoint.
        assertThat(keys.get(0)).doesNotContainKey("d");
    }
}
