package com.mel.expensetracker.bff;

import static org.assertj.core.api.Assertions.assertThat;

import com.mel.expensetracker.bff.support.AbstractBffIntegrationTest;
import java.sql.Timestamp;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * [FEATURE D8] Acceptance criterion 6, the bff-client-local third of it: an
 * "access denied" audit row. {@code login_success}/{@code login_failure}
 * are produced by authorization-server's own handlers -- see its
 * {@code FormLoginSuccessHandlerTest}/{@code FormLoginFailureHandlerTest},
 * already verified against the real, migration-created table. Exercising
 * those end to end from here would need both modules booted together,
 * which is {@code integration-tests}' job (M5), not this module's.
 */
class AuditAcceptanceTest extends AbstractBffIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void csrfRejectionWritesAnAccessDeniedAuditRow() {
        Instant before = Instant.now();

        client().post()
                .uri("/api/reports")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"title\":\"forged\",\"currency\":\"EUR\"}")
                .exchange()
                .expectStatus()
                .isForbidden();

        Integer rowCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM public.audit_log WHERE event_type = 'access_denied' AND occurred_at >= ?",
                Integer.class,
                Timestamp.from(before));

        assertThat(rowCount).isGreaterThanOrEqualTo(1);
    }
}
