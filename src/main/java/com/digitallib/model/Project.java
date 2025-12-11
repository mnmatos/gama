package com.digitallib.model;

public class Project {
    private String name;
    private String path;
    private String acervo;

    // Jackson requires a no-arg constructor for deserialization
    public Project() {
    }

    public Project(String name, String path, String acervo) {
        this.name = name;
        this.path = path;
        this.acervo = acervo;
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

    @Override
    public String toString() {
        return name;
    }
}
