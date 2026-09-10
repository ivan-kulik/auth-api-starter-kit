package com.starter.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtManager {

    private final JwtProperties jwtProperties;

    public String generateAccessToken(Long userId, String email, List<String> roles) {
        Instant now = Instant.now();
        Instant expiration = now.plus(this.jwtProperties.accessTokenTtl());
        return Jwts.builder()
                .subject(email)
                .claim("uid", userId.toString())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .id(UUID.randomUUID().toString())
                .signWith(getAccessKey())
                .compact();
    }

    public String generateRefreshToken(Long userId, String email) {
        Instant now = Instant.now();
        Instant expiration = now.plus(this.jwtProperties.refreshTokenTtl());
        return Jwts.builder()
                .subject(email)
                .claim("uid", userId.toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .id(UUID.randomUUID().toString())
                .signWith(getRefreshKey())
                .compact();
    }

    public Claims parseAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(getAccessKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims parseRefreshToken(String token) {
        return Jwts.parser()
                .verifyWith(getRefreshKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(Claims claims) {
        String userId = claims.get("uid", String.class);

        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException exception) {
            throw new BadCredentialsException("Invalid refresh token");
        }
    }

    public String extractEmail(Claims claims) {
        return claims.getSubject();
    }

    private SecretKey getAccessKey() {
        return Keys.hmacShaKeyFor(
                this.jwtProperties.accessTokenSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }

    private SecretKey getRefreshKey() {
        return Keys.hmacShaKeyFor(
                this.jwtProperties.refreshTokenSecret()
                        .getBytes(StandardCharsets.UTF_8)
        );
    }
}
