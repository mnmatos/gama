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
            statusLabel.setText("Sem transcricao. Clique em 'Transcrever com LLM' para iniciar.");
        }
    }
    @FXML
    private void handleTranscribe() {
        LlmSettings settings = LlmSettingsManager.getInstance().load();
        if (settings.getProvider() == null) {
            showAlert("Configure o provedor LLM antes de transcrever.");
            return;
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
}
