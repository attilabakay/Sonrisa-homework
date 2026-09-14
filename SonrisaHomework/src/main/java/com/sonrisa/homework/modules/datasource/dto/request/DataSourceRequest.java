package com.sonrisa.homework.modules.datasource.dto.request;

import com.sonrisa.homework.common.enums.DataType;
import com.sonrisa.homework.common.enums.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DataSourceRequest(
        @NotNull DataType type,
        @NotBlank String link,
        String apiKey,
        String fieldMapping,
        @NotNull ResourceType resourceType
) {
}
