package com.demo.student.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Reads the access token from the HttpOnly cookie and, if valid, marks the
 * request as authenticated. Invalid or missing tokens simply leave the request
 * anonymous; Spring Security then returns 401 for protected endpoints.
 * <p>
 * Not a @Component on purpose: it is registered only inside the security
 * filter chain (see SecurityConfig), so it does not run twice.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CookieService cookieService;

    public JwtAuthenticationFilter(JwtService jwtService, CookieService cookieService) {
        this.jwtService = jwtService;
        this.cookieService = cookieService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        cookieService.readCookie(request, CookieService.ACCESS_COOKIE)
                .flatMap(jwtService::parseAccessToken)
                .ifPresent(user -> {
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, List.of());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });
        chain.doFilter(request, response);
    }
}
