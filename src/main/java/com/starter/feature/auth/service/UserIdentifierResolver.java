package com.starter.feature.auth.service;

import com.starter.feature.auth.model.UserIdentifier;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class UserIdentifierResolver {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"
            );

    public UserIdentifier resolve(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("Login is required");
        }

        String value = rawValue.trim();

        if (EMAIL_PATTERN.matcher(value).matches()) {
            return UserIdentifier.email(value);
        }

        return UserIdentifier.username(value);
    }
}
