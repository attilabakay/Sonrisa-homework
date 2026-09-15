package com.sonrisa.homework.channel;

import com.sonrisa.homework.common.enums.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailNotificationChannelTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailNotificationChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmailNotificationChannel(mailSender);
    }

    @Test
    void successfulSendMapsPayloadFieldsAndReportsSent() {
        NotificationResult result = channel.send(new NotificationPayload("user@example.com", "Subject", "Body"));

        assertThat(result.status()).isEqualTo(NotificationStatus.SENT);
        assertThat(result.error()).isNull();

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertThat(captor.getValue().getTo()).containsExactly("user@example.com");
        assertThat(captor.getValue().getSubject()).isEqualTo("Subject");
        assertThat(captor.getValue().getText()).isEqualTo("Body");
    }

    @Test
    void mailExceptionIsCaughtAndReportedAsFailedInsteadOfPropagating() {
        doThrow(new MailSendException("SMTP connection refused")).when(mailSender).send(any(SimpleMailMessage.class));

        NotificationResult result = channel.send(new NotificationPayload("user@example.com", "Subject", "Body"));

        assertThat(result.status()).isEqualTo(NotificationStatus.FAILED);
        assertThat(result.error()).contains("SMTP connection refused");
    }
}
