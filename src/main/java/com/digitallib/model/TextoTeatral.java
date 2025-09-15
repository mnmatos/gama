package com.digitallib.model;

import java.util.List;

public class TextoTeatral {
    int quantidadePersonagem;
    int atos;
    int cenas;
    String resumo;

    List<String> palavrasChave;

    public int getQuantidadePersonagem() {
        return quantidadePersonagem;
    }

    public void setQuantidadePersonagem(int quantidadePersonagem) {
        this.quantidadePersonagem = quantidadePersonagem;
    }

    public int getAtos() {
        return atos;
    }

    public void setAtos(int atos) {
        this.atos = atos;
    }

    public int getCenas() {
        return cenas;
    }

    public void setCenas(int cenas) {
        this.cenas = cenas;
    }

    public String getResumo() {
        return resumo;
    }

    public void setResumo(String resumo) {
        this.resumo = resumo;
    }

    public List<String> getPalavrasChave() {
        return palavrasChave;
    }

    public void setPalavrasChave(List<String> palavrasChave) {
        this.palavrasChave = palavrasChave;
    }
}
