package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class PagPubliReferenceBlockBuilder extends BasicReferenceBlock{

    public PagPubliReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        return String.format("p. %s", doc.getPaginaInicio());
    }
}
