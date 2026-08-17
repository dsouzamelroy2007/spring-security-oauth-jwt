package com.mel.expensetracker.shared.org;

import java.util.Set;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * The caller's tenant and role, resolved from JWT claims. Centralizes the
 * claim names ({@code sub}, {@code org}, {@code roles}) so controllers and
 * {@code AuthorizationManager}s don't each hand-parse the raw {@link Jwt}.
 *
 * <p>{@code orgSlug} and {@code roles} are absent (null / empty) on
 * client_credentials (M2M) tokens -- there is no end user, so no tenant and
 * no role to resolve. Callers doing org-scoped or role-gated work must not be
 * reachable by M2M tokens in the first place (enforced in security config,
 * not here).
 */
public record OrgContext(String subject, String orgSlug, Set<String> roles) {

    private static final String ORG_CLAIM = "org";
    private static final String ROLES_CLAIM = "roles";

    public static OrgContext from(Jwt jwt) {
        Set<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM) == null
                ? Set.of()
                : Set.copyOf(jwt.getClaimAsStringList(ROLES_CLAIM));
        return new OrgContext(jwt.getSubject(), jwt.getClaimAsString(ORG_CLAIM), roles);
    }
}
