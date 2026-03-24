package com.digitallib;

import com.digitallib.model.Project;
import com.digitallib.service.ProjectService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProjectManagerController {

    private static final Logger logger = LogManager.getLogger(ProjectManagerController.class);

    @FXML private TableView<Project> projectTableView;
    @FXML private TableColumn<Project, String> projectNameColumn;
    @FXML private TableColumn<Project, String> projectPathColumn;

    private final ProjectService projectService = new ProjectService();
    private final ObservableList<Project> projects = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadProjectsList();
        if (projectNameColumn != null) projectNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (projectPathColumn != null) projectPathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        if (projectTableView != null) projectTableView.setItems(projects);

        if (projectTableView != null) {
            projectTableView.setRowFactory(tv -> {
                TableRow<Project> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) handleSelect();
                });
                return row;
            });
        }
    }

    private void loadProjectsList() {
        try {
            List<Project> list = projectService.loadProjects();
            projects.setAll(list);
        } catch (IOException e) {
            logger.error("Failed to load projects", e);
            showAlert("Error", "Could not load projects: " + e.getMessage());
        }
    }

    private void saveProjectsList() {
        try {
            projectService.saveProjects(new ArrayList<>(projects));
        } catch (IOException e) {
            logger.error("Failed to save projects", e);
            showAlert("Error", "Could not save projects: " + e.getMessage());
        }
    }

    @FXML
    private void handleSelect() {
        Project selected = projectTableView != null ? projectTableView.getSelectionModel().getSelectedItem() : null;
        if (selected == null && !projects.isEmpty()) selected = projects.get(0);
        if (selected == null) { showAlert("Atenção", "Por favor, selecione um projeto."); return; }

        System.setProperty("selected.project.path", selected.getPath());
        System.setProperty("acervo", selected.getAcervo());
        if (selected.getCodeType() != null) System.setProperty("code_type", selected.getCodeType());
        System.setProperty("selected.project.name", selected.getName());

        com.digitallib.manager.CategoryManager.reload();
        com.digitallib.code.CodeManager.reload();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/DocumentList.fxml"));
            Parent root = loader.load();
            Stage stage = (projectTableView != null && projectTableView.getScene() != null)
                    ? (Stage) projectTableView.getScene().getWindow()
                    : new Stage();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Gama Filologia - " + selected.getName());
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            logger.error("Failed to load main window", e);
            showAlert("Error", "Could not load main window: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Project selected = projectTableView != null ? projectTableView.getSelectionModel().getSelectedItem() : null;
        if (selected == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Confirmar exclusão");
        dialog.setHeaderText("Para confirmar a exclusão do projeto '" + selected.getName()
                + "', digite 'excluir' ou 'delete' (sem aspas).");
        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty() || (!("excluir".equalsIgnoreCase(result.get().trim()))
                && !("delete".equalsIgnoreCase(result.get().trim())))) return;

        File projectDir = new File(selected.getPath());
        if (projectDir.exists()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Excluir arquivos do projeto no disco também?", ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Excluir arquivos");
            Optional<ButtonType> confirmRes = confirm.showAndWait();

            if (confirmRes.isPresent() && confirmRes.get() == ButtonType.YES) {
                // Dry-run preview
                List<Path> itemsToDelete;
                try {
                    itemsToDelete = projectService.listProjectFiles(projectDir);
                } catch (IOException e) {
                    logger.error("Failed to scan project directory for preview", e);
                    showAlert("Erro", "Não foi possível listar os arquivos para pré-visualização: " + e.getMessage());
                    return;
                }

                if (!confirmDeletionWithPreview(itemsToDelete)) return;

                try {
                    projectService.deleteProjectDirectory(projectDir);
                    if (Files.exists(projectDir.toPath())) {
                        showWarningManualDelete(projectDir);
                    }
                } catch (IOException e) {
                    logger.error("Failed to delete project directory", e);
                    showWarningManualDelete(projectDir, e.getMessage());
                }
            }
        }

        projects.remove(selected);
        saveProjectsList();
    }

    private boolean confirmDeletionWithPreview(List<Path> itemsToDelete) {
        int limit = 1000;
        StringBuilder previewText = new StringBuilder();
        int count = 0;
        for (Path p : itemsToDelete) {
            previewText.append(p).append(System.lineSeparator());
            if (++count >= limit) break;
        }
        if (itemsToDelete.size() > limit) {
            previewText.append("... (preview truncado, ").append(itemsToDelete.size() - limit).append(" more items)");
        }

        TextArea previewArea = new TextArea(previewText.toString());
        previewArea.setEditable(false);
        previewArea.setWrapText(false);
        previewArea.setPrefRowCount(20);
        previewArea.setPrefColumnCount(80);

        Alert previewAlert = new Alert(Alert.AlertType.CONFIRMATION);
        previewAlert.setTitle("Pré-visualização de exclusão");
        previewAlert.setHeaderText(String.format("Serão excluídos %d itens. Reveja a lista abaixo e confirme.", itemsToDelete.size()));
        previewAlert.getDialogPane().setExpandableContent(previewArea);
        previewAlert.getDialogPane().setExpanded(true);
        previewAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> previewRes = previewAlert.showAndWait();
        return previewRes.isPresent() && previewRes.get() == ButtonType.YES;
    }

    @FXML
    private void handleCreate() {
        TextInputDialog nameDialog = new TextInputDialog();
        nameDialog.setTitle("Nome do Projeto");
        nameDialog.setHeaderText("Digite o nome do projeto:");
        Optional<String> nameOpt = nameDialog.showAndWait();
        if (nameOpt.isEmpty() || nameOpt.get().trim().isEmpty()) return;
        String name = nameOpt.get().trim();

        TextInputDialog acervoDialog = new TextInputDialog();
        acervoDialog.setTitle("Sigla do Acervo");
        acervoDialog.setHeaderText("Digite a sigla do acervo:");
        Optional<String> acervoOpt = acervoDialog.showAndWait();
        if (acervoOpt.isEmpty() || acervoOpt.get().trim().isEmpty()) return;
        String acervo = acervoOpt.get().trim();

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Selecione a pasta do projeto");
        File dir = showDirectoryChooser(dirChooser);
        if (dir == null) return;

        // Optional classes.yaml import
        File classesYamlSource = null;
        Alert importClassesAlert = new Alert(Alert.AlertType.CONFIRMATION,
                "Deseja importar um arquivo de classes (classes.yaml)?", ButtonType.YES, ButtonType.NO);
        importClassesAlert.setTitle("Importar Classes");
        Optional<ButtonType> importResult = importClassesAlert.showAndWait();
        if (importResult.isPresent() && importResult.get() == ButtonType.YES) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Selecione o arquivo classes.yaml");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("YAML files", "*.yaml"));
            classesYamlSource = fileChooser.showOpenDialog(getWindow());
        }

        try {
            Project newProject = projectService.createProject(name, acervo, dir, classesYamlSource);
            projects.add(newProject);
            saveProjectsList();
        } catch (IOException e) {
            logger.error("Failed to create project", e);
            showAlert("Erro", "Não foi possível criar o projeto: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddExisting() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Selecione a pasta do projeto existente");
        File dir = showDirectoryChooser(dirChooser);
        if (dir == null) return;

        try {
            Project p = projectService.loadExistingProject(dir);
            boolean exists = projects.stream().anyMatch(existing -> existing.getPath().equals(p.getPath()));
            if (!exists) {
                projects.add(p);
                saveProjectsList();
                showAlert("Sucesso", "Projeto adicionado com sucesso.");
            } else {
                showAlert("Info", "Este projeto já está na lista.");
            }
        } catch (IOException e) {
            logger.error("Failed to read .gama file", e);
            showAlert("Erro", "Erro ao ler arquivo .gama: " + e.getMessage());
        }
    }

    @FXML
    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importar Projeto (ZIP)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo Compactado", "*.zip"));
        File zipFile = fileChooser.showOpenDialog(getWindow());
        if (zipFile == null) return;

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Selecione onde extrair o projeto");
        File destDir = showDirectoryChooser(dirChooser);
        if (destDir == null) return;

        try {
            Project p = projectService.importProjectFromZip(zipFile, destDir);
            boolean exists = projects.stream().anyMatch(existing -> existing.getPath().equals(p.getPath()));
            if (!exists) {
                projects.add(p);
                saveProjectsList();
                showAlert("Sucesso", "Projeto importado e adicionado com sucesso.");
            } else {
                showAlert("Info", "Projeto importado, mas já existe na lista.");
            }
        } catch (IOException e) {
            logger.error("Erro ao importar ZIP", e);
            showAlert("Erro", "Erro ao importar: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        Project selected = projectTableView != null ? projectTableView.getSelectionModel().getSelectedItem() : null;
        if (selected == null) { showAlert("Atenção", "Por favor, selecione um projeto para exportar."); return; }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Projeto (ZIP)");
        fileChooser.setInitialFileName(selected.getName() + ".zip");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo ZIP", "*.zip"));
        File zipFile = fileChooser.showSaveDialog(getWindow());
        if (zipFile == null) return;

        try {
            projectService.exportProjectToZip(selected, zipFile);
            showAlert("Sucesso", "Projeto exportado (compactado) com sucesso.");
        } catch (IOException e) {
            logger.error("Erro ao exportar projeto", e);
            showAlert("Erro", "Erro ao exportar projeto: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Private UI helpers
    // -------------------------------------------------------------------------

    private javafx.stage.Window getWindow() {
        return (projectTableView != null && projectTableView.getScene() != null)
                ? projectTableView.getScene().getWindow()
                : null;
    }

    private File showDirectoryChooser(DirectoryChooser chooser) {
        return chooser.showDialog(getWindow());
    }

    private void showWarningManualDelete(File projectDir) {
        showWarningManualDelete(projectDir, null);
    }

    private void showWarningManualDelete(File projectDir, String errorMsg) {
        String msg = "A pasta do projeto não pôde ser excluída completamente.\n"
                + (errorMsg != null ? "Mensagem: " + errorMsg + "\n" : "")
                + "A referência será removida da lista, mas por favor, exclua manualmente a pasta:\n"
                + projectDir.getAbsolutePath();
        Alert warning = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        warning.setTitle("Atenção");
        warning.setHeaderText("Exclusão manual necessária");
        warning.showAndWait();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
