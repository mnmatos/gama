package com.digitallib.manager;

import com.digitallib.model.Project;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;

public class ProjectManager {
    private static final Logger logger = LogManager.getLogger(ProjectManager.class);
    private static ProjectManager instance;
    private Project currentProject;
    private ObjectMapper mapper = new ObjectMapper();

    private ProjectManager() {
        loadCurrentProject();
    }

    public static synchronized ProjectManager getInstance() {
        if (instance == null) {
            instance = new ProjectManager();
        }
        return instance;
    }

    public Project getCurrentProject() {
        if (currentProject == null) {
            loadCurrentProject();
        }
        return currentProject;
    }

    public void loadCurrentProject() {
        String projectPath = System.getProperty("selected.project.path");
        if (projectPath != null) {
            File gamaFile = new File(projectPath, ".gama");
            if (gamaFile.exists()) {
                try {
                    currentProject = mapper.readValue(gamaFile, Project.class);
                } catch (IOException e) {
                    logger.error("Failed to read .gama file", e);
                }
            } else {
                currentProject = new Project("Desconhecido", projectPath, System.getProperty("acervo"), "custom");
            }
        }
    }

    public void setCurrentProject(Project project) {
        this.currentProject = project;
        if (project != null) {
            System.setProperty("selected.project.path", project.getPath());
            if (project.getAcervo() != null) {
                System.setProperty("acervo", project.getAcervo());
            }
        }
    }
}

