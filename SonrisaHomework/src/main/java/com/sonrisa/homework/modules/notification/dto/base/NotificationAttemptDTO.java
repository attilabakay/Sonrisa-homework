package com.sonrisa.homework.modules.notification.dto.base;

import com.sonrisa.homework.common.enums.NotificationStatus;
import com.sonrisa.homework.modules.notification.model.NotificationAttempt;

import java.time.Instant;
import java.util.UUID;

// Carries alert.user context (workflow.md §5: NotificationAttempt reads are "joined through
// Alert -> User for context") so the admin view can show whose alert fired without a
// separate lookup.
public record NotificationAttemptDTO(
        UUID id,
        UUID alertId,
        UUID dataEntryId,
        UUID userId,
        String userEmail,
        NotificationStatus status,
        String error,
        Instant sentAt
) {
    public static NotificationAttemptDTO fromEntity(NotificationAttempt entity) {
        return new NotificationAttemptDTO(
                entity.getId(),
                entity.getAlert().getId(),
                entity.getDataEntry().getId(),
                entity.getAlert().getUser().getId(),
                entity.getAlert().getUser().getEmail(),
                entity.getStatus(),
                entity.getError(),
                entity.getSentAt()
        );
    }
}
