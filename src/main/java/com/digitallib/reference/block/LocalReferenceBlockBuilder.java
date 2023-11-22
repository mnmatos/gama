package com.digitallib.reference.block;

import com.digitallib.exception.EntityNotFoundException;
import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.manager.EntityManager;
import com.digitallib.model.Documento;
import com.digitallib.model.entity.Entity;

public class LocalReferenceBlockBuilder extends BasicReferenceBlock {

    public LocalReferenceBlockBuilder(boolean bold, boolean italic, String separator) {
        super(bold, italic, separator);
    }

    @Override
    protected String getContent(Documento doc) throws ReferenceBlockBuilderException {
        Entity entity = null;
        try {
            entity = EntityManager.getEntryById(doc.getLugarPublicacao());
        } catch (EntityNotFoundException e) {
            throw new ReferenceBlockBuilderException(e.getMessage());
        }
        if (entity == null) return "SL";
        return entity.getName();
    }
}
