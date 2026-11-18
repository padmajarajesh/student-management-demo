package com.demo.student.security;

import com.demo.student.config.AppProperties;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

/**
 * Writes and reads the authentication cookies.
 * <ul>
 *   <li>{@code access_token}  - JWT, sent with every API request (path /api)</li>
 *   <li>{@code refresh_token} - opaque token, only sent to /api/auth</li>
 * </ul>
 * Both are HttpOnly, so JavaScript in the browser can never read them.
 */
@Service
public class CookieService {

    public static final String ACCESS_COOKIE = "access_token";
    public static final String REFRESH_COOKIE = "refresh_token";
    private static final String ACCESS_PATH = "/api";
    private static final String REFRESH_PATH = "/api/auth";

    private final AppProperties.Cookie cookieProps;
    private final Duration accessTtl;
    private final Duration refreshTtl;

    public CookieService(AppProperties properties) {
        this.cookieProps = properties.cookie();
        this.accessTtl = Duration.ofMinutes(properties.jwt().accessTokenMinutes());
        this.refreshTtl = Duration.ofDays(properties.jwt().refreshTokenDays());
    }

    public void setAuthCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        addCookie(response, ACCESS_COOKIE, accessToken, ACCESS_PATH, accessTtl);
        addCookie(response, REFRESH_COOKIE, refreshToken, REFRESH_PATH, refreshTtl);
    }

    public void clearAuthCookies(HttpServletResponse response) {
        addCookie(response, ACCESS_COOKIE, "", ACCESS_PATH, Duration.ZERO);
        addCookie(response, REFRESH_COOKIE, "", REFRESH_PATH, Duration.ZERO);
    }

    public Optional<String> readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        return Arrays.stream(cookies)
                .filter(c -> name.equals(c.getName()))
                .map(Cookie::getValue)
                .filter(v -> v != null && !v.isBlank())
                .findFirst();
    }

    private void addCookie(HttpServletResponse response, String name, String value, String path, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieProps.secure())
                .sameSite(cookieProps.sameSite())
                .path(path)
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
