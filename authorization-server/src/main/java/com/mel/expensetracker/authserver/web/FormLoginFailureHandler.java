package com.mel.expensetracker.authserver.web;

import com.mel.expensetracker.shared.audit.AuditEvent;
import com.mel.expensetracker.shared.audit.AuditEventWriter;
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

/**
 * [FEATURE A1] Logs the attempted username (never the password) then redirects
 * to /login?error.
 *
 * <p>[FEATURE D8] Also writes the {@code login_failure} audit row, same
 * never-the-password rule applied to the {@code principal} column.
 */
@Component
public class FormLoginFailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(FormLoginFailureHandler.class);

    private final AuthenticationFailureHandler delegate = new SimpleUrlAuthenticationFailureHandler("/login?error");
    private final AuditEventWriter auditEventWriter;

    public FormLoginFailureHandler(AuditEventWriter auditEventWriter) {
        this.auditEventWriter = auditEventWriter;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        String attemptedUsername = request.getParameter("username");
        log.warn("Login failure for user '{}': {}", attemptedUsername, exception.getMessage());
        auditEventWriter.write(
                new AuditEvent("login_failure", attemptedUsername, null, request.getRemoteAddr(), null));
        delegate.onAuthenticationFailure(request, response, exception);
    }
}
