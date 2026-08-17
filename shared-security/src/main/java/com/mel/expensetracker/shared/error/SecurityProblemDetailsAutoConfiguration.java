package com.mel.expensetracker.shared.error;

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
            HttpMessageConverter<Object> problemDetailHttpMessageConverter) {
        return new ProblemDetailAuthenticationEntryPoint(problemDetailHttpMessageConverter);
    }

    @Bean
    @ConditionalOnMissingBean
    ProblemDetailAccessDeniedHandler problemDetailAccessDeniedHandler(
            HttpMessageConverter<Object> problemDetailHttpMessageConverter) {
        return new ProblemDetailAccessDeniedHandler(problemDetailHttpMessageConverter);
    }
}
