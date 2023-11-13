package com.digitallib.manager.index;

import com.digitallib.model.DocumentResume;
import com.digitallib.model.Documento;
import com.digitallib.model.indexes.Index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static com.digitallib.JsonGenerator.GenerateJsonFromDoc;

public abstract class IndexManager<O> {

    public final String REPO = "repo/index";

    public void updateIndex(List documento){
        try {
            Map<O, List<DocumentResume>> docByIndex = getResumeIndexTypeList(documento);
            for (Map.Entry<O, List<DocumentResume>> entry : docByIndex.entrySet()){
                String jsonText = GenerateJsonFromDoc(new Index(entry.getValue()));
                Files.createDirectories(getRepoPath(REPO));
                saveFiles(getIndexName(entry.getKey()), jsonText);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearIndex(O indexObj){
        try {
            Files.delete(getFilePath(getIndexName(indexObj)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void saveFiles(String indexName, String jsonText) throws IOException {
        Files.write(getFilePath(indexName), jsonText.getBytes());
    }

    Path getFilePath(String id) {
        return Paths.get(String.format("%s/%s/%s.json", REPO, getSubRepoFolder(), id));
    }

    Path getRepoPath(String id) {
        return Paths.get(String.format("%s/%s/", REPO, getSubRepoFolder()));
    }

    Map<O, List<DocumentResume>> getResumeIndexTypeList(List<Documento> documentos) {
        Map<O, List<DocumentResume>> resumes = new HashMap<>();
        documentos.stream().forEach(documento -> {
            List<O> indexes = getIndexKeyFromDocument(documento);
            indexes.forEach(o -> {
                if (!resumes.containsKey(o))
                    resumes.put(o, new ArrayList<>());
                resumes.get(o).add(new DocumentResume(documento));
            });
        });
        return resumes;
    }



    protected abstract String getSubRepoFolder();
    abstract List<O> getIndexKeyFromDocument(Documento documento);

    protected abstract String getIndexName(O indexObj);

}
