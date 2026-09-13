package com.starter.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtManager {

    private static final String CLAIM_USER_ID = "uid";
    private static final String CLAIM_TOKEN_TYPE = "token_type";

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtProperties jwtProperties;
    private final Clock clock;

    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    public JwtManager(JwtProperties jwtProperties, Clock clock) {
        this.jwtProperties = jwtProperties;
        this.clock = clock;

        this.accessKey = Keys.hmacShaKeyFor(
                jwtProperties.accessTokenSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );

        this.refreshKey = Keys.hmacShaKeyFor(
                jwtProperties.refreshTokenSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    public String generateAccessToken(Long userId, String email) {
        Instant now = Instant.now(this.clock);
        Instant expiration = now.plus(this.jwtProperties.accessTokenTtl());

        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .id(UUID.randomUUID().toString())
                .signWith(this.accessKey)
                .compact();
    }

    public String generateRefreshToken(Long userId, String email) {
        Instant now = Instant.now(this.clock);
        Instant expiration = now.plus(this.jwtProperties.refreshTokenTtl());

        return Jwts.builder()
                .subject(email)
                .claim(CLAIM_USER_ID, userId)
                .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .id(UUID.randomUUID().toString())
                .signWith(this.refreshKey)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(this.accessKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        validateTokenType(claims, TOKEN_TYPE_ACCESS);

        return claims;
    }

    public Claims parseRefreshToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(this.refreshKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        validateTokenType(claims, TOKEN_TYPE_REFRESH);

        return claims;
    }

    public Long extractUserId(Claims claims) {
        Object value = claims.get(CLAIM_USER_ID);

        if (value == null) {
            throw new BadCredentialsException("Invalid token");
        }

        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException exception) {
            throw new BadCredentialsException("Invalid token");
        }
    }

    public String extractEmail(Claims claims) {
        String subject = claims.getSubject();

        if (subject == null || subject.isBlank()) {
            throw new BadCredentialsException("Invalid token");
        }

        return subject;
    }

    private void validateTokenType(Claims claims, String expectedType) {
        String actualType = claims.get(CLAIM_TOKEN_TYPE, String.class);

        if (!expectedType.equals(actualType)) {
            throw new BadCredentialsException("Invalid token");
        }
    }
}
