package com.sonrisa.homework.matching.sourcedata;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SourceDataMapperTest {

    private final SourceDataMapper mapper = new SourceDataMapper(new ObjectMapper());

    @Test
    void normalizeRenamesProviderFieldsToInterfaceFieldsPerFieldMapping() {
        Map<String, Object> normalized = mapper.normalize(
                "{\"title\":\"Big event\",\"description\":\"details\",\"unrelated\":\"ignored\"}",
                "{\"title\":\"headline\",\"description\":\"text\"}");

        assertThat(normalized).containsExactlyInAnyOrderEntriesOf(
                Map.of("headline", "Big event", "text", "details"));
    }

    @Test
    void normalizeSkipsProviderFieldsMissingFromTheRawPayload() {
        Map<String, Object> normalized = mapper.normalize(
                "{\"title\":\"Only this field present\"}",
                "{\"title\":\"headline\",\"description\":\"text\"}");

        assertThat(normalized).containsOnly(Map.entry("headline", "Only this field present"));
    }

    @Test
    void normalizeToleratesAMissingOrBlankFieldMapping() {
        assertThat(mapper.normalize("{\"title\":\"x\"}", null)).isEmpty();
        assertThat(mapper.normalize("{\"title\":\"x\"}", "")).isEmpty();
    }

    @Test
    void toNewsReadsHeadlineTextAndTimestamp() {
        NewsData news = mapper.toNews(Map.of("headline", "H", "text", "T", "timestamp", "2026-01-01T00:00:00Z"));

        assertThat(news.headline()).isEqualTo("H");
        assertThat(news.text()).isEqualTo("T");
        assertThat(news.timestamp()).isEqualTo(Instant.parse("2026-01-01T00:00:00Z"));
    }

    @Test
    void toNewsToleratesMissingFields() {
        NewsData news = mapper.toNews(Map.of());

        assertThat(news.headline()).isNull();
        assertThat(news.text()).isNull();
        assertThat(news.timestamp()).isNull();
    }

    @Test
    void toMarketParsesNumericValueRegardlessOfSourceType() {
        MarketData fromNumber = mapper.toMarket(Map.of("ticker", "AAPL", "value", 175.5));
        MarketData fromString = mapper.toMarket(Map.of("ticker", "AAPL", "value", "175.5"));

        assertThat(fromNumber.value()).isEqualByComparingTo(new BigDecimal("175.5"));
        assertThat(fromString.value()).isEqualByComparingTo(new BigDecimal("175.5"));
    }

    @Test
    void toMarketReturnsNullValueForUnparseableNumbers() {
        MarketData data = mapper.toMarket(Map.of("ticker", "AAPL", "value", "not-a-number"));

        assertThat(data.value()).isNull();
    }

    @Test
    void toEmergencyReadsRegionEventTypeAndSeverity() {
        EmergencyData data = mapper.toEmergency(Map.of("region", "California", "eventType", "wildfire", "severity", "high"));

        assertThat(data.region()).isEqualTo("California");
        assertThat(data.eventType()).isEqualTo("wildfire");
        assertThat(data.severity()).isEqualTo("high");
    }

    @Test
    void toWeatherReadsRegionAndCondition() {
        WeatherData data = mapper.toWeather(Map.of("region", "California", "condition", "light rain"));

        assertThat(data.region()).isEqualTo("California");
        assertThat(data.condition()).isEqualTo("light rain");
    }

    @Test
    void unparseableTimestampsComeBackAsNullInsteadOfThrowing() {
        NewsData news = mapper.toNews(Map.of("timestamp", "not-a-date"));

        assertThat(news.timestamp()).isNull();
    }
}
