package com.digitallib.model;

import com.digitallib.manager.RepositoryManager;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Documento {

    @JsonProperty("classe_producao")
    ClasseProducao classeProducao;

    @JsonProperty("subclasse_producao")
    SubClasseProducao subClasseProducao;
    @JsonProperty("autor")
    String autor;
    @JsonProperty("titulo")
    String titulo;

    @JsonProperty("codigo")
    String codigo;

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

    @JsonProperty("monografia")
    String monografia;

    @JsonProperty("tipo_nbr")
    TipoNBR tipoNbr;

    @JsonProperty("num_publicacao")
    Integer numPublicacao;

    @JsonProperty("ano_volume")
    String anoVolume;

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

    public ClasseProducao getClasseProducao() {
        return classeProducao;
    }

    public void setClasseProducao(ClasseProducao classeProducao) {
        this.classeProducao = classeProducao;
    }

    public SubClasseProducao getSubClasseProducao() {
        return subClasseProducao;
    }

    public void setSubClasseProducao(SubClasseProducao subClasseProducao) {
        this.subClasseProducao = subClasseProducao;
    }

    public TipoNBR getTipoNbr() {
        return tipoNbr;
    }

    public void setTipoNbr(TipoNBR tipoNbr) {
        this.tipoNbr = tipoNbr;
    }

    public String getLugarPublicacao() {
        return lugarPublicacao;
    }

    public void setLugarPublicacao(String lugarPublicacao) {
        this.lugarPublicacao = lugarPublicacao;
    }

    public String getAnoVolume() {
        return anoVolume;
    }

    public void setAnoVolume(String anoVolume) {
        this.anoVolume = anoVolume;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
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

    public String getMonografia() {
        return monografia;
    }

    public void setMonografia(String monografia) {
        this.monografia = monografia;
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

    public void generateCodigo() {
        codigo = generateCodigoWithoutAppendix();
        if (RepositoryManager.getDocCodeSet().contains(codigo)){
            for (int i = 1; i < 1000; i++){
                if(!RepositoryManager.getDocCodeSet().contains(codigo+String.format(".%03d", i))){
                    codigo += String.format(".%03d", i);
                    break;
                }
            }
        }
    }

    public String generateCodigoWithoutAppendix() {
        StringBuilder codigoBuilder = new StringBuilder();
        codigoBuilder.append("AAD."+this.subClasseProducao.getCode()).append(".");
        switch (this.classeProducao) {
            case PRODUCAO_INTELECTUAL:
            case MEMORABILIA:
            case RECEPCAO:
            case VIDA:
                codigoBuilder.append(getAcronimo(this.titulo, "ST")).append(".");
                codigoBuilder.append(getDateForCode()).append(".");
                codigoBuilder.append(getAcronimo(this.encontradoEm, "SL")).append(".");
                codigoBuilder.append(getAcronimo(this.instituicaoCustodia, "SL"));
                break;
            case DOCUMENTOS_AUDIOVISUAIS:
                codigoBuilder.append(getAcronimo(this.titulo, "ST")).append(".");
                codigoBuilder.append(getDateForCode()).append(".");
                codigoBuilder.append(getAcronimo(this.instituicaoCustodia, "SL"));
                break;
            case ESBOCOS_NOTAS:
                codigoBuilder.append(getAcronimo(this.titulo, "ST"));
        }
        return codigoBuilder.toString();
    }

    public static boolean isInteger(String str) {
        return str.matches("\\d+");
    }

    private String getDateForCode() {
        if(dataDocumento != null && dataDocumento.getAno()!=null && !dataDocumento.isDataIncerta()) {
            return String.valueOf(dataDocumento.getAno()).substring(2);
        } else {
            return "00";
        }
    }

    private String getAcronimo(String titulo, String defaultValue) {
        if(titulo == null || titulo.isEmpty()) return  defaultValue;

        else {
            HashSet<String> excluido = new HashSet<>(Arrays.asList("uma", "um", "me", "te","do", "dos", "no", "nos","da", "das", "na", "nas", "de", "o", "a", "e", "as", "os", "nas", "nos", "com", "que", "qual", "quais"));
            StringBuilder initials = new StringBuilder();
            titulo = titulo.replaceAll("[^A-Za-z0-9 ]","").replaceAll(" +", " ");
            for (String s : titulo.split(" ")) {
                if(!excluido.contains(s.toLowerCase())) {
                    if (isNumeric(s)){
                        initials.append(s);
                    }
                    else initials.append(new StringBuilder().append(s.charAt(0)).toString().toUpperCase());
                }
            }
            return initials.toString();
        }
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }
}
