package com.mel.expensetracker.resourceserver.support;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Builds {@link Jwt}s matching authorization-server's real claim shape (see
 * {@code OrgRoleClaimsTokenCustomizer}) for the four seeded demo users plus
 * the {@code export-worker} M2M client. Paired with a {@code @MockitoBean
 * JwtDecoder} stubbed to return one of these regardless of the bearer string
 * a {@code RestTestClient} request actually sends -- this project's
 * established test style (see authorization-server's {@code AbstractIntegrationTest})
 * drives real HTTP requests rather than MockMvc, so there is no
 * decoder-bypassing request post-processor available here.
 */
public final class TestJwtSupport {

    public static final String ISSUER = "http://localhost:9000";
    public static final String AUDIENCE = "expense-tracker-api";

    private TestJwtSupport() {}

    public static Jwt alice() {
        return human("alice", "acme", "EMPLOYEE");
    }

    public static Jwt bob() {
        return human("bob", "acme", "MANAGER");
    }

    public static Jwt carol() {
        return human("carol", "globex", "FINANCE");
    }

    public static Jwt dana() {
        return human("dana", "globex", "ORG_ADMIN");
    }

    public static Jwt exportWorker() {
        return base("export-worker").claim("scope", "expenses.export").build();
    }

    public static Jwt human(String username, String orgSlug, String role) {
        return base(username)
                .claim("org", orgSlug)
                .claim("roles", List.of(role))
                .claim("scope", "openid profile expenses.read expenses.write")
                .build();
    }

    private static Jwt.Builder base(String subject) {
        return Jwt.withTokenValue("test-token-" + UUID.randomUUID())
                .header("alg", "RS256")
                .subject(subject)
                .issuer(ISSUER)
                .audience(List.of(AUDIENCE))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300));
    }
}
