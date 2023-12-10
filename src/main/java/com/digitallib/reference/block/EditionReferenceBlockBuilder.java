package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class EditionReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public EditionReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getEdicao() == null || doc.getEdicao() == 0) return "";
        return String.format("%d. ed", doc.getEdicao());
    }
}
