package com.sonrisa.homework.matching.sourcedata;

import java.time.Instant;

// In-memory-only construct corresponding to mvp final.md §4's EmergencyInterface — not persisted.
// Covers both DISASTER and the WEATHER stretch goal, which share the same shape.
public record EmergencyData(String region, String eventType, String severity, Instant timestamp) {
}
