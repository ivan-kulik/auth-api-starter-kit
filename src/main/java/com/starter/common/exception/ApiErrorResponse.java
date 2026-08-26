package com.starter.common.exception;

import org.springframework.http.HttpStatus;

import java.time.Instant;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message
) {
    public static ApiErrorResponse of(HttpStatus status, String message) {
        return new ApiErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message
        );
    }
}
