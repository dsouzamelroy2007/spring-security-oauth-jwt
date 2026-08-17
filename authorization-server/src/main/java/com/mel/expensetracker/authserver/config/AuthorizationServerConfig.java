package com.mel.expensetracker.authserver.config;

import com.mel.expensetracker.authserver.user.AppUserPrincipal;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.jackson.SecurityJacksonModules;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

/**
 * The @Order(1) chain: everything Spring Authorization Server owns
 * (/oauth2/authorize, /oauth2/token, /oauth2/jwks, /.well-known/openid-configuration,
 * /userinfo, ...). {@link com.mel.expensetracker.authserver.config.FormLoginSecurityConfig}
 * is @Order(2) and covers the rest of the app -- see its class comment for why
 * two chains, not one.
 */
@Configuration
public class AuthorizationServerConfig {

    // [FEATURE B9] JDBC-backed, not in-memory: clients/authorizations/consents
    // survive a restart, and this is the persistence story a real deployment needs.
    @Bean
    public RegisteredClientRepository registeredClientRepository(JdbcTemplate jdbcTemplate) {
        return new JdbcRegisteredClientRepository(jdbcTemplate);
    }

    @Bean
    public OAuth2AuthorizationService authorizationService(
            JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
        JdbcOAuth2AuthorizationService service =
                new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);
        // The default row mapper's Jackson allowlist only recognizes Spring
        // Security's own types. AppUserPrincipal is persisted as the stored
        // Authentication's principal, so without this it fails to deserialize
        // with "denied resolution" the moment consent/authorization rows are read back.
        // java.util.* is allowed too: OrgRoleClaimsTokenCustomizer stores the
        // access token's "aud" claim as List.of(...), whose runtime type is a
        // JDK-internal java.util.ImmutableCollections$List12 -- reading a row
        // back a second time (revocation lookup, authorization-code reuse
        // detection) deserializes that stored claim map and fails the same
        // way without an allowance for it. Scoped to the java.util package,
        // not a blanket allowance, to keep the gadget-attack surface JDK-only.
        BasicPolymorphicTypeValidator.Builder typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(AppUserPrincipal.class)
                .allowIfSubType("java.util.");
        JsonMapper jsonMapper = JsonMapper.builder()
                .addModules(SecurityJacksonModules.getModules(getClass().getClassLoader(), typeValidator))
                .build();
        RowMapper<OAuth2Authorization> rowMapper =
                new JdbcOAuth2AuthorizationService.JsonMapperOAuth2AuthorizationRowMapper(
                        registeredClientRepository, jsonMapper);
        service.setAuthorizationRowMapper(rowMapper);
        return service;
    }

    @Bean
    public OAuth2AuthorizationConsentService authorizationConsentService(
            JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
        return new JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(
            @Value("${app.oauth2.issuer}") String issuer) {
        return AuthorizationServerSettings.builder().issuer(issuer).build();
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    // [FEATURE D5] Ordered chain 1 of 2 -- see class comment for why two, not one.
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        // Verified against the actual 7.1.0 jar: the static authorizationServer()
        // factory method is gone now that SAS lives in spring-security-config;
        // the constructor is public and used directly instead.
        OAuth2AuthorizationServerConfigurer authorizationServer = new OAuth2AuthorizationServerConfigurer();

        http.securityMatcher(authorizationServer.getEndpointsMatcher())
                .with(authorizationServer, (server) -> server.oidc(Customizer.withDefaults())
                        // Without this, SAS renders its own built-in consent page
                        // inline (200 OK) instead of redirecting to ours (B10).
                        .authorizationEndpoint(endpoint -> endpoint.consentPage("/oauth2/consent")))
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                // Unauthenticated browser hit on e.g. /oauth2/authorize -> send to the
                // login page instead of a bare 401.
                .exceptionHandling(exceptions -> exceptions.defaultAuthenticationEntryPointFor(
                        new LoginUrlAuthenticationEntryPoint("/login"), new MediaTypeRequestMatcher(MediaType.TEXT_HTML)))
                // /userinfo is called with a bearer access token, not a browser
                // session -- this chain also has to act as a resource server.
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()));

        return http.build();
    }
}
