package com.sonrisa.homework.ingestion;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RssFeedParserTest {

    private final RssFeedParser parser = new RssFeedParser();

    @Test
    void splitsEachItemIntoItsOwnFlatFieldMap() {
        String rss = """
                <rss version="2.0"><channel>
                  <title>Example Feed</title>
                  <item>
                    <title>First article</title>
                    <description>First body</description>
                    <pubDate>Mon, 14 Sep 2026 10:00:00 GMT</pubDate>
                  </item>
                  <item>
                    <title>Second article</title>
                    <description>Second body</description>
                  </item>
                </channel></rss>
                """;

        List<Map<String, String>> items = parser.parseItems(rss);

        assertThat(items).hasSize(2);
        assertThat(items.get(0)).containsEntry("title", "First article").containsEntry("description", "First body");
        assertThat(items.get(1)).containsEntry("title", "Second article").containsEntry("description", "Second body");
    }

    @Test
    void emptyChannelYieldsNoItems() {
        String rss = "<rss version=\"2.0\"><channel><title>Empty</title></channel></rss>";

        assertThat(parser.parseItems(rss)).isEmpty();
    }

    @Test
    void rejectsInputWithADoctypeDeclaration() {
        // XXE-hardening check: a DOCTYPE (the classic entity-injection vector) must be refused
        // outright rather than silently resolved.
        String maliciousRss = """
                <?xml version="1.0"?>
                <!DOCTYPE rss [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <rss version="2.0"><channel><item><title>&xxe;</title></item></channel></rss>
                """;

        assertThatThrownBy(() -> parser.parseItems(maliciousRss))
                .isInstanceOf(IllegalStateException.class);
    }
}
