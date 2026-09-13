package com.starter.feature.auth.event;

import com.starter.feature.auth.service.EmailVerificationService;
import com.starter.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegisteredEventListener {

    private final UserRepository userRepository;
    private final EmailVerificationService emailVerificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserRegistered(UserRegisteredEvent event) {
        this.userRepository.findById(event.userId())
                .ifPresentOrElse(
                        this.emailVerificationService::sendVerificationEmail,
                        () -> log.warn(
                                "User {} not found after commit, verification email skipped",
                                event.userId()
                        )
                );
    }
}
