package com.starter.feature.auth.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresInSeconds
) {
}
