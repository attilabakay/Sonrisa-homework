package com.sonrisa.homework.matching;

import com.sonrisa.homework.channel.NotificationChannel;
import com.sonrisa.homework.channel.NotificationChannelResolver;
import com.sonrisa.homework.channel.NotificationPayload;
import com.sonrisa.homework.channel.NotificationResult;
import com.sonrisa.homework.matching.sourcedata.EmergencyData;
import com.sonrisa.homework.matching.sourcedata.MarketData;
import com.sonrisa.homework.matching.sourcedata.NewsData;
import com.sonrisa.homework.matching.sourcedata.SourceDataMapper;
import com.sonrisa.homework.modules.alert.model.Alert;
import com.sonrisa.homework.modules.alert.repository.AlertRepository;
import com.sonrisa.homework.modules.dataentry.model.DataEntry;
import com.sonrisa.homework.modules.notification.model.NotificationAttempt;
import com.sonrisa.homework.modules.notification.repository.NotificationAttemptRepository;
import com.sonrisa.homework.modules.sender.model.Sender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

// The end-to-end ingest -> match -> notify loop (mvp final.md, "What This MVP Proves" §3).
// Runs synchronously right after a DataEntry is persisted (workflow.md §3: "creation triggers
// Matching") — no queue/async needed at this scale.
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchingEngine {

    private final AlertRepository alertRepository;
    private final NotificationAttemptRepository notificationAttemptRepository;
    private final NotificationChannelResolver channelResolver;
    private final SourceDataMapper sourceDataMapper;
    private final ObjectMapper objectMapper;

    @Transactional
    public void evaluate(DataEntry entry) {
        Map<String, Object> normalized = sourceDataMapper.normalize(
                entry.getRawJsonData(), entry.getDataSource().getFieldMapping());

        for (Alert alert : alertRepository.findMatchableAlerts(entry.getType())) {
            boolean matched = switch (entry.getType()) {
                case NEWS -> matchesKeyword(alert.getCriteria(), sourceDataMapper.toNews(normalized));
                case MARKET -> matchesMarket(alert.getCriteria(), sourceDataMapper.toMarket(normalized));
                case DISASTER, WEATHER -> matchesRegion(alert.getCriteria(), sourceDataMapper.toEmergency(normalized));
            };
            if (matched) {
                dispatch(alert, entry);
            }
        }
    }

    private void dispatch(Alert alert, DataEntry entry) {
        Sender sender = alert.getSender();
        NotificationChannel channel = channelResolver.resolve(sender.getType());
        NotificationPayload payload = new NotificationPayload(
                sender.getConfig(),
                "Alert triggered: " + alert.getType(),
                "Your %s alert matched a new event: %s".formatted(alert.getType(), entry.getRawJsonData())
        );

        NotificationResult result;
        try {
            result = channel.send(payload);
        } catch (Exception e) {
            log.warn("Notification send failed for alert {}", alert.getId(), e);
            result = NotificationResult.failure(e.getMessage());
        }

        notificationAttemptRepository.save(NotificationAttempt.builder()
                .alert(alert)
                .dataEntry(entry)
                .status(result.status())
                .error(result.error())
                .sentAt(Instant.now())
                .build());
    }

    // Simple contains/equals match, no NLP/fuzzy matching (mvp final.md §2).
    private boolean matchesKeyword(String criteriaJson, NewsData data) {
        String keyword = asString(readCriteria(criteriaJson).get("keyword"));
        if (keyword == null || keyword.isBlank()) {
            return false;
        }
        String haystack = ((data.headline() == null ? "" : data.headline()) + " "
                + (data.text() == null ? "" : data.text())).toLowerCase();
        return haystack.contains(keyword.toLowerCase());
    }

    // Only absolute ABOVE/BELOW thresholds for MVP — percent-change needs a previous-value
    // comparison that mvp final.md §6 flags as unresolved for the generic model; deferred here too.
    private boolean matchesMarket(String criteriaJson, MarketData data) {
        Map<String, Object> criteria = readCriteria(criteriaJson);
        String ticker = asString(criteria.get("ticker"));
        String comparator = asString(criteria.get("comparator"));
        Object thresholdValue = criteria.get("threshold");
        if (ticker == null || data.ticker() == null || !ticker.equalsIgnoreCase(data.ticker())
                || data.value() == null || comparator == null || thresholdValue == null) {
            return false;
        }
        BigDecimal threshold = new BigDecimal(thresholdValue.toString());
        return switch (comparator.toUpperCase()) {
            case "ABOVE" -> data.value().compareTo(threshold) > 0;
            case "BELOW" -> data.value().compareTo(threshold) < 0;
            default -> false;
        };
    }

    // Named-region match is enough for MVP; radius-based matching is a stretch goal (mvp final.md §2).
    private boolean matchesRegion(String criteriaJson, EmergencyData data) {
        String region = asString(readCriteria(criteriaJson).get("region"));
        return region != null && data.region() != null && data.region().equalsIgnoreCase(region);
    }

    private Map<String, Object> readCriteria(String criteriaJson) {
        try {
            return objectMapper.readValue(criteriaJson, new TypeReference<>() {
            });
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to parse alert.criteria", e);
        }
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
