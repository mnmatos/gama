package com.digitallib;
import com.digitallib.model.BlockType;
import com.digitallib.model.TextBlock;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
/**
 * Reusable block-based text editor. Set readOnly=true for the comparison view.
 * Call setBlocks() to populate. Call getBlocks() to retrieve current state.
 */
public class BlockEditorController {
    @FXML private ListView<TextBlock> blockListView;
    @FXML private Button btnSplit;
    @FXML private Button btnMerge;
    @FXML private Button btnAddBlock;
    @FXML private Button btnRemove;
    private final ObservableList<TextBlock> blocks = FXCollections.observableArrayList();
    private boolean readOnly = false;
    // Drag support
    private static final DataFormat BLOCK_DRAG_FORMAT = new DataFormat("application/x-textblock-index");
    @FXML
    public void initialize() {
        blockListView.setItems(blocks);
        blockListView.setCellFactory(lv -> new BlockCell());
        blockListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        // Delete key removes selected blocks
        blockListView.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.DELETE && !readOnly) {
                removeSelected();
            }
        });
    }
    public void setBlocks(List<TextBlock> blockList) {
        blocks.setAll(blockList);
        reindex();
    }
    public List<TextBlock> getBlocks() {
        reindex();
        return new ArrayList<>(blocks);
    }
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        if (btnSplit != null) btnSplit.setDisable(readOnly);
        if (btnMerge != null) btnMerge.setDisable(readOnly);
        if (btnAddBlock != null) btnAddBlock.setDisable(readOnly);
        if (btnRemove != null) btnRemove.setDisable(readOnly);
    }
    /** Highlight a block at a given index with a CSS background (for comparison view). */
    public void setBlockHighlight(int index, String cssColor) {
        // Triggered by comparison controller; stored as userData for the cell to read
        if (index >= 0 && index < blocks.size()) {
            blocks.get(index).setBoundingHint("highlight:" + cssColor);
            blockListView.refresh();
        }
    }
    @FXML
    private void handleRemove() {
        removeSelected();
    }

    private void removeSelected() {
        List<TextBlock> selected = new ArrayList<>(blockListView.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) return;
        blocks.removeAll(selected);
        reindex();
    }

    @FXML
    private void handleSplit() {
        TextBlock selected = blockListView.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        int idx = blocks.indexOf(selected);
        // Get caret from last focused TextArea — approximated: split at midpoint of displayText
        String text = selected.displayText();
        int mid = text.length() / 2;
        // Try to split at last space before mid
        int splitAt = text.lastIndexOf(' ', mid);
        if (splitAt < 1) splitAt = mid;
        TextBlock first = new TextBlock(idx, selected.getBlockType(), selected.getOriginalText(), selected.getConfidence());
        first.setEditedText(text.substring(0, splitAt).trim());
        TextBlock second = new TextBlock(idx + 1, selected.getBlockType(), "", 1.0);
        second.setEditedText(text.substring(splitAt).trim());
        blocks.set(idx, first);
        blocks.add(idx + 1, second);
        reindex();
    }
    @FXML
    private void handleMerge() {
        List<TextBlock> selected = new ArrayList<>(blockListView.getSelectionModel().getSelectedItems());
        if (selected.size() < 2) return;
        selected.sort((a, b) -> Integer.compare(a.getOrderIndex(), b.getOrderIndex()));
        TextBlock base = selected.get(0);
        StringBuilder merged = new StringBuilder(base.displayText());
        for (int i = 1; i < selected.size(); i++) {
            merged.append("\n").append(selected.get(i).displayText());
            blocks.remove(selected.get(i));
        }
        base.setEditedText(merged.toString());
        blockListView.refresh();
        reindex();
    }
    @FXML
    private void handleAddBlock() {
        TextBlock block = new TextBlock(blocks.size(), BlockType.PARAGRAPH, "", 1.0);
        block.setEditedText("");
        blocks.add(block);
        blockListView.scrollTo(block);
        blockListView.getSelectionModel().select(block);
    }
    private void reindex() {
        for (int i = 0; i < blocks.size(); i++) {
            blocks.get(i).setOrderIndex(i);
        }
    }
    // ── Custom ListCell ────────────────────────────────────────────────────────
    private class BlockCell extends ListCell<TextBlock> {
        private final VBox card = new VBox(4);
        private final HBox header = new HBox(6);
        private final ComboBox<BlockType> typeCombo = new ComboBox<>();
        private final Label confidenceBadge = new Label();
        private final Button btnDelete = new Button("✕");
        private final TextArea textArea = new TextArea();
        private TextBlock currentBlock;
        BlockCell() {
            typeCombo.getItems().setAll(BlockType.values());
            typeCombo.setPrefWidth(130);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(3);
            VBox.setVgrow(textArea, Priority.ALWAYS);
            // Delete button style
            btnDelete.setStyle("-fx-text-fill: #c0392b; -fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 12;");
            btnDelete.setOnAction(e -> {
                if (currentBlock != null && !readOnly) {
                    blocks.remove(currentBlock);
                    reindex();
                }
            });
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            header.getChildren().addAll(typeCombo, confidenceBadge, spacer, btnDelete);
            card.getChildren().addAll(header, textArea);
            card.setStyle("-fx-padding: 4; -fx-border-color: #ccc; -fx-border-radius: 4; -fx-background-radius: 4;");
            // Write back editedText on change
            textArea.textProperty().addListener((obs, o, n) -> {
                if (currentBlock != null) currentBlock.setEditedText(n);
            });
            // Type change
            typeCombo.setOnAction(e -> {
                if (currentBlock != null) currentBlock.setBlockType(typeCombo.getValue());
            });
            // Drag-to-reorder
            setupDragAndDrop();
        }
        @Override
        protected void updateItem(TextBlock block, boolean empty) {
            super.updateItem(block, empty);
            currentBlock = null;
            if (empty || block == null) {
                setGraphic(null);
                return;
            }
            currentBlock = block;
            typeCombo.setValue(block.getBlockType());
            textArea.setText(block.displayText());
            typeCombo.setDisable(readOnly);
            textArea.setEditable(!readOnly);
            btnDelete.setVisible(!readOnly);
            // Confidence badge
            double conf = block.getConfidence();
            String confText = String.format("%.0f%%", conf * 100);
            boolean needsWarning = conf < 0.8 && !block.isManuallyEdited();
            confidenceBadge.setText(needsWarning ? "⚠ " + confText : confText);
            if (conf >= 0.8) confidenceBadge.setStyle("-fx-text-fill: green;");
            else if (conf >= 0.5) confidenceBadge.setStyle("-fx-text-fill: #b8860b;");
            else confidenceBadge.setStyle("-fx-text-fill: red;");
            // Highlight for comparison
            String hint = block.getBoundingHint();
            if (hint != null && hint.startsWith("highlight:")) {
                String color = hint.substring("highlight:".length());
                card.setStyle("-fx-padding: 4; -fx-border-color: #ccc; -fx-border-radius: 4; -fx-background-color: " + color + ";");
            } else {
                card.setStyle("-fx-padding: 4; -fx-border-color: #ccc; -fx-border-radius: 4;");
            }
            setGraphic(card);
        }
        private void setupDragAndDrop() {
            setOnDragDetected(event -> {
                if (getItem() == null || readOnly) return;
                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent cc = new ClipboardContent();
                cc.put(BLOCK_DRAG_FORMAT, String.valueOf(getIndex()));
                db.setContent(cc);
                event.consume();
            });
            setOnDragOver(event -> {
                if (event.getGestureSource() != this && event.getDragboard().hasContent(BLOCK_DRAG_FORMAT)) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });
            setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasContent(BLOCK_DRAG_FORMAT)) {
                    int from = Integer.parseInt((String) db.getContent(BLOCK_DRAG_FORMAT));
                    int to = getIndex();
                    if (from != to && from >= 0 && from < blocks.size() && to >= 0 && to < blocks.size()) {
                        TextBlock moved = blocks.remove(from);
                        blocks.add(to, moved);
                        reindex();
                    }
                    event.setDropCompleted(true);
                }
                event.consume();
            });
        }
    }
}
