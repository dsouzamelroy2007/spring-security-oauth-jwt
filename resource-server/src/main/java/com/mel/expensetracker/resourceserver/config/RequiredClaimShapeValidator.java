package com.mel.expensetracker.resourceserver.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * [FEATURE B8] Every token this API accepts must carry either a {@code scope}
 * claim (client_credentials) or a {@code roles} claim (a human, via the
 * authorization_code grant) -- that's the whole authorization model this
 * service understands. A same-issuer, correctly-signed token that carries
 * neither shape isn't a stubbed check away from a real threat: it's a sign
 * the token was minted for something other than this API (e.g. a future
 * client this server was never updated to understand), and should be
 * rejected rather than silently treated as an anonymous-authenticated caller.
 */
public class RequiredClaimShapeValidator implements OAuth2TokenValidator<Jwt> {

    private static final OAuth2Error MISSING_CLAIM_SHAPE =
            new OAuth2Error("invalid_token", "The token contains neither a scope nor a roles claim.", null);

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        boolean hasScope = jwt.hasClaim("scope");
        boolean hasRoles = jwt.hasClaim("roles");
        if (hasScope || hasRoles) {
            return OAuth2TokenValidatorResult.success();
        }
        return OAuth2TokenValidatorResult.failure(MISSING_CLAIM_SHAPE);
    }
}
