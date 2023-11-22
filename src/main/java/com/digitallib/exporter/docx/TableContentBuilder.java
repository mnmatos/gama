package com.digitallib.exporter.docx;

import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.model.ClasseProducao;
import com.digitallib.model.Documento;
import com.digitallib.model.TipoNBR;
import com.digitallib.reference.Reference;
import com.digitallib.reference.block.ReferenceBlock;
import org.apache.poi.xwpf.usermodel.*;

import java.math.BigInteger;

public class TableContentBuilder {

    public static final String FONT_FAMILY = "Times New Roman";
    public static final int FONT_SIZE = 12;
    XWPFTable table;

    public TableContentBuilder(XWPFTable table) {
        this.table = table;
        XWPFTableRow tableRowOne = table.getRow(0);
        addTableHeader(tableRowOne);
    }

    private static void addTableHeader(XWPFTableRow tableRowOne) {
        writeWithBasicFont(tableRowOne.getCell(0), "Quantidade de documentos", 1);
        writeWithBasicFont(tableRowOne.addNewTableCell(), "Referência", 1);
        writeWithBasicFont(tableRowOne.addNewTableCell(), "Instituição", 1);
        writeWithBasicFont(tableRowOne.addNewTableCell(), "Código", 1);
    }


    public void addSection(ClasseProducao classe) {
        XWPFTableRow tableRow = table.createRow();
        XWPFTableCell cell = tableRow.getCell(0);
        if (cell.getCTTc().getTcPr() == null) cell.getCTTc().addNewTcPr();
        if (cell.getCTTc().getTcPr().getGridSpan() == null) cell.getCTTc().getTcPr().addNewGridSpan();
        cell.getCTTc().getTcPr().getGridSpan().setVal(BigInteger.valueOf(4));

        writeWithBasicFont(cell, classe.print(), 2);

        for (int i = 1; i < tableRow.getTableCells().size(); i++){
            tableRow.removeCell(1);
        }
    }

    TableContentBuilder addDocument(Documento doc) {
        XWPFTableRow tableRow = table.createRow();
        try {
            writeWithBasicFont(tableRow.getCell(0), String.valueOf(doc.getArquivos() != null ? doc.getArquivos().size() : 0), 1);
            TipoNBR tipoNbr = doc.getTipoNbr() == null ? TipoNBR.JORNAL : doc.getTipoNbr();
            setReferenceCell(doc, tableRow.getCell(1), tipoNbr);
            writeWithBasicFont(tableRow.getCell(2), doc.getEncontradoEm(), 1);
            writeWithBasicFont(tableRow.getCell(3), doc.getCodigo(), 1);
        } catch (ReferenceBlockBuilderException e) {
            table.removeRow(table.getNumberOfRows()-1);
            throw new RuntimeException(e);
        }
        return this;
    }

    private static void setReferenceCell(Documento doc, XWPFTableCell cell, TipoNBR tipoNbr) throws ReferenceBlockBuilderException {
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        paragraph.setFontAlignment(1);
        Reference reference = tipoNbr.getReferenceGenerator().generate(doc);

        for (ReferenceBlock block : reference.getReferenceBlocks()) {
            XWPFRun fieldRun = paragraph.createRun();
            fieldRun.setBold(block.isBold());
            fieldRun.setItalic(block.isItalic());
            setBasicFont(fieldRun);
            fieldRun.setText(block.getContent());
        }
    }

    private static void setBasicFont(XWPFRun fieldRun) {
        fieldRun.setFontSize(FONT_SIZE);
        fieldRun.setFontFamily(FONT_FAMILY);
    }

    private static void writeWithBasicFont(XWPFTableCell cell, String text, int align) {
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        paragraph.setFontAlignment(align);
        XWPFRun fieldRun = paragraph.createRun();
        setBasicFont(fieldRun);
        fieldRun.setText(text);
    }
}
