package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class AcademicTypeReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public AcademicTypeReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getInfoAdicionais() == null) return "";
        String tipo = doc.getInfoAdicionais().getTipoTrabalho();
        if(tipo == null || tipo.isEmpty()) return "";
        return tipo;
    }
}
