package com.mel.expensetracker.authserver.config.jwk;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * [FEATURE B5] In-memory RSA keystore backing the authorization server's JWK
 * source. Single-instance, non-persisted -- a real multi-instance deployment
 * would need a shared key store; out of scope for this reference app.
 *
 * <p>Keeps the current signing key plus one prior generation, so tokens issued
 * before a {@link #rotate()} remain verifiable against {@code /oauth2/jwks}
 * until they expire, instead of being invalidated the instant a new key is
 * generated.
 */
public final class RotatingRsaKeyManager {

    private static final int RETENTION = 2;

    private final Object lock = new Object();
    private List<RSAKey> keys;

    public RotatingRsaKeyManager() {
        this.keys = new ArrayList<>(List.of(generate()));
    }

    /**
     * All currently held keys, current signing key first, including private
     * key material. Used both to sign new tokens and (via {@link JwkSourceConfig})
     * to serve {@code /oauth2/jwks}, where the framework strips private fields
     * before serializing the response.
     */
    public JWKSet currentJwkSet() {
        synchronized (lock) {
            return new JWKSet(List.copyOf(keys));
        }
    }

    public void rotate() {
        synchronized (lock) {
            List<RSAKey> next = new ArrayList<>();
            next.add(generate());
            next.addAll(keys);
            while (next.size() > RETENTION) {
                next.remove(next.size() - 1);
            }
            keys = next;
        }
    }

    private static RSAKey generate() {
        try {
            return new RSAKeyGenerator(2048)
                    .keyUse(KeyUse.SIGNATURE)
                    .algorithm(JWSAlgorithm.RS256)
                    .keyID(UUID.randomUUID().toString())
                    .generate();
        } catch (JOSEException e) {
            throw new IllegalStateException("Unable to generate RSA signing key", e);
        }
    }
}
