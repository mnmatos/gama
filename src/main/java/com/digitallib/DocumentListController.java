package com.digitallib;

import com.digitallib.exception.RepositoryException;
import com.digitallib.exception.ValidationException;
import com.digitallib.exporter.docx.FichaExporter;
import com.digitallib.exporter.docx.InventarioExporter;
import com.digitallib.manager.CategoryManager;
import com.digitallib.manager.EntityManager;
import com.digitallib.manager.MultiSourcedDocumentManager;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.manager.index.DocByEntityIndexManager;
import com.digitallib.manager.index.EntityIndexManager;
import com.digitallib.manager.index.SubClassIndexManager;
import com.digitallib.model.Documento;
import com.digitallib.model.MultiSourcedDocument;
import com.digitallib.model.ui.Filter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class DocumentListController implements Initializable {

    private static final Logger logger = LogManager.getLogger(DocumentListController.class);

    @FXML private ComboBox<String> testemunhoFilter;
    @FXML private ComboBox<String> classeFilter;
    @FXML private TextField filtroCodigo;
    @FXML private Label numDocField;
    @FXML private TableView<Documento> tabelaPoemas;
    @FXML private TableColumn<Documento, String> colTradicao;
    @FXML private TableColumn<Documento, String> colCodigo;
    @FXML private TableColumn<Documento, String> colTitulo;
    @FXML private TableColumn<Documento, String> colSerie;
    @FXML private TableColumn<Documento, String> colEncontradoEm;
    @FXML private TableColumn<Documento, String> colAcoes;
    @FXML private Label projectNameLabel;


    private CategoryManager categoryManager = new CategoryManager();
    private List<Filter> filterList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        projectNameLabel.setText(System.getProperty("selected.project.name"));
        initializeFilters();
        initializeTable();
        refreshTable();
    }

    private void initializeFilters() {
        // Testemunho Filter
        testemunhoFilter.setItems(FXCollections.observableArrayList("Inéditos e Éditos", "Inéditos", "Éditos"));
        testemunhoFilter.getSelectionModel().select(0);
        testemunhoFilter.managedProperty().bind(testemunhoFilter.visibleProperty());

        // Classe Filter
        List<String> classes = new ArrayList<>();
        classes.add("Todas as Séries");
        classes.addAll(Arrays.asList(categoryManager.getClasseAndCodeAsStringArray()));
        classeFilter.setItems(FXCollections.observableArrayList(classes));
        classeFilter.getSelectionModel().select(0);

        // Listeners
        filtroCodigo.textProperty().addListener((obs, oldVal, newVal) -> refreshTable());
        classeFilter.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            updateTestemunhoVisibility(newVal.intValue());
            refreshTable();
        });
        testemunhoFilter.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> refreshTable());

        updateTestemunhoVisibility(classeFilter.getSelectionModel().getSelectedIndex());
    }

    /**
     * The testemunho (inédito/édito) filter is only meaningful when "Todas as Séries" (index 0)
     * is selected. When a specific class is chosen the filter is hidden and reset.
     */
    private void updateTestemunhoVisibility(int classeIndex) {
        if (classeIndex == 0) {
            testemunhoFilter.setVisible(true);
        } else {
            testemunhoFilter.getSelectionModel().select(0);
            testemunhoFilter.setVisible(false);
        }
    }

    @FXML
    private void handleProjectManager() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/ProjectManager.fxml"));
            Stage stage = (Stage) tabelaPoemas.getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
        } catch (IOException e) {
            logger.error("Failed to load Project Manager", e);
            // Show an alert to the user
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load the project management window.");
            alert.setContentText("An unexpected error occurred. Please check the logs for more details.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleRefresh() {
        updateTestemunhoVisibility(classeFilter.getSelectionModel().getSelectedIndex());
        refreshTable();
    }

    private void initializeTable() {
        colTradicao.setCellValueFactory(cellData -> {
            String grupoId = cellData.getValue().getGrupo();
            if (grupoId == null) return new SimpleStringProperty("");
            MultiSourcedDocument msd = MultiSourcedDocumentManager.getEntries().stream()
                .filter(d -> d.getId().equals(grupoId))
                .findFirst().orElse(null);
            return new SimpleStringProperty(msd != null ? msd.getTitulo() : "");
        });

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
                            Documento doc = getTableView().getItems().get(getIndex());
                            HBox hbox = new HBox(5);

                            if (doc.getGrupo() != null) {
                                Button btnGroup = new Button("Tradição");
                                btnGroup.setOnAction(e -> handleEditGroup(doc));
                                hbox.getChildren().add(btnGroup);
                            }

                            Button btnEdit = new Button("Editar");
                            btnEdit.setOnAction(e -> handleEdit(doc));
                            hbox.getChildren().add(btnEdit);

                            Button btnRemove = new Button("Remover");
                            btnRemove.setOnAction(e -> handleRemove(doc));
                            hbox.getChildren().add(btnRemove);

                            setGraphic(hbox);
                        }
                    }
                };
            }
        });
    }

    private void refreshTable() {
        List<Documento> docs = getDocumentosFiltrados();
        tabelaPoemas.setItems(FXCollections.observableArrayList(docs));
        numDocField.setText(String.format("Número de documentos: %d", docs.size()));

        // Refresh indexes logic from Swing
        try {
            new SubClassIndexManager().updateIndex(docs);
            new DocByEntityIndexManager().updateIndex(docs);
            new EntityIndexManager().updateIndex(EntityManager.getEntries());
        } catch (RepositoryException e) {
            logger.error("Failed to update indexes", e);
        }
    }

    private List<Documento> getDocumentosFiltrados() {
        List<Documento> documentos = RepositoryManager.getEntries();

        filterList.clear();
        String codigoTxt = filtroCodigo.getText();
        filterList.add(new Filter("Código", d -> codigoTxt.isEmpty() || d.getCodigo().startsWith(codigoTxt)));

        int classeIdx = classeFilter.getSelectionModel().getSelectedIndex();
        filterList.add(new Filter("Série", d -> classeIdx < 1 || d.getClasseProducao().equals(categoryManager.getClasseForIndex(classeIdx - 1))));

        int testIdx = testemunhoFilter.getSelectionModel().getSelectedIndex();
        filterList.add(new Filter("Testemunho", d -> testIdx < 1 || (d.isInedito() == (testIdx == 1))));

        for (Filter filter : filterList) {
            documentos = filter.filter(documentos);
        }
        return documentos;
    }

    private void openDocumentCreator(Documento doc) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/DocumentCreator.fxml"));
            DialogPane page = loader.load();

            DocumentCreatorController controller = loader.getController();
            controller.setDocumento(doc);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(page);
            dialog.setTitle(doc == null ? "Criar Documento" : "Editar Documento");
            dialog.setResizable(true);

            // Intercept the OK button so we can prevent the dialog from closing when save fails
            Button okBtn = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
                try {
                    controller.saveDocument();
                    // If save succeeded, refresh the table and allow the dialog to close
                    refreshTable();
                } catch (ValidationException | RepositoryException e) {
                    logger.error("Error saving doc", e);
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Erro ao salvar documento");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                    ev.consume(); // prevent dialog from closing
                }
            });

            // Show dialog (it will only close when OK action is not consumed or Cancel pressed)
            dialog.showAndWait();

        } catch (java.io.IOException e) {
            logger.error("Failed to open DocumentCreator dialog", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setContentText("Não foi possível abrir a janela de criação: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleAddMono() {
        openDocumentCreator(null);
    }

    @FXML
    public void handleAddPoli() {
         TextInputDialog dialog = new TextInputDialog();
         dialog.setTitle("Criar Politestemunhal");
         dialog.setHeaderText("Digite o Título");

         Optional<String> result = dialog.showAndWait();
         result.ifPresent(title -> {
             if (!title.trim().isEmpty()) {
                 MultiSourcedDocument doc = new MultiSourcedDocument();
                 doc.setTitulo(title);
                 try {
                     String code = MultiSourcedDocumentManager.addEntry(doc);
                     handleAddPoliItem(code);
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

    public void handleAddPoliItem(String docCode) {
        openMultiSourceDocumentList(docCode);
    }

    private void openMultiSourceDocumentList(String docCode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/MultiSourceDocumentList.fxml"));
            DialogPane pane = loader.load();
            MultiSourceDocumentListController controller = loader.getController();
            controller.setMultiDocCode(docCode);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Documentos do Grupo"); // Tradição
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

    @FXML
    public void handleExportInventory() {
        InventarioExporter exporter = new InventarioExporter();
        exporter.export(RepositoryManager.getEntries()); // Swing used getDocumentos() which calls RepositoryManager.getEntries()
    }

    @FXML
    public void handleExportCatalog() {
        FichaExporter exporter = new FichaExporter();
        exporter.export(RepositoryManager.getEntries());
    }

    private void handleEdit(Documento doc) {
        openDocumentCreator(doc);
    }

    private void handleEditGroup(Documento doc) {
        if (doc.getGrupo() != null) {
            openMultiSourceDocumentList(doc.getGrupo());
        }
    }

    private void handleRemove(Documento doc) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Atenção!!");
        alert.setHeaderText("Você tem certeza que quer remover o " + doc.getCodigo());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                RepositoryManager.removeEntry(doc.getCodigo());
            } catch (RepositoryException e) {
                logger.error("Failed to remove document: " + doc.getCodigo(), e);
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Erro");
                err.setContentText("Não foi possível remover o documento: " + e.getMessage());
                err.showAndWait();
            }
            refreshTable();
        }
    }
}
