package com.digitallib.model;

public enum TIPO_RELACAO {
    ESTUDADO_EM ("Estudado em");

    String name;

    TIPO_RELACAO(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
