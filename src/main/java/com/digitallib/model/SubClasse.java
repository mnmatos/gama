package com.digitallib.model;

import java.util.ArrayList;
import java.util.List;

public class SubClasse {
    private String code;
    private String name;
    private String desc;

    private List<String> extensions;

    public SubClasse() {
    }

    public SubClasse(String code, String name, String desc, List<String> extensions) {
        this.code = code;
        this.name = name;
        this.desc = desc;
        this.extensions = extensions;
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

    public List<String> getExtensions() {
        if (extensions == null) return new ArrayList<>();
        return extensions;
    }

    public void setExtensions(List<String> extensions) {
        this.extensions = extensions;
    }
}
