package com.digitallib;

import com.digitallib.model.Project;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TextInputDialog;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import com.digitallib.utils.RobustFileDeleter;
import com.digitallib.utils.ZipUtils;

public class ProjectManagerController {

    private static final Logger logger = LogManager.getLogger(ProjectManagerController.class);

    @FXML
    private TableView<Project> projectTableView;

    @FXML
    private TableColumn<Project, String> projectNameColumn;

    @FXML
    private TableColumn<Project, String> projectPathColumn;

    private static final File GAMA_DIR = new File(System.getProperty("user.home"), ".gama");
    private static final File PROJECTS_FILE = new File(GAMA_DIR, "projects.json");
    private final ObjectMapper mapper = new ObjectMapper();
    private final ObservableList<Project> projects = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadProjects();
        // Configure table columns
        if (projectNameColumn != null) projectNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        if (projectPathColumn != null) projectPathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));

        if (projectTableView != null) projectTableView.setItems(projects);

        // Double-click a row to open/select the project
        if (projectTableView != null) {
            projectTableView.setRowFactory(tv -> {
                TableRow<Project> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && !row.isEmpty()) {
                        handleSelect();
                    }
                });
                return row;
            });
        }
    }

    private void loadProjects() {
        if (!GAMA_DIR.exists()) {
            GAMA_DIR.mkdirs();
        }

        if (PROJECTS_FILE.exists()) {
            try {
                List<Project> list = mapper.readValue(PROJECTS_FILE, new TypeReference<List<Project>>() {});
                projects.setAll(list);
            } catch (IOException e) {
                logger.error("Failed to load projects", e);
                showAlert("Error", "Could not load projects: " + e.getMessage());
            }
        }
    }

    private void saveProjects() {
        try {
            if (!GAMA_DIR.exists()) {
                GAMA_DIR.mkdirs();
            }
            mapper.writeValue(PROJECTS_FILE, new ArrayList<>(projects));
        } catch (IOException e) {
            logger.error("Failed to save projects", e);
            showAlert("Error", "Could not save projects: " + e.getMessage());
        }
    }

    private void saveProjectFile(Project project) {
        File projectDir = new File(project.getPath());
        File gamaFile = new File(projectDir, ".gama");
        try {
            mapper.writeValue(gamaFile, project);
        } catch (IOException e) {
            logger.error("Failed to save .gama file", e);
        }
    }

    @FXML
    private void handleSelect() {
        Project selected = null;
        if (projectTableView != null) selected = projectTableView.getSelectionModel().getSelectedItem();
        else if (!projects.isEmpty()) selected = projects.get(0);
        if (selected == null) {
            showAlert("Atenção", "Por favor, selecione um projeto.");
            return;
        }

        System.setProperty("selected.project.path", selected.getPath());
        System.setProperty("acervo", selected.getAcervo());
        if (selected.getCodeType() != null) {
            System.setProperty("code_type", selected.getCodeType());
        }
        System.setProperty("selected.project.name", selected.getName());
        // Reload CategoryManager so it picks up this project's classes.yaml
        com.digitallib.manager.CategoryManager.reload();
        // Reload CodeManager so the code generator type is re-resolved for the new project
        com.digitallib.code.CodeManager.reload();


        // Launch Main Window
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/DocumentList.fxml"));
            Parent root = loader.load();
            Stage stage;
            if (projectTableView != null && projectTableView.getScene() != null) stage = (Stage) projectTableView.getScene().getWindow();
            else stage = new Stage();
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
        Project selected = null;
        if (projectTableView != null) selected = projectTableView.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Confirmar exclusão");
        dialog.setHeaderText("Para confirmar a exclusão do projeto '" + selected.getName() + "', digite 'excluir' ou 'delete' (sem aspas).");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && ("excluir".equalsIgnoreCase(result.get().trim()) || "delete".equalsIgnoreCase(result.get().trim()))) {
            // User confirmed by typing 'excluir' or 'delete'
            File projectDir = new File(selected.getPath());
            if (projectDir.exists()) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Excluir arquivos do projeto no disco também?", ButtonType.YES, ButtonType.NO);
                confirm.setTitle("Excluir arquivos");
                Optional<ButtonType> confirmRes = confirm.showAndWait();
                if (confirmRes.isPresent() && confirmRes.get() == ButtonType.YES) {
                    // Dry-run: gather list of files to be deleted and show preview
                    List<Path> itemsToDelete = new ArrayList<>();
                    try (java.util.stream.Stream<Path> stream = Files.walk(projectDir.toPath())) {
                        stream.forEach(itemsToDelete::add);
                    } catch (IOException e) {
                        logger.error("Failed to scan project directory for preview", e);
                        Alert err = new Alert(Alert.AlertType.ERROR, "Não foi possível listar os arquivos para pré-visualização: " + e.getMessage(), ButtonType.OK);
                        err.setTitle("Erro");
                        err.showAndWait();
                        return;
                    }

                    // Build preview content (limit to avoid huge dialogs)
                    int limit = 1000;
                    StringBuilder previewText = new StringBuilder();
                    int count = 0;
                    for (Path p : itemsToDelete) {
                        previewText.append(p.toString()).append(System.lineSeparator());
                        count++;
                        if (count >= limit) break;
                    }
                    if (itemsToDelete.size() > limit) {
                        previewText.append("... (preview truncated, ").append(itemsToDelete.size() - limit).append(" more items)");
                    }

                    javafx.scene.control.TextArea previewArea = new javafx.scene.control.TextArea(previewText.toString());
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
                    if (!(previewRes.isPresent() && previewRes.get() == ButtonType.YES)) {
                        // User canceled at preview stage
                        return;
                    }
                    try {
                        RobustFileDeleter.delete(projectDir.toPath());
                        boolean deleted = !Files.exists(projectDir.toPath());
                        if (!deleted) {
                            Alert warning = new Alert(Alert.AlertType.WARNING,
                                    "A pasta do projeto não pôde ser excluída completamente.\n" +
                                            "A referência será removida da lista, mas por favor, exclua manualmente a pasta:\n" +
                                            projectDir.getAbsolutePath(),
                                    ButtonType.OK);
                            warning.setTitle("Atenção");
                            warning.setHeaderText("Exclusão manual necessária");
                            warning.showAndWait();
                        }
                    } catch (IOException e) {
                        logger.error("Failed to delete project directory", e);
                        Alert warning = new Alert(Alert.AlertType.WARNING,
                                "Ocorreu um erro ao excluir a pasta do projeto.\n" +
                                        "Mensagem: " + e.getMessage() + "\n" +
                                        "A referência será removida da lista, mas por favor, exclua manualmente a pasta:\n" +
                                        projectDir.getAbsolutePath(),
                                ButtonType.OK);
                        warning.setTitle("Atenção");
                        warning.setHeaderText("Exclusão manual necessária");
                        warning.showAndWait();
                    }
                }
            }

            // Remove from list and save
            projects.remove(selected);
            saveProjects();
        }
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
        File dir;
        if (projectTableView != null && projectTableView.getScene() != null) dir = dirChooser.showDialog(projectTableView.getScene().getWindow());
        else dir = dirChooser.showDialog(null);

        if (dir != null) {
            String sanitizedName = name.replaceAll("[\\\\/:*?\"<>|]", "_");
            File projectDir = new File(dir, sanitizedName);
            if (!projectDir.exists()) {
                boolean created = projectDir.mkdirs();
                if (!created) {
                    Alert err = new Alert(Alert.AlertType.ERROR, "Não foi possível criar a pasta do projeto: " + projectDir.getAbsolutePath(), ButtonType.OK);
                    err.setTitle("Erro");
                    err.showAndWait();
                    return;
                }
            }

            // Ask to import classes.yaml
            Alert importClassesAlert = new Alert(Alert.AlertType.CONFIRMATION, "Deseja importar um arquivo de classes (classes.yaml)?", ButtonType.YES, ButtonType.NO);
            importClassesAlert.setTitle("Importar Classes");
            Optional<ButtonType> importResult = importClassesAlert.showAndWait();

            if (importResult.isPresent() && importResult.get() == ButtonType.YES) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Selecione o arquivo classes.yaml");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("YAML files", "*.yaml"));
                File selectedFile = fileChooser.showOpenDialog(projectTableView.getScene().getWindow());

                if (selectedFile != null) {
                    try {
                        Path destPath = new File(projectDir, "classes.yaml").toPath();
                        Files.copy(selectedFile.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        logger.error("Failed to copy classes.yaml", e);
                        showAlert("Erro", "Não foi possível copiar o arquivo classes.yaml: " + e.getMessage());
                    }
                }
            }

            Project newProject = new Project(name, projectDir.getAbsolutePath(), acervo);
            projects.add(newProject);
            saveProjectFile(newProject);
            saveProjects();
        }
    }

    @FXML
    private void handleAddExisting() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Selecione a pasta do projeto existente");
        File dir;
        if (projectTableView != null && projectTableView.getScene() != null) dir = dirChooser.showDialog(projectTableView.getScene().getWindow());
        else dir = dirChooser.showDialog(null);

        if (dir != null) {
            File gamaFile = new File(dir, ".gama");
            if (gamaFile.exists()) {
                try {
                    Project p = mapper.readValue(gamaFile, Project.class);
                    // Override path with actual selected path in case it moved
                    p.setPath(dir.getAbsolutePath());

                    // Check duplicate
                    boolean exists = projects.stream().anyMatch(existing -> existing.getPath().equals(p.getPath()));
                    if (!exists) {
                        projects.add(p);
                        saveProjects();
                        showAlert("Sucesso", "Projeto adicionado com sucesso.");
                    } else {
                        showAlert("Info", "Este projeto já está na lista.");
                    }
                } catch (IOException e) {
                    logger.error("Failed to read .gama file", e);
                    showAlert("Erro", "Erro ao ler arquivo .gama: " + e.getMessage());
                }
            } else {
                showAlert("Erro", "A pasta selecionada não contém um arquivo .gama válido.");
            }
        }
    }

    @FXML
    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importar Projeto (ZIP)");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo Compactado", "*.zip"));

        File zipFile = (projectTableView != null && projectTableView.getScene() != null)
                       ? fileChooser.showOpenDialog(projectTableView.getScene().getWindow())
                       : fileChooser.showOpenDialog(null);

        if (zipFile == null) return;

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Selecione onde extrair o projeto");
        File destDir = (projectTableView != null && projectTableView.getScene() != null)
                       ? dirChooser.showDialog(projectTableView.getScene().getWindow())
                       : dirChooser.showDialog(null);

        if (destDir == null) return;

        try {
            // Create a folder with the project name (derived from zip name)
            String zipName = zipFile.getName();
            String folderName = zipName.lastIndexOf('.') > 0 ? zipName.substring(0, zipName.lastIndexOf('.')) : zipName;

            File extractionDir = new File(destDir, folderName);
            if (!extractionDir.exists() && !extractionDir.mkdirs()) {
                 showAlert("Erro", "Não foi possível criar a pasta do projeto: " + extractionDir.getAbsolutePath());
                 return;
            }

            // Unzip to the created folder
            ZipUtils.unzip(zipFile.toPath(), extractionDir.toPath());

            // Now search for .gama in extractionDir or subdirectories (depth 1)
            File foundGama = null;
            File projectRoot = null;

            // Check extractionDir first
            File localGama = new File(extractionDir, ".gama");
            if (localGama.exists()) {
                foundGama = localGama;
                projectRoot = extractionDir;
            } else {
                // Check immediate subdirectories
                File[] subFiles = extractionDir.listFiles();
                if (subFiles != null) {
                    for (File sub : subFiles) {
                        if (sub.isDirectory()) {
                             File subGama = new File(sub, ".gama");
                             if (subGama.exists()) {
                                 foundGama = subGama;
                                 projectRoot = sub;
                                 break;
                             }
                        }
                    }
                }
            }

            if (foundGama != null) {
                 Project p = mapper.readValue(foundGama, Project.class);
                 p.setPath(projectRoot.getAbsolutePath());

                 boolean exists = projects.stream().anyMatch(existing -> existing.getPath().equals(p.getPath()));
                 if (!exists) {
                     projects.add(p);
                     saveProjects();
                     showAlert("Sucesso", "Projeto importado e adicionado com sucesso.");
                 } else {
                     showAlert("Info", "Projeto importado, mas já existe na lista.");
                 }
            } else {
                 showAlert("Aviso", "Extração concluída, mas nenhum arquivo .gama foi encontrado na raiz ou subpasta imediata.");
            }

        } catch (IOException e) {
            logger.error("Erro ao importar ZIP", e);
            showAlert("Erro", "Erro ao importar: " + e.getMessage());
        }
    }

    @FXML
    private void handleExport() {
        Project selected = null;
        if (projectTableView != null) selected = projectTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Atenção", "Por favor, selecione um projeto para exportar.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exportar Projeto (ZIP)");
        fileChooser.setInitialFileName(selected.getName() + ".zip");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivo ZIP", "*.zip"));

        File zipFile = (projectTableView != null && projectTableView.getScene() != null)
                       ? fileChooser.showSaveDialog(projectTableView.getScene().getWindow())
                       : fileChooser.showSaveDialog(null);

        if (zipFile != null) {
            try {
                // Ensure .gama is up to date
                saveProjectFile(selected);

                ZipUtils.zipFolder(Path.of(selected.getPath()), zipFile.toPath());
                showAlert("Sucesso", "Projeto exportado (compactado) com sucesso.");
            } catch (IOException e) {
                logger.error("Erro ao exportar projeto", e);
                showAlert("Erro", "Erro ao exportar projeto: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
