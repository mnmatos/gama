package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class NumPubliReferenceBlockBuilder extends BasicReferenceBlock{

    public NumPubliReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        return String.format("n. %s", doc.getNumPublicacao());
    }
}
