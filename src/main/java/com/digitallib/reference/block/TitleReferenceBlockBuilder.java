package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class TitleReferenceBlockBuilder extends BasicReferenceBlock{

    public TitleReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        return doc.getTitulo();
    }
}
