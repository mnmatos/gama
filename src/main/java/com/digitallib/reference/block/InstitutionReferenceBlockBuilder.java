package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class InstitutionReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public InstitutionReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getInstituicaoCustodia() == null || doc.getInstituicaoCustodia().isEmpty()) return "";
        return doc.getInstituicaoCustodia();
    }
}
