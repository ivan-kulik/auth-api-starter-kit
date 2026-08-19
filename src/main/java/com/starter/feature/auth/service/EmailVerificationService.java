package com.starter.feature.auth.service;

import com.starter.feature.auth.email.VerificationEmailNotifier;
import com.starter.feature.user.entity.User;
import com.starter.feature.user.repository.UserRepository;
import com.starter.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final VerificationTokenService tokenService;
    private final UserRepository userRepository;
    private final VerificationEmailNotifier emailNotifier;

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

    public void resendVerificationEmail(String email) {
        this.userRepository.findByEmail(email)
                .filter(user -> !user.isEmailVerified())
                .ifPresent(this.emailNotifier::sendVerificationEmail);
    }
}
