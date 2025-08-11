package com.digitallib.model;

import com.digitallib.utils.ConfigReader;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.*;

public enum ClasseProducao {

    PRODUCAO_INTELECTUAL ("01","producao_intelectual", "Produção Intelectual"),
    DOCUMENTOS_AUDIOVISUAIS("02","documentos_audiovisuais", "Documentos Audiovisuais"),
    ESBOCOS_NOTAS("03","esbocos_e_notas","Esboços e notas"),
    MEMORABILIA("04", "memorabilia", "Memorabilia"),
    RECEPCAO("05", "recepcao", "Recepção"),
    VARIA("06",  "varia", "Varia"),

    PUBLICACOES_IMPRENSA("07","publicacoes_imprensa","Publicações na imprensa"),

    CORRESPONDENCIA ("08", "correspondencia", "Correspondência");



    private static Map<String, ClasseProducao> classeMap = new HashMap<String, ClasseProducao>();
    private final String code;
    private final String propertyName;
    private String printableName;

    static {
        List<ClasseProducao> classeList = Arrays.asList(PRODUCAO_INTELECTUAL, DOCUMENTOS_AUDIOVISUAIS, ESBOCOS_NOTAS,MEMORABILIA, RECEPCAO, VARIA, PUBLICACOES_IMPRENSA, CORRESPONDENCIA);
        for (ClasseProducao classe : classeList){
            classeMap.put(classe.propertyName, classe);
        }
    }

    ClasseProducao(String defaultCode, String propertyName, String displayName) {
        this.propertyName = propertyName;
        this.code = ConfigReader.getProperty("code_"+propertyName, defaultCode);
        this.printableName = String.format("%s. %s", code, displayName);
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
