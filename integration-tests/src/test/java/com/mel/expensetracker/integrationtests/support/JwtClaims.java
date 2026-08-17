package com.mel.expensetracker.integrationtests.support;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Decodes a JWT's claims without verifying its signature -- fine for
 * asserting claim shape in tests. Local copy of authorization-server's own
 * {@code JwtTestSupport}; see {@link Pkce} for why it isn't shared directly.
 */
public final class JwtClaims {

    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private JwtClaims() {}

    @SuppressWarnings("unchecked")
    public static Map<String, Object> decode(String jwt) {
        String[] parts = jwt.split("\\.");
        byte[] payload = Base64.getUrlDecoder().decode(parts[1].getBytes(StandardCharsets.US_ASCII));
        return MAPPER.readValue(payload, Map.class);
    }
}
