package com.digitallib;

import com.digitallib.exporter.docx.FichaExporter;
import com.digitallib.manager.CategoryManager;
import com.digitallib.manager.FichaExportConfigManager;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.Classe;
import com.digitallib.model.SubClasse;
import com.digitallib.model.TIPO_RELACAO;
import com.digitallib.model.export.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for FichaExportConfigEditor.fxml.
 * Manages the three-panel UI: config list, filter rules, and block/field editor.
 */
public class FichaExportConfigEditorController {

    // ── Left panel ─────────────────────────────────────────────────────────
    @FXML private ListView<FichaExportConfig> configListView;

    // ── Center panel ───────────────────────────────────────────────────────
    @FXML private VBox                    filterPanel;
    @FXML private ListView<FilterRule>    filterRuleListView;
    @FXML private ComboBox<String>        filterClasseCombo;
    @FXML private ComboBox<String>        filterSubClasseCombo;

    // ── Right panel ────────────────────────────────────────────────────────
    @FXML private VBox                    blocksPanel;
    @FXML private ListView<BlockConfig>   blockListView;
    @FXML private VBox                    blockEditorPane;
    @FXML private ComboBox<BlockType>     blockTypeCombo;
    @FXML private CheckBox                blockEnabledCheck;
    @FXML private TextField               blockLabelField;
    @FXML private Label                   blockLayoutHintLabel;
    @FXML private VBox                    relatedStudiesPane;
    @FXML private ComboBox<TIPO_RELACAO>  relationTypeCombo;
    @FXML private VBox                    fieldListPane;
    @FXML private ListView<FieldConfig>   fieldListView;
    @FXML private VBox                    fieldEditPane;

    // Field sub-editor
    @FXML private ComboBox<FieldSource>   fieldSourceCombo;
    @FXML private TextField               fieldLabelField;
    @FXML private Label                   fieldLiteralLabel;
    @FXML private TextField               fieldLiteralField;
    @FXML private TextField               fieldFormatField;
    @FXML private CheckBox                fieldLabelBoldCheck;
    @FXML private CheckBox                fieldValueBoldCheck;
    @FXML private CheckBox                fieldItalicCheck;
    @FXML private TextFlow                previewTextFlow;

    // Footer
    @FXML private Label  statusLabel;
    @FXML private Button btnSave;
    @FXML private Button btnExportNow;

    // ── State ──────────────────────────────────────────────────────────────
    private final CategoryManager categoryManager = new CategoryManager();
    private final ObservableList<FichaExportConfig> configItems = FXCollections.observableArrayList();
    private boolean suppressListeners = false;

    // ── Init ───────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        // Config list
        configListView.setItems(configItems);
        configListView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(FichaExportConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getName());
            }
        });
        configListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> onConfigSelected(n));

        // Filter rule list
        filterRuleListView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(FilterRule item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });

        // Classe combo
        List<Classe> classes = categoryManager.getClasses();
        filterClasseCombo.getItems().clear();
        for (Classe c : classes) filterClasseCombo.getItems().add(c.getCode() + " – " + c.getDesc());
        filterClasseCombo.setOnAction(e -> refreshSubClasseCombo());

        // Block list
        blockListView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(BlockConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });
        blockListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> onBlockSelected(n));

        // Block type combo
        blockTypeCombo.setItems(FXCollections.observableArrayList(BlockType.values()));

        // Relation type combo
        relationTypeCombo.setItems(FXCollections.observableArrayList(TIPO_RELACAO.values()));

        // Field source combo
        fieldSourceCombo.setItems(FXCollections.observableArrayList(FieldSource.values()));
        fieldSourceCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isLiteral = newVal == FieldSource.LITERAL;
            if (fieldLiteralLabel != null) {
                fieldLiteralLabel.setVisible(isLiteral);
                fieldLiteralLabel.setManaged(isLiteral);
            }
            if (fieldLiteralField != null) {
                fieldLiteralField.setVisible(isLiteral);
                fieldLiteralField.setManaged(isLiteral);
            }
        });

        // Field list
        fieldListView.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(FieldConfig item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toString());
            }
        });
        fieldListView.getSelectionModel().selectedItemProperty()
                .addListener((obs, o, n) -> onFieldSelected(n));

        // Load saved configs
        configItems.setAll(FichaExportConfigManager.getInstance().load());
        if (!configItems.isEmpty()) configListView.getSelectionModel().selectFirst();
    }

    // ── Config list actions ────────────────────────────────────────────────

    @FXML private void handleNew() {
        FichaExportConfig cfg = new FichaExportConfig();
        cfg.setName("Nova Configuração " + (configItems.size() + 1));
        configItems.add(cfg);
        configListView.getSelectionModel().select(cfg);
    }

    @FXML private void handleDuplicate() {
        FichaExportConfig sel = configListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        try {
            // Round-trip through JSON for deep copy
            FichaExportConfigManager mgr = FichaExportConfigManager.getInstance();
            File tmp = File.createTempFile("ficha-dup-", ".json");
            tmp.deleteOnExit();
            mgr.exportToFile(sel, tmp);
            FichaExportConfig copy = mgr.importFromFile(tmp);
            copy.setName(sel.getName() + " (cópia)");
            configItems.add(copy);
            configListView.getSelectionModel().select(copy);
        } catch (IOException e) {
            showError("Erro ao duplicar configuração", e.getMessage());
        }
    }

    @FXML private void handleDelete() {
        FichaExportConfig sel = configListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Remover a configuração \"" + sel.getName() + "\"?", ButtonType.OK, ButtonType.CANCEL);
        confirm.setTitle("Confirmar remoção");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            configItems.remove(sel);
        }
    }

    @FXML private void handleRename() {
        FichaExportConfig sel = configListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        TextInputDialog dlg = new TextInputDialog(sel.getName());
        dlg.setTitle("Renomear");
        dlg.setHeaderText("Novo nome:");
        dlg.showAndWait().ifPresent(name -> {
            if (!name.isBlank()) {
                sel.setName(name);
                configListView.refresh();
            }
        });
    }

    @FXML private void handleImport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Importar Configuração");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File file = fc.showOpenDialog(getStage());
        if (file == null) return;
        try {
            FichaExportConfig imported = FichaExportConfigManager.getInstance().importFromFile(file);
            configItems.add(imported);
            configListView.getSelectionModel().select(imported);
            status("Importado: " + imported.getName());
        } catch (IOException e) {
            showError("Erro ao importar", e.getMessage());
        }
    }

    @FXML private void handleExportJson() {
        FichaExportConfig sel = configListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        FileChooser fc = new FileChooser();
        fc.setTitle("Exportar Configuração");
        fc.setInitialFileName(sel.getName().replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".json");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON", "*.json"));
        File file = fc.showSaveDialog(getStage());
        if (file == null) return;
        try {
            FichaExportConfigManager.getInstance().exportToFile(sel, file);
            status("Exportado para: " + file.getName());
        } catch (IOException e) {
            showError("Erro ao exportar", e.getMessage());
        }
    }

    // ── Filter rule actions ────────────────────────────────────────────────

    @FXML private void handleAddFilter() {
        FichaExportConfig cfg = selectedConfig();
        if (cfg == null) return;

        int classeIdx = filterClasseCombo.getSelectionModel().getSelectedIndex();
        if (classeIdx < 0) {
            showError("Série não selecionada", "Selecione uma série antes de adicionar o filtro.");
            return;
        }
        Classe classe = categoryManager.getClasseForIndex(classeIdx);

        String subVal = filterSubClasseCombo.getValue();
        String subCode = null;
        if (subVal != null && !subVal.isBlank()) {
            subCode = subVal.contains("–") ? subVal.split("–")[0].trim() : subVal.trim();
        }

        FilterRule rule = new FilterRule(classe.getCode(), subCode);
        cfg.getFilterRules().add(rule);
        filterRuleListView.setItems(FXCollections.observableArrayList(cfg.getFilterRules()));
    }

    @FXML private void handleRemoveFilter() {
        FichaExportConfig cfg = selectedConfig();
        if (cfg == null) return;
        FilterRule sel = filterRuleListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        cfg.getFilterRules().remove(sel);
        filterRuleListView.setItems(FXCollections.observableArrayList(cfg.getFilterRules()));
    }


    private void refreshSubClasseCombo() {
        int idx = filterClasseCombo.getSelectionModel().getSelectedIndex();
        filterSubClasseCombo.getItems().clear();
        filterSubClasseCombo.getItems().add(""); // blank = class-only
        if (idx >= 0) {
            List<SubClasse> subs = categoryManager.getSubClassesForIndex(idx);
            for (SubClasse sc : subs) filterSubClasseCombo.getItems().add(sc.getCode() + " – " + sc.getDesc());
        }
        filterSubClasseCombo.getSelectionModel().clearSelection();
    }

    // ── Block list actions ─────────────────────────────────────────────────

    @FXML private void handleAddBlock() {
        FichaExportConfig cfg = selectedConfig();
        if (cfg == null) return;
        BlockConfig block = new BlockConfig();
        block.setType(BlockType.INFO);
        block.setEnabled(true);
        cfg.getBlocks().add(block);
        blockListView.setItems(FXCollections.observableArrayList(cfg.getBlocks()));
        blockListView.getSelectionModel().selectLast();
    }

    @FXML private void handleRemoveBlock() {
        FichaExportConfig cfg = selectedConfig();
        if (cfg == null) return;
        BlockConfig sel = blockListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        cfg.getBlocks().remove(sel);
        blockListView.setItems(FXCollections.observableArrayList(cfg.getBlocks()));
        blockEditorPane.setVisible(false);
        blockEditorPane.setManaged(false);
    }

    @FXML private void handleMoveBlockUp() {
        FichaExportConfig cfg = selectedConfig();
        if (cfg == null) return;
        int idx = blockListView.getSelectionModel().getSelectedIndex();
        if (idx <= 0) return;
        BlockConfig block = cfg.getBlocks().remove(idx);
        cfg.getBlocks().add(idx - 1, block);
        blockListView.setItems(FXCollections.observableArrayList(cfg.getBlocks()));
        blockListView.getSelectionModel().select(idx - 1);
    }

    @FXML private void handleMoveBlockDown() {
        FichaExportConfig cfg = selectedConfig();
        if (cfg == null) return;
        int idx = blockListView.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= cfg.getBlocks().size() - 1) return;
        BlockConfig block = cfg.getBlocks().remove(idx);
        cfg.getBlocks().add(idx + 1, block);
        blockListView.setItems(FXCollections.observableArrayList(cfg.getBlocks()));
        blockListView.getSelectionModel().select(idx + 1);
    }

    private void onBlockSelected(BlockConfig block) {
        if (block == null) {
            blockEditorPane.setVisible(false);
            blockEditorPane.setManaged(false);
            return;
        }
        blockEditorPane.setVisible(true);
        blockEditorPane.setManaged(true);

        suppressListeners = true;
        try {
            blockTypeCombo.setValue(block.getType());
            blockEnabledCheck.setSelected(block.isEnabled());
            blockLabelField.setText(block.getLabel() != null ? block.getLabel() : "");
            relationTypeCombo.setValue(block.getRelationType());
            updateBlockTypeUI(block.getType());
            fieldListView.setItems(FXCollections.observableArrayList(block.getFields()));
        } finally {
            suppressListeners = false;
        }
        refreshPreview();
    }

    @FXML private void handleBlockTypeChanged() {
        if (suppressListeners) return;
        BlockConfig sel = blockListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        BlockType chosen = blockTypeCombo.getValue();
        sel.setType(chosen);
        blockListView.refresh();
        updateBlockTypeUI(chosen);
        refreshPreview();
    }

    private void updateBlockTypeUI(BlockType type) {
        boolean isRelated = type == BlockType.RELATED_STUDIES;
        relatedStudiesPane.setVisible(isRelated);
        relatedStudiesPane.setManaged(isRelated);
        fieldListPane.setVisible(!isRelated);
        fieldListPane.setManaged(!isRelated);

        blockLayoutHintLabel.setText(switch (type) {
            case TITLE -> "📄 Campos exibidos na mesma linha (inline).";
            case RELATED_STUDIES -> "🔗 Referências geradas automaticamente — os campos controlam apenas o cabeçalho.";
            default -> "↩ Cada campo exibido em linha própria.";
        });
    }

    // ── Field list actions ─────────────────────────────────────────────────

    @FXML private void handleAddField() {
        BlockConfig block = blockListView.getSelectionModel().getSelectedItem();
        if (block == null) return;
        FieldConfig field = new FieldConfig();
        field.setSource(FieldSource.LITERAL);
        block.getFields().add(field);
        fieldListView.setItems(FXCollections.observableArrayList(block.getFields()));
        fieldListView.getSelectionModel().selectLast();
        refreshPreview();
    }

    @FXML private void handleRemoveField() {
        BlockConfig block = blockListView.getSelectionModel().getSelectedItem();
        if (block == null) return;
        FieldConfig sel = fieldListView.getSelectionModel().getSelectedItem();
        if (sel == null) return;
        block.getFields().remove(sel);
        fieldListView.setItems(FXCollections.observableArrayList(block.getFields()));
        refreshPreview();
    }

    @FXML private void handleMoveFieldUp() {
        BlockConfig block = blockListView.getSelectionModel().getSelectedItem();
        if (block == null) return;
        int idx = fieldListView.getSelectionModel().getSelectedIndex();
        if (idx <= 0) return;
        FieldConfig f = block.getFields().remove(idx);
        block.getFields().add(idx - 1, f);
        fieldListView.setItems(FXCollections.observableArrayList(block.getFields()));
        fieldListView.getSelectionModel().select(idx - 1);
        refreshPreview();
    }

    @FXML private void handleMoveFieldDown() {
        BlockConfig block = blockListView.getSelectionModel().getSelectedItem();
        if (block == null) return;
        int idx = fieldListView.getSelectionModel().getSelectedIndex();
        if (idx < 0 || idx >= block.getFields().size() - 1) return;
        FieldConfig f = block.getFields().remove(idx);
        block.getFields().add(idx + 1, f);
        fieldListView.setItems(FXCollections.observableArrayList(block.getFields()));
        fieldListView.getSelectionModel().select(idx + 1);
        refreshPreview();
    }

    private void onFieldSelected(FieldConfig field) {
        if (field == null) {
            if (fieldEditPane != null) {
                fieldEditPane.setVisible(false);
                fieldEditPane.setManaged(false);
            }
            return;
        }
        if (fieldEditPane != null) {
            fieldEditPane.setVisible(true);
            fieldEditPane.setManaged(true);
        }
        suppressListeners = true;
        try {
            fieldSourceCombo.setValue(field.getSource());
            fieldLabelField.setText(field.getLabel() != null ? field.getLabel() : "");
            fieldLiteralField.setText(field.getLiteralValue() != null ? field.getLiteralValue() : "");
            fieldFormatField.setText(field.getFormat() != null ? field.getFormat() : "");
            fieldLabelBoldCheck.setSelected(field.isLabelBold());
            fieldValueBoldCheck.setSelected(field.isValueBold());
            fieldItalicCheck.setSelected(field.isItalic());
        } finally {
            suppressListeners = false;
        }
    }

    @FXML private void handleApplyField() {
        BlockConfig block = blockListView.getSelectionModel().getSelectedItem();
        if (block == null) return;
        FieldConfig field = fieldListView.getSelectionModel().getSelectedItem();
        if (field == null) return;

        field.setSource(fieldSourceCombo.getValue());
        String label = fieldLabelField.getText().trim();
        field.setLabel(label.isBlank() ? null : label);
        String literal = fieldLiteralField.getText();
        field.setLiteralValue(literal.isBlank() ? null : literal);
        String fmt = fieldFormatField.getText().trim();
        field.setFormat(fmt.isBlank() ? null : fmt);
        field.setLabelBold(fieldLabelBoldCheck.isSelected());
        field.setValueBold(fieldValueBoldCheck.isSelected());
        field.setItalic(fieldItalicCheck.isSelected());

        fieldListView.setItems(FXCollections.observableArrayList(block.getFields()));
        refreshPreview();
    }

    // ── Preview ───────────────────────────────────────────────────────────

    /** Hard-coded sample values shown in the preview for each FieldSource. */
    private static String sampleValue(FieldSource source) {
        return switch (source) {
            case TITULO                    -> "A Vida do Teatro";
            case SUBTITULO                 -> "Um Drama Moderno";
            case AUTORES                   -> "SILVA, João António.";
            case TESTEMUNHO                -> "A";
            case CODIGO                    -> "01d.001";
            case ANO                       -> "1987";
            case LOCAL_PUBLICACAO          -> "Lisboa";
            case NUM_PAGINA                -> "45";
            case INSTITUICAO_CUSTODIA      -> "Biblioteca Nacional de Portugal";
            case DESCRICAO                 -> "Texto dramático em três atos que explora os conflitos entre personagens.";
            case TEXTO_TEATRAL_PERSONAGENS -> "8";
            case TEXTO_TEATRAL_ATOS        -> "3";
            case TEXTO_TEATRAL_CENAS       -> "12";
            case TEXTO_TEATRAL_RESUMO      -> "Peça que explora os conflitos humanos em contexto familiar.";
            case TEXTO_TEATRAL_PALAVRAS_CHAVE -> "teatro. drama. conflito.";
            case LITERAL                   -> "";
        };
    }

    private String resolvePreviewValue(FieldConfig field) {
        String raw = field.getSource() == FieldSource.LITERAL
                ? (field.getLiteralValue() != null ? field.getLiteralValue() : "")
                : sampleValue(field.getSource());
        if (raw.isEmpty()) return "";
        if (field.getFormat() != null && !field.getFormat().isBlank()) {
            try {
                return String.format(field.getFormat(), Integer.parseInt(raw));
            } catch (NumberFormatException nfe) {
                try { return String.format(field.getFormat(), raw); } catch (Exception ignored) { return raw; }
            }
        }
        return raw;
    }

    @FXML private void handleRefreshPreview() { refreshPreview(); }

    @FXML private void handleFormatInfo() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Formato do Campo");
        alert.setHeaderText("Como usar o campo Formato");
        Label content = new Label("O formato utiliza a sintaxe padrão do Java (printf) para formatar o valor. Se for deixado em branco, o valor será exibido normalmente.\n\n" +
                                  "Exemplos úteis para formatação:\n\n" +
                                  "• Adicionando letras: %df (ex: 45 vira 45f)\n" +
                                  "• Adicionando texto: p. %s (ex: 45 vira p. 45)\n" +
                                  "• Preenchendo com zeros à esquerda: %02d (ex: 3 vira 03)");
        content.setWrapText(true);
        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void refreshPreview() {
        if (previewTextFlow == null) return;
        BlockConfig block = blockListView.getSelectionModel().getSelectedItem();
        previewTextFlow.getChildren().clear();
        if (block == null) {
            Text t = createPreviewText("Selecione um grupo para ver a pré-visualização.", false, false);
            t.setStyle(t.getStyle() + " -fx-fill: gray;");
            previewTextFlow.getChildren().add(t);
            return;
        }

        switch (block.getType()) {
            case RELATED_STUDIES -> {
                String heading = (block.getLabel() != null && !block.getLabel().isBlank())
                        ? block.getLabel() : "Estudos";
                previewTextFlow.getChildren().add(createPreviewText(heading + ": \n", true, false));
                previewTextFlow.getChildren().add(createPreviewText("AUTOR, Nome. Título do estudo. Local: Editora, 1987. [referência NBR gerada automaticamente]\n", false, false));
            }
            case TITLE -> {
                // All fields inline on one line
                for (FieldConfig f : block.getFields()) {
                    String value = resolvePreviewValue(f);
                    if (!value.isEmpty()) {
                        previewTextFlow.getChildren().add(createPreviewText(value, f.isValueBold(), f.isItalic()));
                    }
                }
                previewTextFlow.getChildren().add(createPreviewText("\n", false, false));
            }
            default -> {
                // Block label as heading
                if (block.getLabel() != null && !block.getLabel().isBlank()) {
                    previewTextFlow.getChildren().add(createPreviewText(block.getLabel() + "\n", true, false));
                }
                for (FieldConfig f : block.getFields()) {
                    String value = resolvePreviewValue(f);
                    if (value.isEmpty()) continue;
                    if (f.getLabel() != null && !f.getLabel().isBlank()) {
                        previewTextFlow.getChildren().add(createPreviewText(f.getLabel() + ": ", f.isLabelBold(), false));
                    }
                    previewTextFlow.getChildren().add(createPreviewText(value + "\n", f.isValueBold(), f.isItalic()));
                }
            }
        }
    }

    private Text createPreviewText(String content, boolean bold, boolean italic) {
        Text t = new Text(content);
        t.setStyle("-fx-font-family: 'Times New Roman'; -fx-font-size: 13px;");
        if (bold && italic) {
            t.setStyle(t.getStyle() + " -fx-font-weight: bold; -fx-font-style: italic;");
        } else if (bold) {
            t.setStyle(t.getStyle() + " -fx-font-weight: bold;");
        } else if (italic) {
            t.setStyle(t.getStyle() + " -fx-font-style: italic;");
        }
        return t;
    }

    // ── Footer actions ─────────────────────────────────────────────────────

    @FXML private void handleSave() {
        flushBlockEditorToModel();
        List<FichaExportConfig> list = new ArrayList<>(configItems);
        try {
            FichaExportConfigManager.getInstance().save(list);
            status("Configurações salvas.");
        } catch (Exception e) {
            showError("Erro ao salvar", e.getMessage());
        }
    }

    @FXML private void handleExportNow() {
        handleSave();
        FichaExportConfig sel = configListView.getSelectionModel().getSelectedItem();
        if (sel == null) {
            showError("Nenhuma configuração selecionada", "Selecione uma configuração antes de exportar.");
            return;
        }
        new FichaExporter(sel).export(RepositoryManager.getEntries());
        status("Exportação iniciada.");
    }

    @FXML private void handleClose() {
        getStage().close();
    }

    // ── Config selection ───────────────────────────────────────────────────

    private void onConfigSelected(FichaExportConfig cfg) {
        boolean none = cfg == null;
        filterPanel.setDisable(none);
        blocksPanel.setDisable(none);
        btnSave.setDisable(none);
        btnExportNow.setDisable(none);

        if (none) {
            filterRuleListView.setItems(FXCollections.emptyObservableList());
            blockListView.setItems(FXCollections.emptyObservableList());
            blockEditorPane.setVisible(false);
            blockEditorPane.setManaged(false);
            return;
        }
        filterRuleListView.setItems(FXCollections.observableArrayList(cfg.getFilterRules()));
        blockListView.setItems(FXCollections.observableArrayList(cfg.getBlocks()));
        blockEditorPane.setVisible(false);
        blockEditorPane.setManaged(false);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /**
     * Flushes inline block-editor state (enabled, label, relationType) to the selected block model
     * before saving. Field-level edits are flushed immediately via handleApplyField.
     */
    private void flushBlockEditorToModel() {
        BlockConfig block = blockListView.getSelectionModel().getSelectedItem();
        if (block == null) return;
        block.setEnabled(blockEnabledCheck.isSelected());
        String lbl = blockLabelField.getText().trim();
        block.setLabel(lbl.isBlank() ? null : lbl);
        if (blockTypeCombo.getValue() != null) block.setType(blockTypeCombo.getValue());
        if (block.getType() == BlockType.RELATED_STUDIES && relationTypeCombo.getValue() != null) {
            block.setRelationType(relationTypeCombo.getValue());
        }
    }

    private FichaExportConfig selectedConfig() {
        return configListView.getSelectionModel().getSelectedItem();
    }

    private Stage getStage() {
        return (Stage) configListView.getScene().getWindow();
    }

    private void status(String msg) {
        statusLabel.setText(msg);
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erro");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}









