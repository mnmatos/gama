package com.digitallib.model;

public class Project {
    private String name;
    private String path;
    private String acervo;
    private String code_type;

    // Jackson requires a no-arg constructor for deserialization
    public Project() {
    }

    public Project(String name, String path, String acervo, String code_type) {
        this.name = name;
        this.path = path;
        this.acervo = acervo;
        this.code_type = code_type;
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

    public String getCode_type() {
        return code_type;
    }

    public void setCode_type(String code_type) {
        this.code_type = code_type;
    }

    @Override
    public String toString() {
        return name;
    }
}
