package com.mel.expensetracker.authserver.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/** [FEATURE A1] Logs the attempted username (never the password) then redirects to /login?error. */
@Component
public class FormLoginFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(FormLoginFailureHandler.class);

    private final AuthenticationFailureHandler delegate = new SimpleUrlAuthenticationFailureHandler("/login?error");

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        log.warn("Login failure for user '{}': {}", request.getParameter("username"), exception.getMessage());
        delegate.onAuthenticationFailure(request, response, exception);
    }
}
