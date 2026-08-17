package com.mel.expensetracker.bff.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

/**
 * [FEATURE D2] {@link XorCsrfTokenRequestAttributeHandler} BREACH-protects a
 * token exposed to a server-rendered page, but a fetch-based SPA reads the
 * token straight off the readable {@code XSRF-TOKEN} cookie -- the raw
 * value, never XORed -- and echoes it back verbatim in the
 * {@code X-XSRF-TOKEN} header. Resolving that header value through the XOR
 * handler would fail (it expects an XORed value). This handler keeps XOR
 * protection for the request-attribute path but falls back to plain
 * resolution whenever the token arrives via header, matching Spring
 * Security's own documented SPA guidance.
 */
public final class SpaCsrfTokenRequestHandler extends CsrfTokenRequestAttributeHandler {

    private final CsrfTokenRequestHandler xorHandler = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, Supplier<CsrfToken> csrfToken) {
        this.xorHandler.handle(request, response, csrfToken);
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        String headerValue = request.getHeader(csrfToken.getHeaderName());
        return StringUtils.hasText(headerValue)
                ? super.resolveCsrfTokenValue(request, csrfToken)
                : this.xorHandler.resolveCsrfTokenValue(request, csrfToken);
    }
}
