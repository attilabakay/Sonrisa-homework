package com.sonrisa.homework.ingestion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// The JSON-side equivalent of RssFeedParser's <item> splitting: a feed response bundles many
// events, but matching expects one event at a time. Providers nest their results array under
// different keys (NewsAPI-shaped APIs use "articles", others use "results"/"data"/...), so
// rather than requiring that key to be configured up front, this looks for the first array
// value anywhere at the top level of the response and splits on that. A response with no
// array at all (e.g. a single-ticker market snapshot) is treated as one entry, unchanged.
@Component
@RequiredArgsConstructor
public class JsonFeedParser {

    private final ObjectMapper objectMapper;

    public List<String> splitEntries(String rawJson) {
        JsonNode root = objectMapper.readTree(rawJson);
        JsonNode entriesArray = findEntriesArray(root);
        if (entriesArray == null) {
            return List.of(rawJson);
        }

        List<String> entries = new ArrayList<>();
        for (JsonNode entry : entriesArray) {
            entries.add(entry.toString());
        }
        return entries;
    }

    private JsonNode findEntriesArray(JsonNode root) {
        if (root.isArray()) {
            return root;
        }
        if (root.isObject()) {
            for (Map.Entry<String, JsonNode> field : root.properties()) {
                if (field.getValue().isArray()) {
                    return field.getValue();
                }
            }
        }
        return null;
    }
}
