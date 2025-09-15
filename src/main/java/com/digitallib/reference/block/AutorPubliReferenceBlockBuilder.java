package com.digitallib.reference.block;

import com.digitallib.exception.EntityNotFoundException;
import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.manager.EntityManager;
import com.digitallib.model.Documento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

public class AutorPubliReferenceBlockBuilder extends AutorReferenceBlockBuilder {

    @Override
    public List<ReferenceBlock> build(Documento doc) throws ReferenceBlockBuilderException {
        try {
            List<String> authors = doc.getAutoresPubli();
            return getReferenceBlocksForAuthor(authors);
        } catch (EntityNotFoundException e){
            logger.error(e);
            throw new ReferenceBlockBuilderException(e.getMessage());
        }
    }
}
