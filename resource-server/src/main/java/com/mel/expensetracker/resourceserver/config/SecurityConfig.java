package com.mel.expensetracker.resourceserver.config;

import com.mel.expensetracker.resourceserver.report.ExpenseReportRepository;
import com.mel.expensetracker.resourceserver.report.ReportAccessAuthorizationManager;
import com.mel.expensetracker.shared.error.ProblemDetailAccessDeniedHandler;
import com.mel.expensetracker.shared.error.ProblemDetailAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * The one {@code SecurityFilterChain} this stateless, JWT-only API needs.
 *
 * <p>Endpoint ordering matters: {@code authorizeHttpRequests} is first-match-
 * wins in declaration order, unlike MVC dispatch which correctly prefers a
 * literal segment over a path variable. {@code /reports/export} is therefore
 * registered before the {@code /reports/{id}} family -- otherwise {@code {id}}
 * would silently bind to the literal string "export" and the M2M scope check
 * would never run.
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationConverter jwtAuthenticationConverter;
    private final ProblemDetailAuthenticationEntryPoint authenticationEntryPoint;
    private final ProblemDetailAccessDeniedHandler accessDeniedHandler;
    private final ExpenseReportRepository reportRepository;

    public SecurityConfig(
            JwtAuthenticationConverter jwtAuthenticationConverter,
            ProblemDetailAuthenticationEntryPoint authenticationEntryPoint,
            ProblemDetailAccessDeniedHandler accessDeniedHandler,
            ExpenseReportRepository reportRepository) {
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.reportRepository = reportRepository;
    }

    // [FEATURE D5] Stateless JWT-only API needs exactly one chain, no @Order --
    // the contrast case in the cross-module chain-ordering diagram (docs/flows).
    @Bean
    SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
        var ownershipRequired = new ReportAccessAuthorizationManager(reportRepository, true);
        var tenancyOnly = new ReportAccessAuthorizationManager(reportRepository, false);

        http
                // [FEATURE D2] Every request here carries a bearer JWT, not a
                // session cookie -- CSRF exists to stop a browser from being
                // tricked into replaying an ambient credential (a cookie) it
                // holds automatically. A bearer token isn't ambient: the
                // attacker's page has no way to attach it, so there's nothing
                // for CSRF protection to guard against on this chain.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(authorize -> authorize
                        // Boot's internal error dispatch forward -- must stay
                        // reachable so a genuine 5xx surfaces as itself instead
                        // of being masked as a 403 by anyRequest().denyAll().
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/categories").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/whoami").authenticated()
                        // [FEATURE B2] Must precede /reports/{id}: see class comment.
                        .requestMatchers(HttpMethod.GET, "/api/v1/reports/export").hasAuthority("SCOPE_expenses.export")
                        .requestMatchers(HttpMethod.GET, "/api/v1/reports/{id}").access(ownershipRequired)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/reports/{id}").access(ownershipRequired)
                        .requestMatchers(HttpMethod.POST, "/api/v1/reports/{id}/items").access(ownershipRequired)
                        // Role check (@PreAuthorize hasRole('MANAGER')) and org-admin
                        // check (@IsOrgAdmin) happen at the method layer -- neither
                        // approver nor deleter is expected to be the submitter.
                        .requestMatchers(HttpMethod.POST, "/api/v1/reports/{id}/approve").access(tenancyOnly)
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/reports/{id}").access(tenancyOnly)
                        .requestMatchers(HttpMethod.GET, "/api/v1/reports").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/reports").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/v1/reimbursements").authenticated()
                        .anyRequest().denyAll());

        return http.build();
    }
}
