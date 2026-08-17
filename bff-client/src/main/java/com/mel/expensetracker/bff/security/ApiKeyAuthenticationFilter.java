package com.mel.expensetracker.bff.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

/**
 * [FEATURE A5] Pre-authenticated filter: the credential is a header, not a
 * username/password form. {@link #getPreAuthenticatedPrincipal} returning
 * {@code null} (header absent) makes the filter skip authentication entirely
 * -- the request falls through unauthenticated, and the chain's own
 * {@code authorizeHttpRequests().anyRequest().authenticated()} plus RFC 9457
 * entry point produce the 401.
 */
public class ApiKeyAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter {

    static final String API_KEY_HEADER = "X-API-Key";

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
        return request.getHeader(API_KEY_HEADER);
    }

    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
        return request.getHeader(API_KEY_HEADER);
    }
}
