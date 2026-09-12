package com.starter.feature.user.service;

import com.starter.common.exception.BadRequestException;
import com.starter.feature.user.dto.CurrentUserResponse;
import com.starter.feature.user.entity.User;
import com.starter.feature.user.mapper.UserMapper;
import com.starter.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public CurrentUserResponse getCurrentUser(Long userId) {
        User user = this.userRepository.findById(userId)
                .orElseThrow(() ->
                        new BadRequestException("User not found")
                );

        return this.userMapper.toResponse(user);
    }
}
