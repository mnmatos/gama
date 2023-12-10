package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class DoiReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public DoiReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getInfoAdicionais() == null) return "";
        String doi = doc.getInfoAdicionais().getDoi();
        if(doi == null || doi.isEmpty()) return "";
        return String.format("DOI: %s", doi);
    }
}
