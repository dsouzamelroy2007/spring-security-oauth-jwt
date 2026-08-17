package com.mel.expensetracker.shared.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * [FEATURE D8] The one write path into {@code audit_log} -- authorization-server's
 * {@code V6} migration owns the table, but every module on this classpath with
 * a {@link JdbcTemplate} writes into it directly (same Postgres instance, same
 * default schema search path), rather than each module inventing its own audit
 * mechanism. See {@link AuditAutoConfiguration} for the activation condition.
 */
public class AuditEventWriter {

    private static final Logger log = LoggerFactory.getLogger(AuditEventWriter.class);

    // Schema-qualified: authorization-server's V6 migration always creates
    // this table in the database's default/public schema, but a caller
    // (e.g. resource-server) may have its own default schema/search_path
    // (spring.jpa.properties.hibernate.default_schema, spring.flyway.default-schema)
    // that doesn't include "public" -- an unqualified reference resolved
    // "relation \"audit_log\" does not exist" against that connection.
    private static final String INSERT_SQL =
            """
            INSERT INTO public.audit_log (event_type, principal, org_id, ip_address, detail)
            VALUES (?, ?, ?, ?, ?::jsonb)
            """;

    private final JdbcTemplate jdbcTemplate;

    public AuditEventWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void write(AuditEvent event) {
        log.info(
                "audit: event_type={} principal={} org={} ip={}",
                event.eventType(),
                event.principal(),
                event.orgId(),
                event.ipAddress());
        jdbcTemplate.update(
                INSERT_SQL, event.eventType(), event.principal(), event.orgId(), event.ipAddress(), event.detail());
    }
}
