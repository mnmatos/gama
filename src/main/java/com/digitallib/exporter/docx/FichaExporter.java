package com.digitallib.exporter.docx;

import com.digitallib.exception.EntityNotFoundException;
import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.exporter.BaseFileExporter;
import com.digitallib.manager.CategoryManager;
import com.digitallib.manager.EntityManager;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.*;
import com.digitallib.reference.block.AutorPubliReferenceBlockBuilder;
import com.digitallib.reference.block.AutorReferenceBlockBuilder;
import com.digitallib.reference.block.ReferenceBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.digitallib.exporter.docx.TableContentBuilder.PrintReference;

public class FichaExporter extends BaseFileExporter {

    private static int testimony;
    private static Logger logger = LogManager.getLogger();

    CategoryManager categoryManager = new CategoryManager();

    public FichaExporter() {
    }

    @Override
    public void export(List<Documento> docsToExport) {
        XWPFDocument document = new XWPFDocument();
        CTBody body = document.getDocument().getBody();

        configPage(body);

        for (Classe classe : categoryManager.getClasses()) {
            List<Documento> filteredDoc = docsToExport.stream().filter(doc -> doc.getClasseProducao().equals(classe)).collect(Collectors.toList());
            if (filteredDoc.size() > 0) {
                for (Documento docToExport : filteredDoc) {
                    if(docToExport.getSubClasseProducao().getExtensions().contains("teatro_info")) {
                        CreateDocUnit(document, docToExport);
                        createSpace(document.createParagraph(), 200);
                    }
                }
            }
        }

        createFile(document, "inventário");
    }

    public static void createSpace(XWPFParagraph paragraph, int spacing) {
        paragraph.setSpacingBefore(spacing);
        paragraph.setSpacingAfter(spacing);

        XWPFRun spacerRun = paragraph.createRun();
        spacerRun.setText("");
    }

    public static void CreateDocUnit(XWPFDocument wordDocument, Documento doc) {
        testimony = 1; //TODO: make dynamic

        TextoTeatral textoTeatral = doc.getTextoTeatro();
        String local = "";
        try {
            local = EntityManager.getEntryById(doc.getLugarPublicacao()).getName();
        } catch (EntityNotFoundException e) {
            logger.error("Could not find Local: "+doc.getLugarPublicacao(), e);
        }

        XWPFTableCell cell = generateWrappingCell(wordDocument);
        PrintAuthor(cell, getAuthorText(doc));//"COSTA, Nivalda Silva."
        PrintTitle(cell, doc.getTitulo(), local, doc.getDataDocumento().getAno(), doc.getNumPagina(), testimony);
        createSpace(cell.addParagraph(), 0);

        PrintInfo(cell, doc.getInstituicaoCustodia(), "Adulto", textoTeatral.getQuantidadePersonagem(), textoTeatral.getCenas(), textoTeatral.getAtos());

        String description = doc.getDescricao();

        PrintDescription(cell, "Descrição", testimony, description);
        PrintResumeWithKeywords(cell, textoTeatral.getResumo(), textoTeatral.getPalavrasChave());

        if (doc.getTrabalhosRelacionados() != null) {
            for (Relacao relacao : doc.getTrabalhosRelacionados()) {
                List<Documento> documentos = RepositoryManager.getEntries();
                if (relacao.getTipoRelacao() == TIPO_RELACAO.ESTUDADO_EM) {
                    Optional<Documento> relacionado = documentos.stream().filter(documento -> documento.getCodigo().equals(relacao.getCodDocumento())).findFirst();
                    if (relacionado.isPresent()) PrintReferenceSection(cell, relacionado.get());
                }
            }
        }
    }

    public static String getAuthorText(Documento doc) {
        AutorReferenceBlockBuilder autorReferenceBlockBuilder = new AutorReferenceBlockBuilder();
        try {
            List<ReferenceBlock> referenceBlocks = autorReferenceBlockBuilder.build(doc);
            StringBuilder result = new StringBuilder();

            for (ReferenceBlock block : referenceBlocks) {
                result.append(block.getContent());
            }

            return result.toString().trim();
        } catch (ReferenceBlockBuilderException e) {
            return "";
        }
    }

    public static void PrintReferenceSection(XWPFTableCell cell, Documento doc) {
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        paragraph.setSpacingBefore(0);
        paragraph.setSpacingAfter(0);

        XWPFRun run = paragraph.createRun();
        run.setFontSize(12);
        run.setFontFamily("Times New Roman");
        run.setBold(true);
        run.setText(String.format("Estudos: "));
        run.addBreak();

        TipoNBR tipoNbr = doc.getTipoNbr() == null ? TipoNBR.JORNAL : doc.getTipoNbr();
        try {
            PrintReference(doc, tipoNbr, paragraph);
        } catch (ReferenceBlockBuilderException e) {
            throw new RuntimeException(e);
        }
    }

    public static void PrintResumeWithKeywords(XWPFTableCell cell, String content, List<String> keywords) {
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        paragraph.setSpacingBefore(0);
        paragraph.setSpacingAfter(0);

        CreateTextWithBoldIntro("Resumo", content, paragraph);
        String keywordsText = createKeywordsString(keywords);
        CreateTextWithBoldIntro("Palavras-chave", keywordsText, paragraph);
    }

    public static String createKeywordsString(List<String> keywords) {

        StringBuilder finalText = new StringBuilder();
        for (String topic : keywords) {
            finalText.append(topic).append(". ");
        }
        String keywordsText = finalText.toString().trim();
        return keywordsText;
    }

    public static void CreateTextWithBoldIntro(String contentName, String content, XWPFParagraph paragraph) {
        XWPFRun run = paragraph.createRun();
        run.setFontSize(12);
        run.setFontFamily("Times New Roman");
        run.setBold(true);
        run.setText(String.format("%s: ", contentName));

        run = paragraph.createRun();
        run.setFontSize(12);
        run.setFontFamily("Times New Roman");
        run.setText(content);
        run.addBreak();
    }

    public static void PrintDescription(XWPFTableCell cell, String contentName, int testimony, String description) {
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        paragraph.setSpacingBefore(0);
        paragraph.setSpacingAfter(0);


        XWPFRun run = paragraph.createRun();
        run.setFontSize(12);
        run.setFontFamily("Times New Roman");
        run.setBold(true);
        run.setText(String.format("Testemunho (T%d)", testimony));
        run.addBreak();

        CreateTextWithBoldIntro(contentName, description, paragraph);
    }

    public static void PrintInfo(XWPFTableCell cell, String instituicao, String classificacao, int personagens, int atos, int cenas) {
        XWPFParagraph paragraph = cell.addParagraph();

        PrintInfoLine(paragraph, "Localização: ", instituicao);
        PrintInfoLine(paragraph, "Classificação: ", classificacao);
        PrintInfoLine(paragraph, "Personagens: ", String.format("%02d", personagens));
        PrintInfoLine(paragraph, "Número de Atos: ", String.format("%02d", atos));
        PrintInfoLine(paragraph, "Número de Cenas", String.format("%02d", cenas));
    }

    public static void PrintInfoLine(XWPFParagraph paragraph, String infoName, String infoValue) {
        XWPFRun run = paragraph.createRun();
        run.setFontSize(12);
        run.setFontFamily("Times New Roman");
        run.setBold(true);
        run.setText(infoName + "\n");

        run = paragraph.createRun();
        run.setFontSize(12);
        run.setFontFamily("Times New Roman");
        run.setBold(false);
        run.setText(infoValue);
        run.addBreak();
    }

    public static void PrintTitle(XWPFTableCell cell, String title, String local, String year, int pages, int testimony) {
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        paragraph.setSpacingBefore(0);
        paragraph.setSpacingAfter(0);


        XWPFRun run = paragraph.createRun();
        run.setFontSize(12);
        run.setFontFamily("Times New Roman");
        run.setItalic(true);
        run.setText(title);

        run = paragraph.createRun();
        run.setFontSize(12);
        run.setFontFamily("Times New Roman");
        run.setText(String.format(". %s, [%s]. %df. Testemunho %d", local, year, pages, testimony));
    }

    public static void PrintAuthor(XWPFTableCell cell, String author) {
        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        paragraph.setSpacingBefore(0);
        paragraph.setSpacingAfter(0);

        XWPFRun run = paragraph.createRun();
        run.setFontSize(12);
        run.setFontFamily("Times New Roman");
        run.setText(author);
    }

    public static XWPFTableCell generateWrappingCell(XWPFDocument document) {
        XWPFTable table = document.createTable(1, 1);
        XWPFTableCell cell = table.getRow(0).getCell(0);

        table.setInsideHBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
        table.setInsideVBorder(XWPFTable.XWPFBorderType.SINGLE, 1, 0, "000000");
        table.setCellMargins(100, 100, 100, 100);
        return cell;
    }

    public static void configPage(CTBody body) {
        if (!body.isSetSectPr()) {
            body.addNewSectPr();
        }
        CTSectPr section = body.getSectPr();

        if (!section.isSetPgSz()) {
            section.addNewPgSz();
        }
        CTPageSz pageSize = section.getPgSz();

        pageSize.setOrient(STPageOrientation.LANDSCAPE);
        pageSize.setW(BigInteger.valueOf(15840));
        pageSize.setH(BigInteger.valueOf(12240));
    }
}
