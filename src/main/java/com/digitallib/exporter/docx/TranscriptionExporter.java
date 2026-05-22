package com.digitallib.exporter.docx;
import com.digitallib.model.TextBlock;
import org.apache.poi.xwpf.usermodel.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
/**
 * Exports a list of TextBlocks to DOCX (Apache POI) or plain TXT.
 */
public class TranscriptionExporter {
    public void toDocx(List<TextBlock> blocks, File dest) throws IOException {
        try (XWPFDocument doc = new XWPFDocument();
             FileOutputStream out = new FileOutputStream(dest)) {
            for (TextBlock block : blocks) {
                String text = block.displayText();
                boolean lowConf = block.getConfidence() < 0.8 && !block.isManuallyEdited();
                if (lowConf) text += " [⚠ confianca: " + String.format("%.0f%%", block.getConfidence() * 100) + "]";
                switch (block.getBlockType()) {


                     case HEADING:
                    case HOMENAGEM: {
                        XWPFParagraph p = doc.createParagraph();
                        p.setStyle("Heading2");
                        p.createRun().setText(text);
                        break;
                    }
                    case LIST: {
                        XWPFParagraph p = doc.createParagraph();
                        p.setStyle("ListBullet");
                        p.createRun().setText(text);
                        break;
                    }
                    case TABLE: {
                        XWPFTable table = doc.createTable(1, 1);
                        table.getRow(0).getCell(0).setText(text);
                        break;
                    }
                    case ESTROFE: {
                        XWPFParagraph p = doc.createParagraph();
                        p.setIndentationLeft(720);
                        XWPFRun run = p.createRun();
                        run.setItalic(true);
                        run.setText(text);
                        break;
                    }
                    default: {
                        XWPFParagraph p = doc.createParagraph();
                        p.createRun().setText(text);
                        break;
                    }
                }
            }
            doc.write(out);
        }
    }
    public void toTxt(List<TextBlock> blocks, File dest) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (TextBlock block : blocks) {
            String text = block.displayText();
            boolean lowConf = block.getConfidence() < 0.8 && !block.isManuallyEdited();
            sb.append(text);
            if (lowConf) sb.append(" [⚠ confianca: ").append(String.format("%.0f%%", block.getConfidence() * 100)).append("]");
            sb.append("\n\n");
        }
        Files.writeString(dest.toPath(), sb.toString());
    }
}
