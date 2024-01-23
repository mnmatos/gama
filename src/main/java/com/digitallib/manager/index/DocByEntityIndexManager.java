package com.digitallib.manager.index;

import com.digitallib.exception.EntityNotFoundException;
import com.digitallib.manager.EntityManager;
import com.digitallib.model.Documento;
import com.digitallib.model.IndexElement;
import com.digitallib.model.entity.Entity;
import com.digitallib.model.indexes.Index;

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
    Map<String, Entity> entityMap = new HashMap<>();

    public void updateIndex(List documento){
        try {
            Map<String, List<IndexElement>> docByIndex = getResumeIndexTypeList(documento);
            for (Map.Entry<String, List<IndexElement>> entry : docByIndex.entrySet()){
                String jsonText = GenerateJsonFromDoc(new Index(entry.getValue()));
                Files.createDirectories(getRepoPath(REPO));
                saveFiles(getIndexName(entityMap.get(entry.getKey())), jsonText);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getSubRepoFolder() {
        return SUB_REPO;
    }
    List<Entity> getEntityListFromDocument(Documento documento) {
        return documento.getCitacoes().stream().map(id -> {
            try {
                return EntityManager.getEntryById(id);
            } catch (EntityNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());
    }

    @Override
    protected String getIndexName(Entity indexObj) {
        return indexObj.getId();
    }

    Map<String, List<IndexElement>> getResumeIndexTypeList(List<Documento> documentos) {
        Map<String, List<IndexElement>> resumes = new HashMap<>();
        documentos.stream().forEach(documento -> {
            List<Entity> entities = getEntityListFromDocument(documento);
            entities.forEach(o -> {
                if (!resumes.containsKey(o.getId())) {
                    resumes.put(o.getId(), new ArrayList<>());
                    entityMap.put(o.getId(), o);
                }

                resumes.get(o.getId()).add(new IndexElement(documento.getTitulo(), documento.getCodigo()));
            });
        });
        return resumes;
    }
}
