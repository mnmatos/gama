package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class NumPubliReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public NumPubliReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getNumPublicacao() == null || doc.getNumPublicacao() == 0) return "";
        return String.format("n. %s", doc.getNumPublicacao());
    }
}
