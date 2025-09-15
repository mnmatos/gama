package com.digitallib.exporter;

import com.digitallib.exporter.LibExporter;
import com.digitallib.model.Documento;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class BaseFileExporter implements LibExporter {

    private Logger logger = LogManager.getLogger();

    protected void createFile(XWPFDocument document, String defaultName) {

        SwingUtilities.invokeLater(() -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Escolha o destino");
            fileChooser.setSelectedFile(new File(defaultName+".docx"));

            FileNameExtensionFilter docxFilter = new FileNameExtensionFilter("Word Documents (*.docx)", "docx");
            fileChooser.setFileFilter(docxFilter);

            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File exportFile = fileChooser.getSelectedFile();

                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(exportFile);
                    document.write(out);
                    out.close();
                } catch (FileNotFoundException e) {
                    failFileCreate(exportFile);
                } catch (IOException e) {
                    failFileCreate(exportFile);
                }
                logger.info(exportFile+" written successfully");
                JOptionPane.showMessageDialog(null,
                        "Arquivo criado em:\n" + exportFile.getAbsolutePath());
            } else {
                JOptionPane.showMessageDialog(null,
                        "Exportação cancelada.");
            }
        });
    }

    private void failFileCreate(File selectedFile) {
        logger.error("Failed to write : "+selectedFile);
        JOptionPane.showMessageDialog(null,
                "Falha ao criar arquivo em:\n" + selectedFile.getAbsolutePath());
    }
}
