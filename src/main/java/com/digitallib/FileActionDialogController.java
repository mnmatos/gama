package com.digitallib;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static com.digitallib.manager.RepositoryManager.removeFiles;

public class FileActionDialogController {

    private static final Logger logger = LogManager.getLogger(FileActionDialogController.class);

    @FXML private Label fileNameLabel;
    @FXML private Button openButton;
    @FXML private Button openFolderButton;
    @FXML private Button removeButton;
    @FXML private Button analyzeButton;

    private File file;
    private String editedDocCode;
    private Runnable onRemove;

    public void setFile(File file, String editedDocCode, Runnable onRemove) {
        this.file = file;
        this.editedDocCode = editedDocCode;
        this.onRemove = onRemove;

        fileNameLabel.setText(file.getName());
        boolean isPdf = file.getName().toLowerCase().endsWith(".pdf");
        analyzeButton.setVisible(isPdf);
        analyzeButton.setManaged(isPdf);
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

    private void closeDialog() {
         Stage stage = (Stage) fileNameLabel.getScene().getWindow();
         stage.close();
    }
}
