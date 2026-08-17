package com.mel.expensetracker.shared.audit;

import java.util.UUID;

/**
 * [FEATURE D8] Mirrors {@code audit_log} (authorization-server's {@code V6}
 * migration) column for column. {@code detail}, when present, must already be
 * valid JSON text -- {@link AuditEventWriter} casts it straight into the
 * {@code jsonb} column with no serialization step of its own.
 */
public record AuditEvent(String eventType, String principal, UUID orgId, String ipAddress, String detail) {}
