package com.demo.student.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/**
 * CSRF protection for cookie-based authentication.
 * <p>
 * Because the browser attaches cookies automatically, a malicious site could
 * try to submit a form to our API. Plain HTML forms cannot add custom headers,
 * and a cross-origin JavaScript request with a custom header triggers a CORS
 * preflight that our CORS policy rejects for unknown origins. So requiring the
 * {@code X-Requested-With} header on every state-changing request guarantees
 * the request came from our own frontend.
 */
public class CsrfHeaderFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Requested-With";
    private static final Set<String> SAFE_METHODS = Set.of("GET", "HEAD", "OPTIONS", "TRACE");

    private final JsonErrorWriter errorWriter;

    public CsrfHeaderFilter(JsonErrorWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        boolean unsafe = !SAFE_METHODS.contains(request.getMethod());
        String header = request.getHeader(HEADER);
        if (unsafe && (header == null || header.isBlank())) {
            errorWriter.write(response, HttpServletResponse.SC_FORBIDDEN,
                    "Missing " + HEADER + " header (CSRF protection)");
            return;
        }
        chain.doFilter(request, response);
    }
}
