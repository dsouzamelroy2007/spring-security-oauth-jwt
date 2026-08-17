package com.mel.expensetracker.shared.audit;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class AuditEventWriterTest {

    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final AuditEventWriter writer = new AuditEventWriter(jdbcTemplate);

    @Test
    void insertsEveryFieldInColumnOrder() {
        UUID orgId = UUID.randomUUID();
        AuditEvent event = new AuditEvent("login_success", "alice", orgId, "127.0.0.1", null);

        writer.write(event);

        verify(jdbcTemplate)
                .update(
                        (String) argThat(sql -> sql != null && ((String) sql).contains("INSERT INTO public.audit_log")),
                        eq("login_success"),
                        eq("alice"),
                        eq(orgId),
                        eq("127.0.0.1"),
                        eq((String) null));
    }

    @Test
    void toleratesNullOptionalFields() {
        writer.write(new AuditEvent("authentication_required", null, null, null, null));

        verify(jdbcTemplate)
                .update(
                        (String) argThat(sql -> sql != null),
                        eq("authentication_required"),
                        eq((String) null),
                        eq((UUID) null),
                        eq((String) null),
                        eq((String) null));
    }
}
