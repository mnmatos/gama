package com.digitallib;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.digitallib.manager.RepositoryManager.getPathFromCode;
import static com.digitallib.manager.RepositoryManager.removeFiles;

public class FileActionDialog extends JDialog {
    private static final Logger logger = LogManager.getLogger(FileActionDialog.class);

    private JPanel contentPane;
    private JButton openButton;
    private JLabel fileTypeLabel;
    private JButton openFolderButton;
    private JButton removeFileButton;
    private JLabel fileNameLabel;
    private JButton analyzePdfButton;

    private File file;
    private String editedDocCode;
    private List<File> files;
    private Runnable onRemove;

    public FileActionDialog(File file, String editedDocCode, List<File> files, Runnable onRemove) {
        this.file = file;
        this.editedDocCode = editedDocCode;
        this.files = files;
        this.onRemove = onRemove;

        setContentPane(contentPane);
        setModal(true);

        fileNameLabel.setText(file.getName());

        openButton.addActionListener(e -> openFile());
        openFolderButton.addActionListener(e -> openFolder());
        removeFileButton.addActionListener(e -> removeFile());
        analyzePdfButton.addActionListener(e -> analyzePdf());

        loadFileIcon();
    }

    private void analyzePdf() {
        PdfAnalysisDialog dialog = new PdfAnalysisDialog(file);
        dialog.pack();
        dialog.setVisible(true);
    }

    private void openFile() {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            logger.error("Erro ao abrir arquivo: " + file.getAbsolutePath(), e);
            JOptionPane.showMessageDialog(this, "Não foi possível abrir o arquivo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openFolder() {
        if (editedDocCode != null) {
            String path = getPathFromCode(editedDocCode);
            File fileFolder = new File(path);
            try {
                Process p = new ProcessBuilder("explorer.exe", "/select," + fileFolder.getCanonicalPath()).start();
            } catch (IOException ex) {
                logger.error("Erro ao abrir pasta do arquivo: " + fileFolder.getAbsolutePath(), ex);
            }
        } else {
            // Handle case where editedDocCode is null
        }
    }

    private void removeFile() {
        int result = JOptionPane.showConfirmDialog(this, String.format("Você tem certeza que quer remover o arquivo %s?", file.getName()), "Atenção!!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            if (editedDocCode == null) { //File don't need to be deleted, just removed from list as it hasn't been copied yet
                files.removeIf(f -> f.getName().equals(file.getName()));
            } else {
                removeFiles(editedDocCode, Collections.singletonList(file.getName()));
                files.removeIf(f -> f.getName().equals(file.getName()));
            }
            onRemove.run();
            dispose();
        }
    }

    private void loadFileIcon() {
        String fileName = file.getName();
        String fileExtension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            fileExtension = fileName.substring(i + 1);
        }

        analyzePdfButton.setVisible("pdf".equalsIgnoreCase(fileExtension));

        String iconPath = "/icons/";
        switch (fileExtension.toLowerCase()) {
            case "pdf":
                iconPath += "pdf.png";
                break;
            case "txt":
                iconPath += "txt.png";
                break;
            default:
                iconPath += "file.png";
                break;
        }

        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(iconPath));
            fileTypeLabel.setIcon(icon);
        } catch (Exception e) {
            logger.error("Ícone não encontrado: " + iconPath, e);
            fileTypeLabel.setText("Ícone não encontrado para " + fileExtension);
        }
    }

    public static void main(String[] args) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            FileActionDialog dialog = new FileActionDialog(selectedFile, null, null, () -> {
            });
            dialog.setVisible(true);
        }
        System.exit(0);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        openButton = new JButton();
        openButton.setText("Abrir");
        panel1.add(openButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        openFolderButton = new JButton();
        openFolderButton.setText("Abrir Pasta");
        panel1.add(openFolderButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        removeFileButton = new JButton();
        removeFileButton.setText("Remover Arquivo");
        panel1.add(removeFileButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        analyzePdfButton = new JButton();
        analyzePdfButton.setText("Analisar PDF");
        panel1.add(analyzePdfButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        fileTypeLabel = new JLabel();
        contentPane.add(fileTypeLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        fileNameLabel = new JLabel();
        fileNameLabel.setText("File Name");
        contentPane.add(fileNameLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
