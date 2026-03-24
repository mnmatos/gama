package com.digitallib.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Project {
    private String name;
    private String path;
    private String acervo;

    @JsonProperty("code_type")
    private String codeType;

    // Jackson requires a no-arg constructor for deserialization
    public Project() {
    }

    public Project(String name, String path, String acervo, String codeType) {
        this.name = name;
        this.path = path;
        this.acervo = acervo;
        this.codeType = codeType;
    }

    public Project(String name, String path, String acervo) {
        this(name, path, acervo, "custom");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAcervo() {
        return acervo;
    }

    public void setAcervo(String acervo) {
        this.acervo = acervo;
    }

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }

    @Override
    public String toString() {
        return name;
    }
}
