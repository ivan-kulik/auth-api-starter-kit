package com.starter.feature.auth.controller;

import com.starter.feature.auth.service.EmailVerificationService;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/auth/verification")
@RequiredArgsConstructor
public class VerificationController {

    private final EmailVerificationService emailVerificationService;

    @GetMapping("/verify-email")
    public ResponseEntity<Void> confirmEmail(
            @RequestParam("token") @NotBlank String tokenValue
    ) {
        this.emailVerificationService.confirmEmail(tokenValue);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Void> resendVerification(
            @RequestParam("email") @Email String email
    ) {
        this.emailVerificationService.resendVerificationEmail(email);
        return ResponseEntity.accepted().build();
    }
}
