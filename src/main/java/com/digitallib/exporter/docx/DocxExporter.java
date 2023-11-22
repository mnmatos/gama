package com.digitallib.exporter.docx;

import com.digitallib.exporter.LibExporter;
import com.digitallib.model.ClasseProducao;
import com.digitallib.model.Documento;
import com.digitallib.model.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class DocxExporter implements LibExporter {

    private Logger logger = LogManager.getLogger();
    private String fileName;

    public DocxExporter(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void export(List<Documento> docsToExport) throws IOException {
        XWPFDocument document = new XWPFDocument();
        CTBody body = document.getDocument().getBody();

        configPage(body);
        TableContentBuilder tableContentBuilder = new TableContentBuilder(document.createTable());
        for (ClasseProducao classe : ClasseProducao.values()){

            List<Documento> filteredDoc = docsToExport.stream().filter(doc -> doc.getClasseProducao().equals(classe)).collect(Collectors.toList());
            if(filteredDoc.size() > 0){
                tableContentBuilder.addSection(classe);
                for(Documento docToExport: filteredDoc) {
                    tableContentBuilder.addDocument(docToExport);
                }
            }
        }


        exportToDoc(document);
    }

    private void exportToDoc(XWPFDocument document) throws IOException {
        String exportFolder = "export";
        Files.createDirectories(Paths.get(exportFolder));
        File file = new File(String.format("%s/%s", exportFolder, fileName));
        FileOutputStream out = new FileOutputStream(file);
        document.write(out);
        out.close();
        logger.info(fileName+" written successfully");
    }

    private static void configPage(CTBody body) {
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
