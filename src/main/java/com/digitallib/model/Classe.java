package com.digitallib.model;

import com.digitallib.utils.ConfigReader;

import java.util.*;

public class Classe {
    private String code;
    private String name;
    private String desc;
    private String format;
    private List<SubClasse> subclasses;

    public Classe() {
    }

    public Classe(String code, String name, String desc, List<SubClasse> subclasses) {
        this.code = code;
        this.name = name;
        this.desc = desc;
        this.subclasses = subclasses;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<SubClasse> getSubclasses() {
        return subclasses;
    }

    public void setSubclasses(List<SubClasse> subclasses) {
        this.subclasses = subclasses;
    }
}
