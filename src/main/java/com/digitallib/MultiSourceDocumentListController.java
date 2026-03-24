package com.digitallib;

import com.digitallib.exception.RepositoryException;
import com.digitallib.manager.MultiSourcedDocumentManager;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.Documento;
import com.digitallib.model.MultiSourcedDocument;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MultiSourceDocumentListController implements Initializable {

    private static final Logger logger = LogManager.getLogger(MultiSourceDocumentListController.class);

    @FXML private TextField titleField;
    @FXML private TableView<Documento> docTable;
    @FXML private TableColumn<Documento, String> colCodigo;
    @FXML private TableColumn<Documento, String> colTitulo;
    @FXML private TableColumn<Documento, String> colSerie;
    @FXML private TableColumn<Documento, String> colEncontradoEm;
    @FXML private TableColumn<Documento, String> colAcoes;

    private MultiSourcedDocument multiDoc;
    private List<Documento> documentos = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeTable();
    }

    private void initializeTable() {
        colCodigo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCodigo()));
        colTitulo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitulo()));
        colSerie.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClasseProducao().getDesc()));
        colEncontradoEm.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEncontradoEm()));

        colAcoes.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Documento, String> call(TableColumn<Documento, String> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                         super.updateItem(item, empty);
                         if (empty) {
                             setGraphic(null);
                         } else {
                             Button btnRemove = new Button("Remover");
                             btnRemove.setOnAction(e -> {
                                 Documento doc = getTableView().getItems().get(getIndex());
                                 multiDoc.getDocuments().remove(doc.getCodigo());
                                 refreshTable();
                             });
                             setGraphic(btnRemove);
                         }
                    }
                };
            }
        });
    }

    public void setMultiDocCode(String code) {
        try {
            this.multiDoc = MultiSourcedDocumentManager.getEntryById(code);
            titleField.setText(multiDoc.getTitulo());
            refreshTable();
        } catch (RepositoryException e) {
            logger.error("Failed to load MultiDoc", e);
        }
    }

    public void setMultiDoc(MultiSourcedDocument doc) {
        this.multiDoc = doc;
        titleField.setText(doc.getTitulo());
         refreshTable();
    }

    private void refreshTable() {
        if (multiDoc != null) {
            documentos = RepositoryManager.getEntries().stream()
                .filter(d -> multiDoc.getDocuments().contains(d.getCodigo()))
                .collect(Collectors.toList());
            docTable.setItems(FXCollections.observableArrayList(documentos));
        }
    }

    @FXML
    private void handleAdd() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/DocumentSelector.fxml"));
            DialogPane pane = loader.load();
            DocumentSelectorController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Selecionar Documentos");
            dialog.setResizable(true);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                List<String> selectedCodes = controller.getSelectedDocCodes();
                if (multiDoc != null) {
                    multiDoc.addDocuments(selectedCodes);
                    refreshTable();
                }
            }
        } catch (Exception e) {
            logger.error("Failed during selecting documents", e);
        }
    }

    public void save() {
         if (multiDoc != null) {
             multiDoc.setTitulo(titleField.getText());
             try {
                 MultiSourcedDocumentManager.updateEntry(multiDoc);
                 for (Documento doc : documentos) {
                     doc.setGrupo(multiDoc.getId());
                     RepositoryManager.updateEntry(doc);
                 }
             } catch (com.digitallib.exception.RepositoryException e) {
                 logger.error("Failed to save multi-source document group", e);
             }
         }
    }
}
