package com.digitallib.exporter.docx;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FichaExporterTest {

    @Test
    void testCreateKeywordsString() {
        List<String> keywords = Arrays.asList("keyword1", "keyword2", "keyword3");
        String result = FichaExporter.createKeywordsString(keywords);
        assertEquals("keyword1. keyword2. keyword3.", result);
    }
}