package com.digitallib.manager.index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class IndexManager<O> {

    public final String REPO = "repo/index";

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

    protected abstract String getSubRepoFolder();

    protected abstract String getIndexName(O indexObj);

}
