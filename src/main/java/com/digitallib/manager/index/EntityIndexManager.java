package com.digitallib.manager.index;

import com.digitallib.model.IndexElement;
import com.digitallib.model.entity.Entity;
import com.digitallib.model.entity.EntityType;
import com.digitallib.model.indexes.Index;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.digitallib.JsonGenerator.GenerateJsonFromDoc;

public class EntityIndexManager extends IndexManager<EntityType> {

    public final String SUB_REPO = "/entity";

    private Logger logger = LogManager.getLogger();
    @Override
    protected String getSubRepoFolder() {
        return SUB_REPO;
    }

    public void updateIndex(List<Entity> entities){
        try {
            Map<EntityType, List<IndexElement>> docByIndex = getResumeIndexTypeList(entities);
            for (Map.Entry<EntityType, List<IndexElement>> entry : docByIndex.entrySet()){
                String jsonText = GenerateJsonFromDoc(new Index(entry.getValue()));
                Files.createDirectories(getRepoPath(REPO));
                saveFiles(getIndexName(entry.getKey()), jsonText);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    Map<EntityType, List<IndexElement>> getResumeIndexTypeList(List<Entity> entities) {
        Map<EntityType, List<IndexElement>> resumes = new HashMap<>();
        entities.stream().forEach(entity -> {
            if (!resumes.containsKey(entity.getType()))
                resumes.put(entity.getType(), new ArrayList<>());
            resumes.get(entity.getType()).add(new IndexElement(entity.getName(), entity.getId()));
        });
        return resumes;
    }

    @Override
    protected String getIndexName(EntityType entityType) {
        return entityType.name();
    }

}
