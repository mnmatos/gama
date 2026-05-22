package com.digitallib.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TextBlock {
    @JsonProperty("id")
    private String id;
    @JsonProperty("orderIndex")
    private int orderIndex;
    @JsonProperty("blockType")
    private BlockType blockType;
    @JsonProperty("originalText")
    private String originalText;
    @JsonProperty("editedText")
    private String editedText;
    @JsonProperty("confidence")
    private double confidence;
    @JsonProperty("boundingHint")
    private String boundingHint;
    public TextBlock() {
        this.id = UUID.randomUUID().toString();
    }
    public TextBlock(int orderIndex, BlockType blockType, String originalText, double confidence) {
        this.id = UUID.randomUUID().toString();
        this.orderIndex = orderIndex;
        this.blockType = blockType;
        this.originalText = originalText;
        this.confidence = confidence;
    }
    public String displayText() {
        return (editedText != null && !editedText.isBlank()) ? editedText : originalText;
    }
    public boolean isManuallyEdited() {
        return editedText != null && !editedText.isBlank();
    }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public BlockType getBlockType() { return blockType; }
    public void setBlockType(BlockType blockType) { this.blockType = blockType; }
    public String getOriginalText() { return originalText; }
    public void setOriginalText(String originalText) { this.originalText = originalText; }
    public String getEditedText() { return editedText; }
    public void setEditedText(String editedText) { this.editedText = editedText; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getBoundingHint() { return boundingHint; }
    public void setBoundingHint(String boundingHint) { this.boundingHint = boundingHint; }
}