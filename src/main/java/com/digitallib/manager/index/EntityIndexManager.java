package com.digitallib.manager.index;

import com.digitallib.exception.EntityNotFoundException;
import com.digitallib.manager.EntityManager;
import com.digitallib.model.Documento;
import com.digitallib.model.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class EntityIndexManager extends IndexManager<Entity> {

    public final String SUB_REPO = "/entity";

    private Logger logger = LogManager.getLogger();
    @Override
    protected String getSubRepoFolder() {
        return SUB_REPO;
    }

    List<Entity> getIndexKeyFromDocument(Documento documento) {
        return documento.getCitacoes().stream().map(citation -> {
            try {
                return EntityManager.getEntryById(citation);
            } catch (EntityNotFoundException e) {
                logger.error(e);
                return null;
            }
        }).filter(entity -> entity!=null).collect(Collectors.toList());
    }

    @Override
    protected String getIndexName(Entity indexObj) {
        return indexObj.getId();
    }

}
