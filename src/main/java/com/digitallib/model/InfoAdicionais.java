package com.digitallib.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class InfoAdicionais {

    @JsonProperty("ano_submissao")
    String anoSubmissao;

    @JsonProperty("tipo_trabalho")
    String tipoTrabalho;

    @JsonProperty("grau")
    String grau;

    @JsonProperty("curso")
    String curso;

    @JsonProperty("doi")
    String doi;

    @JsonProperty("entrevistador")
    String entrevistador;

    @JsonProperty("publicado_por")
    String publicadoPor;

    @JsonProperty("destinatario")
    String destinatario;

    @JsonProperty("descricaoFisica")
    String descricaoFisica;

    @JsonProperty("info_complementares")
    String infoComplementares;

    public String getAnoSubmissao() {
        return anoSubmissao;
    }

    public void setAnoSubmissao(String anoSubmissao) {
        this.anoSubmissao = anoSubmissao;
    }

    public String getTipoTrabalho() {
        return tipoTrabalho;
    }

    public void setTipoTrabalho(String tipoTrabalho) {
        this.tipoTrabalho = tipoTrabalho;
    }

    public String getGrau() {
        return grau;
    }

    public void setGrau(String grau) {
        this.grau = grau;
    }

    public String getCurso() {
        return curso;
    }

    public void setCurso(String curso) {
        this.curso = curso;
    }

    public String getDoi() {
        return doi;
    }

    public void setDoi(String doi) {
        this.doi = doi;
    }

    public String getEntrevistador() {
        return entrevistador;
    }

    public void setEntrevistador(String entrevistador) {
        this.entrevistador = entrevistador;
    }

    public String getPublicadoPor() {
        return publicadoPor;
    }

    public void setPublicadoPor(String publicadoPor) {
        this.publicadoPor = publicadoPor;
    }

    public String getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(String destinatario) {
        this.destinatario = destinatario;
    }

    public String getDescricaoFisica() {
        return descricaoFisica;
    }

    public void setDescricaoFisica(String descricaoFisica) {
        this.descricaoFisica = descricaoFisica;
    }

    public String getInfoComplementares() {
        return infoComplementares;
    }

    public void setInfoComplementares(String infoComplementares) {
        this.infoComplementares = infoComplementares;
    }
}
