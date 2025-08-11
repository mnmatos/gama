package com.digitallib.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public enum SubClasseProducao {

    POESIA("01a", "Poesia"),
    CONTO("01b", "Conto"),
    ENSAIO("01c", "Ensaio"),
    PEÇAS_TEATRAIS("01d", "Peças teatrais"),
    CANCOES("01e", "Canções"),
    DISCURSOS("01f", "Discursos"),
    MANIFESTO("01g", "Manifestos"),

    HOMENAGEM("01h", "Homenagem"),
    FOTOS_AUTORA("02a", "Fotos"),
    ENTREVISTAS("02b", "Entrevistas"),

    NOTAS_MANUSCRITAS("03a", "Notas manuscritas"),
    DOC_DIVERSOS("04a", "Documentos diversos relacionados com a autora"),
    HOMENAGEM_IN_MEMORIAM("04b", "Homenagem in memoriam"),
    CRITICA_UNIVERSITARIA("05a", "Crítica acadêmica universitária"),
    CRITICA_ACADEMIA_FEIRA("05b", "Crítica da Academia Feirense de Letras e Artes"),
    MEMORIAL_E_BIOGRAFICOS("05c", "Memorial e demais estudos biográficos sobre a autora"),
    POETAS("05d", "Poetas"),
    CERTIDAO("06a", "Certidão de inteiro teor"),
    VIDA("06b", "Vida"),

    NOTICIAS("07a", "Notícias relacionadas com a autora"),
    CORRESPONDENCIA_ENVIADA("08a", "Enviada"),
    CORRESPONDENCIA_RECEBIDA("08b", "Recebida");


    private static Map<String, SubClasseProducao> subClasseMap = new LinkedHashMap<>();
    private String printableText;
    private String code;

    static {
        subClasseMap.put(POESIA.getCode(), POESIA);
        subClasseMap.put(CONTO.getCode(), CONTO);
        subClasseMap.put(ENSAIO.getCode(), ENSAIO);
        subClasseMap.put(PEÇAS_TEATRAIS.getCode(), PEÇAS_TEATRAIS);
        subClasseMap.put(CANCOES.getCode(), CANCOES);
        subClasseMap.put(DISCURSOS.getCode(), DISCURSOS);
        subClasseMap.put(MANIFESTO.getCode(), MANIFESTO);
        subClasseMap.put(HOMENAGEM.getCode(), HOMENAGEM);

        subClasseMap.put(FOTOS_AUTORA.getCode(), FOTOS_AUTORA);
        subClasseMap.put(ENTREVISTAS.getCode(), ENTREVISTAS);

        subClasseMap.put(NOTAS_MANUSCRITAS.getCode(), NOTAS_MANUSCRITAS);

        subClasseMap.put(DOC_DIVERSOS.getCode(), DOC_DIVERSOS);
        subClasseMap.put(HOMENAGEM_IN_MEMORIAM.getCode(), HOMENAGEM_IN_MEMORIAM);

        subClasseMap.put(CRITICA_UNIVERSITARIA.getCode(), CRITICA_UNIVERSITARIA);
        subClasseMap.put(CRITICA_ACADEMIA_FEIRA.getCode(), CRITICA_ACADEMIA_FEIRA);
        subClasseMap.put(MEMORIAL_E_BIOGRAFICOS.getCode(), MEMORIAL_E_BIOGRAFICOS);
        subClasseMap.put(POETAS.getCode(), POETAS);

        subClasseMap.put(CERTIDAO.getCode(), CERTIDAO);
        subClasseMap.put(VIDA.getCode(), VIDA);

        subClasseMap.put(NOTICIAS.getCode(), NOTICIAS);

        subClasseMap.put(CORRESPONDENCIA_ENVIADA.getCode(), CORRESPONDENCIA_ENVIADA);
        subClasseMap.put(CORRESPONDENCIA_RECEBIDA.getCode(), CORRESPONDENCIA_RECEBIDA);

    }

    SubClasseProducao(String code, String printableText) {
        this.printableText = printableText;
        this.code = code;
    }

    public String print() {
        return String.format("%s, %s", code, printableText);
    }

    public String getCode() {
        return code;
    }

    @JsonCreator
    public static SubClasseProducao forValue(String value) {
        return SubClasseProducao.valueOf(value.toUpperCase());
    }

    public static SubClasseProducao forCode(String code) {
        return subClasseMap.get(code);
    }

    @JsonValue
    public String toValue() {
        return this.toString();
    }

    public static Map<String, SubClasseProducao> getSubClasseMap() {
        return subClasseMap;
    }
}
