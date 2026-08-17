package com.mel.expensetracker.authserver.web;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.mel.expensetracker.shared.audit.AuditEvent;
import com.mel.expensetracker.shared.audit.AuditEventWriter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

class FormLoginFailureHandlerTest {

    private final AuditEventWriter auditEventWriter = mock(AuditEventWriter.class);
    private final FormLoginFailureHandler handler = new FormLoginFailureHandler(auditEventWriter);

    @Test
    void writesLoginFailureAuditRowWithTheAttemptedUsernameNeverThePassword() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setParameter("username", "alice");
        request.setParameter("password", "hunter2");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationFailure(request, response, new BadCredentialsException("bad credentials"));

        verify(auditEventWriter)
                .write(argThat((AuditEvent event) ->
                        event.eventType().equals("login_failure") && event.principal().equals("alice")));
    }
}
