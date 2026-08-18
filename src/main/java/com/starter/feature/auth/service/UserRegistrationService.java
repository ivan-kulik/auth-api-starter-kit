package com.starter.feature.auth.service;

import com.starter.feature.auth.dto.RegisterRequest;
import com.starter.feature.auth.event.UserRegisteredEvent;
import com.starter.feature.user.RoleName;
import com.starter.feature.user.entity.Role;
import com.starter.feature.user.entity.User;
import com.starter.feature.user.repository.RoleRepository;
import com.starter.feature.user.repository.UserRepository;
import com.starter.shared.exception.BusinessRuleViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void register(RegisterRequest request) {
        ensureUsernameIsFree(request.username());
        ensureEmailIsFree(request.email());

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(this.passwordEncoder.encode(request.password()))
                .roles(Set.of(defaultUserRole()))
                .build();

        User savedUser = saveUser(user);

        this.eventPublisher.publishEvent(
                new UserRegisteredEvent(savedUser.getId(), savedUser.getEmail())
        );
    }

    private void ensureUsernameIsFree(String username) {
        if (this.userRepository.existsByUsername(username)) {
            throw new BusinessRuleViolationException("Username already taken: " + username);
        }
    }

    private void ensureEmailIsFree(String email) {
        if (this.userRepository.existsByEmail(email)) {
            throw new BusinessRuleViolationException("Email already registered: " + email);
        }
    }

    private Role defaultUserRole() {
        return this.roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException(
                        "Default role ROLE_USER not found. Run database migrations."));
    }

    private User saveUser(User user) {
        try {
            return this.userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessRuleViolationException("Username or email is already taken.");
        }
    }
}
