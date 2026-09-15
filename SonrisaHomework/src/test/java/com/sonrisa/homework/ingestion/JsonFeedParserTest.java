package com.sonrisa.homework.ingestion;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JsonFeedParserTest {

    private final JsonFeedParser parser = new JsonFeedParser(new ObjectMapper());

    @Test
    void splitsATopLevelArrayIntoOneEntryPerElement() {
        List<String> entries = parser.splitEntries("[{\"a\":1},{\"a\":2},{\"a\":3}]");

        assertThat(entries).hasSize(3);
        assertThat(entries.get(1)).contains("\"a\":2");
    }

    @Test
    void findsTheResultsArrayNestedUnderAProviderSpecificKey() {
        // NewsAPI-shaped response: the array isn't the top-level value, it's nested under
        // "articles" alongside unrelated scalar fields.
        String newsApiShaped = "{\"status\":\"ok\",\"totalResults\":2,\"articles\":[{\"title\":\"one\"},{\"title\":\"two\"}]}";

        List<String> entries = parser.splitEntries(newsApiShaped);

        assertThat(entries).hasSize(2);
        assertThat(entries.get(0)).contains("\"title\":\"one\"");
        assertThat(entries.get(1)).contains("\"title\":\"two\"");
    }

    @Test
    void aResponseWithNoArrayAtAllIsTreatedAsExactlyOneEntry() {
        String singleSnapshot = "{\"symbol\":\"AAPL\",\"price\":175}";

        List<String> entries = parser.splitEntries(singleSnapshot);

        assertThat(entries).containsExactly(singleSnapshot);
    }

    @Test
    void anEmptyResultsArrayProducesNoEntries() {
        List<String> entries = parser.splitEntries("{\"articles\":[]}");

        assertThat(entries).isEmpty();
    }

    @Test
    void picksTheFirstArrayFieldWhenMultipleArraysArePresent() {
        String multipleArrays = "{\"tags\":[\"a\",\"b\"],\"results\":[{\"id\":1}]}";

        List<String> entries = parser.splitEntries(multipleArrays);

        // "tags" appears first in the object, so it wins -- documenting the actual (simple,
        // order-based) disambiguation rule rather than leaving it unspecified.
        assertThat(entries).containsExactly("\"a\"", "\"b\"");
    }
}
