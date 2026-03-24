package com.digitallib.utils;

import com.digitallib.exception.EntityNotFoundException;
import com.digitallib.exception.RepositoryException;
import com.digitallib.manager.EntityManager;
import com.digitallib.model.entity.Entity;
import com.digitallib.model.entity.EntityType;

import java.io.IOException;
import java.nio.file.Files;

public class TestUtils {

    private static void ensureProjectPathSet() {
        if (System.getProperty("selected.project.path") == null) {
            try {
                String tempDir = Files.createTempDirectory("gama-test-repo").toAbsolutePath().toString();
                System.setProperty("selected.project.path", tempDir);
            } catch (IOException e) {
                throw new RuntimeException("Failed to create temp directory for tests", e);
            }
        }
    }

    public static Entity registerAuthor(String name) {
        ensureProjectPathSet();
        Entity author = null;
        try {
            author = EntityManager.getEntryById("-999");
            if (author.getName() != name) {
                author = new Entity(EntityType.PESSOA, name, "for unit test", "-999");
                EntityManager.updateEntry(author);
            }
        } catch (EntityNotFoundException e) {
            try {
                author = new Entity(EntityType.PESSOA, name, "for unit test", "-999");
                EntityManager.addEntry(author);
            } catch (RepositoryException re) {
                throw new RuntimeException("Failed to register test author", re);
            }
        } catch (RepositoryException e) {
            throw new RuntimeException("Failed to register test author", e);
        }
        return author;
    }
}
