package com.digitallib.reference.block;

public class ReferenceBlock {
    String content;
    boolean bold;
    boolean italic;

    public ReferenceBlock(String content, boolean bold, boolean italic) {
        this.content = content;
        this.bold = bold;
        this.italic = italic;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isItalic() {
        return italic;
    }

    public void setItalic(boolean italic) {
        this.italic = italic;
    }
}
