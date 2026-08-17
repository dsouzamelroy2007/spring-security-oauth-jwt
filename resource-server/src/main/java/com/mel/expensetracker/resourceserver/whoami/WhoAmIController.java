package com.mel.expensetracker.resourceserver.whoami;

import com.mel.expensetracker.resourceserver.whoami.dto.WhoAmIResponseV1;
import com.mel.expensetracker.resourceserver.whoami.dto.WhoAmIResponseV2;
import com.mel.expensetracker.shared.org.OrgContext;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dumps the resolved principal, authorities and JWT claims -- the single most
 * useful endpoint for showing what the framework actually did with a token.
 * Demonstrates Boot 4's native API versioning (feature note: {@code v2} adds
 * the bare {@code roles} claim and the full claim map, {@code v1} stays
 * minimal).
 */
@RestController
public class WhoAmIController {

    @GetMapping(value = "/api/v1/whoami", version = "1")
    @PreAuthorize("isAuthenticated()")
    public WhoAmIResponseV1 whoAmIV1(OrgContext orgContext, Authentication authentication) {
        return new WhoAmIResponseV1(orgContext.subject(), orgContext.orgSlug(), authorityStrings(authentication));
    }

    @GetMapping(value = "/api/v1/whoami", version = "2")
    @PreAuthorize("isAuthenticated()")
    public WhoAmIResponseV2 whoAmIV2(
            OrgContext orgContext, Authentication authentication, @AuthenticationPrincipal Jwt jwt) {
        return new WhoAmIResponseV2(
                orgContext.subject(),
                orgContext.orgSlug(),
                orgContext.roles(),
                authorityStrings(authentication),
                jwt.getClaims());
    }

    private static List<String> authorityStrings(Authentication authentication) {
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }
}
