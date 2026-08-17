package com.mel.expensetracker.shared.audit;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * [FEATURE D8] Only activates for modules with a JDBC connection to the shared
 * Postgres database -- a module with no {@link JdbcTemplate} bean never gets an
 * {@link AuditEventWriter}, and the RFC 9457 handlers in {@code shared.error}
 * that optionally take one simply skip audit-writing in that case.
 */
@AutoConfiguration
@ConditionalOnClass(JdbcTemplate.class)
public class AuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    AuditEventWriter auditEventWriter(JdbcTemplate jdbcTemplate) {
        return new AuditEventWriter(jdbcTemplate);
    }
}
