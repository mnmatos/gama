package com.digitallib;

import com.digitallib.manager.EntityManager;
import com.digitallib.model.entity.Entity;
import com.digitallib.model.entity.EntityType;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class EntityFormController {

    @FXML private TextField nameField;
    @FXML private TextArea descField;

    private EntityType type;
    private Entity edited;

    public void setType(EntityType type) {
        this.type = type;
    }

    public void setEntity(Entity entity) {
        this.edited = entity;
        if (entity != null) {
            this.type = entity.getType();
            nameField.setText(entity.getName());
            descField.setText(entity.getDescription());
        }
    }

    public void saveData() {
        if (edited != null) {
            EntityManager.updateEntry(new Entity(edited.getType(), nameField.getText(), descField.getText(), edited.getId()));
        } else {
            EntityManager.addEntry(new Entity(type, nameField.getText(), descField.getText()));
        }
    }
}
