package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class AnoPubliReferenceBlockBuilder extends BasicReferenceBlock{

    public AnoPubliReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getAnoVolume() != null) {
            return String.format("ano %s", doc.getAnoVolume());
        } else {
            return "";
        }
    }
}
