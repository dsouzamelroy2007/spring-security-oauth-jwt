package com.mel.expensetracker.shared.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

class JwtAuthenticationConverterAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(JwtAuthenticationConverterAutoConfiguration.class));

    @Test
    void suppliesADefaultConverterWhenResourceServerClassesArePresent() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(JwtAuthenticationConverter.class));
    }

    @Test
    void backsOffWhenAUserSuppliesTheirOwnConverter() {
        contextRunner
                .withUserConfiguration(CustomConverterConfig.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(JwtAuthenticationConverter.class);
                    assertThat(context).getBean(JwtAuthenticationConverter.class)
                            .isSameAs(context.getBean(CustomConverterConfig.class).customConverter);
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class CustomConverterConfig {
        final JwtAuthenticationConverter customConverter = new JwtAuthenticationConverter();

        @Bean
        JwtAuthenticationConverter jwtAuthenticationConverter() {
            return customConverter;
        }
    }
}
