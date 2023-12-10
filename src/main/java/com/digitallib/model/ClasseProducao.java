package com.digitallib.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum ClasseProducao {

    PRODUCAO_INTELECTUAL ("01. Produção Intelectual"),
    DOCUMENTOS_AUDIOVISUAIS("02. Documentos Audiovisuais"),
    ESBOCOS_NOTAS("03. Esboços e notas"),
    MEMORABILIA("04. Memorabilia"),
    RECEPCAO("05. Recepção"),
    VIDA("06. Vida"),

    PUBLICACOES_IMPRENSA("07. Publicações na imprensa"),

    CORRESPONDENCIA ("08. Correspondência");



    private static Map<String, ClasseProducao> classeMap = new HashMap<String, ClasseProducao>();
    private String printableName;

    static {
        classeMap.put("producao_intelectual", PRODUCAO_INTELECTUAL);
        classeMap.put("documentos_audiovisuais", DOCUMENTOS_AUDIOVISUAIS);
        classeMap.put("esbocos_e_notas", ESBOCOS_NOTAS);
        classeMap.put("memorabilia", MEMORABILIA);
        classeMap.put("recepcao", RECEPCAO);
        classeMap.put("vida", VIDA);
        classeMap.put("publicacoes_imprensa", PUBLICACOES_IMPRENSA);
        classeMap.put("correspondencia", CORRESPONDENCIA);
    }

    ClasseProducao(String printableName) {
        this.printableName = printableName;
    }

    @JsonCreator
    public static ClasseProducao forValue(String value) {
        return classeMap.get(value.toLowerCase());
    }

    public static ClasseProducao fromPosition(int value) {
        return ClasseProducao.values()[value];
    }
    @JsonValue
    public String toValue() {
        for (Map.Entry<String, ClasseProducao> entry : classeMap.entrySet()) {
            if (entry.getValue() == this)
                return entry.getKey();
        }

        return null; // or fail
    }

    public int getPosition() {
        return ordinal();
    }

    public static String[] getAsStringList() {
        String[] classeArray = new String[ClasseProducao.values().length];
        int i = 0;
        for (ClasseProducao classe : ClasseProducao.values()) {
            classeArray[i++] = classe.print();
        }
        return classeArray;
    }

    public String print() {
        return printableName;
    }
}
