package com.digitallib.llm;
import com.digitallib.model.BlockType;
import com.digitallib.model.TextBlock;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
/**
 * Parses raw LLM response text into a list of TextBlock objects.
 * Strips markdown fences, parses JSON array, falls back to a single paragraph block.
 */
public class ResponseParser {
    private static final Logger logger = LogManager.getLogger(ResponseParser.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    private ResponseParser() {}
    public static final String TRANSCRIPTION_PROMPT =
        "You are a philological transcription assistant. Transcribe all text visible in the image faithfully. " +
        "Return ONLY a JSON array of objects. Each object must have exactly these keys: " +
        "\"blockType\" (one of: heading, paragraph, table, list, date, label, footer, other), " +
        "\"text\" (the transcribed text), and \"confidence\" (a number between 0.0 and 1.0). " +
        "Group lines that belong to the same logical block (e.g. a paragraph, a heading, a table) into a SINGLE object — " +
        "do NOT create one object per line. Use \\n within the \"text\" field to preserve line breaks inside a block. " +
        "Preserve original spelling and punctuation. Do not summarize or paraphrase. " +
        "Do not include any text outside the JSON array.";
    public static List<TextBlock> parse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return fallback("[empty response]");
        }
        String json = stripMarkdownFences(rawResponse.trim());
        try {
            JsonNode arr = mapper.readTree(json);
            if (!arr.isArray()) throw new Exception("Not a JSON array");
            List<TextBlock> blocks = new ArrayList<>();
            for (int i = 0; i < arr.size(); i++) {
                JsonNode node = arr.get(i);
                String typeStr = node.has("blockType") ? node.get("blockType").asText("other") : "other";
                String text = node.has("text") ? node.get("text").asText("") : "";
                double confidence = node.has("confidence") ? node.get("confidence").asDouble(0.5) : 0.5;
                blocks.add(new TextBlock(i, toBlockType(typeStr), text, confidence));
            }
            return blocks;
        } catch (Exception e) {
            logger.warn("Failed to parse LLM response as JSON array, falling back: {}", e.getMessage());
            return fallback(json);
        }
    }
    private static String stripMarkdownFences(String text) {
        if (text.startsWith("```")) {
            int firstNewline = text.indexOf('\n');
            if (firstNewline > 0) text = text.substring(firstNewline + 1);
            if (text.endsWith("```")) text = text.substring(0, text.lastIndexOf("```")).trim();
        }
        return text;
    }
    private static List<TextBlock> fallback(String rawText) {
        List<TextBlock> list = new ArrayList<>();
        list.add(new TextBlock(0, BlockType.PARAGRAPH, rawText, 0.5));
        return list;
    }
    private static BlockType toBlockType(String s) {
        switch (s.toLowerCase(Locale.ROOT)) {
            case "heading": return BlockType.HEADING;
            case "table": return BlockType.TABLE;
            case "list": return BlockType.LIST;
            case "date": return BlockType.DATE;
            case "label": return BlockType.LABEL;
            case "footer": return BlockType.FOOTER;
            case "paragraph": return BlockType.PARAGRAPH;
            default: return BlockType.OTHER;
        }
    }
    /** Build a Base64-encoded data URI from image bytes, used for vision model payloads. */
    public static String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}