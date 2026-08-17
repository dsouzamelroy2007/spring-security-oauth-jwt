package com.mel.expensetracker.resourceserver.report;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mel.expensetracker.resourceserver.support.AbstractIntegrationTest;
import com.mel.expensetracker.resourceserver.support.TestJwtSupport;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * [style: meta-annotation] DELETE /api/v1/reports/{id} requires {@code @IsOrgAdmin}
 * (org tenancy checked upstream, org-admin role checked at the method layer)
 * -- proves the two checks compose rather than either alone being sufficient.
 */
class ExpenseReportDeleteAuthorizationTest extends AbstractIntegrationTest {

    // dana's own DRAFT report, globex org -- seeded in V6__seed_demo_data.sql.
    private static final String GLOBEX_DRAFT_REPORT_ID = "e5555555-5555-5555-5555-555555555555";

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void orgAdminInSameOrgCanDelete() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.dana());

        client().delete()
                .uri("/api/v1/reports/" + GLOBEX_DRAFT_REPORT_ID)
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isNoContent();
    }

    @Test
    void nonOrgAdminInSameOrgIsForbiddenDespitePassingTenancy() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.carol());

        client().delete()
                .uri("/api/v1/reports/" + GLOBEX_DRAFT_REPORT_ID)
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void orgAdminInADifferentOrgIsForbiddenAtTheTenancyGateAlready() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.bob());

        client().delete()
                .uri("/api/v1/reports/" + GLOBEX_DRAFT_REPORT_ID)
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isForbidden();
    }
}
