package com.digitallib.model.export;

/**
 * Semantic type of a {@link BlockConfig} block inside a ficha export config.
 */
public enum BlockType {
    AUTHOR("Autor(es)"),
    TITLE("Título"),
    INFO("Informações Adicionais"),
    DESCRIPTION("Descrição"),
    RESUME_KEYWORDS("Resumo e Palavras-chave"),
    RELATED_STUDIES("Estudos Relacionados");

    private final String displayName;

    BlockType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
