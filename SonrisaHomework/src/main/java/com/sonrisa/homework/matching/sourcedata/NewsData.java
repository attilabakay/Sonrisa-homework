package com.sonrisa.homework.matching.sourcedata;

import java.time.Instant;

// In-memory-only construct corresponding to mvp final.md §4's NewsInterface — not persisted.
public record NewsData(String headline, String text, Instant timestamp) {
}
