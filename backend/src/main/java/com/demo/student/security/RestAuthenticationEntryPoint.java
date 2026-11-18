package com.demo.student.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Returns a JSON 401 when an unauthenticated request hits a protected endpoint. */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonErrorWriter errorWriter;

    public RestAuthenticationEntryPoint(JsonErrorWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        errorWriter.write(response, HttpServletResponse.SC_UNAUTHORIZED,
                "Authentication required. Please log in.");
    }
}
