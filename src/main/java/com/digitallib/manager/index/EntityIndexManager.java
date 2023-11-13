package com.digitallib.manager.index;

import com.digitallib.manager.EntityManager;
import com.digitallib.model.Documento;
import com.digitallib.model.entity.Entity;

import java.util.List;
import java.util.stream.Collectors;

public class EntityIndexManager extends IndexManager<Entity> {

    public final String SUB_REPO = "/entity";

    @Override
    protected String getSubRepoFolder() {
        return SUB_REPO;
    }

    List<Entity> getIndexKeyFromDocument(Documento documento) {
        return documento.getCitacoes().stream().map(citation -> EntityManager.getEntryById(citation)).collect(Collectors.toList());
    }

    @Override
    protected String getIndexName(Entity indexObj) {
        return indexObj.getId();
    }

}
