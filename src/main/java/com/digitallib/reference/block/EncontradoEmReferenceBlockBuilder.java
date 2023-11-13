package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class EncontradoEmReferenceBlockBuilder extends BasicReferenceBlock {

    public EncontradoEmReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        return doc.getEncontradoEm();
    }
}
