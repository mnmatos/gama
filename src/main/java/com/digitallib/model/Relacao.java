package com.digitallib.model;

import com.digitallib.manager.RepositoryManager;

import java.util.List;

public class Relacao {
    String codDocumento;
    TIPO_RELACAO tipoRelacao;

    public Relacao() {
    }

    public Relacao(String codDocumento, TIPO_RELACAO tipoRelacao) {
        this.codDocumento = codDocumento;
        this.tipoRelacao = tipoRelacao;
    }

    public String getCodDocumento() {
        return codDocumento;
    }

    public void setCodDocumento(String codDocumento) {
        this.codDocumento = codDocumento;
    }

    public TIPO_RELACAO getTipoRelacao() {
        return tipoRelacao;
    }

    public void setTipoRelacao(TIPO_RELACAO tipoRelacao) {
        this.tipoRelacao = tipoRelacao;
    }

    @Override
    public String toString() {
        List<Documento> docs = RepositoryManager.getEntriesByCodigo(codDocumento);
        String docName;
        if (docs.size() == 0) docName = codDocumento;
        else {
            docName = docs.get(0).getTitulo();
        }
        return String.format("%s %s", tipoRelacao.name, docName);
    }
}
