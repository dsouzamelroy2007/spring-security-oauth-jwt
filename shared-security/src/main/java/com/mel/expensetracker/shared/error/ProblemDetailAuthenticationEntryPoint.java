package com.mel.expensetracker.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    private final HttpMessageConverter<Object> problemDetailConverter;

    public ProblemDetailAuthenticationEntryPoint(HttpMessageConverter<Object> problemDetailConverter) {
        this.problemDetailConverter = problemDetailConverter;
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
    }
}
