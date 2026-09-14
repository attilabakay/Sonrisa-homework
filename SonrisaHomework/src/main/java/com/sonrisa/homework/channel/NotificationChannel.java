package com.sonrisa.homework.channel;

import com.sonrisa.homework.common.enums.SenderType;

// Adding a third channel later = write one new adapter, no changes to alert config,
// matching engine, or data model (mvp final.md §3).
public interface NotificationChannel {

    SenderType type();

    NotificationResult send(NotificationPayload payload);
}
