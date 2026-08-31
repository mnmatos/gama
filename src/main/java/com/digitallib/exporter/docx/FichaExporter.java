package com.digitallib.exporter.docx;

import com.digitallib.exception.EntityNotFoundException;
import com.digitallib.exception.ReferenceBlockBuilderException;
import com.digitallib.exporter.BaseFileExporter;
import com.digitallib.manager.EntityManager;
import com.digitallib.manager.FichaExportConfigManager;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.*;
import com.digitallib.model.export.*;
import com.digitallib.model.export.BlockType;
import com.digitallib.reference.block.AutorReferenceBlockBuilder;
import com.digitallib.reference.block.ReferenceBlock;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBody;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageSz;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.digitallib.exporter.docx.TableContentBuilder.PrintReference;

/**
 * Data-driven ficha-catálogo exporter.
 *
 * <p>The no-arg constructor uses the first saved {@link FichaExportConfig}; shows an error
 * dialog and becomes a no-op when none exist.
 * Use {@link #FichaExporter(FichaExportConfig)} to supply a specific config.
 */
public class FichaExporter extends BaseFileExporter {

    private static final Logger logger = LogManager.getLogger(FichaExporter.class);
    private static final String FONT_FAMILY = "Times New Roman";
    private static final int FONT_SIZE = 12;

    private final FichaExportConfig config;

    /** No-arg constructor: uses first saved config, or shows an error dialog if none. */
    public FichaExporter() {
        List<FichaExportConfig> configs = FichaExportConfigManager.getInstance().load();
        if (configs.isEmpty()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro");
                alert.setHeaderText("Nenhuma configuração de Ficha-catálogo encontrada");
                alert.setContentText(
                        "Crie uma configuração em Exportar → Configurar Ficha-catálogo… antes de exportar.");
                alert.showAndWait();
            });
            this.config = null;
        } else {
            this.config = configs.get(0);
        }
    }

    /** Constructs an exporter with an explicit config. */
    public FichaExporter(FichaExportConfig config) {
        this.config = config;
    }

    @Override
    public void export(List<Documento> docsToExport) {
        if (config == null) return;

        XWPFDocument document = new XWPFDocument();
        CTBody body = document.getDocument().getBody();
        configPage(body);

        List<Documento> matched = docsToExport.stream()
                .filter(this::matchesConfig)
                .toList();

        for (Documento doc : matched) {
            renderDocUnit(document, doc);
            createSpace(document.createParagraph(), 200);
        }

        createFile(document, "Ficha");
    }

    // ── Filtering ──────────────────────────────────────────────────────────

    private boolean matchesConfig(Documento doc) {
        List<FilterRule> rules = config.getFilterRules();
        if (rules == null || rules.isEmpty()) return true;
        for (FilterRule rule : rules) {
            if (matchesRule(doc, rule)) return true;
        }
        return false;
    }

    private boolean matchesRule(Documento doc, FilterRule rule) {
        if (doc.getClasseProducao() == null) return false;
        if (!doc.getClasseProducao().getCode().equals(rule.getClasseCode())) return false;
        if (rule.getSubClasseCode() == null) return true;
        if (doc.getSubClasseProducao() == null) return false;
        return doc.getSubClasseProducao().getCode().equals(rule.getSubClasseCode());
    }

    // ── Rendering ──────────────────────────────────────────────────────────

    private void renderDocUnit(XWPFDocument wordDocument, Documento doc) {
        XWPFTableCell cell = generateWrappingCell(wordDocument);
        boolean firstBlock = true;
        for (BlockConfig block : config.getBlocks()) {
            if (!block.isEnabled()) continue;
            renderBlock(cell, doc, block, firstBlock);
            firstBlock = false;
        }
    }

    private void renderBlock(XWPFTableCell cell, Documento doc, BlockConfig block, boolean isFirst) {
        switch (block.getType()) {
            case AUTHOR -> renderAuthorBlock(cell, doc, block, isFirst);
            case RELATED_STUDIES -> renderRelatedStudies(cell, doc, block);
            default -> renderFieldBlock(cell, doc, block);
        }
    }

    private void renderAuthorBlock(XWPFTableCell cell, Documento doc, BlockConfig block, boolean isFirst) {
        XWPFParagraph paragraph = isFirst
                ? cell.getParagraphs().get(0)
                : cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        paragraph.setSpacingBefore(0);
        paragraph.setSpacingAfter(0);

        if (block.getFields().isEmpty()) {
            appendRun(paragraph, getAuthorText(doc), false, false);
        } else {
            for (FieldConfig field : block.getFields()) {
                String value = resolveField(doc, field);
                if (value.isEmpty()) continue;
                appendRun(paragraph, value, field.isValueBold(), field.isItalic());
            }
        }
    }

    private void renderFieldBlock(XWPFTableCell cell, Documento doc, BlockConfig block) {
        XWPFParagraph paragraph = cell.addParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        paragraph.setSpacingBefore(0);
        paragraph.setSpacingAfter(0);

        if (block.getLabel() != null && !block.getLabel().isBlank()) {
            XWPFRun h = paragraph.createRun();
            h.setFontFamily(FONT_FAMILY); h.setFontSize(FONT_SIZE);
            h.setBold(true);
            h.setText(block.getLabel());
            h.addBreak();
        }

        boolean addedAnyField = false;
        for (FieldConfig field : block.getFields()) {
            String value = resolveField(doc, field);
            if (value.isEmpty() && field.getSource() != FieldSource.LITERAL) continue;
            if (value.isEmpty()) continue;

            addedAnyField = true;

            if (field.getLabel() != null && !field.getLabel().isBlank()) {
                XWPFRun lr = paragraph.createRun();
                lr.setFontFamily(FONT_FAMILY); lr.setFontSize(FONT_SIZE);
                lr.setBold(field.isLabelBold());
                lr.setText(field.getLabel() + ": ");
            }

            XWPFRun vr = paragraph.createRun();
            vr.setFontFamily(FONT_FAMILY); vr.setFontSize(FONT_SIZE);
            vr.setBold(field.isValueBold());
            vr.setItalic(field.isItalic());
            vr.setText(value);

            // TITLE fields are inline; all others line-break after the value
            if (block.getType() != BlockType.TITLE) {
                vr.addBreak();
            }
        }

        if (block.getType() == BlockType.TITLE && addedAnyField) {
            paragraph.createRun().addBreak();
        }
    }

    private void renderRelatedStudies(XWPFTableCell cell, Documento doc, BlockConfig block) {
        List<Relacao> relacoes = doc.getTrabalhosRelacionados();
        if (relacoes == null || relacoes.isEmpty()) return;

        List<Documento> allDocs = RepositoryManager.getEntries();
        for (Relacao relacao : relacoes) {
            if (relacao.getTipoRelacao() != block.getRelationType()) continue;

            Optional<Documento> relOpt = allDocs.stream()
                    .filter(d -> d.getCodigo().equals(relacao.getCodDocumento()))
                    .findFirst();
            if (relOpt.isEmpty()) continue;
            Documento relacionado = relOpt.get();

            XWPFParagraph paragraph = cell.addParagraph();
            paragraph.setAlignment(ParagraphAlignment.LEFT);
            paragraph.setSpacingBefore(0);
            paragraph.setSpacingAfter(0);

            // Derive heading from block label, then first non-empty field, then default
            String heading = block.getLabel();
            if (heading == null || heading.isBlank()) {
                heading = "Estudos";
                for (FieldConfig f : block.getFields()) {
                    String v = resolveField(doc, f);
                    if (!v.isEmpty()) { heading = v; break; }
                }
            }

            XWPFRun hr = paragraph.createRun();
            hr.setFontFamily(FONT_FAMILY); hr.setFontSize(FONT_SIZE);
            hr.setBold(true);
            hr.setText(heading + ": ");
            hr.addBreak();

            TipoNBR tipoNbr = relacionado.getTipoNbr() == null ? TipoNBR.JORNAL : relacionado.getTipoNbr();
            try {
                PrintReference(relacionado, tipoNbr, paragraph);
            } catch (ReferenceBlockBuilderException e) {
                logger.error("Failed to render reference for: " + relacionado.getCodigo(), e);
            }
        }
    }

    // ── Field resolution ───────────────────────────────────────────────────

    private String resolveField(Documento doc, FieldConfig field) {
        String raw = resolveRaw(doc, field);
        if (raw == null || raw.isEmpty()) return "";
        if (field.getFormat() != null && !field.getFormat().isBlank()) {
            try {
                return String.format(field.getFormat(), Integer.parseInt(raw));
            } catch (NumberFormatException nfe) {
                try { return String.format(field.getFormat(), raw); } catch (Exception ignored) { return raw; }
            }
        }
        return raw;
    }

    private String resolveRaw(Documento doc, FieldConfig field) {
        if (field.getSource() == null) return "";
        return switch (field.getSource()) {
            case TITULO -> nvl(doc.getTitulo());
            case SUBTITULO -> nvl(doc.getSubtitulo());
            case AUTORES -> {
                List<String> a = doc.getAutores();
                if (a == null || a.isEmpty()) {
                    yield getAuthorText(doc);
                } else {
                    List<String> authorNames = new java.util.ArrayList<>();
                    for (String authorId : a) {
                        try {
                            authorNames.add(com.digitallib.manager.EntityManager.getEntryById(authorId).getName());
                        } catch (com.digitallib.exception.EntityNotFoundException e) {
                            logger.warn("Author not found: " + authorId);
                        }
                    }
                    yield String.join("; ", authorNames);
                }
            }
            case TESTEMUNHO -> nvl(doc.getTestemunho());
            case CODIGO -> nvl(doc.getCodigo());
            case ANO -> doc.getDataDocumento() != null ? nvl(doc.getDataDocumento().getAno()) : "";
            case LOCAL_PUBLICACAO -> {
                if (doc.getLugarPublicacao() == null) yield "";
                try { yield EntityManager.getEntryById(doc.getLugarPublicacao()).getName(); }
                catch (EntityNotFoundException e) {
                    logger.warn("Local not found: " + doc.getLugarPublicacao());
                    yield "";
                }
            }
            case NUM_PAGINA -> doc.getNumPagina() != null ? String.valueOf(doc.getNumPagina()) : "";
            case INSTITUICAO_CUSTODIA -> nvl(doc.getInstituicaoCustodia());
            case DESCRICAO -> nvl(doc.getDescricao());
            case TEXTO_TEATRAL_PERSONAGENS -> {
                TextoTeatral t = doc.getTextoTeatro();
                yield t == null ? "" : String.valueOf(t.getQuantidadePersonagem());
            }
            case TEXTO_TEATRAL_ATOS -> {
                TextoTeatral t = doc.getTextoTeatro();
                yield t == null ? "" : String.valueOf(t.getAtos());
            }
            case TEXTO_TEATRAL_CENAS -> {
                TextoTeatral t = doc.getTextoTeatro();
                yield t == null ? "" : String.valueOf(t.getCenas());
            }
            case TEXTO_TEATRAL_RESUMO -> {
                TextoTeatral t = doc.getTextoTeatro();
                yield (t == null || t.getResumo() == null) ? "" : t.getResumo();
            }
            case TEXTO_TEATRAL_PALAVRAS_CHAVE -> {
                TextoTeatral t = doc.getTextoTeatro();
                if (t == null || t.getPalavrasChave() == null || t.getPalavrasChave().isEmpty()) yield "";
                yield createKeywordsString(t.getPalavrasChave());
            }
            case LITERAL -> nvl(field.getLiteralValue());
        };
    }

    private static String nvl(String s) { return s != null ? s : ""; }

    private static void appendRun(XWPFParagraph p, String text, boolean bold, boolean italic) {
        XWPFRun r = p.createRun();
        r.setFontFamily(FONT_FAMILY); r.setFontSize(FONT_SIZE);
        r.setBold(bold); r.setItalic(italic);
        r.setText(text);
    }

    // ── Static helpers (kept for compatibility) ────────────────────────────

    public static String createKeywordsString(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return "";
        return keywords.stream().map(k -> k + ".").collect(Collectors.joining(" "));
    }

    public static String getAuthorText(Documento doc) {
        AutorReferenceBlockBuilder b = new AutorReferenceBlockBuilder();
        try {
            List<ReferenceBlock> blocks = b.build(doc);
            StringBuilder sb = new StringBuilder();
            for (ReferenceBlock block : blocks) sb.append(block.getContent());
            return sb.toString().trim();
        } catch (ReferenceBlockBuilderException e) {
            return "";
        }
    }

    public static void createSpace(XWPFParagraph paragraph, int spacing) {
        paragraph.setSpacingBefore(spacing);
        paragraph.setSpacingAfter(spacing);
        paragraph.createRun().setText("");
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
        if (!body.isSetSectPr()) body.addNewSectPr();
        CTSectPr section = body.getSectPr();
        if (!section.isSetPgSz()) section.addNewPgSz();
        CTPageSz pageSize = section.getPgSz();

        pageSize.setOrient(STPageOrientation.LANDSCAPE);
        pageSize.setW(BigInteger.valueOf(15840));
        pageSize.setH(BigInteger.valueOf(12240));
    }
}
