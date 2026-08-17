package com.mel.expensetracker.resourceserver.whoami;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mel.expensetracker.resourceserver.support.AbstractIntegrationTest;
import com.mel.expensetracker.resourceserver.support.TestJwtSupport;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class WhoAmIControllerTest extends AbstractIntegrationTest {

    // Real HTTP requests, not MockMvc, per this project's established style --
    // so the bearer string itself is a placeholder; decode() is stubbed per
    // test to return the persona under test.
    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void anonymousRequestIsRejectedWithProblemDetailBody() {
        Map<String, Object> body = client()
                .get()
                .uri("/api/v1/whoami")
                .exchange()
                .expectStatus()
                .isUnauthorized()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).containsEntry("type", "urn:expensetracker:problem:unauthenticated");
    }

    @Test
    @SuppressWarnings("unchecked")
    void v1WhoAmIResolvesSubjectOrgAndAuthorities() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.bob());

        Map<String, Object> body = client()
                .get()
                .uri("/api/v1/whoami")
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).containsEntry("subject", "bob").containsEntry("orgSlug", "acme");
        // FACTOR_BEARER is added by Spring Security itself (authentication-factor
        // tracking for step-up auth), not by our own converter.
        assertThat((List<String>) body.get("authorities"))
                .containsExactlyInAnyOrder(
                        "ROLE_MANAGER",
                        "SCOPE_openid",
                        "SCOPE_profile",
                        "SCOPE_expenses.read",
                        "SCOPE_expenses.write",
                        "FACTOR_BEARER");
    }

    @Test
    @SuppressWarnings("unchecked")
    void v2WhoAmIAddsRolesAndRawClaims() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.carol());

        Map<String, Object> body = client()
                .get()
                .uri("/api/v1/whoami")
                .header("Authorization", "Bearer placeholder")
                .header("X-API-Version", "2")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).containsEntry("subject", "carol");
        assertThat((List<String>) body.get("roles")).containsExactly("FINANCE");
        assertThat((Map<String, Object>) body.get("claims")).containsEntry("org", "globex");
    }

    @Test
    void exportWorkerTokenIsAuthenticatedButHasNoOrgClaim() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.exportWorker());

        Map<String, Object> body = client()
                .get()
                .uri("/api/v1/whoami")
                .header("Authorization", "Bearer placeholder")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).containsEntry("subject", "export-worker");
        assertThat(body.get("orgSlug")).isNull();
    }
}
