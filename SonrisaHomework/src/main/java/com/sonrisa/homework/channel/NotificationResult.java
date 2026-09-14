package com.sonrisa.homework.channel;

import com.sonrisa.homework.common.enums.NotificationStatus;

// Call-success only (mvp final.md §3) — not delivery/read confirmation.
public record NotificationResult(NotificationStatus status, String error) {

    public static NotificationResult success() {
        return new NotificationResult(NotificationStatus.SENT, null);
    }

    public static NotificationResult failure(String error) {
        return new NotificationResult(NotificationStatus.FAILED, error);
    }
}
