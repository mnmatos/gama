package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class VolumeReferenceBlockBuilder extends BasicReferenceBlockBuilder {

    public VolumeReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        if(doc.getVolume() != null && !doc.getVolume().isEmpty()) {
            return String.format("v. %s", doc.getVolume());
        } else {
            return "";
        }
    }
}
