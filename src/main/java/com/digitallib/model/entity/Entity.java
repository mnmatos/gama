package com.digitallib.model.entity;

import java.time.LocalDate;

public class Entity {
    EntityType type;
    String name;

    String description;
    String id;

    // Campos específicos para Pessoa (Autor e Pessoa consolidados)
    String genero;
    LocalDate dataNascimento;
    LocalDate dataObito;
    String localNascimento;

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

    // Getters e setters para campos de Pessoa
    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public LocalDate getDataObito() {
        return dataObito;
    }

    public void setDataObito(LocalDate dataObito) {
        this.dataObito = dataObito;
    }

    public String getLocalNascimento() {
        return localNascimento;
    }

    public void setLocalNascimento(String localNascimento) {
        this.localNascimento = localNascimento;
    }
}

