package com.sonrisa.homework.channel;

import com.sonrisa.homework.common.enums.SenderType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NotificationChannelResolver {

    private final Map<SenderType, NotificationChannel> channelsByType;

    public NotificationChannelResolver(List<NotificationChannel> channels) {
        this.channelsByType = channels.stream()
                .collect(Collectors.toMap(NotificationChannel::type, Function.identity()));
    }

    public NotificationChannel resolve(SenderType type) {
        NotificationChannel channel = channelsByType.get(type);
        if (channel == null) {
            throw new IllegalStateException("No channel adapter registered for sender type: " + type);
        }
        return channel;
    }
}
