package com.mel.expensetracker.resourceserver.report;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mel.expensetracker.resourceserver.support.AbstractIntegrationTest;
import com.mel.expensetracker.resourceserver.support.TestJwtSupport;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/** [style: role-gated] POST /api/v1/reports/{id}/approve requires ROLE_MANAGER (or higher, via hierarchy). */
class ExpenseReportApprovalTest extends AbstractIntegrationTest {

    // alice's own SUBMITTED report, acme org -- seeded in V6__seed_demo_data.sql.
    private static final String ALICE_SUBMITTED_REPORT_ID = "e2222222-2222-2222-2222-222222222222";

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void managerInSameOrgCanApprove() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.bob());

        client().post()
                .uri("/api/v1/reports/" + ALICE_SUBMITTED_REPORT_ID + "/approve")
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void employeeCannotApproveEvenTheirOwnReport() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.alice());

        client().post()
                .uri("/api/v1/reports/" + ALICE_SUBMITTED_REPORT_ID + "/approve")
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void managerInADifferentOrgCannotApprove() {
        // dana is ORG_ADMIN (implies MANAGER via hierarchy) but in globex, not acme.
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.dana());

        client().post()
                .uri("/api/v1/reports/" + ALICE_SUBMITTED_REPORT_ID + "/approve")
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isForbidden();
    }
}
