package com.starter.feature.auth.service;

import com.starter.feature.auth.model.UserIdentifier;
import com.starter.feature.user.entity.User;
import com.starter.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserLookupService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User find(UserIdentifier identifier) {
        return switch (identifier.type()) {
            case USERNAME -> this.userRepository.findByUsername(identifier.value())
                    .orElseThrow(this::userNotFound);
            case EMAIL -> this.userRepository.findByEmail(identifier.value())
                    .orElseThrow(this::userNotFound);
        };
    }

    private UsernameNotFoundException userNotFound() {
        return new UsernameNotFoundException("Invalid credentials");
    }
}
