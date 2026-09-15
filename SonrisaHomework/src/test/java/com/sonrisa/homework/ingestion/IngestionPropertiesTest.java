package com.sonrisa.homework.ingestion;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IngestionPropertiesTest {

    @Test
    void defaultsToFiveMinutes() {
        IngestionProperties properties = new IngestionProperties();

        assertThat(properties.getEffectiveIntervalMillis()).isEqualTo(5 * 60_000L);
    }

    @Test
    void aValueAtOrAboveTheFloorIsUsedAsIs() {
        IngestionProperties properties = new IngestionProperties();
        properties.setIntervalMinutes(10);

        assertThat(properties.getEffectiveIntervalMillis()).isEqualTo(10 * 60_000L);
    }

    @Test
    void aValueBelowTheTwoMinuteFloorIsClampedUp() {
        IngestionProperties properties = new IngestionProperties();
        properties.setIntervalMinutes(1);

        assertThat(properties.getEffectiveIntervalMillis()).isEqualTo(2 * 60_000L);
    }

    @Test
    void zeroOrNegativeValuesAreAlsoClampedToTheFloor() {
        IngestionProperties properties = new IngestionProperties();
        properties.setIntervalMinutes(0);
        assertThat(properties.getEffectiveIntervalMillis()).isEqualTo(2 * 60_000L);

        properties.setIntervalMinutes(-5);
        assertThat(properties.getEffectiveIntervalMillis()).isEqualTo(2 * 60_000L);
    }
}
