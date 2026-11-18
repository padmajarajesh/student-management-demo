package com.demo.student.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Returns a JSON 403 when an authenticated user is not allowed to do something. */
@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private final JsonErrorWriter errorWriter;

    public RestAccessDeniedHandler(JsonErrorWriter errorWriter) {
        this.errorWriter = errorWriter;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        errorWriter.write(response, HttpServletResponse.SC_FORBIDDEN,
                "You do not have permission to perform this action.");
    }
}
