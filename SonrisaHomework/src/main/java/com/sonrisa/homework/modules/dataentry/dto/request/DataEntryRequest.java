package com.sonrisa.homework.modules.dataentry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Manual/admin ingestion until the polling job (mvp final.md §4) exists.
public record DataEntryRequest(
        @NotNull UUID dataSourceId,
        @NotBlank String rawJsonData
) {
}
