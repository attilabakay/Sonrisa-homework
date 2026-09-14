package com.sonrisa.homework.modules.alert.dto.request;

import com.sonrisa.homework.common.enums.DataType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AlertRequest(
        @NotNull DataType type,
        @NotBlank String criteria,
        @NotNull UUID userId,
        @NotNull UUID senderId
) {
}
