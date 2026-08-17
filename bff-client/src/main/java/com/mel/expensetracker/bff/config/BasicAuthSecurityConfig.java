package com.mel.expensetracker.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * [FEATURE A2] HTTP Basic on its own chain, scoped to a narrow diagnostic
 * namespace that exists purely to demonstrate ordered chains -- no real
 * bff-client feature depends on it. Declared with a lower {@code @Order}
 * (higher precedence) than {@link BffSecurityConfig}'s catch-all, or the
 * catch-all would win first and this chain would never engage.
 *
 * <p>Builds its own {@code DaoAuthenticationProvider} and attaches it
 * directly to this chain via {@code authenticationProvider(...)}, rather
 * than relying on Spring Security's global default {@code AuthenticationManager}
 * resolution: with more than one {@code UserDetailsService} bean visible
 * (this one, plus {@code ActuatorSecurityConfig}'s), that global resolution
 * backs off entirely and every Basic-auth attempt fails with 401 regardless
 * of credentials -- the same fix already applied in authorization-server's
 * {@code FormLoginSecurityConfig} for the same reason.
 */
@Configuration
public class BasicAuthSecurityConfig {

    @Bean
    PasswordEncoder basicAuthPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    InMemoryUserDetailsManager basicAuthUserDetailsManager(
            @Value("${app.security.basic-user}") String basicUser,
            @Value("${app.security.basic-password}") String basicPassword,
            PasswordEncoder basicAuthPasswordEncoder) {
        return new InMemoryUserDetailsManager(User.withUsername(basicUser)
                .password(basicAuthPasswordEncoder.encode(basicPassword))
                .roles("OPS")
                .build());
    }

    @Bean
    @Order(1)
    SecurityFilterChain basicAuthSecurityFilterChain(
            HttpSecurity http,
            InMemoryUserDetailsManager basicAuthUserDetailsManager,
            PasswordEncoder basicAuthPasswordEncoder)
            throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(basicAuthUserDetailsManager);
        provider.setPasswordEncoder(basicAuthPasswordEncoder);

        http.securityMatcher("/internal/basic/**")
                .csrf(csrf -> csrf.disable()) // [FEATURE D2] Basic auth sends the credential on every request; there is no ambient cookie for CSRF to protect against.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(provider)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().hasRole("OPS"))
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
