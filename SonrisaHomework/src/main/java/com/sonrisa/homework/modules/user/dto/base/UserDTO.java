package com.sonrisa.homework.modules.user.dto.base;

import com.sonrisa.homework.modules.user.model.User;

import java.util.UUID;

public record UserDTO(
        UUID id,
        String email,
        boolean admin,
        boolean active
) {
    public static UserDTO fromEntity(User entity) {
        return new UserDTO(entity.getId(), entity.getEmail(), entity.isAdmin(), entity.isActive());
    }
}
