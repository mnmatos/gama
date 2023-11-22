package com.digitallib.model;

import com.digitallib.reference.generator.*;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.HashMap;
import java.util.Map;

public enum TipoNBR {

    JORNAL ("Jornal", new NewspaperReferenceGenerator()),
    MONOGRAFIA("Monografia", new MonographReferenceGenerator()),
    ENTREVISTA("Entrevista", new InterviewReferenceGenerator()),
    DOCUMENTO_DIGITAL("Documento Digital", new DigitalDocumentReferenceGenerator()),
    IMAGEM("Imagem", new ImageReferenceGenerator()),
    VIDEO("Vídeo", new VideoReferenceGenerator()),
    CORRESPONDENCIA("Correspondência", new MailReferenceGenerator()),
    DOCUMENTOS_CIVIS("Documentos Civis", new CivilianDocumentReferenceGenerator());

    private static Map<String, TipoNBR> classeMap = new HashMap<String, TipoNBR>();
    private String printableName;
    private ReferenceGenerator referenceGenerator;

    static {
        classeMap.put("jornal", JORNAL);
        classeMap.put("monografia", MONOGRAFIA);
        classeMap.put("entrevista", ENTREVISTA);
        classeMap.put("documento_digital", DOCUMENTO_DIGITAL);
        classeMap.put("imagem", IMAGEM);
        classeMap.put("video", VIDEO);
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
