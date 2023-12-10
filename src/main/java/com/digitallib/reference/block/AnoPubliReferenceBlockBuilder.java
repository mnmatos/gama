package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class AnoPubliReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public AnoPubliReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getAno() != null && doc.getAno() != "") {
            return String.format("ano %s", doc.getAno());
        } else {
            return "";
        }
    }
}
