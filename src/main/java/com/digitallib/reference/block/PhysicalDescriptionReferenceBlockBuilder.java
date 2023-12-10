package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class PhysicalDescriptionReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public PhysicalDescriptionReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getInfoAdicionais() == null) return "";
        String desc = doc.getInfoAdicionais().getDescricaoFisica();
        if(desc == null || desc.isEmpty()) return "";
        return desc;
    }
}
