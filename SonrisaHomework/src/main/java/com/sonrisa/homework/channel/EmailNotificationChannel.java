package com.sonrisa.homework.channel;

import com.sonrisa.homework.common.enums.SenderType;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailNotificationChannel implements NotificationChannel {

    private final JavaMailSender mailSender;

    @Override
    public SenderType type() {
        return SenderType.EMAIL;
    }

    @Override
    public NotificationResult send(NotificationPayload payload) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(payload.destinationConfig());
            message.setSubject(payload.subject());
            message.setText(payload.message());
            mailSender.send(message);
            return NotificationResult.success();
        } catch (MailException e) {
            return NotificationResult.failure(e.getMessage());
        }
    }
}
