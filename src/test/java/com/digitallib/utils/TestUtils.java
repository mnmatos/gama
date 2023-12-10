package com.digitallib.utils;

import com.digitallib.exception.EntityNotFoundException;
import com.digitallib.manager.EntityManager;
import com.digitallib.model.entity.Entity;
import com.digitallib.model.entity.EntityType;

public class TestUtils {

    public static Entity registerAuthor(String name) {
        Entity author = null;
        try {
            author = EntityManager.getEntryById("-999");
            if(author.getName() != name) {
                author = new Entity(EntityType.PESSOA, name, "for unit test", "-999");
                EntityManager.updateEntry(author);
            }
        } catch (EntityNotFoundException e) {
            author = new Entity(EntityType.PESSOA, name, "for unit test", "-999");
            EntityManager.addEntry(author);
        }
        return author;
    }
}
