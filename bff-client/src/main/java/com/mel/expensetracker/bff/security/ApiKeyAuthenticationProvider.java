package com.mel.expensetracker.bff.security;

import java.util.List;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * [FEATURE A5] Compares the presented key against the single configured
 * demo key (see {@code app.security.api-key}). No user store, no per-client
 * keys -- this exists to demonstrate a pre-authenticated filter/chain, not
 * to be a real API-key system.
 */
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {

    private final String expectedApiKey;

    public ApiKeyAuthenticationProvider(String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String presentedKey = (String) authentication.getPrincipal();
        if (presentedKey == null || !presentedKey.equals(expectedApiKey)) {
            throw new BadCredentialsException("Invalid API key");
        }
        return new PreAuthenticatedAuthenticationToken(
                "api-key-client", presentedKey, List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT")));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
