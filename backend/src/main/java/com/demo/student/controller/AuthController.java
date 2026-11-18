package com.demo.student.controller;

import com.demo.student.dto.ApiResponse;
import com.demo.student.dto.LoginRequest;
import com.demo.student.dto.RegisterRequest;
import com.demo.student.dto.UserResponse;
import com.demo.student.exception.UnauthorizedException;
import com.demo.student.security.AuthenticatedUser;
import com.demo.student.security.CookieService;
import com.demo.student.service.AuthService;
import com.demo.student.service.AuthService.AuthResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CookieService cookieService;

    public AuthController(AuthService authService, CookieService cookieService) {
        this.authService = authService;
        this.cookieService = cookieService;
    }

    /** Creates the account and logs the user in (sets auth cookies). */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request,
                                                              HttpServletResponse response) {
        AuthResult result = authService.register(request);
        cookieService.setAuthCookies(response, result.accessToken(), result.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registration successful", result.user()));
    }

    @PostMapping("/login")
    public ApiResponse<UserResponse> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthResult result = authService.login(request);
        cookieService.setAuthCookies(response, result.accessToken(), result.refreshToken());
        return ApiResponse.ok("Login successful", result.user());
    }

    @PostMapping("/refresh")
    public ApiResponse<UserResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.readCookie(request, CookieService.REFRESH_COOKIE)
                .orElseThrow(() -> {
                    cookieService.clearAuthCookies(response);
                    return new UnauthorizedException("No active session. Please log in.");
                });
        try {
            AuthResult result = authService.refresh(refreshToken);
            cookieService.setAuthCookies(response, result.accessToken(), result.refreshToken());
            return ApiResponse.ok("Session refreshed", result.user());
        } catch (UnauthorizedException e) {
            cookieService.clearAuthCookies(response);
            throw e;
        }
    }

    /** Revokes the refresh token and clears both cookies. Works even if the access token expired. */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(cookieService.readCookie(request, CookieService.REFRESH_COOKIE).orElse(null));
        cookieService.clearAuthCookies(response);
        return ApiResponse.ok("Logged out successfully");
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal AuthenticatedUser user) {
        return ApiResponse.ok("Current user", authService.getCurrentUser(user.id()));
    }
}
