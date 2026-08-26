package com.starter.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(basePackages = "com.starter.feature.auth.controller")
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessRuleViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessRuleViolation(
            BusinessRuleViolationException e
    ) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ApiErrorResponse.of(
                        HttpStatus.CONFLICT, e.getMessage())
                );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            BadRequestException e
    ) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST, e.getMessage())
                );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException e
    ) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiErrorResponse.of(
                        HttpStatus.BAD_REQUEST, message)
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception e
    ) {
        log.error("Unexpected error in registration flow", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiErrorResponse.of(
                        HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")
                );
    }
}
