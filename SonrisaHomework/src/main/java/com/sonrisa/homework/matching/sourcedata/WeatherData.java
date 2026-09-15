package com.sonrisa.homework.matching.sourcedata;

import java.time.Instant;

// In-memory-only construct corresponding to mvp final.md §4's WeatherInterface — the stretch
// goal. Proof-of-concept only: no working free/keyless weather API was found to build a real
// ingestion path against, so this is exercised with manually-posted DataEntry payloads shaped
// like a plausible provider response. `condition` holds the provider's raw weather
// description text (e.g. "Rain", "light rain", "Sunny"); the alert trigger matches it for a
// "rain" substring, separately from DISASTER's region-only match.
public record WeatherData(String region, String condition, Instant timestamp) {
}
