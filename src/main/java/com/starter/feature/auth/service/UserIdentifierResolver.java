package com.starter.feature.auth.service;

import com.starter.feature.auth.model.UserIdentifier;
import org.springframework.stereotype.Component;

@Component
public class UserIdentifierResolver {

    public UserIdentifier resolve(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Login is required");
        }

        String value = rawValue.trim();

        if (isEmail(value)) {
            return UserIdentifier.email(value);
        }

        return UserIdentifier.username(value);
    }

    private boolean isEmail(String value) {
        return value.contains("@");
    }
}
