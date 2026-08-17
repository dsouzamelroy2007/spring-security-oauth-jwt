package com.mel.expensetracker.integrationtests;

import static org.assertj.core.api.Assertions.assertThat;

import com.mel.expensetracker.integrationtests.support.CrossModuleTestSupport;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

/**
 * Backs acceptance criterion 2: a real token from a real, running
 * authorization-server works against a real, running resource-server end to
 * end. {@code client_credentials} for {@code export-worker} is the
 * deterministic path -- no cookies, no CSRF, no redirect chain -- and still
 * proves the full JWKS-rotation-aware decode path (issuer discovery, real
 * RSA signature verification, audience, claim shape) that a mocked-decoder
 * unit test can't.
 */
class RealTokenAgainstResourceServerIT extends CrossModuleTestSupport {

    @Test
    void clientCredentialsTokenFromTheRealAuthorizationServerIsAcceptedByTheRealResourceServer() {
        RestTestClient authServerClient =
                RestTestClient.bindToServer().baseUrl("http://localhost:9000").build();
        RestTestClient resourceServerClient =
                RestTestClient.bindToServer().baseUrl("http://localhost:8082").build();

        String credentials = Base64.getEncoder()
                .encodeToString("export-worker:export-worker-secret".getBytes(StandardCharsets.UTF_8));

        Map<String, Object> tokenResponse = authServerClient
                .post()
                .uri("/oauth2/token")
                .header("Authorization", "Basic " + credentials)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("grant_type=client_credentials&scope=expenses.export")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        String accessToken = (String) tokenResponse.get("access_token");
        assertThat(accessToken).isNotBlank();

        List<Object> exported = resourceServerClient
                .get()
                .uri("/api/v1/reports/export")
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(List.class)
                .returnResult()
                .getResponseBody();

        assertThat(exported).isNotNull();
    }
}
