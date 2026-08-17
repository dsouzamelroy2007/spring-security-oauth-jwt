package com.mel.expensetracker.resourceserver.export;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.mel.expensetracker.resourceserver.support.AbstractIntegrationTest;
import com.mel.expensetracker.resourceserver.support.TestJwtSupport;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/** [style: scope-gated M2M] GET /api/v1/reports/export requires SCOPE_expenses.export. */
class ReportExportScopeTest extends AbstractIntegrationTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void exportWorkerWithTheScopeCanExport() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.exportWorker());

        client().get()
                .uri("/api/v1/reports/export")
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isOk();
    }

    @Test
    void aHumanCallerWithoutTheExportScopeIsForbidden() {
        // alice's token carries expenses.read/write, not expenses.export.
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.alice());

        client().get()
                .uri("/api/v1/reports/export")
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isForbidden();
    }

    @Test
    void anonymousIsUnauthorized() {
        client().get().uri("/api/v1/reports/export").exchange().expectStatus().isUnauthorized();
    }
}
