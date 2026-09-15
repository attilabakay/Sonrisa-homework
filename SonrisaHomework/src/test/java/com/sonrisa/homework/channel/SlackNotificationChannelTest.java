package com.sonrisa.homework.channel;

import com.sonrisa.homework.common.enums.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SlackNotificationChannelTest {

    private static final String WEBHOOK_URL = "https://hooks.slack.com/services/DEMO/WEBHOOK";

    private MockRestServiceServer server;
    private SlackNotificationChannel channel;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        channel = new SlackNotificationChannel(builder);
    }

    @Test
    void postsTheSubjectAndMessageAsTextAndReportsSentOnSuccess() {
        server.expect(requestTo(WEBHOOK_URL))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.text").value("Alert triggered: NEWS\nSomething happened"))
                .andRespond(withSuccess());

        NotificationResult result = channel.send(new NotificationPayload(WEBHOOK_URL, "Alert triggered: NEWS", "Something happened"));

        assertThat(result.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(result.error()).isNull();
        server.verify();
    }

    @Test
    void aNonSuccessResponseIsReportedAsFailedWithSlacksOwnErrorText() {
        server.expect(requestTo(WEBHOOK_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).body("invalid_payload"));

        NotificationResult result = channel.send(new NotificationPayload(WEBHOOK_URL, "Subject", "Message"));

        assertThat(result.status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(result.error()).isNotBlank();
    }
}
