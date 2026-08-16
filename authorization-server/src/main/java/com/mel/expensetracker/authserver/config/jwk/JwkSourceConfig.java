package com.mel.expensetracker.authserver.config.jwk;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/** [FEATURE B5] RSA signing key rotation, wired into both signing and JWKS serving. */
@Configuration
public class JwkSourceConfig {

    @Bean
    public RotatingRsaKeyManager rotatingRsaKeyManager() {
        return new RotatingRsaKeyManager();
    }

    /**
     * Used by Spring Authorization Server both to serve {@code /oauth2/jwks}
     * (the framework strips private key material before responding) and,
     * historically, to verify already-issued tokens against a retired key.
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource(RotatingRsaKeyManager keyManager) {
        return (jwkSelector, securityContext) -> jwkSelector.select(keyManager.currentJwkSet());
    }

    /**
     * A hand-built encoder, not the one Spring Authorization Server would
     * build for us implicitly: with the retained prior key also present in
     * {@code jwkSource}, more than one JWK matches the RS256/signature
     * selector, and NimbusJwtEncoder throws on an ambiguous match by default
     * (since Spring Security 6.5). setJwkSelector(List::getFirst) is safe here
     * because RotatingRsaKeyManager always returns the current signing key first.
     */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        NimbusJwtEncoder encoder = new NimbusJwtEncoder(jwkSource);
        encoder.setJwkSelector(List::getFirst);
        return encoder;
    }
}
