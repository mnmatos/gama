package com.digitallib.ocr;

import com.digitallib.model.BlockType;
import com.digitallib.model.LlmSettings;
import com.digitallib.model.TextBlock;
import com.digitallib.transcription.TranscriptionAdapter;
import com.digitallib.transcription.TranscriptionException;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transcription engine using Tesseract OCR via Tess4J.
 * Does not require any internet connection, API key or GPU.
 */
public class TesseractOcrAdapter implements TranscriptionAdapter {

    private static final Logger logger = LogManager.getLogger(TesseractOcrAdapter.class);

    private final LlmSettings settings;

    public TesseractOcrAdapter(LlmSettings settings) { this.settings = settings; }

    @Override
    public List<TextBlock> transcribe(byte[] imageBytes, String mimeType) throws TranscriptionException {
        try {
            Tesseract tesseract = buildTesseract();
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (img == null) throw new TranscriptionException("Não foi possível decodificar a imagem para OCR.");
            BufferedImage processed = preprocess(img);
            String raw = tesseract.doOCR(processed);
            logger.debug("Tesseract raw output: {}", raw);
            return parseBlocks(raw);
        } catch (TesseractException | IOException e) {
            throw new TranscriptionException("Tesseract OCR falhou: " + e.getMessage(), e);
        }
    }

    @Override
    public String testConnection() throws TranscriptionException {
        try {
            Tesseract t = buildTesseract();
            BufferedImage blank = new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_GRAY);
            t.doOCR(blank);
            return "Tesseract OCR disponível (idioma: " + resolveLanguage() + ")";
        } catch (TesseractException e) {
            throw new TranscriptionException("Tesseract não encontrado ou tessdata inválido: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------

    private Tesseract buildTesseract() {
        Tesseract t = new Tesseract();
        String datapath = resolveTessdataPath();
        if (datapath != null && !datapath.isBlank()) t.setDatapath(datapath);
        t.setLanguage(resolveLanguage());
        // Page segmentation mode 1 = auto with OSD; try 6 (uniform block of text) for documents
        t.setPageSegMode(6);
        // OCR Engine Mode 1 = LSTM (best accuracy)
        t.setOcrEngineMode(1);
        // Improve accuracy for documents
        t.setVariable("tessedit_pageseg_mode", "6");
        t.setVariable("preserve_interword_spaces", "1");
        return t;
    }

    /**
     * Preprocesses the image to improve OCR accuracy:
     * - Converts to grayscale
     * - Scales up small images (Tesseract works best at ~300 DPI)
     * - Increases contrast
     */
    private BufferedImage preprocess(BufferedImage src) {
        // 1. Convert to grayscale
        BufferedImage gray = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = gray.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();

        // 2. Scale up if image is too small (Tesseract needs ~300 DPI equivalent)
        //    Assume ~96 DPI screen images — scale to ~2.5x for better results
        int targetWidth = gray.getWidth();
        int targetHeight = gray.getHeight();
        if (targetWidth < 1500) {
            double scale = 2.5;
            targetWidth = (int) (gray.getWidth() * scale);
            targetHeight = (int) (gray.getHeight() * scale);
        }

        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D gs = scaled.createGraphics();
        gs.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        gs.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        gs.drawImage(gray, 0, 0, targetWidth, targetHeight, null);
        gs.dispose();

        return scaled;
    }

    private String resolveTessdataPath() {
        String path = settings.getTessdataPath();
        if (path != null && !path.isBlank()) return path;
        // Fall back to the system property injected by the installer (jpackage sets $APPDIR)
        String sysProp = System.getProperty("tessdata.path");
        if (sysProp != null && !sysProp.isBlank()) {
            logger.debug("Using tessdata.path from system property: {}", sysProp);
            return sysProp;
        }
        return null;
    }

    private String resolveLanguage() {
        String lang = settings.getOcrLanguage();
        return (lang != null && !lang.isBlank()) ? lang : "por";
    }

    private List<TextBlock> parseBlocks(String raw) {
        if (raw == null || raw.isBlank()) return List.of(new TextBlock(0, BlockType.PARAGRAPH, "", 1.0));
        String[] paragraphs = raw.split("(?m)^\\s*$\\n?");
        int[] idx = {0};
        return Arrays.stream(paragraphs)
                .map(String::strip)
                .filter(p -> !p.isEmpty())
                .map(p -> new TextBlock(idx[0]++, BlockType.PARAGRAPH, p, 1.0))
                .collect(Collectors.toList());
    }
}
