package com.mel.expensetracker.bff.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * [FEATURE D1] The SPA itself is same-origin with the BFF (served from
 * {@code static/}), so it never needs CORS. This exists for the one
 * genuinely cross-origin case: a separate origin's page calling the BFF's
 * API directly with the browser attaching the session cookie. Scoped to a
 * single, explicitly configured origin -- {@code allowCredentials(true)}
 * (required so the session cookie is sent) is rejected by browsers if paired
 * with a wildcard origin, so there is no broader setting to fall back to.
 */
@Configuration
public class CorsConfig {

    private final String allowedOrigin;

    public CorsConfig(@Value("${app.cors.allowed-origin:http://localhost:5500}") String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigin));
        configuration.setAllowedMethods(List.of("GET", "POST"));
        configuration.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
