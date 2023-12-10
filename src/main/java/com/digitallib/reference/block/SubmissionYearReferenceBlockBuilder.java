package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class SubmissionYearReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public SubmissionYearReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getInfoAdicionais() == null) return "";
        String anoSub = doc.getInfoAdicionais().getAnoSubmissao();
        if(anoSub == null || anoSub.isEmpty()) return "";
        return anoSub;
    }
}
