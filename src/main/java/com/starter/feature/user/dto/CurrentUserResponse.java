package com.starter.feature.user.dto;

public record CurrentUserResponse(
        String username,
        String email,
        boolean emailVerified
) {
}
