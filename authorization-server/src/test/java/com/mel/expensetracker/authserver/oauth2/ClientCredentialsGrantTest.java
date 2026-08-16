package com.mel.expensetracker.authserver.oauth2;

import static org.assertj.core.api.Assertions.assertThat;

import com.mel.expensetracker.authserver.support.AbstractIntegrationTest;
import com.mel.expensetracker.authserver.support.JwtTestSupport;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

class ClientCredentialsGrantTest extends AbstractIntegrationTest {

    @Test
    void grantsAnAccessTokenScopedToExpensesExport() {
        String credentials = Base64.getEncoder()
                .encodeToString("export-worker:export-worker-secret".getBytes(StandardCharsets.UTF_8));

        Map<String, Object> tokenResponse = client()
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

        assertThat(tokenResponse).containsKey("access_token");

        Map<String, Object> claims = JwtTestSupport.decodeClaims((String) tokenResponse.get("access_token"));
        assertThat((List<String>) claims.get("scope")).contains("expenses.export");
        // No end user in this grant: the org/role token customizer must not fire.
        assertThat(claims).doesNotContainKeys("org", "roles");
    }
}
