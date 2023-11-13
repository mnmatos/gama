package com.digitallib.model;

public class DataDocumento {
    Integer dia;
    Integer mes;
    String ano;

    boolean dataIncerta = false;

    public Integer getDia() {
        return dia;
    }

    public void setDia(Integer dia) {
        this.dia = dia;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
        this.mes = mes;
    }

    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }

    public boolean isDataIncerta() {
        return dataIncerta;
    }

    public void setDataIncerta(boolean dataIncerta) {
        this.dataIncerta = dataIncerta;
    }
}
