package com.sonrisa.homework.matching.sourcedata;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

// Builds a DataSource's typed interface at match time from DataEntry.rawJsonData +
// DataSource.fieldMapping (mvp final.md §5's mapping UI is what produces fieldMapping).
// fieldMapping keys are the provider's raw field names, values are the interface field names.
@Component
@RequiredArgsConstructor
public class SourceDataMapper {

    private final ObjectMapper objectMapper;

    public Map<String, Object> normalize(String rawJsonData, String fieldMappingJson) {
        Map<String, Object> raw = readJson(rawJsonData);
        Map<String, String> mapping = fieldMappingJson == null || fieldMappingJson.isBlank()
                ? Map.of()
                : readMapping(fieldMappingJson);

        Map<String, Object> normalized = new HashMap<>();
        mapping.forEach((rawField, interfaceField) -> {
            if (raw.containsKey(rawField)) {
                normalized.put(interfaceField, raw.get(rawField));
            }
        });
        return normalized;
    }

    public NewsData toNews(Map<String, Object> normalized) {
        return new NewsData(asString(normalized.get("headline")), asString(normalized.get("text")), asInstant(normalized.get("timestamp")));
    }

    public MarketData toMarket(Map<String, Object> normalized) {
        return new MarketData(asString(normalized.get("ticker")), asDecimal(normalized.get("value")), asInstant(normalized.get("timestamp")));
    }

    public EmergencyData toEmergency(Map<String, Object> normalized) {
        return new EmergencyData(
                asString(normalized.get("region")),
                asString(normalized.get("eventType")),
                asString(normalized.get("severity")),
                asInstant(normalized.get("timestamp"))
        );
    }

    public WeatherData toWeather(Map<String, Object> normalized) {
        return new WeatherData(
                asString(normalized.get("region")),
                asString(normalized.get("condition")),
                asInstant(normalized.get("timestamp"))
        );
    }

    private Map<String, Object> readJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to parse dataEntry.rawJsonData", e);
        }
    }

    private Map<String, String> readMapping(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (JacksonException e) {
            throw new IllegalStateException("Failed to parse dataSource.fieldMapping", e);
        }
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private BigDecimal asDecimal(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Instant asInstant(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value.toString());
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
