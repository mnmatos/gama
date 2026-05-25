package com.digitallib.model;

/**
 * Represents a document that failed to load during repository scan,
 * retaining enough information to attempt a repair.
 */
public class FailedDocument {

    /** Absolute path to the JSON file. */
    private final String path;

    /** Raw JSON content of the file. */
    private final String rawJson;

    /** Human-readable error message produced during load. */
    private final String errorMessage;

    public FailedDocument(String path, String rawJson, String errorMessage) {
        this.path = path;
        this.rawJson = rawJson;
        this.errorMessage = errorMessage;
    }

    public String getPath() {
        return path;
    }

    public String getRawJson() {
        return rawJson;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Returns true when the failure is a class / subclass mapping problem that
     * can be fixed by remapping through the UI.
     */
    public boolean isMappingError() {
        return errorMessage != null && (
                errorMessage.contains("Failed to deserialize Classe")
                || errorMessage.contains("Failed to deserialize SubClasse")
                || errorMessage.contains("Classe não encontrada")
                || errorMessage.contains("Could not find category")
        );
    }

    /** The bad class name extracted from the error message, or null. */
    public String getBadClasseName() {
        return extractBetween(errorMessage, "Failed to deserialize Classe '", "'");
    }

    /** The bad subclass name extracted from the error message, or null. */
    public String getBadSubClasseName() {
        return extractBetween(errorMessage, "Failed to deserialize SubClasse '", "'");
    }

    /**
     * Attempts to extract the {@code titulo} field directly from the raw JSON
     * without fully deserializing the document (which would fail again).
     * Returns an empty string if the field is absent or the JSON is unparseable.
     */
    public String getTitulo() {
        return extractJsonString(rawJson, "titulo");
    }

    /**
     * Attempts to extract the {@code codigo} field directly from the raw JSON.
     * Returns an empty string if absent or unparseable.
     */
    public String getCodigo() {
        return extractJsonString(rawJson, "codigo");
    }

    /**
     * Naive but dependency-free extraction of a top-level string value from JSON.
     * Good enough for simple scalar fields like "titulo" and "codigo".
     */
    private static String extractJsonString(String json, String key) {
        if (json == null || json.isEmpty()) return "";
        // Match  "key"  :  "value"  allowing whitespace and escaped chars
        String pattern = "\"" + key + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"";
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (m.find()) {
            // Unescape basic JSON escape sequences
            return m.group(1)
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\/", "/")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t");
        }
        return "";
    }

    private static String extractBetween(String src, String open, String close) {
        if (src == null) return null;
        int s = src.indexOf(open);
        if (s < 0) return null;
        s += open.length();
        int e = src.indexOf(close, s);
        if (e < 0) return null;
        return src.substring(s, e);
    }
}

