package com.sonrisa.homework.modules.notification.dto.base;

import com.sonrisa.homework.common.enums.NotificationStatus;
import com.sonrisa.homework.modules.notification.model.NotificationAttempt;

import java.time.Instant;
import java.util.UUID;

public record NotificationAttemptDTO(
        UUID id,
        UUID alertId,
        UUID dataEntryId,
        NotificationStatus status,
        String error,
        Instant sentAt
) {
    public static NotificationAttemptDTO fromEntity(NotificationAttempt entity) {
        return new NotificationAttemptDTO(
                entity.getId(),
                entity.getAlert().getId(),
                entity.getDataEntry().getId(),
                entity.getStatus(),
                entity.getError(),
                entity.getSentAt()
        );
    }
}
