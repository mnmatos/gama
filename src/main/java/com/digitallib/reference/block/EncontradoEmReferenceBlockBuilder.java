package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class EncontradoEmReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public EncontradoEmReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getEncontradoEm() == null || doc.getEncontradoEm().isEmpty()) return "";
        return doc.getEncontradoEm();
    }
}
