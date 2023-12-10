package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class InterviewerReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public InterviewerReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getInfoAdicionais() == null) return "";
        String interviewer = doc.getInfoAdicionais().getEntrevistador();
        if(interviewer == null || interviewer.isEmpty()) return "";
        return String.format("[Entrevista cedida a] %s", interviewer);
    }
}
