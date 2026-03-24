package com.digitallib.manager.index;

import com.digitallib.exception.EntityNotFoundException;
import com.digitallib.exception.RepositoryException;
import com.digitallib.manager.EntityManager;
import com.digitallib.model.Documento;
import com.digitallib.model.IndexElement;
import com.digitallib.model.entity.Entity;
import com.digitallib.model.indexes.Index;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.digitallib.JsonGenerator.GenerateJsonFromDoc;

public class DocByEntityIndexManager extends IndexManager<Entity> {

    public final String SUB_REPO = "/docByEntity";
    private static final Logger logger = LogManager.getLogger(DocByEntityIndexManager.class);
    Map<String, Entity> entityMap = new HashMap<>();

    public void updateIndex(List documento) throws RepositoryException {
        try {
            Map<String, List<IndexElement>> docByIndex = getResumeIndexTypeList(documento);
            for (Map.Entry<String, List<IndexElement>> entry : docByIndex.entrySet()) {
                String jsonText = GenerateJsonFromDoc(new Index(entry.getValue()));
                Files.createDirectories(getRepoPath(REPO));
                saveFiles(getIndexName(entityMap.get(entry.getKey())), jsonText);
            }
        } catch (IOException e) {
            throw new RepositoryException("Failed to update DocByEntity index", e);
        }
    }

    @Override
    protected String getSubRepoFolder() {
        return SUB_REPO;
    }

    List<Entity> getEntityListFromDocument(Documento documento) throws RepositoryException {
        if (documento.getCitacoes() == null) return new ArrayList<>();
        List<Entity> result = new ArrayList<>();
        for (String id : documento.getCitacoes()) {
            try {
                result.add(EntityManager.getEntryById(id));
            } catch (EntityNotFoundException e) {
                throw new RepositoryException("Entity not found while building index: " + id, e);
            }
        }
        return result;
    }

    @Override
    protected String getIndexName(Entity indexObj) {
        return indexObj.getId();
    }

    Map<String, List<IndexElement>> getResumeIndexTypeList(List<Documento> documentos) throws RepositoryException {
        Map<String, List<IndexElement>> resumes = new HashMap<>();
        for (Documento documento : documentos) {
            List<Entity> entities = getEntityListFromDocument(documento);
            for (Entity o : entities) {
                if (!resumes.containsKey(o.getId())) {
                    resumes.put(o.getId(), new ArrayList<>());
                    entityMap.put(o.getId(), o);
                }
                resumes.get(o.getId()).add(new IndexElement(documento.getTitulo(), documento.getCodigo()));
            }
        }
        return resumes;
    }
}
