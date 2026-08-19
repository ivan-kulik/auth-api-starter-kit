package com.starter.feature.auth.controller;

import com.starter.feature.auth.dto.RegisterRequest;
import com.starter.feature.auth.service.UserRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserRegistrationService registrationService;

    @PostMapping
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        this.registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
