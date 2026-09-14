package com.sonrisa.homework.matching.sourcedata;

import java.math.BigDecimal;
import java.time.Instant;

// In-memory-only construct corresponding to mvp final.md §4's MarketInterface — not persisted.
public record MarketData(String ticker, BigDecimal value, Instant timestamp) {
}
