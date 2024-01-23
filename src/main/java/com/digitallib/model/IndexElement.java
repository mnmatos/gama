package com.digitallib.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IndexElement {
    @JsonProperty("titulo")
    String titulo;

    @JsonProperty("codigo")
    String codigo;

    public IndexElement() {
    }

    public IndexElement(String titulo, String codigo) {
        this.titulo = titulo;
        this.codigo = codigo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}