package com.starter.feature.auth.controller;

import com.starter.common.security.CustomUserDetails;
import com.starter.feature.auth.dto.LoginRequest;
import com.starter.feature.auth.dto.RefreshTokenRequest;
import com.starter.feature.auth.dto.RegisterRequest;
import com.starter.feature.auth.dto.ResendEmailRequest;
import com.starter.feature.auth.dto.TokenResponse;
import com.starter.feature.auth.service.AuthService;
import com.starter.feature.auth.service.EmailVerificationService;
import com.starter.feature.auth.service.UserRegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    private final AuthService authService;

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

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(this.authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(this.authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            Authentication authentication
    ) {
        CustomUserDetails principal =
                (CustomUserDetails) authentication.getPrincipal();

        this.authService.logout(principal.getId());
        return ResponseEntity.noContent().build();
    }
}
