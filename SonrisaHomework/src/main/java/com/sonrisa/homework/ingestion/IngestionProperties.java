package com.sonrisa.homework.ingestion;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

// Single global setting for how often the scheduled job polls all active data sources
// (app.ingestion.interval-minutes) — floored at 2 minutes regardless of what's configured.
@Component
@ConfigurationProperties(prefix = "app.ingestion")
@Getter
@Setter
@Slf4j
public class IngestionProperties {

    private static final int MIN_INTERVAL_MINUTES = 2;

    private int intervalMinutes = 5;

    @PostConstruct
    void warnIfBelowMinimum() {
        if (intervalMinutes < MIN_INTERVAL_MINUTES) {
            log.warn("app.ingestion.interval-minutes={} is below the {}-minute minimum; using {} instead.",
                    intervalMinutes, MIN_INTERVAL_MINUTES, MIN_INTERVAL_MINUTES);
        }
    }

    public long getEffectiveIntervalMillis() {
        return Math.max(intervalMinutes, MIN_INTERVAL_MINUTES) * 60_000L;
    }
}
