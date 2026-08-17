package com.mel.expensetracker.resourceserver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * [FEATURE B8] Issuer/timestamp validation comes from
 * {@link JwtValidators#createDefaultWithIssuer}; audience and claim-shape
 * validation are added on top. Discovery-based ({@code withIssuerLocation}),
 * not a pinned key, since authorization-server's JWKS rotates on a schedule.
 */
@Configuration
public class JwtDecoderConfig {

    private final String issuerUri;
    private final String expectedAudience;

    public JwtDecoderConfig(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri,
            @Value("${app.security.expected-audience}") String expectedAudience) {
        this.issuerUri = issuerUri;
        this.expectedAudience = expectedAudience;
    }

    @Bean
    JwtDecoder jwtDecoder() {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();

        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtAudienceValidator(expectedAudience);
        OAuth2TokenValidator<Jwt> requiredClaimShapeValidator = new RequiredClaimShapeValidator();
        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(defaultValidators, audienceValidator, requiredClaimShapeValidator));

        return decoder;
    }
}
