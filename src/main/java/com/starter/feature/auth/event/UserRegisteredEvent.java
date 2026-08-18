package com.starter.feature.auth.event;

public record UserRegisteredEvent(Long userId, String email) {
}
