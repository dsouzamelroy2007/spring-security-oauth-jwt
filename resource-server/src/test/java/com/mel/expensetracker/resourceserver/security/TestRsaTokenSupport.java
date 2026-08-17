package com.mel.expensetracker.resourceserver.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * Mints real, RSA-signed JWTs against a locally generated keypair (mirrors
 * authorization-server's own {@code RotatingRsaKeyManager} pattern) so
 * {@link JwtDecoderValidationTest} exercises the real validator chain -- issuer,
 * timestamp, audience, claim-shape, signature -- without needing a live
 * authorization-server or network JWKS endpoint.
 */
public final class TestRsaTokenSupport {

    private final RSAKey rsaKey;

    public TestRsaTokenSupport() {
        try {
            this.rsaKey = new RSAKeyGenerator(2048)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID(UUID.randomUUID().toString())
                    .generate();
        } catch (JOSEException e) {
            throw new IllegalStateException("Unable to generate RSA signing key", e);
        }
    }

    public RSAPublicKey publicKey() {
        try {
            return rsaKey.toRSAPublicKey();
        } catch (JOSEException e) {
            throw new IllegalStateException("Unable to derive RSA public key", e);
        }
    }

    public String sign(JWTClaimsSet claims) {
        try {
            SignedJWT jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaKey.getKeyID()).build(), claims);
            jwt.sign(new RSASSASigner(rsaKey));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Unable to sign test JWT", e);
        }
    }

    /** Flips a character in the signature segment -- same header/payload, different bytes to verify. */
    public static String tamperSignature(String compactJwt) {
        String[] parts = compactJwt.split("\\.");
        char[] signature = parts[2].toCharArray();
        signature[0] = signature[0] == 'A' ? 'B' : 'A';
        return parts[0] + "." + parts[1] + "." + new String(signature);
    }

    public static JWTClaimsSet.Builder validClaimsBuilder(String issuer, String audience) {
        java.time.Instant now = java.time.Instant.now();
        return new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject("alice")
                .claim("org", "acme")
                .claim("roles", java.util.List.of("EMPLOYEE"))
                .claim("scope", "openid profile expenses.read expenses.write")
                .issueTime(java.util.Date.from(now))
                .expirationTime(java.util.Date.from(now.plusSeconds(300)));
    }
}
