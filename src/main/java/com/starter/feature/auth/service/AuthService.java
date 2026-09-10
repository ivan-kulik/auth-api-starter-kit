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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtManager jwtManager;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

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

        RefreshToken storedToken = findValidRefreshToken(refreshToken);
        validateTokenOwnership(storedToken, userId);

        User user = findUser(userId);

        revokeRefreshToken(storedToken);

        CustomUserDetails principal = new CustomUserDetails(user);
        return generateTokenPair(principal);
    }

    @Transactional
    public void logout(Long userId) {
        this.refreshTokenRepository.deleteByUserId(userId);
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
        List<String> userRoles = extractUserRoles(principal);
        return this.jwtManager.generateAccessToken(
                principal.getId(), principal.getEmail(), userRoles
        );
    }

    private String generateRefreshToken(CustomUserDetails principal) {
        return this.jwtManager.generateRefreshToken(
                principal.getId(), principal.getEmail()
        );
    }

    private void saveRefreshTokenToDatabase(CustomUserDetails principal, String refreshToken) {
        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .tokenHash(HashUtil.sha256(refreshToken))
                .userId(principal.getId())
                .expiryDate(Instant.now()
                        .plus(this.jwtProperties.refreshTokenTtl())
                )
                .revoked(false)
                .build();
        this.refreshTokenRepository.save(refreshTokenEntity);
    }

    private List<String> extractUserRoles(CustomUserDetails principal) {
        return principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    private Claims parseRefreshToken(String refreshToken) {
        try {
            return this.jwtManager.parseRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new BadCredentialsException("Invalid refresh token");
        }
    }

    private RefreshToken findValidRefreshToken(String refreshToken) {
        String tokenHash = HashUtil.sha256(refreshToken);

        RefreshToken storedToken = this.refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid refresh token")
                );

        if (storedToken.isRevoked() ||
                !storedToken.getExpiryDate().isAfter(Instant.now())
        ) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        return storedToken;
    }

    private void validateTokenOwnership(RefreshToken refreshToken, Long userId) {
        if (!refreshToken.getUserId().equals(userId)) {
            throw new BadCredentialsException("Invalid refresh token");
        }
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
