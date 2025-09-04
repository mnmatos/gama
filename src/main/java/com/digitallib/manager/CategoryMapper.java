package com.digitallib.manager;

import com.digitallib.model.Classe;

import java.util.List;

public class CategoryMapper {

    private List<Classe> classes;

    public CategoryMapper() {
    }

    public CategoryMapper(List<Classe> classes) {
        this.classes = classes;
    }

    public List<Classe> getClasses() {
        return classes;
    }

    public void setClasses(List<Classe> classes) {
        this.classes = classes;
    }
}
