package com.digitallib.exporter;

import com.digitallib.model.Documento;

import java.io.IOException;
import java.util.List;

public interface LibExporter {

    void export(List<Documento> doc) throws IOException;
}
