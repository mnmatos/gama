package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class EditorReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public EditorReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getEditora() == null || doc.getEditora().isEmpty()){
            italic = true;
            return "[s.n.]";
        }
        return doc.getEditora();
    }
}
