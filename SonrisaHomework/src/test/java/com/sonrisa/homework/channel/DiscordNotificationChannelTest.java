package com.sonrisa.homework.channel;

import com.sonrisa.homework.common.enums.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class DiscordNotificationChannelTest {

    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/000000000000000000/fake-token";

    private MockRestServiceServer server;
    private DiscordNotificationChannel channel;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        channel = new DiscordNotificationChannel(builder);
    }

    @Test
    void postsSubjectAndMessageAsContentAndReportsSentOnSuccess() throws Exception {
        AtomicReference<String> capturedBody = new AtomicReference<>();
        server.expect(requestTo(WEBHOOK_URL))
                .andExpect(request -> capturedBody.set(((MockClientHttpRequest) request).getBodyAsString()))
                .andRespond(withSuccess());

        NotificationResult result = channel.send(new NotificationPayload(WEBHOOK_URL, "Alert triggered: NEWS", "Something happened"));

        assertThat(result.status()).isEqualTo(NotificationStatus.SENT);
        JsonNode body = new ObjectMapper().readTree(capturedBody.get());
        assertThat(body.get("content").asText()).isEqualTo("Alert triggered: NEWS\nSomething happened");
    }

    @Test
    void aNonSuccessResponseIsReportedAsFailed() {
        server.expect(requestTo(WEBHOOK_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).body("{\"webhook_id\":[\"Value \\\"fake\\\" is not snowflake.\"]}"));

        NotificationResult result = channel.send(new NotificationPayload(WEBHOOK_URL, "Subject", "Message"));

        assertThat(result.status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(result.error()).contains("snowflake");
    }

    @Test
    void messagesLongerThanDiscordsContentLimitAreTruncatedWithAnEllipsis() throws Exception {
        String longMessage = "x".repeat(3000);
        AtomicReference<String> capturedBody = new AtomicReference<>();
        server.expect(requestTo(WEBHOOK_URL))
                .andExpect(request -> capturedBody.set(((MockClientHttpRequest) request).getBodyAsString()))
                .andRespond(withSuccess());

        channel.send(new NotificationPayload(WEBHOOK_URL, "Subject", longMessage));

        String content = new ObjectMapper().readTree(capturedBody.get()).get("content").asText();
        assertThat(content).hasSize(2000);
        assertThat(content).endsWith("…");
    }
}
