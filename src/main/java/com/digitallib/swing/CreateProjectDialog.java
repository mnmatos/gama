package com.digitallib.swing;

import com.digitallib.model.Project;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;

public class CreateProjectDialog extends JDialog {
    private JTextField nameField;
    private JTextField acervoField;
    private JTextField pathField;
    private JTextField folderNameField;
    private JCheckBox customFolderCheckBox;
    private JButton createButton;
    private JButton cancelButton;
    private JButton browseButton;
    private Project project;

    public CreateProjectDialog(Frame owner) {
        super(owner, "Create New Project", true);
        setLayout(new BorderLayout());
        setSize(480, 260);
        setLocationRelativeTo(owner);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        formPanel.add(new JLabel("Project Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Acervo:"));
        acervoField = new JTextField();
        formPanel.add(acervoField);

        formPanel.add(new JLabel("Folder Name:"));
        // folderNameField + custom checkbox in one panel
        folderNameField = new JTextField();
        folderNameField.setEnabled(false);
        customFolderCheckBox = new JCheckBox("Use custom folder name");
        JPanel folderPanel = new JPanel(new BorderLayout(5, 0));
        folderPanel.add(folderNameField, BorderLayout.CENTER);
        folderPanel.add(customFolderCheckBox, BorderLayout.EAST);
        formPanel.add(folderPanel);

        formPanel.add(new JLabel("Location:"));
        pathField = new JTextField();
        pathField.setEditable(false);
        browseButton = new JButton("Browse");

        JPanel pathPanel = new JPanel(new BorderLayout());
        pathPanel.add(pathField, BorderLayout.CENTER);
        pathPanel.add(browseButton, BorderLayout.EAST);
        formPanel.add(pathPanel);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        createButton = new JButton("Create");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Track the base folder selected by the user. The pathField shows the base folder or preview.
        final String[] baseFolder = new String[1];

        browseButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                baseFolder[0] = chooser.getSelectedFile().getAbsolutePath();
                updatePreview(baseFolder[0]);
            }
        });

        // Toggle enabling of custom folder name field
        customFolderCheckBox.addActionListener(e -> {
            boolean custom = customFolderCheckBox.isSelected();
            folderNameField.setEnabled(custom);
            if (!custom) {
                // reset folderNameField to sanitized project name preview
                folderNameField.setText(sanitizeName(nameField.getText()));
            }
            updatePreview(baseFolder[0]);
        });

        // Update folderName preview when project name changes (unless custom folder name is enabled)
        nameField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                if (!customFolderCheckBox.isSelected()) {
                    folderNameField.setText(sanitizeName(nameField.getText()));
                }
                updatePreview(baseFolder[0]);
            }

            @Override
            public void insertUpdate(DocumentEvent e) { update(); }

            @Override
            public void removeUpdate(DocumentEvent e) { update(); }

            @Override
            public void changedUpdate(DocumentEvent e) { update(); }
        });

        // initialize folderNameField as sanitized empty
        folderNameField.setText("");

        createButton.addActionListener(e -> {
            String rawProjectName = nameField.getText();
            String rawAcervo = acervoField.getText();
            String rawBase = baseFolder[0] != null ? baseFolder[0] : pathField.getText().trim();

            if (rawProjectName == null || rawProjectName.trim().isEmpty() || rawAcervo == null || rawAcervo.trim().isEmpty() || rawBase.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Project name, Acervo and Location are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String projectName = sanitizeName(rawProjectName);
            if (projectName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Project name is invalid after sanitization. Please choose a different name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String folderRaw = customFolderCheckBox.isSelected() ? folderNameField.getText() : projectName;
            String folderName = sanitizeName(folderRaw);
            if (folderName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Folder name is invalid after sanitization. Please choose a different folder name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File base = new File(rawBase);
            if (!base.exists() || !base.isDirectory()) {
                JOptionPane.showMessageDialog(this, "Selected base location is not a valid directory.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            File desired = new File(base, folderName);
            File target = desired;

            if (desired.exists()) {
                if (!desired.isDirectory()) {
                    JOptionPane.showMessageDialog(this, "A file with the same name exists and is not a directory.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // enforce uniqueness by finding a non-colliding name
                File unique = findUniqueFolder(base, folderName);
                if (!unique.getAbsolutePath().equals(desired.getAbsolutePath())) {
                    int ans = JOptionPane.showConfirmDialog(this, "Folder '" + desired.getAbsolutePath() + "' already exists. A non-conflicting folder will be created: '\n'" + unique.getAbsolutePath() + "\nProceed?", "Folder exists", JOptionPane.YES_NO_OPTION);
                    if (ans != JOptionPane.YES_OPTION) {
                        return;
                    }
                    target = unique;
                } else {
                    // desired exists and is same path (shouldn't happen because unique would differ), but keep it
                    int ans = JOptionPane.showConfirmDialog(this, "Folder '" + desired.getAbsolutePath() + "' already exists. Use this existing folder?", "Folder exists", JOptionPane.YES_NO_OPTION);
                    if (ans != JOptionPane.YES_OPTION) return;
                }
            } else {
                // attempt to create
                boolean created = target.mkdirs();
                if (!created && !target.exists()) {
                    // maybe permission problems
                    JOptionPane.showMessageDialog(this, "Could not create project folder: " + target.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // if target is not created yet but was unique, create it now
            if (!target.exists()) {
                boolean created = target.mkdirs();
                if (!created) {
                    JOptionPane.showMessageDialog(this, "Could not create project folder: " + target.getAbsolutePath(), "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            pathField.setText(target.getAbsolutePath());
            project = new Project(projectName, target.getAbsolutePath(), rawAcervo.trim());
            dispose();
        });

        cancelButton.addActionListener(e -> {
            project = null;
            dispose();
        });
    }

    private void updatePreview(String base) {
        String baseVal = base == null ? "" : base;
        String folder = customFolderCheckBox.isSelected() ? folderNameField.getText().trim() : sanitizeName(nameField.getText());
        if (baseVal.isEmpty()) {
            pathField.setText(folder);
        } else if (folder.isEmpty()) {
            pathField.setText(baseVal);
        } else {
            File preview = new File(baseVal, folder);
            pathField.setText(preview.getAbsolutePath());
        }
    }

    private static String sanitizeName(String input) {
        if (input == null) return "";
        String s = input.trim();
        if (s.isEmpty()) return "";
        // replace control characters first
        s = s.replaceAll("\\p{Cntrl}", "_");
        // Replace invalid file name characters for Windows/Unix
        s = s.replaceAll("[\\\\/:\"<>|?*]+", "_");
        // Remove trailing dots and spaces (Windows disallows)
        while (s.endsWith(".") || s.endsWith(" ")) {
            s = s.substring(0, s.length() - 1);
        }
        // Windows reserved names
        String[] reserved = {"CON","PRN","AUX","NUL","COM1","COM2","COM3","COM4","COM5","COM6","COM7","COM8","COM9","LPT1","LPT2","LPT3","LPT4","LPT5","LPT6","LPT7","LPT8","LPT9"};
        for (String r : reserved) {
            if (s.equalsIgnoreCase(r)) {
                s = s + "_";
                break;
            }
        }
        // Collapse multiple underscores
        s = s.replaceAll("_+", "_");
        // Final trim
        return s.trim();
    }

    private static File findUniqueFolder(File base, String name) {
        File candidate = new File(base, name);
        if (!candidate.exists()) return candidate;
        int i = 1;
        while (true) {
            File f = new File(base, name + "-" + i);
            if (!f.exists()) return f;
            i++;
            if (i > 10000) return f; // safety cap
        }
    }

    public Project getProject() {
        return project;
    }
}
