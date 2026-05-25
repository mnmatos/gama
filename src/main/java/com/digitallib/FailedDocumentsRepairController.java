package com.digitallib;

import com.digitallib.manager.CategoryManager;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.Classe;
import com.digitallib.model.FailedDocument;
import com.digitallib.model.SubClasse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class FailedDocumentsRepairController implements Initializable {

    private static final Logger logger = LogManager.getLogger(FailedDocumentsRepairController.class);

    @FXML private Label summaryLabel;
    @FXML private TableView<FailedDocument> failedTable;
    @FXML private TableColumn<FailedDocument, String> colTitulo;
    @FXML private TableColumn<FailedDocument, String> colCodigo;
    @FXML private TableColumn<FailedDocument, String> colPath;
    @FXML private TableColumn<FailedDocument, String> colError;
    @FXML private TableColumn<FailedDocument, String> colType;

    // Detail panel
    @FXML private Label docTitleLabel;
    @FXML private Label docCodigoLabel;
    @FXML private Label docPathLabel;
    @FXML private Button openFolderBtn;
    @FXML private Button repairBtn;
    @FXML private Button deleteDocBtn;

    @FXML private VBox repairSection;
    @FXML private TextArea errorDetailArea;
    @FXML private GridPane remapPane;
    @FXML private Label badValueLabel;
    @FXML private ComboBox<String> classeDrop;
    @FXML private ComboBox<String> subclasseDrop;
    @FXML private Label statusLabel;

    private final CategoryManager categoryManager = new CategoryManager();
    private FailedDocument selected;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colTitulo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getTitulo()));
        colCodigo.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getCodigo()));
        colPath.setCellValueFactory(cd -> {
            String path = cd.getValue().getPath();
            String[] parts = path.replace('\\', '/').split("/");
            String display = parts.length > 3
                    ? "…/" + parts[parts.length - 3] + "/" + parts[parts.length - 2] + "/" + parts[parts.length - 1]
                    : path;
            return new SimpleStringProperty(display);
        });
        colError.setCellValueFactory(cd -> {
            String msg = cd.getValue().getErrorMessage();
            return new SimpleStringProperty(msg != null ? msg.split("\n")[0] : "");
        });
        colType.setCellValueFactory(cd ->
                new SimpleStringProperty(cd.getValue().isMappingError() ? "Mapeamento" : "Outro"));

        failedTable.getSelectionModel().selectedItemProperty().addListener((obs, old, nw) -> {
            selected = nw;
            populateDetail(nw);
        });

        // Populate classe dropdown
        List<String> classeItems = new ArrayList<>();
        classeItems.add("— selecione —");
        for (Classe c : categoryManager.getClasses()) {
            classeItems.add(c.getDesc() + " [" + c.getName() + "]");
        }
        classeDrop.setItems(FXCollections.observableArrayList(classeItems));
        classeDrop.getSelectionModel().select(0);

        loadFailedDocuments();
    }

    private void loadFailedDocuments() {
        List<FailedDocument> failed = RepositoryManager.getFailedDocuments();
        failedTable.setItems(FXCollections.observableArrayList(failed));
        summaryLabel.setText(failed.size() + " documento(s) com falha encontrado(s).");
        statusLabel.setText("");
    }

    private void populateDetail(FailedDocument doc) {
        boolean hasDoc = doc != null;

        openFolderBtn.setDisable(!hasDoc);
        if (repairBtn != null) repairBtn.setDisable(!hasDoc);
        deleteDocBtn.setDisable(!hasDoc);

        // Always collapse the repair section when the selection changes
        if (repairSection != null) {
            repairSection.setVisible(false);
            repairSection.setManaged(false);
        }
        if (repairBtn != null) repairBtn.setText("🔧  Reparar documento");

        if (!hasDoc) {
            docTitleLabel.setText("");
            docCodigoLabel.setText("");
            docPathLabel.setText("");
            statusLabel.setText("");
            return;
        }

        String titulo = doc.getTitulo();
        docTitleLabel.setText(titulo.isEmpty() ? "(sem título)" : titulo);
        String codigo = doc.getCodigo();
        docCodigoLabel.setText(codigo.isEmpty() ? "(desconhecido)" : codigo);
        docPathLabel.setText(doc.getPath());
        statusLabel.setText("");
    }

    @FXML
    private void handleToggleRepair() {
        if (selected == null || repairSection == null) return;
        boolean show = !repairSection.isVisible();
        repairSection.setVisible(show);
        repairSection.setManaged(show);
        if (repairBtn != null) repairBtn.setText(show ? "✖  Cancelar reparo" : "🔧  Reparar documento");

        if (show) {
            errorDetailArea.setText(selected.getErrorMessage());
            boolean isMappingErr = selected.isMappingError();
            remapPane.setDisable(!isMappingErr);
            if (isMappingErr) {
                String bad = selected.getBadClasseName();
                if (bad == null) bad = selected.getBadSubClasseName();
                badValueLabel.setText(bad != null ? bad : "(desconhecido)");
            } else {
                badValueLabel.setText("N/A – reparo automático não disponível para este tipo de erro.");
            }
            classeDrop.getSelectionModel().select(0);
            subclasseDrop.setItems(FXCollections.observableArrayList());
            statusLabel.setText("");
        }
    }

    @FXML
    private void handleOpenFolder() {
        if (selected == null) return;
        File folder = new File(selected.getPath()).getParentFile();
        if (folder == null || !folder.exists()) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Pasta não encontrada: " + (folder != null ? folder.getPath() : ""));
            return;
        }
        try {
            Desktop.getDesktop().open(folder);
        } catch (IOException e) {
            logger.error("Could not open folder: " + folder.getPath(), e);
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Não foi possível abrir a pasta: " + e.getMessage());
        }
    }

    @FXML
    private void handleDeleteDoc() {
        if (selected == null) return;

        String label = !selected.getTitulo().isEmpty() ? selected.getTitulo()
                : !selected.getCodigo().isEmpty() ? selected.getCodigo()
                : new File(selected.getPath()).getName();

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar remoção");
        confirm.setHeaderText("Remover documento permanentemente?");
        confirm.setContentText("\"" + label + "\"\n\n"
                + "O arquivo JSON e todos os ficheiros na mesma pasta serão eliminados.\n"
                + "Esta ação não pode ser desfeita.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        String codigo = selected.getCodigo();
        try {
            if (!codigo.isEmpty()) {
                RepositoryManager.removeEntry(codigo);
                logger.info("Deleted failed document via RepositoryManager: " + codigo);
            } else {
                // No code could be extracted — fall back to direct file deletion
                File docFile = new File(selected.getPath());
                File folder = docFile.getParentFile();
                if (folder != null && folder.exists()) {
                    File[] children = folder.listFiles();
                    if (children != null) for (File f : children) f.delete();
                    folder.delete();
                } else {
                    docFile.delete();
                }
                logger.info("Deleted failed document directly (no code): " + selected.getPath());
            }

            failedTable.getItems().remove(selected);
            summaryLabel.setText(failedTable.getItems().size() + " documento(s) com falha encontrado(s).");
            failedTable.getSelectionModel().clearSelection();
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("Documento removido com sucesso.");
        } catch (Exception e) {
            logger.error("Failed to delete document: " + selected.getPath(), e);
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Erro ao remover: " + e.getMessage());
        }
    }

    @FXML
    private void handleClasseSelected() {
        int idx = classeDrop.getSelectionModel().getSelectedIndex();
        subclasseDrop.getItems().clear();
        if (idx <= 0) return;

        Classe classe = categoryManager.getClasseForIndex(idx - 1);
        List<String> subs = new ArrayList<>();
        subs.add("— selecione —");
        for (SubClasse sc : classe.getSubclasses()) {
            subs.add(sc.getDesc() + " [" + sc.getName() + "]");
        }
        subclasseDrop.setItems(FXCollections.observableArrayList(subs));
        subclasseDrop.getSelectionModel().select(0);
    }

    @FXML
    private void handleApplyFix() {
        if (selected == null) return;

        int classeIdx = classeDrop.getSelectionModel().getSelectedIndex();
        if (classeIdx <= 0) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Selecione uma série.");
            return;
        }

        int subIdx = subclasseDrop.getSelectionModel().getSelectedIndex();
        if (subIdx <= 0) {
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Selecione uma subsérie.");
            return;
        }

        Classe newClasse = categoryManager.getClasseForIndex(classeIdx - 1);
        SubClasse newSubClasse = newClasse.getSubclasses().get(subIdx - 1);

        try {
            String patched = patchJson(selected.getRawJson(), newClasse.getName(), newSubClasse.getName());
            Files.write(Paths.get(selected.getPath()), patched.getBytes(StandardCharsets.UTF_8));
            statusLabel.setStyle("-fx-text-fill: green;");
            statusLabel.setText("Arquivo corrigido com sucesso. Recarregue a lista de documentos principal.");
            logger.info("Repaired failed document at: " + selected.getPath());

            failedTable.getItems().remove(selected);
            summaryLabel.setText(failedTable.getItems().size() + " documento(s) com falha encontrado(s).");
            failedTable.getSelectionModel().clearSelection();
        } catch (IOException e) {
            logger.error("Failed to patch document: " + selected.getPath(), e);
            statusLabel.setStyle("-fx-text-fill: red;");
            statusLabel.setText("Erro ao salvar: " + e.getMessage());
        }
    }

    private String patchJson(String rawJson, String newClasseName, String newSubClasseName) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = (ObjectNode) mapper.readTree(rawJson);
        root.put("classe_producao", newClasseName);
        root.put("subclasse_producao", newSubClasseName);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);
    }

    @FXML
    private void handleReload() {
        RepositoryManager.scanForFailedDocuments();
        loadFailedDocuments();
        populateDetail(null);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) failedTable.getScene().getWindow();
        stage.close();
    }
}

