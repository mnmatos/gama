package com.digitallib.model.ui;

import com.digitallib.model.Documento;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Filter {

    String name;
    Predicate<Documento> predicate;

    public Filter(String name, Predicate<Documento> predicate) {
        this.name = name;
        this.predicate = predicate;
    }

    public List<Documento> filter (List<Documento> list){
       return list.stream().filter(getPredicate()).collect(Collectors.toList());
    }

    public String getName() {
        return name;
    }

    public Predicate<Documento> getPredicate() {
        return predicate;
    }
}
