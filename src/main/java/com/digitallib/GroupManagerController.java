package com.digitallib;

import com.digitallib.exception.RepositoryException;
import com.digitallib.manager.MultiSourcedDocumentManager;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.Documento;
import com.digitallib.model.MultiSourcedDocument;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class GroupManagerController {

    private static final Logger logger = LogManager.getLogger(GroupManagerController.class);

    @FXML private TextField searchField;
    @FXML private TableView<MultiSourcedDocument> groupTable;
    @FXML private TableColumn<MultiSourcedDocument, String> colId;
    @FXML private TableColumn<MultiSourcedDocument, String> colTitulo;
    @FXML private TableColumn<MultiSourcedDocument, String> colQtd;
    @FXML private TableColumn<MultiSourcedDocument, String> colAcoes;

    private FilteredList<MultiSourcedDocument> filteredData;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        colTitulo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitulo()));
        colQtd.setCellValueFactory(cellData -> {
            List<String> docs = cellData.getValue().getDocuments();
            return new SimpleStringProperty(String.valueOf(docs == null ? 0 : docs.size()));
        });

        colAcoes.setCellFactory(new Callback<>() {
            @Override
            public TableCell<MultiSourcedDocument, String> call(TableColumn<MultiSourcedDocument, String> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                            setGraphic(null);
                        } else {
                            MultiSourcedDocument doc = getTableRow().getItem();
                            HBox flow = new HBox(5);

                            Button btnEdit = new Button("Gerenciar");
                            btnEdit.setOnAction(e -> handleManageGroup(doc));
                            flow.getChildren().add(btnEdit);

                            Button btnRemove = new Button("Remover");
                            btnRemove.setOnAction(e -> handleRemoveGroup(doc));
                            flow.getChildren().add(btnRemove);

                            setGraphic(flow);
                        }
                    }
                };
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> refreshTable());

        refreshTable();
    }

    private void refreshTable() {
        try {
            List<MultiSourcedDocument> docs = MultiSourcedDocumentManager.getEntries();
            filteredData = new FilteredList<>(FXCollections.observableArrayList(docs), p -> true);

            String search = searchField.getText();
            if (search != null && !search.trim().isEmpty()) {
                String lowerSearch = search.toLowerCase();
                filteredData.setPredicate(group ->
                    (group.getTitulo() != null && group.getTitulo().toLowerCase().contains(lowerSearch)) ||
                    (group.getId() != null && group.getId().toLowerCase().contains(lowerSearch))
                );
            }

            groupTable.setItems(filteredData);
        } catch (Exception e) {
            logger.error("Error loading multi-sourced documents", e);
        }
    }

    private void handleManageGroup(MultiSourcedDocument doc) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/MultiSourceDocumentList.fxml"));
            DialogPane pane = loader.load();
            MultiSourceDocumentListController controller = loader.getController();
            controller.setMultiDoc(doc);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Documentos do Grupo - " + doc.getTitulo());
            dialog.setResizable(true);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                controller.save();
                refreshTable();
            }
        } catch (Exception e) {
            logger.error("Failed to open MultiSourceDocumentList", e);
        }
    }

    private void handleRemoveGroup(MultiSourcedDocument doc) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Atenção");
        alert.setHeaderText("Remover grupo: " + doc.getTitulo() + "?");
        alert.setContentText("Isto removerá o grupo. Os documentos continuarão existindo mas serão desvinculados.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                MultiSourcedDocumentManager.removeEntry(doc.getId());

                // Unlink documents
                List<Documento> allDocs = RepositoryManager.getEntries();
                for (Documento d : allDocs) {
                    if (doc.getId().equals(d.getGrupo())) {
                        d.setGrupo(null);
                        RepositoryManager.updateEntry(d);
                    }
                }
                refreshTable();
            } catch (RepositoryException e) {
                logger.error("Error removing group", e);
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Erro");
                err.setContentText("Erro ao remover o grupo: " + e.getMessage());
                err.showAndWait();
            }
        }
    }

    @FXML
    public void handleCreateGroup() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Criar Nova Tradição");
        dialog.setHeaderText("Digite o Título da nova Tradição");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(title -> {
            if (!title.trim().isEmpty()) {
                MultiSourcedDocument doc = new MultiSourcedDocument();
                doc.setTitulo(title);
                try {
                    MultiSourcedDocumentManager.addEntry(doc);
                    refreshTable();
                } catch (RepositoryException e) {
                    logger.error("Failed to create multi-source document", e);
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Erro");
                    err.setContentText("Não foi possível criar o documento politestemunhal: " + e.getMessage());
                    err.showAndWait();
                }
            }
        });
    }

    @FXML
    public void handleClose() {
        Stage stage = (Stage) searchField.getScene().getWindow();
        stage.close();
    }
}

