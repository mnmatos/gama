package com.digitallib.manager.index;

import com.digitallib.model.IndexElement;
import com.digitallib.model.Documento;
import com.digitallib.model.SubClasseProducao;
import com.digitallib.model.indexes.Index;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.digitallib.JsonGenerator.GenerateJsonFromDoc;

public class SubClassIndexManager extends IndexManager<SubClasseProducao> {

    public final String SUB_REPO = "/subClass";

    public void updateIndex(List documento){
        try {
            Map<SubClasseProducao, List<IndexElement>> docByIndex = getResumeIndexTypeList(documento);
            for (Map.Entry<SubClasseProducao, List<IndexElement>> entry : docByIndex.entrySet()){
                String jsonText = GenerateJsonFromDoc(new Index(entry.getValue()));
                Files.createDirectories(getRepoPath(REPO));
                saveFiles(getIndexName(entry.getKey()), jsonText);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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

    Map<SubClasseProducao, List<IndexElement>> getResumeIndexTypeList(List<Documento> documentos) {
        Map<SubClasseProducao, List<IndexElement>> resumes = new HashMap<>();
        documentos.stream().forEach(documento -> {
            List<SubClasseProducao> indexes = getIndexKeyFromDocument(documento);
            indexes.forEach(o -> {
                if (!resumes.containsKey(o))
                    resumes.put(o, new ArrayList<>());
                resumes.get(o).add(new IndexElement(documento.getTitulo(), documento.getCodigo()));
            });
        });
        return resumes;
    }
}
