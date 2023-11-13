package com.digitallib.manager.index;

import com.digitallib.model.Documento;
import com.digitallib.model.SubClasseProducao;

import java.util.List;

public class SubClassIndexManager extends IndexManager<SubClasseProducao> {

    public final String SUB_REPO = "/subClass";

    @Override
    protected String getSubRepoFolder() {
        return SUB_REPO;
    }
    List<SubClasseProducao> getIndexKeyFromDocument(Documento documento) {
        return List.of(documento.getSubClasseProducao());
    }

    @Override
    protected String getIndexName(SubClasseProducao indexObj) {
        return indexObj.getCode();
    }

}
