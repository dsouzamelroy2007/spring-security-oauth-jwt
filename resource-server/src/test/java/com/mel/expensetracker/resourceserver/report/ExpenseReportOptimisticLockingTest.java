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

/** Optimistic locking via {@code @Version} + ETag/{@code If-Match}. */
class ExpenseReportOptimisticLockingTest extends AbstractIntegrationTest {

    // alice's own DRAFT report, dedicated to this test -- seeded in
    // V7__add_optimistic_lock_fixture.sql (not V6's e1111111, which other
    // tests assert on by title and shouldn't see mutated).
    private static final String ALICE_DRAFT_REPORT_ID = "e6666666-6666-6666-6666-666666666666";

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void mismatchedIfMatchIsRejectedWithPreconditionFailed() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.alice());

        client().put()
                .uri("/api/v1/reports/" + ALICE_DRAFT_REPORT_ID)
                .header("Authorization", "Bearer placeholder")
                .header("If-Match", "\"999\"")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Map.of("title", "Renamed", "description", "d", "currency", "USD"))
                .exchange()
                .expectStatus()
                .isEqualTo(412);
    }

    @Test
    @SuppressWarnings("unchecked")
    void matchingIfMatchSucceedsAndBumpsTheVersion() {
        when(jwtDecoder.decode(anyString())).thenReturn(TestJwtSupport.alice());

        Map<String, Object> current = client()
                .get()
                .uri("/api/v1/reports/" + ALICE_DRAFT_REPORT_ID)
                .header("Authorization", "Bearer placeholder")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();
        long currentVersion = ((Number) current.get("version")).longValue();

        Map<String, Object> updated = client()
                .put()
                .uri("/api/v1/reports/" + ALICE_DRAFT_REPORT_ID)
                .header("Authorization", "Bearer placeholder")
                .header("If-Match", "\"" + currentVersion + "\"")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(Map.of("title", "Conference trip -- confirmed", "description", "d", "currency", "USD"))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Map.class)
                .returnResult()
                .getResponseBody();

        assertThat(updated).containsEntry("title", "Conference trip -- confirmed");
        assertThat(((Number) updated.get("version")).longValue()).isEqualTo(currentVersion + 1);
    }
}
