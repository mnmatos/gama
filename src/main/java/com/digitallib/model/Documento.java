package com.digitallib.model;

import com.digitallib.serialization.ClasseDeserializer;
import com.digitallib.serialization.ClasseSerializer;
import com.digitallib.serialization.SubClasseDeserializer;
import com.digitallib.serialization.SubClasseSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDate;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Documento {

    @JsonProperty("classe_producao")
    @JsonSerialize(using = ClasseSerializer.class, as=Classe.class)
    @JsonDeserialize(using = ClasseDeserializer.class, as=Classe.class)
    Classe classeProducao;

    @JsonProperty("subclasse_producao")
    @JsonSerialize(using = SubClasseSerializer.class, as=SubClasse.class)
    @JsonDeserialize(using = SubClasseDeserializer.class, as=SubClasse.class)
    SubClasse subClasseProducao;

    @JsonProperty("autores")
    List<String> autores;

    @JsonProperty("titulo")
    String titulo;

    @JsonProperty("sub_titulo")
    String subtitulo;

    @JsonProperty("codigo")
    String codigo;

    @JsonProperty("transcricao")
    String transcricao;

    @JsonProperty("descricao")
    String descricao;

    @JsonProperty("inedito")
    boolean inedito;

    @JsonProperty("lugar_publicacao")
    String lugarPublicacao;

    @JsonProperty("encontrado_em")
    String encontradoEm;

    @JsonProperty("instituicao_custodia")
    String instituicaoCustodia;

    @JsonProperty("data")
    LocalDate data;

   @JsonProperty("data_documento")
   DataDocumento dataDocumento;

    @JsonProperty("data_aproximada")
    String dataAproximada;

    @JsonProperty("titulo_publicacao")
    String tituloPublicacao;

    @JsonProperty("subtitulo_publicacao")
    String subtituloPublicacao;

    @JsonProperty("tipo_nbr")
    TipoNBR tipoNbr;

    @JsonProperty("autores_publicacao")
    List<String> autoresPubli;

    @JsonProperty("num_publicacao")
    Integer numPublicacao;

    @JsonProperty("edicao")
    Integer edicao;

    @JsonProperty("editora")
    String editora;


    @JsonProperty("ano")
    String ano;

    @JsonProperty("volume")
    String volume;

    @JsonProperty("pagina_inicio")
    Integer paginaInicio;

    @JsonProperty("num_pagina")
    Integer numPagina;

    @JsonProperty("coluna")
    Integer coluna;

    @JsonProperty("disponivel_em")
    String disponivelEm;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    @JsonProperty("acesso_em")
    LocalDate acessoEm;

    @JsonProperty("danos_suporte")
    boolean danosSuporte;

    @JsonProperty("citacoes")
    List<String> citacoes;

    @JsonProperty("arquivos")
    List<String> arquivos;

    @JsonProperty("info_adicionais")
    InfoAdicionais infoAdicionais;

    @JsonProperty("texto_teatral")
    TextoTeatral textoTeatro;

    public Classe getClasseProducao() {
        return classeProducao;
    }

    public void setClasseProducao(Classe classeProducao) {
        this.classeProducao = classeProducao;
    }

    public SubClasse getSubClasseProducao() {
        return subClasseProducao;
    }

    public void setSubClasseProducao(SubClasse subClasseProducao) {
        this.subClasseProducao = subClasseProducao;
    }

    public String getTranscricao() {
        return transcricao;
    }

    public void setTranscricao(String transcricao) {
        this.transcricao = transcricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public TipoNBR getTipoNbr() {
        return tipoNbr;
    }

    public void setTipoNbr(TipoNBR tipoNbr) {
        this.tipoNbr = tipoNbr;
    }

    public List<String> getAutoresPubli() {
        return autoresPubli;
    }

    public void setAutoresPubli(List<String> autoresPubli) {
        this.autoresPubli = autoresPubli;
    }

    public String getLugarPublicacao() {
        return lugarPublicacao;
    }

    public void setLugarPublicacao(String lugarPublicacao) {
        this.lugarPublicacao = lugarPublicacao;
    }

    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public List<String> getAutores() {
        return autores;
    }

    public void setAutores(List<String> autores) {
        this.autores = autores;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public boolean isInedito() {
        return inedito;
    }

    public void setInedito(boolean inedito) {
        this.inedito = inedito;
    }

    public String getEncontradoEm() {
        return encontradoEm;
    }

    public void setEncontradoEm(String encontradoEm) {
        this.encontradoEm = encontradoEm;
    }

    public String getInstituicaoCustodia() {
        return instituicaoCustodia;
    }

    public void setInstituicaoCustodia(String instituicaoCustodia) {
        this.instituicaoCustodia = instituicaoCustodia;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getDataAproximada() {
        return dataAproximada;
    }

    public void setDataAproximada(String dataAproximada) {
        this.dataAproximada = dataAproximada;
    }

    public Integer getNumPublicacao() {
        return numPublicacao;
    }

    public void setNumPublicacao(Integer numPublicacao) {
        this.numPublicacao = numPublicacao;
    }

    public Integer getPaginaInicio() {
        return paginaInicio;
    }

    public void setPaginaInicio(Integer paginaInicio) {
        this.paginaInicio = paginaInicio;
    }

    public Integer getNumPagina() {
        return numPagina;
    }

    public void setNumPagina(Integer numPagina) {
        this.numPagina = numPagina;
    }

    public Integer getColuna() {
        return coluna;
    }

    public void setColuna(Integer coluna) {
        this.coluna = coluna;
    }

    public String getTituloPublicacao() {
        return tituloPublicacao;
    }

    public void setTituloPublicacao(String tituloPublicacao) {
        this.tituloPublicacao = tituloPublicacao;
    }

    public String getSubtituloPublicacao() {
        return subtituloPublicacao;
    }

    public void setSubtituloPublicacao(String subtituloPublicacao) {
        this.subtituloPublicacao = subtituloPublicacao;
    }

    public Integer getEdicao() {
        return edicao;
    }

    public void setEdicao(Integer edicao) {
        this.edicao = edicao;
    }

    public String getEditora() {
        return editora;
    }

    public void setEditora(String editora) {
        this.editora = editora;
    }

    public String getDisponivelEm() {
        return disponivelEm;
    }

    public void setDisponivelEm(String disponivelEm) {
        this.disponivelEm = disponivelEm;
    }

    public LocalDate getAcessoEm() {
        return acessoEm;
    }

    public void setAcessoEm(LocalDate acessoEm) {
        this.acessoEm = acessoEm;
    }

    public boolean isDanosSuporte() {
        return danosSuporte;
    }

    public void setDanosSuporte(boolean danosSuporte) {
        this.danosSuporte = danosSuporte;
    }

    public List<String> getCitacoes() {
        return citacoes;
    }

    public void setCitacoes(List<String> citacoes) {
        this.citacoes = citacoes;
    }

    public DataDocumento getDataDocumento() {
        return dataDocumento;
    }

    public void setDataDocumento(DataDocumento dataDocumento) {
        this.dataDocumento = dataDocumento;
    }

    public List<String> getArquivos() {
        return arquivos;
    }

    public void setArquivos(List<String> arquivos) {
        this.arquivos = arquivos;
    }

    public InfoAdicionais getInfoAdicionais() {
        return infoAdicionais;
    }

    public void setInfoAdicionais(InfoAdicionais infoAdicionais) {
        this.infoAdicionais = infoAdicionais;
    }

    public TextoTeatral getTextoTeatro() {
        return textoTeatro;
    }

    public void setTextoTeatro(TextoTeatral textoTeatro) {
        this.textoTeatro = textoTeatro;
    }

    public static boolean isInteger(String str) {
        return str.matches("\\d+");
    }
}
