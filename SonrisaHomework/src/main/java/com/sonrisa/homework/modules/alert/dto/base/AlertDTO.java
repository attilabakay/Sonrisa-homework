package com.sonrisa.homework.modules.alert.dto.base;

import com.sonrisa.homework.common.enums.DataType;
import com.sonrisa.homework.modules.alert.model.Alert;

import java.util.UUID;

public record AlertDTO(
        UUID id,
        DataType type,
        String criteria,
        UUID userId,
        UUID senderId,
        boolean active
) {
    public static AlertDTO fromEntity(Alert entity) {
        return new AlertDTO(
                entity.getId(),
                entity.getType(),
                entity.getCriteria(),
                entity.getUser().getId(),
                entity.getSender().getId(),
                entity.isActive()
        );
    }
}
