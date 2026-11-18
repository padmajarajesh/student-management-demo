package com.demo.student.security;

import com.demo.student.config.AppProperties;
import com.demo.student.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Creates and verifies short-lived access tokens (JWT, signed with HS256).
 */
@Service
public class JwtService {

    private static final String ISSUER = "student-management";
    private static final String TOKEN_TYPE_CLAIM = "type";
    private static final String ACCESS_TOKEN_TYPE = "access";

    private final SecretKey signingKey;
    private final Duration accessTokenTtl;

    public JwtService(AppProperties properties) {
        this.signingKey = Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = Duration.ofMinutes(properties.jwt().accessTokenMinutes());
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(ISSUER)
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTokenTtl)))
                .signWith(signingKey)
                .compact();
    }

    /** Returns the user if the token is valid, unexpired and an access token; otherwise empty. */
    public Optional<AuthenticatedUser> parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(ISSUER)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            if (!ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))) {
                return Optional.empty();
            }
            return Optional.of(new AuthenticatedUser(
                    UUID.fromString(claims.getSubject()),
                    claims.get("email", String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }
}
