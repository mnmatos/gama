package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class PublisherReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public PublisherReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getInfoAdicionais() == null) return "";
        String publisher = doc.getInfoAdicionais().getPublicadoPor();
        if(publisher == null || publisher.isEmpty()) return "";
        return String.format("Publicado pelo canal %s", publisher);
    }
}
