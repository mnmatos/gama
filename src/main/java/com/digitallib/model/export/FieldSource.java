package com.digitallib.model.export;

/**
 * Enumerates the possible data sources for a FieldConfig within a ficha export block.
 * All null / missing values resolve to empty string — no exceptions or placeholders.
 */
public enum FieldSource {
    TITULO("Título"),
    SUBTITULO("Subtítulo"),
    AUTORES("Autor(es)"),
    TESTEMUNHO("Testemunho"),
    CODIGO("Código"),
    ANO("Ano"),
    LOCAL_PUBLICACAO("Local de Publicação"),
    NUM_PAGINA("Número de Páginas"),
    INSTITUICAO_CUSTODIA("Instituição de Custódia"),
    DESCRICAO("Descrição"),
    TEXTO_TEATRAL_PERSONAGENS("Texto Teatral: Personagens"),
    TEXTO_TEATRAL_ATOS("Texto Teatral: Atos"),
    TEXTO_TEATRAL_CENAS("Texto Teatral: Cenas"),
    TEXTO_TEATRAL_RESUMO("Texto Teatral: Resumo"),
    TEXTO_TEATRAL_PALAVRAS_CHAVE("Texto Teatral: Palavras-chave"),
    /** Renders the literalValue of the FieldConfig as-is. */
    LITERAL("Dados Fixos");

    private final String displayName;

    FieldSource(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
