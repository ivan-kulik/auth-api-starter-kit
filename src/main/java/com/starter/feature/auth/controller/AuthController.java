package com.starter.feature.auth.controller;

import com.starter.feature.auth.dto.RegisterRequest;
import com.starter.feature.auth.dto.ResendEmailRequest;
import com.starter.feature.auth.service.EmailVerificationService;
import com.starter.feature.auth.service.UserRegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRegistrationService registrationService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        this.registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/verify")
    public ResponseEntity<Void> confirmEmail(
            @RequestParam("token") @NotBlank String tokenValue
    ) {
        this.emailVerificationService.confirmEmail(tokenValue);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(
            @Valid @RequestBody ResendEmailRequest request
    ) {
        this.emailVerificationService.resendVerificationEmail(request);
        return ResponseEntity.accepted().build();
    }
}
