package com.mel.expensetracker.shared.jwt;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

/**
 * [FEATURE B7] [FEATURE C1] Role vs authority vs scope, documented in one place.
 *
 * <p>Three related-but-different concepts collapse into Spring Security's single
 * {@link GrantedAuthority} model here, deliberately, so the mapping lives in one
 * file instead of being re-guessed by every {@code @PreAuthorize} expression:
 *
 * <ul>
 *   <li><b>Scope</b> -- what the <i>client application</i> was authorized to ask
 *       for, e.g. {@code expenses.export}. Present on every token, including
 *       client_credentials tokens with no end user. Mapped to
 *       {@code SCOPE_expenses.export} by the standard
 *       {@link JwtGrantedAuthoritiesConverter}.
 *   <li><b>Role</b> -- what the <i>end user</i> is, e.g. {@code MANAGER}. Only
 *       present when the principal is a human (see the authorization server's
 *       {@code OrgRoleClaimsTokenCustomizer}). Mapped to {@code ROLE_MANAGER}
 *       here, matching Spring Security's own {@code hasRole(...)} convention,
 *       which silently assumes that prefix.
 *   <li><b>Authority</b> -- the umbrella {@link GrantedAuthority} type both of
 *       the above become once mapped. {@code hasAuthority(...)} checks the raw
 *       string; {@code hasRole(...)} is sugar that only ever matches the
 *       {@code ROLE_} family.
 * </ul>
 *
 * <p>The {@code roles} claim is a bare JSON array (no {@code ROLE_} prefix) by
 * the authorization server's own design -- token claims stay framework-agnostic,
 * and prefixing is this converter's job, not the token issuer's.
 */
public final class RoleAndScopeAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String ROLES_CLAIM = "roles";
    private static final String ROLE_PREFIX = "ROLE_";

    private final JwtGrantedAuthoritiesConverter scopeAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new LinkedHashSet<>(scopeAuthoritiesConverter.convert(jwt));
        List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
        // Absent for client_credentials (M2M) tokens -- no end user, no roles.
        if (roles != null) {
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role)));
        }
        return authorities;
    }
}
