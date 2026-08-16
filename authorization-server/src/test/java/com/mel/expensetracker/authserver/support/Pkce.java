package com.mel.expensetracker.authserver.support;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/** RFC 7636 code_verifier / code_challenge (S256) pair for driving a PKCE flow in tests. */
public record Pkce(String verifier, String challenge) {

    public static Pkce generate() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        String verifier = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String challenge = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(sha256(verifier.getBytes(StandardCharsets.US_ASCII)));
        return new Pkce(verifier, challenge);
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
