package com.digitallib;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.Documento;
import com.digitallib.model.MultiSourcedDocument;
import com.digitallib.model.TextBlock;
import com.digitallib.model.TranscriptionRecord;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
public class TranscriptionComparisonController {
    private static final Logger logger = LogManager.getLogger(TranscriptionComparisonController.class);
    @FXML private Label titleLabel;
    @FXML private ScrollPane horizontalScroll;
    @FXML private HBox panelsBox;
    @FXML private ToggleButton syncScrollToggle;
    private final List<ScrollPane> panelScrolls = new ArrayList<>();
    private final List<BlockEditorController> editors = new ArrayList<>();
    private boolean syncingScroll = false;
    @FXML
    public void initialize() {
        syncScrollToggle.selectedProperty().addListener((obs, o, selected) -> {
            if (selected) syncAllScrollsTo(0);
        });
    }
    public void setMultiDoc(MultiSourcedDocument multiDoc) {
        titleLabel.setText(multiDoc.getTitulo());
        List<Documento> docs = RepositoryManager.getEntries().stream()
                .filter(d -> multiDoc.getDocuments().contains(d.getCodigo()))
                .collect(Collectors.toList());
        panelsBox.getChildren().clear();
        panelScrolls.clear();
        editors.clear();
        for (Documento doc : docs) {
            addPanel(doc);
        }
        applyDiffHighlights();
    }
    private void addPanel(Documento doc) {
        try {
            // Image picker — show first image with a transcription, or first image
            String imageFile = null;
            if (doc.getArquivos() != null) {
                // prefer first image that has a transcription
                imageFile = doc.getArquivos().stream()
                        .filter(f -> isImage(f) && doc.getTranscriptions().containsKey(f))
                        .findFirst()
                        .orElse(doc.getArquivos().stream().filter(this::isImage).findFirst().orElse(null));
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/BlockEditor.fxml"));
            VBox editorNode = loader.load();
            BlockEditorController editor = loader.getController();
            editor.setReadOnly(true);
            List<TextBlock> blocks = new ArrayList<>();
            if (imageFile != null) {
                TranscriptionRecord rec = doc.getTranscriptions().get(imageFile);
                if (rec != null && rec.getBlocks() != null) blocks = rec.getBlocks();
            }
            editor.setBlocks(blocks);
            editors.add(editor);
            // Panel header
            Label codeLabel = new Label(doc.getCodigo());
            codeLabel.setStyle("-fx-font-weight: bold;");
            Label titleLbl = new Label(doc.getTitulo() != null ? doc.getTitulo() : "");
            VBox header = new VBox(2, codeLabel, titleLbl);
            header.setStyle("-fx-padding: 6; -fx-background-color: #eee;");
            // Scrollable block list
            ScrollPane scroll = new ScrollPane(editorNode);
            scroll.setFitToWidth(true);
            scroll.setPrefWidth(340);
            scroll.vvalueProperty().addListener((obs, o, n) -> {
                if (syncScrollToggle.isSelected() && !syncingScroll) {
                    syncingScroll = true;
                    syncAllScrollsTo(n.doubleValue());
                    syncingScroll = false;
                }
            });
            panelScrolls.add(scroll);
            VBox panel = new VBox(header, scroll);
            VBox.setVgrow(scroll, Priority.ALWAYS);
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
    private void syncAllScrollsTo(double vvalue) {
        for (ScrollPane sp : panelScrolls) sp.setVvalue(vvalue);
    }
    private boolean isImage(String f) {
        String lower = f.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")
                || lower.endsWith(".tif") || lower.endsWith(".tiff") || lower.endsWith(".webp");
    }
}
