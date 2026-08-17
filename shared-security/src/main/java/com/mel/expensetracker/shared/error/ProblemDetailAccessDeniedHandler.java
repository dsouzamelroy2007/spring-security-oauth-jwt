package com.mel.expensetracker.shared.error;

import com.mel.expensetracker.shared.audit.AuditEvent;
import com.mel.expensetracker.shared.audit.AuditEventWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * [FEATURE D6] Authenticated but not authorized -> RFC 9457 {@link ProblemDetail}
 * body. The {@code type} URI is deliberately distinct from other 403 causes
 * (role-gated, scope-gated, owner-only) so a caller -- or a test asserting on
 * acceptance criterion 3 -- can tell a cross-tenant/cross-org denial apart
 * from an ordinary role check without parsing the free-text {@code detail}.
 *
 * <p>[FEATURE D8] Also the CSRF-rejection path's {@code AccessDeniedHandler}
 * (Spring Security's CSRF exceptions extend {@link AccessDeniedException}), so
 * this one handler covers both the role/tenancy-denial and CSRF-denial halves
 * of the "access denied" audit event.
 */
public class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(ProblemDetailAccessDeniedHandler.class);

    private final HttpMessageConverter<Object> problemDetailConverter;
    private final AuditEventWriter auditEventWriter;

    public ProblemDetailAccessDeniedHandler(HttpMessageConverter<Object> problemDetailConverter) {
        this(problemDetailConverter, null);
    }

    /**
     * [FEATURE D8] {@code auditEventWriter} is nullable: a module with no JDBC
     * connection to the audit database (see {@code AuditAutoConfiguration})
     * still gets working RFC 9457 responses, just without an audit row.
     */
    public ProblemDetailAccessDeniedHandler(
            HttpMessageConverter<Object> problemDetailConverter, AuditEventWriter auditEventWriter) {
        this.problemDetailConverter = problemDetailConverter;
        this.auditEventWriter = auditEventWriter;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN, "You do not have permission to access this resource.");
        problem.setType(URI.create("urn:expensetracker:problem:access-denied"));
        problem.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(HttpStatus.FORBIDDEN.value());
        problemDetailConverter.write(
                problem, MediaType.APPLICATION_PROBLEM_JSON, new ServletServerHttpResponse(response));

        if (auditEventWriter != null) {
            // [FEATURE D8] Never let an audit-write failure turn an already
            // -written 403 response into a 500 -- the response above is
            // already committed to the wire by this point.
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String principal = authentication != null ? authentication.getName() : null;
                auditEventWriter.write(new AuditEvent("access_denied", principal, null, request.getRemoteAddr(), null));
            } catch (RuntimeException e) {
                log.warn("Failed to write access_denied audit row", e);
            }
        }
    }
}
