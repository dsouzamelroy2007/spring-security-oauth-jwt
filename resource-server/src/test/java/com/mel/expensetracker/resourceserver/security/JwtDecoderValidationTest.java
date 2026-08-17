package com.mel.expensetracker.resourceserver.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mel.expensetracker.resourceserver.config.RequiredClaimShapeValidator;
import com.mel.expensetracker.resourceserver.support.TestJwtSupport;
import com.nimbusds.jwt.JWTClaimsSet;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Exercises the real validator composition from {@code JwtDecoderConfig}
 * (issuer, timestamp, audience, claim-shape) plus real RSA signature
 * verification, against a locally-signed token rather than a live
 * authorization-server -- see {@link TestRsaTokenSupport}.
 */
class JwtDecoderValidationTest {

    private final TestRsaTokenSupport tokens = new TestRsaTokenSupport();
    private final JwtDecoder decoder = buildDecoder();

    private JwtDecoder buildDecoder() {
        NimbusJwtDecoder nimbusDecoder =
                NimbusJwtDecoder.withPublicKey(tokens.publicKey()).build();
        OAuth2TokenValidator<Jwt> defaultValidators = JwtValidators.createDefaultWithIssuer(TestJwtSupport.ISSUER);
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtAudienceValidator(TestJwtSupport.AUDIENCE);
        OAuth2TokenValidator<Jwt> claimShapeValidator = new RequiredClaimShapeValidator();
        nimbusDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidators, audienceValidator, claimShapeValidator));
        return nimbusDecoder;
    }

    @Test
    void validTokenDecodesSuccessfully() {
        String token = tokens.sign(
                TestRsaTokenSupport.validClaimsBuilder(TestJwtSupport.ISSUER, TestJwtSupport.AUDIENCE).build());

        Jwt jwt = decoder.decode(token);

        assertThat(jwt.getSubject()).isEqualTo("alice");
    }

    @Test
    void expiredTokenIsRejected() {
        Instant past = Instant.now().minusSeconds(3600);
        JWTClaimsSet claims = TestRsaTokenSupport.validClaimsBuilder(TestJwtSupport.ISSUER, TestJwtSupport.AUDIENCE)
                .issueTime(Date.from(past.minusSeconds(300)))
                .expirationTime(Date.from(past))
                .build();
        String token = tokens.sign(claims);

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void wrongIssuerIsRejected() {
        String token = tokens.sign(TestRsaTokenSupport.validClaimsBuilder("http://evil.example.com", TestJwtSupport.AUDIENCE)
                .build());

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void wrongAudienceIsRejected() {
        String token = tokens.sign(TestRsaTokenSupport.validClaimsBuilder(TestJwtSupport.ISSUER, "some-other-api")
                .build());

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void tamperedSignatureIsRejected() {
        String token = tokens.sign(
                TestRsaTokenSupport.validClaimsBuilder(TestJwtSupport.ISSUER, TestJwtSupport.AUDIENCE).build());
        String tampered = TestRsaTokenSupport.tamperSignature(token);

        assertThatThrownBy(() -> decoder.decode(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void missingScopeAndRolesClaimsIsRejected() {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(TestJwtSupport.ISSUER)
                .audience(TestJwtSupport.AUDIENCE)
                .subject("someone")
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .build();
        String token = tokens.sign(claims);

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void missingScopeButPresentRolesClaimIsAccepted() {
        Instant now = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(TestJwtSupport.ISSUER)
                .audience(TestJwtSupport.AUDIENCE)
                .subject("alice")
                .claim("roles", List.of("EMPLOYEE"))
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusSeconds(300)))
                .build();
        String token = tokens.sign(claims);

        assertThat(decoder.decode(token).getSubject()).isEqualTo("alice");
    }
}
