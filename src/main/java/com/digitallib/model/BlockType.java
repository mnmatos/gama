package com.digitallib.model;

public enum BlockType {
    HEADING("Título"),
    PARAGRAPH("Parágrafo"),
    TABLE("Tabela"),
    LIST("Lista"),
    DATE("Data"),
    LABEL("Rótulo"),
    FOOTER("Rodapé"),
    HOMENAGEM("Homenagem"),
    ESTROFE("Estrofe"),
    OTHER("Outro");

    private final String displayName;

    BlockType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}

