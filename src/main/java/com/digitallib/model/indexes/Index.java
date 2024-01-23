package com.digitallib.model.indexes;

import com.digitallib.model.IndexElement;

import java.util.ArrayList;
import java.util.List;

public class Index {

    List<IndexElement> documentos = new ArrayList<>();

    public Index() {
    }

    public Index(List<IndexElement> documentos) {
        this.documentos = documentos;
    }

    public List<IndexElement> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<IndexElement> documentos) {
        this.documentos = documentos;
    }
}
