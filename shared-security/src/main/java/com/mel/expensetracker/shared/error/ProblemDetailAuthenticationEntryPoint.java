package com.mel.expensetracker.shared.error;

import com.mel.expensetracker.shared.audit.AuditEvent;
import com.mel.expensetracker.shared.audit.AuditEventWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * [FEATURE D6] Missing/invalid bearer token -> RFC 9457 {@link ProblemDetail}
 * body instead of Spring Security's default WWW-Authenticate-only 401. A
 * consistent machine-readable error shape across every endpoint in this repo
 * (form login failures included, via the sibling access-denied handler) beats
 * one API returning HTML and another returning a bespoke JSON shape.
 */
public class ProblemDetailAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger log = LoggerFactory.getLogger(ProblemDetailAuthenticationEntryPoint.class);

    private final HttpMessageConverter<Object> problemDetailConverter;
    private final AuditEventWriter auditEventWriter;

    public ProblemDetailAuthenticationEntryPoint(HttpMessageConverter<Object> problemDetailConverter) {
        this(problemDetailConverter, null);
    }

    /**
     * [FEATURE D8] {@code auditEventWriter} is nullable -- see the sibling
     * access-denied handler's constructor for why.
     */
    public ProblemDetailAuthenticationEntryPoint(
            HttpMessageConverter<Object> problemDetailConverter, AuditEventWriter auditEventWriter) {
        this.problemDetailConverter = problemDetailConverter;
        this.auditEventWriter = auditEventWriter;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED, "Authentication is required to access this resource.");
        problem.setType(java.net.URI.create("urn:expensetracker:problem:unauthenticated"));
        problem.setInstance(java.net.URI.create(request.getRequestURI()));

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        problemDetailConverter.write(
                problem, MediaType.APPLICATION_PROBLEM_JSON, new ServletServerHttpResponse(response));

        if (auditEventWriter != null) {
            // [FEATURE D8] Never let an audit-write failure turn an already
            // -written 401 response into a 500 -- the response above is
            // already committed to the wire by this point.
            try {
                auditEventWriter.write(new AuditEvent("authentication_required", null, null, request.getRemoteAddr(), null));
            } catch (RuntimeException e) {
                log.warn("Failed to write authentication_required audit row", e);
            }
        }
    }
}
