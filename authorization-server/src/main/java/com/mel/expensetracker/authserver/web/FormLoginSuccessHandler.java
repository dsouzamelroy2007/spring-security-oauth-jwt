package com.mel.expensetracker.authserver.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * [FEATURE A1] Delegates the actual redirect to Spring's own
 * SavedRequestAwareAuthenticationSuccessHandler (so a login triggered mid
 * /oauth2/authorize returns there, not to a fixed page), adding only a log
 * line -- the natural hook point for D8's audit event once that lands in M4.
 */
@Component
public class FormLoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(FormLoginSuccessHandler.class);

    private final AuthenticationSuccessHandler delegate = new SavedRequestAwareAuthenticationSuccessHandler();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        log.info("Login success for user '{}'", authentication.getName());
        delegate.onAuthenticationSuccess(request, response, authentication);
    }
}
