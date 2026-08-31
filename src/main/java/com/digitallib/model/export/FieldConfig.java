package com.digitallib.model.export;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Configuration for one field inside a {@link BlockConfig}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldConfig {

    /** Optional display label shown before the value (e.g., "Localização"). */
    private String label;

    /** The data source for this field's value. */
    private FieldSource source = FieldSource.LITERAL;

    /** The literal text to render when source == LITERAL. */
    private String literalValue;

    /**
     * Optional printf-style format string applied to the resolved value
     * (e.g., "%02d" for zero-padded integers).
     */
    private String format;

    /** Whether the label portion should be rendered bold. */
    private boolean labelBold = true;

    /** Whether the value portion should be rendered bold. */
    private boolean valueBold = false;

    /** Whether the value should be rendered in italic. */
    private boolean italic = false;

    public FieldConfig() {}

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public FieldSource getSource() { return source; }
    public void setSource(FieldSource source) { this.source = source; }

    public String getLiteralValue() { return literalValue; }
    public void setLiteralValue(String literalValue) { this.literalValue = literalValue; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public boolean isLabelBold() { return labelBold; }
    public void setLabelBold(boolean labelBold) { this.labelBold = labelBold; }

    public boolean isValueBold() { return valueBold; }
    public void setValueBold(boolean valueBold) { this.valueBold = valueBold; }

    public boolean isItalic() { return italic; }
    public void setItalic(boolean italic) { this.italic = italic; }

    @Override
    public String toString() {
        if (source == FieldSource.LITERAL) return "\"" + literalValue + "\"";
        return source.toString() + (label != null && !label.isBlank() ? " (" + label + ")" : "");
    }
}
