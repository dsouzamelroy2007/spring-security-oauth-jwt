package com.mel.expensetracker.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * [FEATURE D6] Authenticated but not authorized -> RFC 9457 {@link ProblemDetail}
 * body. The {@code type} URI is deliberately distinct from other 403 causes
 * (role-gated, scope-gated, owner-only) so a caller -- or a test asserting on
 * acceptance criterion 3 -- can tell a cross-tenant/cross-org denial apart
 * from an ordinary role check without parsing the free-text {@code detail}.
 */
public class ProblemDetailAccessDeniedHandler implements AccessDeniedHandler {

    private final HttpMessageConverter<Object> problemDetailConverter;

    public ProblemDetailAccessDeniedHandler(HttpMessageConverter<Object> problemDetailConverter) {
        this.problemDetailConverter = problemDetailConverter;
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
    }
}
