package com.sonrisa.homework.modules.datasource.dto.base;

import com.sonrisa.homework.common.enums.DataType;
import com.sonrisa.homework.common.enums.ResourceType;
import com.sonrisa.homework.modules.datasource.model.DataSource;

import java.util.UUID;

public record DataSourceDTO(
        UUID id,
        DataType type,
        String link,
        String apiKey,
        String fieldMapping,
        ResourceType resourceType,
        boolean active
) {
    public static DataSourceDTO fromEntity(DataSource entity) {
        return new DataSourceDTO(
                entity.getId(),
                entity.getType(),
                entity.getLink(),
                entity.getApiKey(),
                entity.getFieldMapping(),
                entity.getResourceType(),
                entity.isActive()
        );
    }
}
