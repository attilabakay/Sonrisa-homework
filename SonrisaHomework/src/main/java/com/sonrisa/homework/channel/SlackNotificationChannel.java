package com.sonrisa.homework.channel;

import com.sonrisa.homework.common.enums.SenderType;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

// Sender.config for a SLACK sender is the incoming-webhook URL.
@Component
public class SlackNotificationChannel implements NotificationChannel {

    private final RestClient restClient;

    // Built from the injected (Spring Boot auto-configured) builder rather than
    // RestClient.create() directly, so tests can bind a mock server to it.
    public SlackNotificationChannel(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public SenderType type() {
        return SenderType.SLACK;
    }

    @Override
    public NotificationResult send(NotificationPayload payload) {
        try {
            restClient.post()
                    .uri(payload.destinationConfig())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", payload.subject() + "\n" + payload.message()))
                    .retrieve()
                    .toBodilessEntity();
            return NotificationResult.success();
        } catch (RestClientException e) {
            return NotificationResult.failure(e.getMessage());
        }
    }
}
