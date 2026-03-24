package com.digitallib.service;

import com.digitallib.model.Project;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.digitallib.utils.ZipUtils;
import com.digitallib.utils.RobustFileDeleter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for project management operations.
 * <p>
 * Extracted from {@code ProjectManagerController} so the controller
 * only handles UI wiring while file/IO operations live here.
 */
public class ProjectService {

    private static final Logger logger = LogManager.getLogger(ProjectService.class);

    private static final File GAMA_DIR = new File(System.getProperty("user.home"), ".gama");
    private static final File PROJECTS_FILE = new File(GAMA_DIR, "projects.json");

    private final ObjectMapper mapper;

    public ProjectService() {
        this.mapper = new ObjectMapper();
    }

    // -------------------------------------------------------------------------
    // Project list persistence
    // -------------------------------------------------------------------------

    /** Loads the global list of registered projects from disk. */
    public List<Project> loadProjects() throws IOException {
        if (!GAMA_DIR.exists()) GAMA_DIR.mkdirs();
        if (!PROJECTS_FILE.exists()) return new ArrayList<>();
        return mapper.readValue(PROJECTS_FILE, new TypeReference<List<Project>>() {});
    }

    /** Saves the global list of registered projects to disk. */
    public void saveProjects(List<Project> projects) throws IOException {
        if (!GAMA_DIR.exists()) GAMA_DIR.mkdirs();
        mapper.writeValue(PROJECTS_FILE, projects);
    }

    // -------------------------------------------------------------------------
    // Individual project operations
    // -------------------------------------------------------------------------

    /**
     * Writes the project metadata file ({@code .gama}) inside the project directory.
     *
     * @param project the project whose metadata to persist
     */
    public void saveProjectFile(Project project) throws IOException {
        File gamaFile = new File(project.getPath(), ".gama");
        mapper.writeValue(gamaFile, project);
    }

    /**
     * Creates the directory structure for a new project and saves its metadata.
     *
     * @param name      display name
     * @param acervo    collection acronym
     * @param parentDir parent directory under which the project folder is created
     * @param classesYamlSource optional classes.yaml to copy into the new project (may be null)
     * @return the newly created {@link Project}
     * @throws IOException if directory creation or file copy fails
     */
    public Project createProject(String name, String acervo, File parentDir, File classesYamlSource)
            throws IOException {
        String sanitizedName = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        File projectDir = new File(parentDir, sanitizedName);
        if (!projectDir.exists() && !projectDir.mkdirs()) {
            throw new IOException("Could not create project directory: " + projectDir.getAbsolutePath());
        }

        if (classesYamlSource != null && classesYamlSource.exists()) {
            Path destPath = new File(projectDir, "classes.yaml").toPath();
            Files.copy(classesYamlSource.toPath(), destPath, StandardCopyOption.REPLACE_EXISTING);
        }

        Project project = new Project(name, projectDir.getAbsolutePath(), acervo);
        saveProjectFile(project);
        return project;
    }

    /**
     * Reads project metadata from an existing project directory.
     *
     * @param dir the root directory of the project (must contain a {@code .gama} file)
     * @return the loaded {@link Project} with path updated to {@code dir}
     * @throws IOException if the {@code .gama} file is missing or cannot be parsed
     */
    public Project loadExistingProject(File dir) throws IOException {
        File gamaFile = new File(dir, ".gama");
        if (!gamaFile.exists()) {
            throw new IOException("No .gama file found in: " + dir.getAbsolutePath());
        }
        Project p = mapper.readValue(gamaFile, Project.class);
        p.setPath(dir.getAbsolutePath());
        return p;
    }

    /**
     * Deletes the project directory from disk.
     *
     * @param projectDir the directory to delete
     * @throws IOException if deletion fails
     */
    public void deleteProjectDirectory(File projectDir) throws IOException {
        RobustFileDeleter.delete(projectDir.toPath());
    }

    /**
     * Lists all items that would be deleted for a dry-run preview.
     *
     * @param projectDir the directory to inspect
     * @return ordered list of paths under the directory
     * @throws IOException if the directory cannot be walked
     */
    public List<Path> listProjectFiles(File projectDir) throws IOException {
        List<Path> items = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.walk(projectDir.toPath())) {
            stream.forEach(items::add);
        }
        return items;
    }

    // -------------------------------------------------------------------------
    // Import / Export
    // -------------------------------------------------------------------------

    /**
     * Imports a project from a ZIP archive into {@code destDir}.
     *
     * @param zipFile the ZIP file to import
     * @param destDir the directory where the archive should be extracted
     * @return the imported {@link Project}
     * @throws IOException if extraction or metadata reading fails
     */
    public Project importProjectFromZip(File zipFile, File destDir) throws IOException {
        String zipName = zipFile.getName();
        String folderName = zipName.lastIndexOf('.') > 0
                ? zipName.substring(0, zipName.lastIndexOf('.'))
                : zipName;

        File extractionDir = new File(destDir, folderName);
        if (!extractionDir.exists() && !extractionDir.mkdirs()) {
            throw new IOException("Could not create extraction directory: " + extractionDir.getAbsolutePath());
        }

        ZipUtils.unzip(zipFile.toPath(), extractionDir.toPath());

        // Locate .gama in extractionDir or an immediate subdirectory
        File foundGama = findGamaFile(extractionDir);
        if (foundGama == null) {
            throw new IOException("No .gama file found after extraction in: " + extractionDir.getAbsolutePath());
        }

        Project p = mapper.readValue(foundGama, Project.class);
        p.setPath(foundGama.getParentFile().getAbsolutePath());
        return p;
    }

    /**
     * Exports a project as a ZIP archive.
     *
     * @param project the project to export
     * @param zipFile destination ZIP file
     * @throws IOException if compression fails
     */
    public void exportProjectToZip(Project project, File zipFile) throws IOException {
        saveProjectFile(project);
        ZipUtils.zipFolder(Path.of(project.getPath()), zipFile.toPath());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private File findGamaFile(File root) {
        File local = new File(root, ".gama");
        if (local.exists()) return local;

        File[] children = root.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    File sub = new File(child, ".gama");
                    if (sub.exists()) return sub;
                }
            }
        }
        return null;
    }
}

