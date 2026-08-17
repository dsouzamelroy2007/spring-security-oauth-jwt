package com.mel.expensetracker.resourceserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Boot 4's native API versioning (not a URL-path convention), demonstrated on
 * {@code /api/v1/whoami} only -- the lowest-risk endpoint to carry the
 * demo, since it has no side effects and isn't part of the authorization
 * matrix's routing-order-sensitive endpoint list.
 */
@Configuration
public class ApiVersioningConfig implements WebMvcConfigurer {

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .useRequestHeader("X-API-Version")
                .addSupportedVersions("1", "2")
                .setDefaultVersion("1")
                .setVersionRequired(false);
    }
}
