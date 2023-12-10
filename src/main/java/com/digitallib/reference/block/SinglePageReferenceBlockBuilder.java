package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class SinglePageReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public SinglePageReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getPaginaInicio() == null || doc.getPaginaInicio() == 0) return "";
        return String.format("p. %s", doc.getPaginaInicio());
    }
}
