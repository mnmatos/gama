package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class PagesReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public PagesReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getPaginaInicio() == null || doc.getPaginaInicio() == 0) return "";
        if(doc.getNumPagina() == null || doc.getNumPagina() <= 1){
            return String.format("p. %d", doc.getPaginaInicio());
        } else {
            return String.format("p. %d-%d", doc.getPaginaInicio(), doc.getPaginaInicio()+doc.getNumPagina()-1);
        }
    }
}
