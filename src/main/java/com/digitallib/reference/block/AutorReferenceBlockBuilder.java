package com.digitallib.reference.block;

import com.digitallib.model.Documento;

public class AutorReferenceBlockBuilder implements ReferenceBlockBuilder {
    @Override
    public ReferenceBlock build(Documento doc) {
        String name;
        if(doc.getAutor() == null){
            name = "Alcina Gomes Dantas"; //TODO: load form config
        } else {
            name = doc.getAutor();

        }
        String[] splitName = name.split(" ");
        return new ReferenceBlock(String.format("%s, %s. ", splitName[splitName.length-1].toUpperCase(), splitName[0]), false, false);
    }
}
