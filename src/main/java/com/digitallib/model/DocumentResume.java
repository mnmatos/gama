package com.digitallib.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DocumentResume {
    @JsonProperty("titulo")
    String titulo;

    @JsonProperty("codigo")
    String codigo;

    public DocumentResume() {
    }

    public DocumentResume(Documento doc) {
        this.titulo = doc.getTitulo();
        this.codigo = doc.getCodigo();
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