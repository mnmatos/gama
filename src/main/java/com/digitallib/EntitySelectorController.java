package com.digitallib;

import com.digitallib.manager.EntityManager;
import com.digitallib.model.entity.Entity;
import com.digitallib.model.entity.EntityType;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class EntitySelectorController implements Initializable {

    private static final Logger logger = LogManager.getLogger(EntitySelectorController.class);

    @FXML private Label headerLabel;
    @FXML private ListView<Entity> entityListView;

    private EntityType type;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        entityListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // Custom cell factory to show name
        entityListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Entity item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        });
    }

    public void setEntityType(EntityType type) {
        this.type = type;
        headerLabel.setText("Selecione " + type.name());
        loadEntities();
    }

    private void loadEntities() {
        if (type != null) {
            List<Entity> list = EntityManager.getEntriesByType(type);
            // sort by name
            list.sort(Comparator.comparing(Entity::getName));
            entityListView.setItems(FXCollections.observableArrayList(list));
        }
    }

    public List<String> getSelectedIds() {
        return entityListView.getSelectionModel().getSelectedItems().stream()
                .map(Entity::getId)
                .collect(Collectors.toList());
    }

    @FXML
    private void handleCreate() {
        openEntityForm(null);
    }

    @FXML
    private void handleEdit() {
        Entity selected = entityListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        openEntityForm(selected);
    }

    private void openEntityForm(Entity entity) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/EntityForm.fxml"));
            DialogPane pane = loader.load();
            EntityFormController controller = loader.getController();

            if (entity != null) controller.setEntity(entity);
            else controller.setType(type);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle(entity == null ? "Nova Entidade" : "Editar Entidade");

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                controller.saveData();
                loadEntities();
            }
        } catch (Exception e) {
            logger.error("Failed to open entity form", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Erro ao abrir formulário: " + e.getMessage());
            alert.showAndWait();
        }
    }

    @FXML
    private void handleRemove() {
        List<Entity> selected = new ArrayList<>(entityListView.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Remover e excluir permanentemente as entidades selecionadas?");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
             for (Entity e : selected) {
                 EntityManager.removeEntry(e.getId());
             }
             loadEntities();
        }
    }
}
