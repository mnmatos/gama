package com.digitallib.model.export;

import com.digitallib.model.TIPO_RELACAO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * One rendering block within a {@link FichaExportConfig}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BlockConfig {

    private BlockType type = BlockType.INFO;

    /** Optional section heading label displayed above this block. */
    private String label;

    private boolean enabled = true;

    /**
     * Ordered list of field configurations. For RELATED_STUDIES blocks the fields
     * control only the heading text; the references are generated automatically.
     */
    private List<FieldConfig> fields = new ArrayList<>();

    /**
     * Only meaningful when type == RELATED_STUDIES.
     * Filters related-work links by relation type.
     */
    private TIPO_RELACAO relationType = TIPO_RELACAO.ESTUDADO_EM;

    public BlockConfig() {}

    // ── Getters & Setters ──────────────────────────────────────────────────

    public BlockType getType() { return type; }
    public void setType(BlockType type) { this.type = type; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public List<FieldConfig> getFields() { return fields; }
    public void setFields(List<FieldConfig> fields) { this.fields = fields != null ? fields : new ArrayList<>(); }

    public TIPO_RELACAO getRelationType() { return relationType; }
    public void setRelationType(TIPO_RELACAO relationType) { this.relationType = relationType; }

    @Override
    public String toString() {
        return type + (label != null && !label.isBlank() ? " — " + label : "");
    }
}

