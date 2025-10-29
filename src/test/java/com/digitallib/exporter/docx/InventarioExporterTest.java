package com.digitallib.exporter.docx;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventarioExporterTest {

    @Test
    void testConfigPage() {
        XWPFDocument document = new XWPFDocument();
        CTBody body = document.getDocument().getBody();
        InventarioExporter.configPage(body);

        assertEquals(STPageOrientation.LANDSCAPE, body.getSectPr().getPgSz().getOrient());
        assertEquals(BigInteger.valueOf(15840), body.getSectPr().getPgSz().getW());
        assertEquals(BigInteger.valueOf(12240), body.getSectPr().getPgSz().getH());
    }
}