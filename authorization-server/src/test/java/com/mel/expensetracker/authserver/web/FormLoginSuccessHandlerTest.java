package com.mel.expensetracker.authserver.web;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.mel.expensetracker.shared.audit.AuditEvent;
import com.mel.expensetracker.shared.audit.AuditEventWriter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

class FormLoginSuccessHandlerTest {

    private final AuditEventWriter auditEventWriter = mock(AuditEventWriter.class);
    private final FormLoginSuccessHandler handler = new FormLoginSuccessHandler(auditEventWriter);

    @Test
    void writesLoginSuccessAuditRowForTheAuthenticatedUser() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new TestingAuthenticationToken("alice", "n/a");

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(auditEventWriter)
                .write(argThat((AuditEvent event) ->
                        event.eventType().equals("login_success") && event.principal().equals("alice")));
    }
}
