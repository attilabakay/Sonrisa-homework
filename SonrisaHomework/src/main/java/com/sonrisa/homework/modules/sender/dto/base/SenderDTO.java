package com.sonrisa.homework.modules.sender.dto.base;

import com.sonrisa.homework.common.enums.SenderType;
import com.sonrisa.homework.modules.sender.model.Sender;

import java.util.UUID;

public record SenderDTO(
        UUID id,
        SenderType type,
        UUID userId,
        String config
) {
    public static SenderDTO fromEntity(Sender entity) {
        return new SenderDTO(entity.getId(), entity.getType(), entity.getUser().getId(), entity.getConfig());
    }
}
