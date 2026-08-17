package com.mel.expensetracker.shared.error;

import com.mel.expensetracker.shared.audit.AuditEventWriter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * [FEATURE D6] Only activates where a {@code SecurityFilterChain} bean can
 * actually exist (i.e. Spring Security's web support is on the classpath).
 */
@AutoConfiguration
@ConditionalOnClass(SecurityFilterChain.class)
public class SecurityProblemDetailsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "problemDetailHttpMessageConverter")
    HttpMessageConverter<Object> problemDetailHttpMessageConverter() {
        return new JacksonJsonHttpMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean
    ProblemDetailAuthenticationEntryPoint problemDetailAuthenticationEntryPoint(
            HttpMessageConverter<Object> problemDetailHttpMessageConverter,
            ObjectProvider<AuditEventWriter> auditEventWriter) {
        return new ProblemDetailAuthenticationEntryPoint(
                problemDetailHttpMessageConverter, auditEventWriter.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    ProblemDetailAccessDeniedHandler problemDetailAccessDeniedHandler(
            HttpMessageConverter<Object> problemDetailHttpMessageConverter,
            ObjectProvider<AuditEventWriter> auditEventWriter) {
        return new ProblemDetailAccessDeniedHandler(
                problemDetailHttpMessageConverter, auditEventWriter.getIfAvailable());
    }
}
