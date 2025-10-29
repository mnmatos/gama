package com.digitallib.exporter.docx;

import com.digitallib.model.Classe;
import com.digitallib.model.Documento;
import com.digitallib.model.TipoNBR;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TableContentBuilderTest {

    private TableContentBuilder tableContentBuilder;
    private XWPFTable table;

    @BeforeEach
    void setUp() {
        XWPFDocument document = new XWPFDocument();
        table = document.createTable();
        tableContentBuilder = new TableContentBuilder(table);
    }

    @Test
    void testAddSection() {
        Classe classe = new Classe("C1", "Classe 1", "Desc 1", Collections.emptyList());
        tableContentBuilder.addSection(classe);
        assertEquals("Desc 1", table.getRow(1).getCell(0).getText());
    }

    @Test
    void testAddDocument() {
        Documento doc = new Documento();
        doc.setCodigo("D1");
        doc.setEncontradoEm("Inst 1");
        doc.setTipoNbr(TipoNBR.JORNAL);
        tableContentBuilder.addDocument(doc);
        assertNotNull(table.getRow(1));
    }
}