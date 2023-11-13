package com.digitallib.reference.block;

import com.digitallib.manager.EntityManager;
import com.digitallib.model.Documento;
import com.digitallib.model.entity.Entity;

public class LocalReferenceBlockBuilder extends BasicReferenceBlock {

    public LocalReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) {
        Entity entity = EntityManager.getEntryById(doc.getLugarPublicacao());
        if (entity == null) return "SL";
        return entity.getName();
    }
}
