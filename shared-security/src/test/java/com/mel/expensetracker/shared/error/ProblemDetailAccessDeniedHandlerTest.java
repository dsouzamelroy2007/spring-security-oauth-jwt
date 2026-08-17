package com.mel.expensetracker.shared.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.mel.expensetracker.shared.audit.AuditEvent;
import com.mel.expensetracker.shared.audit.AuditEventWriter;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

class ProblemDetailAccessDeniedHandlerTest {

    private final ProblemDetailAccessDeniedHandler handler =
            new ProblemDetailAccessDeniedHandler(new JacksonJsonHttpMessageConverter());

    @Test
    void writesRfc9457BodyOn403WithDistinguishableType() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/reports/99");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("cross-org"));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        String body = response.getContentAsString();
        assertThat(body).contains("\"status\":403");
        assertThat(body).contains("urn:expensetracker:problem:access-denied");
        assertThat(body).contains("/api/v1/reports/99");
    }

    @Test
    void writesAccessDeniedAuditRowWhenWriterIsConfigured() throws Exception {
        AuditEventWriter auditEventWriter = mock(AuditEventWriter.class);
        ProblemDetailAccessDeniedHandler auditingHandler =
                new ProblemDetailAccessDeniedHandler(new JacksonJsonHttpMessageConverter(), auditEventWriter);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/reports");
        MockHttpServletResponse response = new MockHttpServletResponse();

        auditingHandler.handle(request, response, new AccessDeniedException("csrf"));

        verify(auditEventWriter).write(argThat((AuditEvent event) -> event.eventType().equals("access_denied")));
    }

    @Test
    void writesNothingWhenWriterAbsent() throws Exception {
        AuditEventWriter auditEventWriter = mock(AuditEventWriter.class);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/reports/99");
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.handle(request, response, new AccessDeniedException("cross-org"));

        verifyNoInteractions(auditEventWriter);
    }
}
