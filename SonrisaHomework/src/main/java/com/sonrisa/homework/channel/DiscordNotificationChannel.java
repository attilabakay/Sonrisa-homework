package com.sonrisa.homework.channel;

import com.sonrisa.homework.common.enums.SenderType;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

// Sender.config for a DISCORD sender is a channel webhook URL (Server Settings >
// Integrations > Webhooks). This is the third channel adapter — nothing outside this class
// and the SenderType enum entry changed to add it, proving mvp final.md §3's "adding a third
// channel later = write one new adapter, no changes to alert config, matching engine, or
// data model." NotificationChannelResolver already picks it up automatically via Spring's
// List<NotificationChannel> injection.
@Component
public class DiscordNotificationChannel implements NotificationChannel {

    // Discord's hard limit on a webhook message's "content" field.
    private static final int MAX_CONTENT_LENGTH = 2000;

    private final RestClient restClient = RestClient.create();

    @Override
    public SenderType type() {
        return SenderType.DISCORD;
    }

    @Override
    public NotificationResult send(NotificationPayload payload) {
        try {
            String content = truncate(payload.subject() + "\n" + payload.message());
            restClient.post()
                    .uri(payload.destinationConfig())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("content", content))
                    .retrieve()
                    .toBodilessEntity();
            return NotificationResult.success();
        } catch (RestClientException e) {
            return NotificationResult.failure(e.getMessage());
        }
    }

    private String truncate(String content) {
        if (content.length() <= MAX_CONTENT_LENGTH) {
            return content;
        }
        return content.substring(0, MAX_CONTENT_LENGTH - 1) + "…";
    }
}
