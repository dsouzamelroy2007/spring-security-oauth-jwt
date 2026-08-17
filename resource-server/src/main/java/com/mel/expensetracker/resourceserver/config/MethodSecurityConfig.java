package com.mel.expensetracker.resourceserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

/**
 * [FEATURE C2] [FEATURE C3] {@code @EnableMethodSecurity}'s default
 * {@code prePostEnabled = true} covers {@code @PreAuthorize}/{@code @PostAuthorize};
 * {@code @PreFilter}/{@code @PostFilter} and {@code @AuthorizeReturnObject} are
 * enabled by the same annotation in Spring Security 7.1 without a separate flag.
 *
 * <p>Role hierarchy: {@code ORG_ADMIN > FINANCE > MANAGER > EMPLOYEE}. A bare
 * {@link RoleHierarchy} bean is picked up automatically by both the web
 * ({@code hasRole(...)} in {@code SecurityConfig}) and method
 * ({@code @PreAuthorize}) expression handlers.
 */
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {

    @Bean
    RoleHierarchy roleHierarchy() {
        return RoleHierarchyImpl.withDefaultRolePrefix()
                .role("ORG_ADMIN")
                .implies("FINANCE")
                .role("FINANCE")
                .implies("MANAGER")
                .role("MANAGER")
                .implies("EMPLOYEE")
                .build();
    }
}
