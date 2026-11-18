package com.demo.student.service;

import com.demo.student.config.AppProperties;
import com.demo.student.dto.LoginRequest;
import com.demo.student.dto.RegisterRequest;
import com.demo.student.dto.UserResponse;
import com.demo.student.entity.RefreshToken;
import com.demo.student.entity.User;
import com.demo.student.exception.DuplicateResourceException;
import com.demo.student.exception.UnauthorizedException;
import com.demo.student.exception.ValidationException;
import com.demo.student.repository.RefreshTokenRepository;
import com.demo.student.repository.UserRepository;
import com.demo.student.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    /** Result of a successful login/registration/refresh: the user plus the two tokens to put in cookies. */
    public record AuthResult(UserResponse user, String accessToken, String refreshToken) {
    }

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final long refreshTokenDays;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AppProperties properties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenDays = properties.jwt().refreshTokenDays();
    }

    @Transactional
    public AuthResult register(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new ValidationException("confirmPassword", "Passwords do not match");
        }
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("email", "An account with this email already exists");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPhoneNumber(request.phoneNumber().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        return issueTokens(user);
    }

    @Transactional
    public AuthResult login(LoginRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        refreshTokenRepository.deleteExpiredForUser(user.getId(), LocalDateTime.now(ZoneOffset.UTC));
        return issueTokens(user);
    }

    /**
     * Exchanges a valid refresh token for a new access token AND a new refresh
     * token (rotation). The old refresh token is deleted, so it cannot be reused.
     */
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public AuthResult refresh(String rawRefreshToken) {
        RefreshToken stored = refreshTokenRepository.findByTokenHash(hash(rawRefreshToken))
                .orElseThrow(() -> new UnauthorizedException("Session expired. Please log in again."));
        refreshTokenRepository.delete(stored);
        if (stored.isExpired()) {
            throw new UnauthorizedException("Session expired. Please log in again.");
        }
        return issueTokens(stored.getUser());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        if (rawRefreshToken != null) {
            refreshTokenRepository.deleteByTokenHash(hash(rawRefreshToken));
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(UUID userId) {
        return userRepository.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> new UnauthorizedException("User no longer exists"));
    }

    private AuthResult issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);

        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        LocalDateTime expiresAt = LocalDateTime.now(ZoneOffset.UTC).plusDays(refreshTokenDays);
        refreshTokenRepository.save(new RefreshToken(user, hash(refreshToken), expiresAt));

        return new AuthResult(UserResponse.from(user), accessToken, refreshToken);
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
