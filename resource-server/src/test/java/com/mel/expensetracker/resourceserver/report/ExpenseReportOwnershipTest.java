package com.mel.expensetracker.resourceserver.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mel.expensetracker.resourceserver.support.AbstractIntegrationTest;
import com.mel.expensetracker.resourceserver.support.TestJwtSupport;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/** [style: owner-only] GET/PUT /api/v1/reports/{id} is restricted to the submitter, regardless of role. */
class ExpenseReportOwnershipTest extends AbstractIntegrationTest {

    // alice's own SUBMITTED report -- seeded in V6__seed_demo_data.sql.
    private static final String ALICE_REPORT_ID = "e2222222-2222-2222-2222-222222222222";

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void ownerCanGetTheirOwnReport() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.alice());

        Map<String, Object> body = client()
                .get()
                .uri("/api/v1/reports/" + ALICE_REPORT_ID)
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isOk()
                .expectHeader()
                .exists("ETag")
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(body).containsEntry("submitterSubject", "alice");
    }

    @Test
    void sameOrgNonOwnerIsForbidden() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.bob());

        client().get()
                .uri("/api/v1/reports/" + ALICE_REPORT_ID)
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void crossOrgCallerIsForbidden() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.carol());

        client().get()
                .uri("/api/v1/reports/" + ALICE_REPORT_ID)
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void anonymousIsUnauthorized() {
        client().get().uri("/api/v1/reports/" + ALICE_REPORT_ID).exchange().expectStatus().isUnauthorized();
    }
}
