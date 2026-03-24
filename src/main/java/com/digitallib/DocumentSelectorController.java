package com.digitallib;

import com.digitallib.manager.CategoryManager;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.Documento;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DocumentSelectorController implements Initializable {

    @FXML private ComboBox<String> classeFilter;
    @FXML private TextField filtroCodigo;
    @FXML private TableView<Documento> docTable;
    @FXML private TableColumn<Documento, String> colCodigo;
    @FXML private TableColumn<Documento, String> colTitulo;
    @FXML private TableColumn<Documento, String> colSerie;
    @FXML private TableColumn<Documento, String> colEncontradoEm;

    CategoryManager categoryManager = new CategoryManager();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeFilters();
        initializeTable();
        refreshTable();
    }

    private void initializeFilters() {
        List<String> classes = new ArrayList<>();
        classes.add("Todas as Séries");
        classes.addAll(Arrays.asList(categoryManager.getClasseAndCodeAsStringArray()));
        classeFilter.setItems(FXCollections.observableArrayList(classes));
        classeFilter.getSelectionModel().select(0);

        filtroCodigo.textProperty().addListener((obs, oldVal, newVal) -> refreshTable());
    }

    private void initializeTable() {
        docTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        colCodigo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCodigo()));
        colTitulo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitulo()));
        colSerie.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClasseProducao().getDesc()));
        colEncontradoEm.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEncontradoEm()));
    }

    @FXML
    private void handleRefresh() {
        refreshTable();
    }

    private void refreshTable() {
        List<Documento> documentos = RepositoryManager.getEntries();
        String codeFilter = filtroCodigo.getText();
        int classIndex = classeFilter.getSelectionModel().getSelectedIndex();

        List<Documento> filtered = documentos.stream()
                .filter(d -> classIndex < 1 || d.getClasseProducao().equals(categoryManager.getClasseForIndex(classIndex - 1)))
                .filter(d -> codeFilter.isEmpty() || d.getCodigo().startsWith(codeFilter))
                .collect(Collectors.toList());

        docTable.setItems(FXCollections.observableArrayList(filtered));
    }

    public List<String> getSelectedDocCodes() {
        return docTable.getSelectionModel().getSelectedItems().stream()
                .map(Documento::getCodigo)
                .collect(Collectors.toList());
    }
}
