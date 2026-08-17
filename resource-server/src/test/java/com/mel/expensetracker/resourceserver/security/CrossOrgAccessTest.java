package com.mel.expensetracker.resourceserver.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mel.expensetracker.resourceserver.support.AbstractIntegrationTest;
import com.mel.expensetracker.resourceserver.support.TestJwtSupport;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/** Backs acceptance criterion 3: cross-org access returns 403 with an RFC 9457 body. */
class CrossOrgAccessTest extends AbstractIntegrationTest {

    // bob's own report, acme org -- seeded in V6__seed_demo_data.sql.
    private static final String ACME_REPORT_ID = "e3333333-3333-3333-3333-333333333333";

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @SuppressWarnings("unchecked")
    void crossOrgGetReturns403WithAnRfc9457Body() {
        // carol is globex, the report belongs to acme.
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.carol());

        Map<String, Object> body = client()
                .get()
                .uri("/api/v1/reports/" + ACME_REPORT_ID)
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isForbidden()
                .expectHeader()
                .contentType("application/problem+json")
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(body)
                .containsEntry("status", 403)
                .containsEntry("type", "urn:expensetracker:problem:access-denied")
                .containsKey("detail")
                .containsKey("instance");
    }
}
