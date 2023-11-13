package com.digitallib.exporter.docx;

import com.digitallib.model.Documento;
import com.digitallib.model.TipoNBR;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

public class TableContentBuilder {

    XWPFTable table;

    public TableContentBuilder(XWPFTable table) {
        this.table = table;
        XWPFTableRow tableRowOne = table.getRow(0);
        tableRowOne.getCell(0).setText("Quantidade de documentos");
        tableRowOne.addNewTableCell().setText("Referência");
        tableRowOne.addNewTableCell().setText("Instituição");
        tableRowOne.addNewTableCell().setText("Código");
    }

    TableContentBuilder addDocument(Documento doc) {
        XWPFTableRow tableRow = table.createRow();
        tableRow.getCell(0).setText(String.valueOf(doc.getArquivos() != null? doc.getArquivos().size() : 0));
        TipoNBR tipoNbr = doc.getTipoNbr() == null ? TipoNBR.JORNAL : doc.getTipoNbr();
        tableRow.getCell(1).setText(tipoNbr.getReferenceGenerator().generate(doc).toString());
        tableRow.getCell(2).setText(doc.getEncontradoEm());
        tableRow.getCell(3).setText(doc.getCodigo());
        return this;
    }
}
