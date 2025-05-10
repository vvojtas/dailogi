package com.github.vvojtas.dailogi_server.model.auth.mapper;

import com.github.vvojtas.dailogi_server.db.entity.AppUser;
import com.github.vvojtas.dailogi_server.model.auth.response.UserDto;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class UserMapper {
    
    public UserDto toDto(AppUser user) {
        return new UserDto(
            user.getId(),
            user.getName(),
            user.getCreatedAt(),
            StringUtils.hasText(user.getEncryptedApiKey())
        );
    }
} 