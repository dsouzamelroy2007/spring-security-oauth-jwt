package com.mel.expensetracker.authserver.web;

import com.mel.expensetracker.shared.audit.AuditEvent;
import com.mel.expensetracker.shared.audit.AuditEventWriter;
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
 * /oauth2/authorize returns there, not to a fixed page).
 *
 * <p>[FEATURE D8] Also writes the {@code login_success} audit row -- the hook
 * point this class's own log line always pointed to.
 */
@Component
public class FormLoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(FormLoginSuccessHandler.class);

    private final AuthenticationSuccessHandler delegate = new SavedRequestAwareAuthenticationSuccessHandler();
    private final AuditEventWriter auditEventWriter;

    public FormLoginSuccessHandler(AuditEventWriter auditEventWriter) {
        this.auditEventWriter = auditEventWriter;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        log.info("Login success for user '{}'", authentication.getName());
        auditEventWriter.write(
                new AuditEvent("login_success", authentication.getName(), null, request.getRemoteAddr(), null));
        delegate.onAuthenticationSuccess(request, response, authentication);
    }
}
