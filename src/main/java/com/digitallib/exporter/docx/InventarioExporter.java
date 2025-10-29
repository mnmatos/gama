package com.digitallib.exporter.docx;

import com.digitallib.exporter.BaseFileExporter;
import com.digitallib.manager.CategoryManager;
import com.digitallib.model.Classe;
import com.digitallib.model.Documento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

public class InventarioExporter extends BaseFileExporter {

    private Logger logger = LogManager.getLogger();

    CategoryManager categoryManager = new CategoryManager();

    public InventarioExporter() {
    }

    @Override
    public void export(List<Documento> docsToExport) {
        XWPFDocument document = new XWPFDocument();
        CTBody body = document.getDocument().getBody();

        configPage(body);
        TableContentBuilder tableContentBuilder = new TableContentBuilder(document.createTable());
        for (Classe classe : categoryManager.getClasses()){

            List<Documento> filteredDoc = docsToExport.stream().filter(doc -> doc.getClasseProducao().equals(classe)).collect(Collectors.toList());
            if(filteredDoc.size() > 0){
                tableContentBuilder.addSection(classe);
                for(Documento docToExport: filteredDoc) {
                    tableContentBuilder.addDocument(docToExport);
                }
            }
        }

        createFile(document, "inventário");
    }

    public static void configPage(CTBody body) {
        if (!body.isSetSectPr()) {
            body.addNewSectPr();
        }
        CTSectPr section = body.getSectPr();

        if(!section.isSetPgSz()) {
            section.addNewPgSz();
        }
        CTPageSz pageSize = section.getPgSz();

        pageSize.setOrient(STPageOrientation.LANDSCAPE);
        pageSize.setW(BigInteger.valueOf(15840));
        pageSize.setH(BigInteger.valueOf(12240));
    }
}
