package com.starter.feature.auth.event;

import com.starter.common.email.EmailSender;
import com.starter.feature.auth.config.EmailVerificationProperties;
import com.starter.feature.auth.service.VerificationTokenService;
import com.starter.feature.user.entity.User;
import com.starter.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegisteredEventListener {

    private final VerificationTokenService tokenService;
    private final EmailSender emailSender;
    private final UserRepository userRepository;
    private final EmailVerificationProperties properties;
    private final SpringTemplateEngine templateEngine;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        this.userRepository.findById(event.userId())
                .ifPresentOrElse(
                        this::sendVerificationEmail,
                        () -> log.warn(
                                "User {} not found after commit, verification email skipped",
                                event.userId()
                        ));
    }

    @Async
    public void sendVerificationEmail(User user) {
        String tokenValue = this.tokenService.issueToken(user);
        String link = buildVerificationLink(tokenValue);

        this.emailSender.send(user.getEmail(), this.properties.subject(), buildBody(link));
        log.info("Verification email sent to {}", user.getEmail());
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
