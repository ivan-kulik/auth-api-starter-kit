package com.starter.feature.auth.service;

import com.starter.common.security.CustomUserDetails;
import com.starter.common.security.JwtManager;
import com.starter.common.security.JwtProperties;
import com.starter.common.util.HashUtil;
import com.starter.feature.auth.dto.LoginRequest;
import com.starter.feature.auth.dto.RefreshTokenRequest;
import com.starter.feature.auth.dto.TokenResponse;
import com.starter.feature.auth.entity.RefreshToken;
import com.starter.feature.auth.repository.RefreshTokenRepository;
import com.starter.feature.user.entity.User;
import com.starter.feature.user.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtManager jwtManager;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = this.authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.login(),
                        request.password()
                )
        );

        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        if (!principal.isEmailVerified()) {
            throw new BadCredentialsException("Invalid credentials");
        }

        return generateTokenPair(principal);
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.refreshToken();

        Claims claims = parseRefreshToken(refreshToken);

        Long userId = this.jwtManager.extractUserId(claims);

        RefreshToken storedToken = findValidRefreshToken(
                refreshToken, userId);

        User user = findUser(userId);

        revokeRefreshToken(storedToken);

        CustomUserDetails principal = new CustomUserDetails(user);
        return generateTokenPair(principal);
    }

    @Transactional
    public void logout(Long userId) {
        this.refreshTokenRepository.deleteByUserId(userId);
    }

    @Scheduled(cron = "${app.refresh-token.cleanup-cron:0 0 4 * * ?}")
    @Transactional
    public void cleanupRefreshTokens() {
        this.refreshTokenRepository.deleteExpiredOrRevoked(
                Instant.now(this.clock)
        );
    }

    private TokenResponse generateTokenPair(CustomUserDetails principal) {
        String accessToken = generateAccessToken(principal);
        String refreshToken = generateRefreshToken(principal);

        saveRefreshTokenToDatabase(principal, refreshToken);

        return new TokenResponse(
                accessToken,
                refreshToken,
                TokenResponse.TOKEN_TYPE,
                this.jwtProperties.accessTokenTtl().getSeconds()
        );
    }

    private String generateAccessToken(CustomUserDetails principal) {
        return this.jwtManager.generateAccessToken(
                principal.getId(),
                principal.getEmail()
        );
    }

    private String generateRefreshToken(CustomUserDetails principal) {
        return this.jwtManager.generateRefreshToken(
                principal.getId(), principal.getEmail()
        );
    }

    private void saveRefreshTokenToDatabase(CustomUserDetails principal, String refreshToken) {
        Instant now = Instant.now(this.clock);
        Instant expiryDate = now.plus(this.jwtProperties.refreshTokenTtl());

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .tokenHash(HashUtil.sha256(refreshToken))
                .userId(principal.getId())
                .createdAt(now)
                .expiryDate(expiryDate)
                .revoked(false)
                .build();
        this.refreshTokenRepository.save(refreshTokenEntity);
    }

    private Claims parseRefreshToken(String refreshToken) {
        try {
            return this.jwtManager.parseRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BadCredentialsException("Invalid refresh token");
        }
    }

    private RefreshToken findValidRefreshToken(String refreshToken, Long userId) {
        String tokenHash = HashUtil.sha256(refreshToken);

        RefreshToken storedToken = this.refreshTokenRepository
                .findByTokenHashAndUserId(tokenHash, userId)
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid refresh token")
                );

        if (storedToken.isRevoked() ||
                !storedToken.getExpiryDate().isAfter(Instant.now(this.clock))
        ) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        return storedToken;
    }

    private User findUser(Long userId) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid refresh token")
                );

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        return user;
    }

    private void revokeRefreshToken(RefreshToken refreshToken) {
        refreshToken.setRevoked(true);
    }
}
