package com.digitallib.swing;

import com.digitallib.MainApp;
import com.digitallib.model.Project;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ProjectManager extends JFrame {

    private static final File PROJECTS_FILE = new File(System.getProperty("user.home"), ".gama_projects.json");

    private static final Logger logger = LogManager.getLogger(ProjectManager.class);

    private JList<Project> projectList;
    private DefaultListModel<Project> listModel;

    public ProjectManager() {
        setTitle("Gerenciador de Projetos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null);

        listModel = new DefaultListModel<>();
        projectList = new JList<>(listModel);
        projectList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Load saved projects
        loadProjectList();

        JScrollPane scrollPane = new JScrollPane(projectList);
        scrollPane.setPreferredSize(new Dimension(480, 300));

        JButton selectButton = new JButton("Selecionar Projeto");
        selectButton.setFont(new Font("Arial", Font.PLAIN, 16));
        selectButton.addActionListener(e -> selectProject());

        JButton deleteButton = new JButton("Excluir Projeto");
        deleteButton.setFont(new Font("Arial", Font.PLAIN, 16));
        deleteButton.addActionListener(e -> deleteProject());

        JButton importButton = new JButton("Importar Projeto");
        importButton.setFont(new Font("Arial", Font.PLAIN, 16));
        importButton.addActionListener(e -> importProject());

        JButton exportButton = new JButton("Exportar Projeto");
        exportButton.setFont(new Font("Arial", Font.PLAIN, 16));
        exportButton.addActionListener(e -> exportProject());

        JButton createButton = new JButton("Criar Projeto");
        createButton.setFont(new Font("Arial", Font.PLAIN, 16));
        createButton.addActionListener(e -> createProject());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton);
        buttonPanel.add(selectButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(importButton);
        buttonPanel.add(exportButton);

        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void selectProject() {
        Project selectedProject = projectList.getSelectedValue();
        if (selectedProject != null) {
            System.setProperty("selected.project.path", selectedProject.getPath());
            System.setProperty("acervo", selectedProject.getAcervo());
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                new MainApp().setVisible(true);
            });
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um projeto.");
        }
    }

    private void deleteProject() {
        Project selectedProject = projectList.getSelectedValue();
        if (selectedProject != null) {
            String prompt = "Para confirmar a exclusão do projeto '" + selectedProject.getName() + "', digite 'excluir' (sem aspas) e pressione OK.\nEsta ação não pode ser desfeita.";
            String input = (String) JOptionPane.showInputDialog(this, prompt, "Confirmar exclusão", JOptionPane.WARNING_MESSAGE, null, null, "");
            if (input == null) {
                // usuario cancelou
                return;
            }
            String trimmed = input.trim();
            if (!("delete".equalsIgnoreCase(trimmed) || "excluir".equalsIgnoreCase(trimmed))) {
                JOptionPane.showMessageDialog(this, "Exclusão cancelada.");
                return;
            }

            // usuario confirmou digitando 'excluir' ou 'delete'
            File projectDir = new File(selectedProject.getPath());
            if (projectDir.exists()) {
                int deleteFiles = JOptionPane.showConfirmDialog(this, "Excluir arquivos do projeto no disco também?", "Excluir arquivos", JOptionPane.YES_NO_OPTION);
                if (deleteFiles == JOptionPane.YES_OPTION) {
                    boolean deleted = deleteDirectoryRecursively(projectDir);
                    if (!deleted) {
                        JOptionPane.showMessageDialog(this, "Não foi possível excluir a pasta do projeto: " + projectDir.getAbsolutePath(), "Erro", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            listModel.removeElement(selectedProject);
            saveProjectList();
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um projeto para excluir.");
        }
    }

    // Delete a directory and all its contents. Returns true on success.
    private boolean deleteDirectoryRecursively(File dir) {
        if (dir == null) return false;
        if (!dir.exists()) return true;
        File[] entries = dir.listFiles();
        if (entries != null) {
            for (File entry : entries) {
                if (entry.isDirectory()) {
                    if (!deleteDirectoryRecursively(entry)) return false;
                } else {
                    if (!entry.delete()) return false;
                }
            }
        }
        return dir.delete();
    }

    private void createProject() {
        CreateProjectDialog dialog = new CreateProjectDialog(this);
        dialog.setVisible(true);
        Project newProject = dialog.getProject();

        if (newProject != null) {
            listModel.addElement(newProject);
            saveProjectList();
        }
    }

    private void importProject() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Importar Projeto");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Arquivos de Projeto", "proj", "json"));

        int userSelection = fileChooser.showOpenDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToOpen = fileChooser.getSelectedFile();
            try {
                ObjectMapper mapper = new ObjectMapper();
                // try reading a list first
                if (fileToOpen.length() == 0) return;
                List<Project> projects;
                try {
                    projects = mapper.readValue(fileToOpen, new TypeReference<List<Project>>(){});
                } catch (Exception ex) {
                    // fallback: single project
                    Project p = mapper.readValue(fileToOpen, Project.class);
                    projects = new ArrayList<>();
                    projects.add(p);
                }

                for (Project project : projects) {
                    listModel.addElement(project);
                }

                saveProjectList();
            } catch (IOException e) {
                logger.error("Erro ao importar projeto", e);
                JOptionPane.showMessageDialog(this, "Erro ao importar projeto: " + e.getMessage());
            }
        }
    }

    private void exportProject() {
        Project selectedProject = projectList.getSelectedValue();
        if (selectedProject != null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Exportar Projeto");
            fileChooser.setSelectedFile(new File(selectedProject.getName() + ".proj"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.writerWithDefaultPrettyPrinter().writeValue(fileToSave, selectedProject);

                    JOptionPane.showMessageDialog(this, "Projeto exportado com sucesso.");
                } catch (IOException e) {
                    logger.error("Erro ao exportar projeto", e);
                    JOptionPane.showMessageDialog(this, "Erro ao exportar projeto: " + e.getMessage());
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Por favor, selecione um projeto para exportar.");
        }
    }

    private void saveProjectList() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Project> projects = new ArrayList<>();
            for (int i = 0; i < listModel.getSize(); i++) {
                projects.add(listModel.getElementAt(i));
            }
            mapper.writerWithDefaultPrettyPrinter().writeValue(PROJECTS_FILE, projects);
        } catch (IOException e) {
            logger.error("Erro ao salvar lista de projetos", e);
        }
    }

    private void loadProjectList() {
        if (!PROJECTS_FILE.exists()) return;
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Project> projects = mapper.readValue(PROJECTS_FILE, new TypeReference<List<Project>>(){});
            for (Project p : projects) listModel.addElement(p);
        } catch (IOException e) {
            logger.error("Erro ao carregar lista de projetos", e);
        }
    }

    public static void main() {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ProjectManager frame = new ProjectManager();
                    frame.setVisible(true);
                } catch (Exception e) {
                    logger.error("Erro ao iniciar ProjectManager", e);
                }
            }
        });
    }
}
