package com.digitallib.model.export;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * Top-level configuration object for a single ficha-catálogo export profile.
 * Multiple configs can coexist, persisted as a JSON array in
 * {@code ficha-export-configs.json} under the project directory.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FichaExportConfig {

    private String name = "Nova Configuração";

    /**
     * OR-logic filter: a document matches this config if it satisfies at least
     * one FilterRule in this list.
     */
    private List<FilterRule> filterRules = new ArrayList<>();

    /** Ordered list of rendering blocks. */
    private List<BlockConfig> blocks = new ArrayList<>();

    public FichaExportConfig() {}

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<FilterRule> getFilterRules() { return filterRules; }
    public void setFilterRules(List<FilterRule> filterRules) { this.filterRules = filterRules != null ? filterRules : new ArrayList<>(); }

    public List<BlockConfig> getBlocks() { return blocks; }
    public void setBlocks(List<BlockConfig> blocks) { this.blocks = blocks != null ? blocks : new ArrayList<>(); }

    @Override
    public String toString() { return name; }
}

