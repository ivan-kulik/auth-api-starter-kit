package com.starter.feature.auth.event;

public record UserRegisteredEvent(
        String email,
        Long userId,
        String verificationToken
) {
}
