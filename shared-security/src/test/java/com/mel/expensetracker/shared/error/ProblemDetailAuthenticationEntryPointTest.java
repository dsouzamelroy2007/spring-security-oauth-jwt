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
import org.springframework.security.authentication.BadCredentialsException;

class ProblemDetailAuthenticationEntryPointTest {

    private final ProblemDetailAuthenticationEntryPoint entryPoint =
            new ProblemDetailAuthenticationEntryPoint(new JacksonJsonHttpMessageConverter());

    @Test
    void writesRfc9457BodyOn401() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/whoami");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("no token"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        String body = response.getContentAsString();
        assertThat(body).contains("\"status\":401");
        assertThat(body).contains("urn:expensetracker:problem:unauthenticated");
        assertThat(body).contains("/api/v1/whoami");
    }

    @Test
    void writesAuthenticationRequiredAuditRowWhenWriterIsConfigured() throws Exception {
        AuditEventWriter auditEventWriter = mock(AuditEventWriter.class);
        ProblemDetailAuthenticationEntryPoint auditingEntryPoint =
                new ProblemDetailAuthenticationEntryPoint(new JacksonJsonHttpMessageConverter(), auditEventWriter);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/whoami");
        MockHttpServletResponse response = new MockHttpServletResponse();

        auditingEntryPoint.commence(request, response, new BadCredentialsException("no token"));

        verify(auditEventWriter)
                .write(argThat((AuditEvent event) -> event.eventType().equals("authentication_required")));
    }

    @Test
    void writesNothingWhenWriterAbsent() throws Exception {
        AuditEventWriter auditEventWriter = mock(AuditEventWriter.class);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/whoami");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("no token"));

        verifyNoInteractions(auditEventWriter);
    }
}
