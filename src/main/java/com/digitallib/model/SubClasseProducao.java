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
    VARIA("06b", "Varia"),

    NOTICIAS("07a", "Notícias relacionadas com a autora");


    private static Map<String, SubClasseProducao> subClasseMap = new LinkedHashMap<>();
    private String printableText;
    private String code;

    static {
        subClasseMap.put("01a", POESIA);
        subClasseMap.put("01b", CONTO);
        subClasseMap.put("01c", ENSAIO);
        subClasseMap.put("01d", PEÇAS_TEATRAIS);
        subClasseMap.put("01e", CANCOES);
        subClasseMap.put("01f", DISCURSOS);
        subClasseMap.put("01g", MANIFESTO);
        subClasseMap.put("01h", HOMENAGEM);

        subClasseMap.put("02a", FOTOS_AUTORA);
        subClasseMap.put("02b", ENTREVISTAS);

        subClasseMap.put("03a", NOTAS_MANUSCRITAS);

        subClasseMap.put("04a", DOC_DIVERSOS);
        subClasseMap.put("04b", HOMENAGEM_IN_MEMORIAM);

        subClasseMap.put("05a", CRITICA_UNIVERSITARIA);
        subClasseMap.put("05b", CRITICA_ACADEMIA_FEIRA);
        subClasseMap.put("05c", MEMORIAL_E_BIOGRAFICOS);
        subClasseMap.put("05d", POETAS);

        subClasseMap.put("06a", CERTIDAO);
        subClasseMap.put("06b", VARIA);
        subClasseMap.put("07a", NOTICIAS);

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
