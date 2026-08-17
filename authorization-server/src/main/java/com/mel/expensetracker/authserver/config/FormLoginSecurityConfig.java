package com.mel.expensetracker.authserver.config;

import com.mel.expensetracker.authserver.user.JpaUserDetailsService;
import com.mel.expensetracker.authserver.web.FormLoginFailureHandler;
import com.mel.expensetracker.authserver.web.FormLoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * The @Order(2) chain: everything the @Order(1) authorization-server chain in
 * {@link AuthorizationServerConfig} doesn't claim -- the login page, the
 * consent page, actuator health. Two chains (not one) is how Spring
 * Authorization Server itself is meant to be composed: the first chain's
 * {@code securityMatcher} scopes it to just the OAuth2/OIDC endpoints, so it
 * can require authentication unconditionally, while this chain handles the
 * interactive browser flow (login, consent) around it.
 */
@Configuration
public class FormLoginSecurityConfig {

    /**
     * [FEATURE A3] A second, in-memory UserDetailsService kept only for
     * comparison against the JPA-backed one below -- not used by any seeded
     * demo user or test flow. Its password is encoded through the same
     * {@link PasswordEncoderConfig} bean so it round-trips consistently.
     */
    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder passwordEncoder) {
        return new InMemoryUserDetailsManager(User.withUsername("sandbox")
                .password(passwordEncoder.encode("sandbox-demo-password"))
                .roles("EMPLOYEE")
                .build());
    }

    // [FEATURE D5] Ordered chain 2 of 2 -- see class comment for why two, not one.
    @Bean
    @Order(2)
    public SecurityFilterChain formLoginSecurityFilterChain(
            HttpSecurity http,
            JpaUserDetailsService jpaUserDetailsService,
            InMemoryUserDetailsManager inMemoryUserDetailsManager,
            PasswordEncoder passwordEncoder,
            FormLoginSuccessHandler successHandler,
            FormLoginFailureHandler failureHandler)
            throws Exception {

        DaoAuthenticationProvider jpaProvider = new DaoAuthenticationProvider(jpaUserDetailsService);
        jpaProvider.setPasswordEncoder(passwordEncoder);
        jpaProvider.setUserDetailsPasswordService(jpaUserDetailsService);

        DaoAuthenticationProvider inMemoryProvider = new DaoAuthenticationProvider(inMemoryUserDetailsManager);
        inMemoryProvider.setPasswordEncoder(passwordEncoder);

        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/actuator/health", "/webjars/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                // Tried in order: a username absent from the org-backed JPA store
                // falls through to the in-memory comparison store rather than
                // failing outright.
                .authenticationProvider(jpaProvider)
                .authenticationProvider(inMemoryProvider)
                .formLogin(form -> form.loginPage("/login")
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .permitAll());

        return http.build();
    }
}
