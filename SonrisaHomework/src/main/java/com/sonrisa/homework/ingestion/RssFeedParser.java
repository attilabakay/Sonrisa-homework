package com.sonrisa.homework.ingestion;

import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Parses an RSS 2.0 <channel><item> feed into one flat field map per item. Good enough for the
// MVP's "one news provider" scope (mvp final.md §4) — a second RSS-shaped provider is a
// DataSource.fieldMapping change, same as a JSON provider would be.
@Component
public class RssFeedParser {

    public List<Map<String, String>> parseItems(String xml) {
        Document document = parseDocument(xml);
        NodeList items = document.getElementsByTagName("item");
        List<Map<String, String>> result = new ArrayList<>();
        for (int i = 0; i < items.getLength(); i++) {
            result.add(flattenItem((Element) items.item(i)));
        }
        return result;
    }

    private Map<String, String> flattenItem(Element item) {
        Map<String, String> fields = new LinkedHashMap<>();
        NodeList children = item.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                String text = child.getTextContent();
                if (text != null) {
                    fields.put(child.getNodeName(), text.trim());
                }
            }
        }
        return fields;
    }

    private Document parseDocument(String xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xml)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new IllegalStateException("Failed to parse XML feed", e);
        }
    }
}
