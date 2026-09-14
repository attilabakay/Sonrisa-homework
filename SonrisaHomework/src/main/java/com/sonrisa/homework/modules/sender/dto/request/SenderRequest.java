package com.sonrisa.homework.modules.sender.dto.request;

import com.sonrisa.homework.common.enums.SenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SenderRequest(
        @NotNull SenderType type,
        @NotNull UUID userId,
        @NotBlank String config
) {
}
