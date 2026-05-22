package com.digitallib;
import com.digitallib.exporter.docx.TranscriptionExporter;
import com.digitallib.manager.LlmSettingsManager;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.*;
import com.digitallib.service.TranscriptionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
public class ImageTranscriptionController {
    private static final Logger logger = LogManager.getLogger(ImageTranscriptionController.class);
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    @FXML private ImageView imageView;
    @FXML private ScrollPane imageScrollPane;
    @FXML private Label imageNameLabel;
    @FXML private VBox blockEditorContainer;
    @FXML private Button btnTranscribe;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;
    private Documento documento;
    private String imageFilename;
    private BlockEditorController blockEditor;
    @FXML
    public void initialize() {
        progressIndicator.setVisible(false);
        // Zoom with Ctrl+scroll
        imageScrollPane.setOnScroll(e -> {
            if (e.isControlDown()) {
                double factor = e.getDeltaY() > 0 ? 1.1 : 0.9;
                imageView.setScaleX(imageView.getScaleX() * factor);
                imageView.setScaleY(imageView.getScaleY() * factor);
                e.consume();
            }
        });
        loadBlockEditor();
    }
    private void loadBlockEditor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/BlockEditor.fxml"));
            VBox editorNode = loader.load();
            blockEditor = loader.getController();
            blockEditorContainer.getChildren().add(editorNode);
            VBox.setVgrow(editorNode, javafx.scene.layout.Priority.ALWAYS);
        } catch (IOException e) {
            logger.error("Failed to load BlockEditor", e);
        }
    }
    public void setData(Documento doc, String filename) {
        this.documento = doc;
        this.imageFilename = filename;
        imageNameLabel.setText(filename);
        // Update button label based on configured transcription tool
        LlmSettings cfg = LlmSettingsManager.getInstance().load();
        boolean isOcr = "ocr".equalsIgnoreCase(cfg.getTranscriptionTool());
        btnTranscribe.setText(isOcr ? "Transcrever com OCR" : "Transcrever com LLM");
        // Load image
        Path imgPath = TranscriptionService.getInstance().resolveImagePath(doc.getCodigo(), filename);
        if (Files.exists(imgPath)) {
            try (InputStream is = Files.newInputStream(imgPath)) {
                imageView.setImage(new Image(is));
                imageView.setPreserveRatio(true);
                imageView.setFitWidth(500);
            } catch (IOException e) {
                logger.error("Failed to load image", e);
            }
        } else {
            statusLabel.setText("Imagem nao encontrada: " + imgPath);
        }
        // Load existing transcription if present
        TranscriptionRecord record = doc.getTranscriptions().get(filename);
        if (record != null && record.getBlocks() != null && !record.getBlocks().isEmpty()) {
            blockEditor.setBlocks(record.getBlocks());
            updateStatusBar(record);
        } else {
            String toolName = isOcr ? "OCR (Tesseract)" : "LLM";
            statusLabel.setText("Sem transcricao. Clique em 'Transcrever com " + toolName + "' para iniciar.");
        }
    }
    @FXML
    private void handleTranscribe() {
        LlmSettings settings = LlmSettingsManager.getInstance().load();
        boolean isOcr = "ocr".equalsIgnoreCase(settings.getTranscriptionTool());
        if (!isOcr && settings.getProvider() == null) {
            showAlert("Configure o provedor LLM antes de transcrever.");
            return;
        }
        if (isOcr && (settings.getTessdataPath() == null || settings.getTessdataPath().isBlank())) {
            // Try to auto-detect Tesseract default installation on Windows
            String autoPath = detectTessdataPath();
            if (autoPath != null) {
                settings.setTessdataPath(autoPath);
            } else {
                showAlert(
                    "Tesseract OCR não encontrado.\n\n" +
                    "A pasta 'tessdata' contém os modelos de reconhecimento de texto " +
                    "e é necessária para o OCR funcionar.\n\n" +
                    "Para usar o OCR:\n" +
                    "1. Instale o Tesseract OCR: https://github.com/UB-Mannheim/tesseract/wiki\n" +
                    "2. Durante a instalação, selecione o idioma 'Portuguese'\n" +
                    "3. Em Ferramentas → Configurações de Transcrição → OCR,\n" +
                    "   aponte para a pasta tessdata (ex: C:\\Program Files\\Tesseract-OCR\\tessdata)"
                );
                return;
            }
        }
        btnTranscribe.setDisable(true);
        progressIndicator.setVisible(true);
        statusLabel.setText("Transcrevendo...");
        TranscriptionService.getInstance().transcribeAsync(documento, imageFilename, settings,
            record -> {
                blockEditor.setBlocks(record.getBlocks());
                updateStatusBar(record);
                progressIndicator.setVisible(false);
                btnTranscribe.setDisable(false);
            },
            error -> {
                statusLabel.setText("Erro: " + error.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
                progressIndicator.setVisible(false);
                btnTranscribe.setDisable(false);
            }
        );
    }
    @FXML
    private void handleSave() {
        if (documento == null) return;
        TranscriptionRecord record = documento.getTranscriptions().getOrDefault(
                imageFilename, new TranscriptionRecord(imageFilename));
        record.setBlocks(blockEditor.getBlocks());
        record.setStatus(TranscriptionStatus.DONE);
        documento.getTranscriptions().put(imageFilename, record);
        try {
            RepositoryManager.updateEntry(documento);
            statusLabel.setText("Salvo.");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (com.digitallib.exception.RepositoryException e) {
            showAlert("Erro ao salvar: " + e.getMessage());
        }
    }
    @FXML
    private void handleExportDocx() {
        if (documento == null) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Exportar transcricao DOCX");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Word Document", "*.docx"));
        fc.setInitialFileName(imageFilename.replaceAll("\\.[^.]+$", "") + "_transcricao.docx");
        File dest = fc.showSaveDialog(btnTranscribe.getScene().getWindow());
        if (dest == null) return;
        try {
            new TranscriptionExporter().toDocx(blockEditor.getBlocks(), dest);
            statusLabel.setText("Exportado: " + dest.getName());
            statusLabel.setStyle(null);
        } catch (Exception e) {
            showAlert("Erro ao exportar: " + e.getMessage());
        }
    }
    @FXML
    private void handleExportTxt() {
        if (documento == null) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Exportar transcricao TXT");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text File", "*.txt"));
        fc.setInitialFileName(imageFilename.replaceAll("\\.[^.]+$", "") + "_transcricao.txt");
        File dest = fc.showSaveDialog(btnTranscribe.getScene().getWindow());
        if (dest == null) return;
        try {
            new TranscriptionExporter().toTxt(blockEditor.getBlocks(), dest);
            statusLabel.setText("Exportado: " + dest.getName());
            statusLabel.setStyle(null);
        } catch (Exception e) {
            showAlert("Erro ao exportar: " + e.getMessage());
        }
    }
    @FXML
    private void handleZoomIn() {
        imageView.setScaleX(imageView.getScaleX() * 1.2);
        imageView.setScaleY(imageView.getScaleY() * 1.2);
    }
    @FXML
    private void handleZoomOut() {
        imageView.setScaleX(imageView.getScaleX() / 1.2);
        imageView.setScaleY(imageView.getScaleY() / 1.2);
    }
    @FXML
    private void handleZoomReset() {
        imageView.setScaleX(1.0);
        imageView.setScaleY(1.0);
    }
    private void updateStatusBar(TranscriptionRecord record) {
        StringBuilder sb = new StringBuilder();
        if (record.getLlmProvider() != null) sb.append(record.getLlmProvider());
        if (record.getLlmModel() != null) sb.append(" / ").append(record.getLlmModel());
        if (record.getUpdatedAt() != null) sb.append(" · ").append(record.getUpdatedAt().format(DT_FMT));
        statusLabel.setText(sb.toString());
        statusLabel.setStyle(null);
    }
    private void showAlert(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.showAndWait();
    }

    /**
     * Tries to auto-detect the tessdata folder from common Tesseract installation paths.
     * Falls back to bundled tessdata extracted from the JAR resources.
     * Returns the path string if found, or null if not found.
     */
    private String detectTessdataPath() {
        String[] candidates = {
            "C:\\Program Files\\Tesseract-OCR\\tessdata",
            "C:\\Program Files (x86)\\Tesseract-OCR\\tessdata",
            System.getenv("TESSDATA_PREFIX"),
            System.getenv("PROGRAMFILES") + "\\Tesseract-OCR\\tessdata",
        };
        for (String path : candidates) {
            if (path != null && new java.io.File(path).isDirectory()) {
                return path;
            }
        }
        // Fallback: extract bundled .traineddata files from resources
        return extractBundledTessdata();
    }

    /**
     * Scans /tessdata/ in the classpath resources, copies every *.traineddata
     * file found to a temp directory, and returns that directory's path.
     * This way users only need to drop .traineddata files into
     * src/main/resources/tessdata/ — no Tesseract installation required.
     */
    private String extractBundledTessdata() {
        try {
            java.net.URL resourceDir = getClass().getResource("/tessdata/");
            if (resourceDir == null) {
                logger.warn("No bundled tessdata folder found in resources.");
                return null;
            }
            java.nio.file.Path tempTessdata = java.nio.file.Path.of(
                    System.getProperty("java.io.tmpdir"), "digitallib-tessdata");
            java.nio.file.Files.createDirectories(tempTessdata);

            // Walk the resource directory and copy every .traineddata file
            java.net.URI uri = resourceDir.toURI();
            java.nio.file.Path resourcePath;
            if (uri.getScheme().equals("jar")) {
                java.nio.file.FileSystem fs = java.nio.file.FileSystems.newFileSystem(uri, java.util.Collections.emptyMap());
                resourcePath = fs.getPath("/tessdata/");
            } else {
                resourcePath = java.nio.file.Path.of(uri);
            }

            boolean anyFound = false;
            try (var stream = java.nio.file.Files.walk(resourcePath, 1)) {
                for (java.nio.file.Path entry : (Iterable<java.nio.file.Path>) stream::iterator) {
                    String name = entry.getFileName().toString();
                    if (name.endsWith(".traineddata")) {
                        java.nio.file.Path dest = tempTessdata.resolve(name);
                        if (!java.nio.file.Files.exists(dest)) {
                            try (java.io.InputStream is = getClass().getResourceAsStream("/tessdata/" + name)) {
                                if (is != null) {
                                    java.nio.file.Files.copy(is, dest);
                                    logger.info("Extracted bundled tessdata: {}", name);
                                }
                            }
                        }
                        anyFound = true;
                    }
                }
            }

            if (anyFound) {
                logger.info("Using bundled tessdata from: {}", tempTessdata);
                return tempTessdata.toString();
            }
        } catch (Exception e) {
            logger.warn("Failed to extract bundled tessdata", e);
        }
        return null;
    }
}
