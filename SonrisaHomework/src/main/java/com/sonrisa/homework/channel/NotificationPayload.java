package com.sonrisa.homework.channel;

// destinationConfig is Sender.config as-is (address, webhook URL, token, etc.) — its shape
// depends on which adapter reads it.
public record NotificationPayload(
        String destinationConfig,
        String subject,
        String message
) {
}
