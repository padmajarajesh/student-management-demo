package com.demo.student.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Typed view of the "app.*" settings in application.properties.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(String frontendUrl, Jwt jwt, Cookie cookie) {

    public AppProperties {
        if (jwt == null || jwt.secret() == null || jwt.secret().getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters long");
        }
        if (frontendUrl == null || frontendUrl.isBlank()) {
            throw new IllegalStateException("FRONTEND_URL must be set");
        }
    }

    /** Exact allowed origins, e.g. "https://my-app.vercel.app". Trailing slashes are removed. */
    public List<String> allowedOrigins() {
        List<String> origins = Arrays.stream(frontendUrl.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
                .toList();
        if (origins.contains("*")) {
            throw new IllegalStateException("FRONTEND_URL must list exact origins; '*' is not allowed with credentials");
        }
        return origins;
    }

    public record Jwt(String secret, long accessTokenMinutes, long refreshTokenDays) {
    }

    public record Cookie(boolean secure, String sameSite) {
    }
}
