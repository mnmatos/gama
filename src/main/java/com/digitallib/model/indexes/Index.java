package com.digitallib.model.indexes;

import com.digitallib.model.DocumentResume;

import java.util.ArrayList;
import java.util.List;

public class Index {

    List<DocumentResume> documentos = new ArrayList<>();

    public Index() {
    }

    public Index(List<DocumentResume> documentos) {
        this.documentos = documentos;
    }

    public List<DocumentResume> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(List<DocumentResume> documentos) {
        this.documentos = documentos;
    }
}
