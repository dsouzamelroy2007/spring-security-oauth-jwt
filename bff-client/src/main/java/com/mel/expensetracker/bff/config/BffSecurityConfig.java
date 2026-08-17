package com.mel.expensetracker.bff.config;

import com.mel.expensetracker.bff.security.SpaCsrfTokenRequestHandler;
import com.mel.expensetracker.shared.error.ProblemDetailAccessDeniedHandler;
import com.mel.expensetracker.shared.error.ProblemDetailAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

/**
 * [FEATURE B1] The real chain: code+PKCE login, the SPA's own API surface,
 * and every remaining M4 hardening feature. Declared last / lowest
 * precedence ({@code @Order(3)}, no {@code securityMatcher}) so
 * {@link BasicAuthSecurityConfig} and {@link ApiKeySecurityConfig} get first
 * refusal on their own narrow namespaces.
 */
@Configuration
public class BffSecurityConfig {

    @Bean
    @Order(3)
    SecurityFilterChain bffSecurityFilterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository,
            ProblemDetailAuthenticationEntryPoint apiAuthenticationEntryPoint,
            ProblemDetailAccessDeniedHandler accessDeniedHandler,
            SessionRegistry sessionRegistry)
            throws Exception {

        // [FEATURE B4] RP-initiated logout: redirects to authorization-server's
        // end_session_endpoint (from OIDC discovery), which itself then honors
        // the registered post-logout-redirect-uri back to this app's home page.
        OidcClientInitiatedLogoutSuccessHandler logoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);
        logoutSuccessHandler.setPostLogoutRedirectUri("http://127.0.0.1:8080/");

        // Unauthenticated hits on the SPA's own fetch-only endpoints get an
        // RFC 9457 401 body; everything else falls through to oauth2Login's
        // own entry point, which redirects the browser into the code+PKCE flow.
        RequestMatcher apiRequestMatcher = new OrRequestMatcher(
                PathPatternRequestMatcher.pathPattern("/api/**"), PathPatternRequestMatcher.pathPattern("/whoami"));

        http.oauth2Login(Customizer.withDefaults())
                .logout(logout -> logout.logoutSuccessHandler(logoutSuccessHandler))
                .cors(Customizer.withDefaults())
                // [FEATURE D2] SPA-friendly CSRF: the token cookie must be
                // JS-readable (it's a nonce, not a secret) so app.js can echo
                // it back as a header on every state-changing fetch() call.
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
                .headers(headers -> headers
                        // [FEATURE D3]
                        .contentSecurityPolicy(csp ->
                                csp.policyDirectives("default-src 'self'; frame-ancestors 'none'; object-src 'none'"))
                        .referrerPolicy(referrer ->
                                referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        // This stack runs plain HTTP locally (no TLS termination
                        // anywhere in the compose setup); HSTS's writer only fires
                        // for request.isSecure() by default, so force it -- the
                        // header still needs to be visible for curl -I to show
                        // "the full header set" per acceptance criterion 5.
                        .httpStrictTransportSecurity(hsts -> hsts.requestMatcher(request -> true)))
                .sessionManagement(session -> session
                        // [FEATURE D4] changeSessionId() is the DSL default; named
                        // explicitly so the fixation-protection feature is visible
                        // at the call site, not just implied.
                        .sessionFixation(fixation -> fixation.changeSessionId())
                        .sessionConcurrency(concurrency ->
                                concurrency.maximumSessions(1).sessionRegistry(sessionRegistry)))
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(apiAuthenticationEntryPoint, apiRequestMatcher)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        // [FEATURE D7] Actuator health lives entirely on
                        // management.server.port (8090), a separate embedded
                        // connector -- there is no /actuator/** on this port
                        // to permit or deny.
                        .requestMatchers("/", "/index.html", "/app.js", "/favicon.ico")
                        .permitAll()
                        .requestMatchers("/login/**", "/oauth2/**")
                        .permitAll()
                        .anyRequest()
                        .authenticated());

        return http.build();
    }
}
