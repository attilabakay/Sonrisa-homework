package com.sonrisa.homework.modules.dataentry.dto.base;

import com.sonrisa.homework.common.enums.DataType;
import com.sonrisa.homework.modules.dataentry.model.DataEntry;

import java.time.Instant;
import java.util.UUID;

public record DataEntryDTO(
        UUID id,
        DataType type,
        UUID dataSourceId,
        String rawJsonData,
        Instant receivedAt
) {
    public static DataEntryDTO fromEntity(DataEntry entity) {
        return new DataEntryDTO(
                entity.getId(),
                entity.getType(),
                entity.getDataSource().getId(),
                entity.getRawJsonData(),
                entity.getReceivedAt()
        );
    }
}
