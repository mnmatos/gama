package com.digitallib;

import com.digitallib.model.Documento;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.Parent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.Optional;


public class FileActionDialogController {

    private static final Logger logger = LogManager.getLogger(FileActionDialogController.class);

    @FXML private Label fileNameLabel;
    @FXML private Button analyzeButton;
    @FXML private Button transcribeButton;

    private File file;
    private String editedDocCode;
    private Runnable onRemove;
    private Documento documento;

    public void setFile(File file, String editedDocCode, Runnable onRemove) {
        setFile(file, editedDocCode, null, onRemove);
    }

    public void setFile(File file, String editedDocCode, Documento documento, Runnable onRemove) {
        this.file = file;
        this.editedDocCode = editedDocCode;
        this.onRemove = onRemove;
        this.documento = documento;

        fileNameLabel.setText(file.getName());

        String lower = file.getName().toLowerCase();
        boolean isPdf = lower.endsWith(".pdf");
        analyzeButton.setVisible(isPdf);
        analyzeButton.setManaged(isPdf);

        boolean isImage = lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                || lower.endsWith(".gif") || lower.endsWith(".bmp") || lower.endsWith(".tiff")
                || lower.endsWith(".tif") || lower.endsWith(".webp");
        transcribeButton.setVisible(isImage);
        transcribeButton.setManaged(isImage);
    }

    @FXML
    private void handleOpen() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file);
            } catch (IOException e) {
                logger.error("Failed to open file", e);
            }
        }
    }

    @FXML
    private void handleOpenFolder() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(file.getParentFile());
            } catch (IOException e) {
                logger.error("Failed to open containing folder", e);
            }
        }
    }

    @FXML
    private void handleRemove() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Atenção");
        alert.setContentText("Você tem certeza que quer remover o arquivo " + file.getName() + "?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
             if (editedDocCode != null) {
                 // Logic to remove from repository if it's saved?
                 // Original Swing code: removeFiles(editedDocCode, Collections.singletonList(fileName));
                 // files.removeIf(...) -> this has to happen in parent controller via callback
             }
             if (onRemove != null) onRemove.run();
             closeDialog();
        }
    }

    @FXML
    private void handleAnalyze() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/PdfAnalysisDialog.fxml"));
            DialogPane pane = loader.load();
            PdfAnalysisDialogController controller = loader.getController();
            controller.setFile(file);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Análise de PDF");
            dialog.setResizable(true);
            dialog.showAndWait();
        } catch (IOException e) {
            logger.error("Failed to open PDF analysis dialog", e);
        }
    }

    @FXML
    private void handleTranscribe() {
        if (documento == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aviso");
            alert.setContentText("Documento não disponível para transcrição.");
            alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/ImageTranscription.fxml"));
            Parent root = loader.load();
            ImageTranscriptionController controller = loader.getController();
            controller.setData(documento, file.getName());

            Stage stage = new Stage();
            stage.setTitle("Transcrição de Imagem — " + file.getName());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setResizable(true);
            stage.showAndWait();
        } catch (IOException e) {
            logger.error("Failed to open image transcription screen", e);
        }
    }

    private void closeDialog() {
         Stage stage = (Stage) fileNameLabel.getScene().getWindow();
         stage.close();
    }
}
