package com.mel.expensetracker.bff.config;

import com.mel.expensetracker.bff.security.ApiKeyAuthenticationFilter;
import com.mel.expensetracker.bff.security.ApiKeyAuthenticationProvider;
import com.mel.expensetracker.shared.error.ProblemDetailAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * [FEATURE A5] Pre-authenticated API-key filter, added via {@code addFilterBefore}
 * -- the framework has no built-in DSL entry for this, unlike {@code httpBasic()}.
 * Own ordered chain, narrow diagnostic namespace, same rationale as A2's
 * {@link BasicAuthSecurityConfig}.
 */
@Configuration
public class ApiKeySecurityConfig {

    // [FEATURE D5] Ordered chain 3 of 4 in bff-client -- see class comment.
    @Bean
    @Order(2)
    SecurityFilterChain apiKeySecurityFilterChain(
            HttpSecurity http,
            @Value("${app.security.api-key}") String apiKey,
            ProblemDetailAuthenticationEntryPoint authenticationEntryPoint)
            throws Exception {
        ApiKeyAuthenticationProvider provider = new ApiKeyAuthenticationProvider(apiKey);
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter();
        filter.setAuthenticationManager(new ProviderManager(provider));

        http.securityMatcher("/internal/api-key/**")
                // [FEATURE D2] The credential is a request header the caller
                // sets deliberately -- not a cookie the browser attaches
                // automatically -- so there's nothing for CSRF to protect
                // against, same rationale as resource-server's bearer-token
                // chain and this module's own Basic-auth chain.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated());

        return http.build();
    }
}
