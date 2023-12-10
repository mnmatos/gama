package com.digitallib.reference.block;

import com.digitallib.exception.EntityNotFoundException;
import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.manager.EntityManager;
import com.digitallib.model.Documento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

public class AutorReferenceBlockBuilder implements ReferenceBlockBuilder {

    protected Logger logger = LogManager.getLogger();
    @Override
    public List<ReferenceBlock> build(Documento doc) throws ReferenceBlockBuilderException {
        try {
            List<String> authors = doc.getAutores();
            return getReferenceBlocksForAuthor(authors, doc);
        } catch (EntityNotFoundException e){
            logger.error(e);
            throw new ReferenceBlockBuilderException(e.getMessage());
        }
    }

    protected List<ReferenceBlock> getReferenceBlocksForAuthor(List<String> authors, Documento doc) throws EntityNotFoundException {
        StringBuilder nameBuilder = new StringBuilder();
        if (authors != null && authors.size() > 0) {
            switch (authors.size()) {
                case 1:
                    nameBuilder.append(getFormattedName(EntityManager.getEntryById(authors.get(0)).getName()));
                    nameBuilder.append(". ");
                    break;
                case 2:
                    nameBuilder.append(getFormattedName(EntityManager.getEntryById(authors.get(0)).getName()));
                    nameBuilder.append("; ");
                    nameBuilder.append(getFormattedName(EntityManager.getEntryById(authors.get(1)).getName()));
                    nameBuilder.append(". ");
                    break;
                default:
                    nameBuilder.append(getFormattedName(EntityManager.getEntryById(authors.get(0)).getName()));
                    nameBuilder.append(". ");
                    return List.of(new ReferenceBlock(nameBuilder.toString(), false, false),
                            new ReferenceBlock("et al. ", false, true));
            }
        }
        return Collections.singletonList(new ReferenceBlock(nameBuilder.toString(), false, false));
    }

    private String getFormattedName(String name) {
        String[] splitName = name.split(" ");
        if(splitName.length==1) return name.toUpperCase();
        return String.format("%s, %s", splitName[splitName.length-1].toUpperCase(), splitName[0]);
    }

}
