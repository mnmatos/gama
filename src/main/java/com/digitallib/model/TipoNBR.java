package com.digitallib.model;

import com.digitallib.reference.generator.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum TipoNBR {

    JORNAL ("Jornal", new NewspaperReferenceGenerator()), //7.7.7
    MONOGRAFIA_LIVRO("Livro", new BookReferenceGenerator()), //7.1
    MONOGRAFIA_ACADEMICO("Trabalho acadêmico", new AcademicReferenceGenerator()), //7.1.2
    ARTIGO("Artigo", new PaperReferenceGenerator()), //7.7.5
    ENTREVISTA("Entrevista", new InterviewReferenceGenerator()), //8.1.1.9
    IMAGEM("Imagem", new ImageReferenceGenerator()),  //7.15
    VIDEO_ONLINE("Vídeo online", new OnlineVideoReferenceGenerator()), //7.13.2
    CORRESPONDENCIA("Correspondência", new MailReferenceGenerator()), //7.5
    DOCUMENTOS_CIVIS("Documentos civis", new CivilianDocumentReferenceGenerator()); //7.12

    private static Map<String, TipoNBR> classeMap = new HashMap<String, TipoNBR>();
    private String printableName;
    private ReferenceGenerator referenceGenerator;

    static {
        classeMap.put("jornal", JORNAL);
        classeMap.put("monografia_livro", MONOGRAFIA_LIVRO);
        classeMap.put("monografia_academico", MONOGRAFIA_ACADEMICO);
        classeMap.put("artigo", ARTIGO);
        classeMap.put("entrevista", ENTREVISTA);
        classeMap.put("imagem", IMAGEM);
        classeMap.put("video_online", VIDEO_ONLINE);
        classeMap.put("correspondencia", CORRESPONDENCIA);
        classeMap.put("documentos_civis", DOCUMENTOS_CIVIS);
    }

    TipoNBR(String printableName, ReferenceGenerator referenceGenerator) {
        this.printableName = printableName;
        this.referenceGenerator = referenceGenerator;
    }

    @JsonCreator
    public static TipoNBR forValue(String value) {
        return classeMap.get(value.toLowerCase());
    }

    public static TipoNBR fromPosition(int value) {
        return TipoNBR.values()[value];
    }
    @JsonValue
    public String toValue() {
        for (Map.Entry<String, TipoNBR> entry : classeMap.entrySet()) {
            if (entry.getValue() == this)
                return entry.getKey();
        }

        return null; // or fail
    }

    public int getPosition() {
        return ordinal();
    }

    public ReferenceGenerator getReferenceGenerator() {
        return referenceGenerator;
    }

    public static String[] getAsStringList() {
        String[] typeArray = new String[TipoNBR.values().length];
        int i = 0;
        for (TipoNBR type : TipoNBR.values()) {
            typeArray[i++] = type.print();
        }
        return typeArray;
    }

    public String print() {
        return printableName;
    }
}
