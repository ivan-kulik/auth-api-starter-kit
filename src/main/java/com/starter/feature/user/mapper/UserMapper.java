package com.starter.feature.user.mapper;

import com.starter.feature.user.dto.CurrentUserResponse;
import com.starter.feature.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public CurrentUserResponse toResponse(User user) {
        return new CurrentUserResponse(
                user.getUsername(),
                user.getEmail(),
                user.isEmailVerified()
        );
    }
}
