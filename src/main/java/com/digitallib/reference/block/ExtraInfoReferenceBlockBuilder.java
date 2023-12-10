package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class ExtraInfoReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public ExtraInfoReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getInfoAdicionais() == null) return "";
        String info = doc.getInfoAdicionais().getInfoComplementares();
        if(info == null || info.isEmpty()) return "";
        return info;
    }
}
