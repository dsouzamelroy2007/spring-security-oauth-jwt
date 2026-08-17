package com.mel.expensetracker.shared.error;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;

class SecurityProblemDetailsAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(SecurityProblemDetailsAutoConfiguration.class));

    @Test
    void suppliesBothHandlersWhenSpringSecurityWebIsPresent() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(ProblemDetailAuthenticationEntryPoint.class);
            assertThat(context).hasSingleBean(ProblemDetailAccessDeniedHandler.class);
        });
    }

    @Test
    void backsOffWhenAUserSuppliesTheirOwnHandlers() {
        contextRunner.withUserConfiguration(CustomHandlerConfig.class).run(context -> {
            assertThat(context).hasSingleBean(ProblemDetailAuthenticationEntryPoint.class);
            assertThat(context.getBean(ProblemDetailAuthenticationEntryPoint.class))
                    .isSameAs(context.getBean(CustomHandlerConfig.class).customEntryPoint);
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomHandlerConfig {
        final ProblemDetailAuthenticationEntryPoint customEntryPoint =
                new ProblemDetailAuthenticationEntryPoint(jacksonConverter());

        @Bean
        ProblemDetailAuthenticationEntryPoint problemDetailAuthenticationEntryPoint() {
            return customEntryPoint;
        }

        private static HttpMessageConverter<Object> jacksonConverter() {
            return new JacksonJsonHttpMessageConverter();
        }
    }
}
