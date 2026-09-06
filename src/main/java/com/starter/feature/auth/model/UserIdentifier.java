package com.starter.feature.auth.model;

public record UserIdentifier(
        Type type,
        String value
) {
    public UserIdentifier {
        if (type == null) {
            throw new IllegalArgumentException("Identifier type is required");
        }
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Identifier value is required");
        }
    }

    public enum Type {
        USERNAME,
        EMAIL
    }

    public static UserIdentifier username(String value) {
        return new UserIdentifier(Type.USERNAME, value);
    }

    public static UserIdentifier email(String value) {
        return new UserIdentifier(Type.EMAIL, value);
    }
}
