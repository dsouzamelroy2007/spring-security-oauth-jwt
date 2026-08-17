package com.mel.expensetracker.bff.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextType;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * [FEATURE D7] {@code management.server.port} (application.yml) makes Boot
 * stand up a second embedded connector with its own <b>child</b>
 * {@code ApplicationContext} -- the main app's own controllers/services
 * never reach it. {@code @ManagementContextConfiguration} (registered via
 * this module's own {@code ManagementContextConfiguration.imports}) is
 * Boot's mechanism for contributing beans specifically into that child
 * context.
 *
 * <p>Spring Security's {@code WebSecurityConfiguration} resolves its list of
 * {@code SecurityFilterChain} beans ancestor-inclusively, so the child
 * context's own {@code springSecurityFilterChain} still sees the parent
 * (main app) context's chains alongside this one. {@code @Order(0)} --
 * ahead of every chain in {@link BasicAuthSecurityConfig},
 * {@link ApiKeySecurityConfig} and {@link BffSecurityConfig} -- keeps this
 * narrowly-matched ({@code EndpointRequest.toAnyEndpoint()}) chain reachable
 * ahead of {@link BffSecurityConfig}'s any-request catch-all wherever both
 * happen to be visible together. The same ancestor visibility is why this
 * chain attaches its own {@code DaoAuthenticationProvider} explicitly rather
 * than relying on Spring Security's global default {@code AuthenticationManager}
 * resolution -- see {@link BasicAuthSecurityConfig} for why that resolution
 * silently stops working once more than one {@code UserDetailsService} bean
 * is visible.
 */
@ManagementContextConfiguration(ManagementContextType.CHILD)
public class ActuatorSecurityConfig {

    @Bean
    PasswordEncoder actuatorPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    InMemoryUserDetailsManager actuatorUserDetailsManager(
            @Value("${app.security.actuator-user}") String actuatorUser,
            @Value("${app.security.actuator-password}") String actuatorPassword,
            PasswordEncoder actuatorPasswordEncoder) {
        return new InMemoryUserDetailsManager(User.withUsername(actuatorUser)
                .password(actuatorPasswordEncoder.encode(actuatorPassword))
                .roles("ACTUATOR")
                .build());
    }

    // [FEATURE D5] Ordered chain 1 of 4 in bff-client -- see class comment.
    @Bean
    @Order(0)
    SecurityFilterChain actuatorSecurityFilterChain(
            HttpSecurity http,
            InMemoryUserDetailsManager actuatorUserDetailsManager,
            PasswordEncoder actuatorPasswordEncoder)
            throws Exception {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(actuatorUserDetailsManager);
        provider.setPasswordEncoder(actuatorPasswordEncoder);

        http.securityMatcher(EndpointRequest.toAnyEndpoint())
                .authenticationProvider(provider)
                .authorizeHttpRequests(authorize -> authorize.anyRequest().hasRole("ACTUATOR"))
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
