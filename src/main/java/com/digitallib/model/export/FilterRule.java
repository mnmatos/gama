package com.digitallib.model.export;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A filter rule that determines which documents a {@link FichaExportConfig} applies to.
 * Matching logic: classeCode always compared; subClasseCode compared only when non-null.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FilterRule {

    /** Code of the Classe (e.g., "01"). Must not be null. */
    private String classeCode;

    /**
     * Code of the SubClasse (e.g., "01d"). When null the rule matches any
     * subclass within classeCode.
     */
    private String subClasseCode;

    public FilterRule() {}

    public FilterRule(String classeCode, String subClasseCode) {
        this.classeCode = classeCode;
        this.subClasseCode = subClasseCode;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String getClasseCode() { return classeCode; }
    public void setClasseCode(String classeCode) { this.classeCode = classeCode; }

    public String getSubClasseCode() { return subClasseCode; }
    public void setSubClasseCode(String subClasseCode) { this.subClasseCode = subClasseCode; }

    @Override
    public String toString() {
        return subClasseCode != null ? classeCode + " / " + subClasseCode : classeCode + " (todas)";
    }
}

