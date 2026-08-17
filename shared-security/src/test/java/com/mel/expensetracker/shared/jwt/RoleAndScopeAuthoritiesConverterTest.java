package com.mel.expensetracker.shared.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class RoleAndScopeAuthoritiesConverterTest {

    private final RoleAndScopeAuthoritiesConverter converter = new RoleAndScopeAuthoritiesConverter();

    @Test
    void mapsScopeAndRolesToPrefixedAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("bob")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("scope", "expenses.read expenses.write")
                .claim("roles", List.of("MANAGER"))
                .build();

        List<String> authorities = converter.convert(jwt).stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        assertThat(authorities)
                .containsExactlyInAnyOrder("SCOPE_expenses.read", "SCOPE_expenses.write", "ROLE_MANAGER");
    }

    @Test
    void clientCredentialsTokenWithNoRolesClaimYieldsScopeOnlyAuthorities() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .subject("export-worker")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .claim("scope", "expenses.export")
                .build();

        List<String> authorities = converter.convert(jwt).stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());

        assertThat(authorities).containsExactly("SCOPE_expenses.export");
    }
}
