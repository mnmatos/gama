package com.digitallib.model.entity;

public class Entity {
    EntityType type;
    String name;

    String description;
    String id;

    public Entity() {
    }

    public Entity(EntityType type, String name, String description, String id) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.id = id;
    }

    public Entity(EntityType type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public EntityType getType() {
        return type;
    }

    public void setType(EntityType type) {
        this.type = type;
    }
}

