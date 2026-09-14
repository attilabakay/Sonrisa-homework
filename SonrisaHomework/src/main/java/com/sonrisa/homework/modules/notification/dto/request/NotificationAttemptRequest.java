package com.sonrisa.homework.modules.notification.dto.request;

import com.sonrisa.homework.common.enums.NotificationStatus;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Recorded by the channel adapter after send() (mvp final.md §3) — no matching engine yet,
// so this is also reachable directly for now.
public record NotificationAttemptRequest(
        @NotNull UUID alertId,
        @NotNull UUID dataEntryId,
        @NotNull NotificationStatus status,
        String error
) {
}
