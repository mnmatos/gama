package com.digitallib;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.Documento;
import com.digitallib.model.MultiSourcedDocument;
import com.digitallib.model.TextBlock;
import com.digitallib.model.TranscriptionRecord;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
public class TranscriptionComparisonController {
    private static final Logger logger = LogManager.getLogger(TranscriptionComparisonController.class);
    @FXML private Label titleLabel;
    @FXML private ScrollPane horizontalScroll;
    @FXML private HBox panelsBox;
    private final List<ScrollPane> panelScrolls = new ArrayList<>();
    private final List<BlockEditorController> editors = new ArrayList<>();
    @FXML
    public void initialize() {
    }
    public void setMultiDoc(MultiSourcedDocument multiDoc) {
        titleLabel.setText(multiDoc.getTitulo());
        List<Documento> allDocs = RepositoryManager.getEntries().stream()
                .filter(d -> multiDoc.getDocuments().contains(d.getCodigo()))
                .collect(Collectors.toList());
        // Show setup dialog so the user can choose which docs/images to compare
        Optional<List<DocImageSelection>> result = showSetupDialog(multiDoc.getTitulo(), allDocs);
        if (result.isEmpty()) return; // user cancelled
        List<DocImageSelection> selections = result.get().stream()
                .filter(s -> s.included)
                .collect(Collectors.toList());
        if (selections.isEmpty()) return;
        panelsBox.getChildren().clear();
        panelScrolls.clear();
        editors.clear();
        for (DocImageSelection sel : selections) {
            addPanel(sel.doc, sel.imageFile);
        }
        // Bind each panel's prefWidth so they share the viewport evenly
        int count = panelsBox.getChildren().size();
        if (count > 0) {
            panelsBox.getChildren().forEach(node -> {
                if (node instanceof VBox panel) {
                    panel.prefWidthProperty().bind(
                            horizontalScroll.widthProperty().divide(count).subtract(2));
                }
            });
        }
        applyDiffHighlights();
    }

    // ---------- setup dialog ----------

    /** Simple value holder for one document's selection. */
    private static class DocImageSelection {
        final Documento doc;
        String imageFile;
        boolean included;
        DocImageSelection(Documento doc, String imageFile, boolean included) {
            this.doc = doc; this.imageFile = imageFile; this.included = included;
        }
    }

    /**
     * Shows a Dialog where the user can deselect documents and choose which image
     * to use for each document.  Returns empty if cancelled.
     */
    private Optional<List<DocImageSelection>> showSetupDialog(String groupTitle, List<Documento> docs) {
        Dialog<List<DocImageSelection>> dialog = new Dialog<>();
        dialog.setTitle("Configurar comparação");
        dialog.setHeaderText("Grupo: " + groupTitle
                + "\nSelecione os testemunhos e as imagens a comparar:");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(12));

        List<DocImageSelection> selections = new ArrayList<>();
        List<CheckBox> checkBoxes = new ArrayList<>();
        List<ComboBox<String>> combos = new ArrayList<>();

        for (Documento doc : docs) {
            List<String> images = doc.getArquivos() == null ? Collections.emptyList()
                    : doc.getArquivos().stream().filter(this::isImage).collect(Collectors.toList());

            // default image: first with transcription, then first image
            String defaultImage = images.stream()
                    .filter(f -> doc.getTranscriptions() != null && doc.getTranscriptions().containsKey(f))
                    .findFirst().orElse(images.isEmpty() ? null : images.get(0));

            DocImageSelection sel = new DocImageSelection(doc, defaultImage, true);
            selections.add(sel);

            CheckBox cb = new CheckBox(doc.getCodigo()
                    + (doc.getTitulo() != null && !doc.getTitulo().isBlank() ? " — " + doc.getTitulo() : ""));
            cb.setSelected(true);
            cb.setStyle("-fx-font-weight: bold;");
            checkBoxes.add(cb);

            ComboBox<String> combo = new ComboBox<>();
            combo.getItems().addAll(images);
            if (defaultImage != null) combo.setValue(defaultImage);
            combo.setMaxWidth(Double.MAX_VALUE);
            combo.setDisable(images.isEmpty());
            combos.add(combo);

            // disable combo when doc is deselected
            cb.selectedProperty().addListener((obs, o, n) -> combo.setDisable(!n || images.isEmpty()));

            // wire changes back to selection object
            cb.selectedProperty().addListener((obs, o, n) -> sel.included = n);
            combo.valueProperty().addListener((obs, o, n) -> sel.imageFile = n);

            Label imgLabel = new Label("Imagem:");
            imgLabel.setPadding(new Insets(0, 0, 0, 20));
            HBox imgRow = new HBox(6, imgLabel, combo);
            HBox.setHgrow(combo, Priority.ALWAYS);
            imgRow.setPadding(new Insets(0, 0, 0, 20));

            Separator sep = new Separator();
            content.getChildren().addAll(cb, imgRow, sep);
        }

        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.setPrefViewportHeight(Math.min(400, docs.size() * 90 + 20));
        dialog.getDialogPane().setContent(sp);
        dialog.getDialogPane().setPrefWidth(520);

        dialog.setResultConverter(bt -> bt == ButtonType.OK ? selections : null);
        return dialog.showAndWait();
    }

    private void addPanel(Documento doc, String imageFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/BlockEditor.fxml"));
            VBox editorNode = loader.load();
            BlockEditorController editor = loader.getController();
            editor.setReadOnly(true);
            List<TextBlock> blocks = new ArrayList<>();
            if (imageFile != null && doc.getTranscriptions() != null) {
                TranscriptionRecord rec = doc.getTranscriptions().get(imageFile);
                if (rec != null && rec.getBlocks() != null) blocks = rec.getBlocks();
            }
            editor.setBlocks(blocks);
            editors.add(editor);
            // Panel header
            Label codeLabel = new Label(doc.getCodigo());
            codeLabel.setStyle("-fx-font-weight: bold;");
            String imgName = imageFile != null ? imageFile.replaceAll(".*/", "") : "(sem imagem)";
            Label imgLabel = new Label(imgName);
            imgLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");
            Label titleLbl = new Label(doc.getTitulo() != null ? doc.getTitulo() : "");
            VBox header = new VBox(2, codeLabel, titleLbl, imgLabel);
            header.setStyle("-fx-padding: 6; -fx-background-color: #eee;");
            // Scrollable block list
            ScrollPane scroll = new ScrollPane(editorNode);
            scroll.setFitToWidth(true);
            panelScrolls.add(scroll);
            VBox panel = new VBox(header, scroll);
            VBox.setVgrow(scroll, Priority.ALWAYS);
            HBox.setHgrow(panel, Priority.ALWAYS);
            panel.setMaxWidth(Double.MAX_VALUE);
            panel.setStyle("-fx-border-color: #aaa; -fx-border-width: 0 1 0 0;");
            panelsBox.getChildren().add(panel);
        } catch (IOException e) {
            logger.error("Failed to add comparison panel for doc {}", doc.getCodigo(), e);
        }
    }
    private void applyDiffHighlights() {
        if (editors.size() < 2) return;
        int maxBlocks = editors.stream().mapToInt(e -> e.getBlocks().size()).max().orElse(0);
        for (int i = 0; i < maxBlocks; i++) {
            final int idx = i;
            List<String> texts = editors.stream()
                    .map(e -> {
                        List<TextBlock> b = e.getBlocks();
                        return idx < b.size() ? b.get(idx).displayText() : null;
                    })
                    .collect(Collectors.toList());
            boolean anyNull = texts.stream().anyMatch(t -> t == null);
            boolean allSame = !anyNull && texts.stream().distinct().count() == 1;
            String color = null;
            if (anyNull) color = "#ffcccc";       // red — missing in some
            else if (!allSame) color = "#fff3cd";  // yellow — differs
            if (color != null) {
                final String c = color;
                editors.forEach(e -> e.setBlockHighlight(idx, c));
            }
        }
    }
    private boolean isImage(String f) {
        String lower = f.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".tif") || lower.endsWith(".tiff") || lower.endsWith(".webp");
    }
}
