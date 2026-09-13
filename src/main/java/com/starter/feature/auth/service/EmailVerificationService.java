package com.starter.feature.auth.service;

import com.starter.common.email.EmailSender;
import com.starter.feature.auth.config.EmailVerificationProperties;
import com.starter.feature.auth.dto.ResendEmailRequest;
import com.starter.feature.user.entity.User;
import com.starter.common.exception.BadRequestException;
import com.starter.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final VerificationTokenService tokenService;
    private final UserRepository userRepository;
    private final EmailSender emailSender;
    private final EmailVerificationProperties properties;
    private final SpringTemplateEngine templateEngine;

    @Async
    public void sendVerificationEmail(User user) {
        String tokenValue = this.tokenService.issueToken(user);
        String link = buildVerificationLink(tokenValue);
        String body = buildBody(link);

        this.emailSender.send(
                user.getEmail(),
                this.properties.subject(),
                body);
    }

    @Transactional
    public void confirmEmail(String tokenValue) {
        User user = this.tokenService.consumeToken(tokenValue);

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified.");
        }

        user.setEmailVerified(true);
        user.setEnabled(true);
        this.userRepository.save(user);
    }

    public void resendVerificationEmail(ResendEmailRequest request) {
        this.userRepository.findByEmail(request.email())
                .filter(user -> !user.isEmailVerified())
                .ifPresent(this::sendVerificationEmail);
    }

    private String buildVerificationLink(String tokenValue) {
        return UriComponentsBuilder
                .fromUriString(this.properties.baseUrl())
                .path("/verify-email")
                .queryParam("token", tokenValue)
                .build()
                .encode()
                .toUriString();
    }

    private String buildBody(String link) {
        Context context = new Context();
        context.setVariable("link", link);

        return this.templateEngine.process("email/verification-message", context);
    }
}
