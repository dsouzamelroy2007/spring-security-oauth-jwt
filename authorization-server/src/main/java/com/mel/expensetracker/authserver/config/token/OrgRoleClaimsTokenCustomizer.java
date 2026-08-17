package com.mel.expensetracker.authserver.config.token;

import com.mel.expensetracker.authserver.user.AppUserPrincipal;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

/**
 * [FEATURE B6] Adds org + role claims to every JWT-shaped token this server
 * issues -- the access token and id_token share this same context type and
 * customizer invocation, so one bean covers both.
 *
 * <p>Role names are emitted bare (no ROLE_ prefix): the token itself stays
 * framework-agnostic. Mapping claims to Spring's GrantedAuthority convention
 * is deliberately left to the resource server's JwtAuthenticationConverter
 * (M3, feature B7) -- the correct layer for that decision, per C1's
 * role-vs-authority-vs-scope writeup.
 */
@Component
public class OrgRoleClaimsTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    private final String resourceAudience;

    public OrgRoleClaimsTokenCustomizer(@Value("${app.oauth2.resource-audience}") String resourceAudience) {
        this.resourceAudience = resourceAudience;
    }

    @Override
    public void customize(JwtEncodingContext context) {
        // client_credentials (export-worker) has no end user -- context.getPrincipal()
        // is the client's own authentication, so this simply doesn't match and
        // only the standard `scope` claim appears, as it should.
        if (context.getPrincipal().getPrincipal() instanceof AppUserPrincipal user) {
            context.getClaims().claim("org", user.getOrgSlug());
            context.getClaims().claim("roles", user.getRoleNames());
        }

        // [FEATURE B8] Audience is scoped to access tokens only: id_tokens are
        // consumed by the client itself (via client_id), not by a resource
        // server checking `aud`, so stamping it there would be meaningless.
        // Without this, resource-server's audience validator would have no
        // real claim to check against -- an unacceptable stubbed check.
        if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
            context.getClaims().audience(List.of(resourceAudience));
        }
    }
}
