package com.digitallib.manager;

import com.digitallib.JsonGenerator;
import com.digitallib.model.Documento;
import com.digitallib.model.SubClasse;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.digitallib.JsonGenerator.GenerateJsonFromDoc;

public class RepositoryManager {
    private static Logger logger = LogManager.getLogger();

    public static void addEntry(Documento documento, List<File> files){
        try {
            createFolderAndJson(documento);
            saveFiles(documento.getCodigo(), files);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateEntry(Documento documento){
        try {
            String jsonText = GenerateJsonFromDoc(documento);
            Files.write(Paths.get(String.format("%s/%s.json", getPathFromCode(documento.getCodigo()), documento.getCodigo())), jsonText.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createFolderAndJson(Documento documento) throws IOException {
        String jsonText = GenerateJsonFromDoc(documento);
        Files.createDirectories(Paths.get(getPathFromCode(documento.getCodigo())));
        Files.write(Paths.get(String.format("%s/%s.json", getPathFromCode(documento.getCodigo()), documento.getCodigo())), jsonText.getBytes());
    }

    public static void removeEntry(String code){
        try {
            File file = new File(getPathFromCode(code));
            if (Arrays.stream(file.listFiles()).filter(f -> f.isDirectory()).collect(Collectors.toList()).size() == 0) {
                FileUtils.deleteDirectory(file);
                for(int i = 0; i < 3; i++) {
                    file = file.getParentFile();
                    if (file.listFiles().length == 0) FileUtils.deleteDirectory(file);
                    else break;
                }
            } else {
                for(File fileToBeRemoved : Arrays.stream(file.listFiles()).filter(f -> !f.isDirectory()).collect(Collectors.toList())){
                    FileUtils.delete(fileToBeRemoved);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveFiles(String codigo, List<File> files) throws IOException {
        if(files != null) {
            for (File file : files.stream().filter(file -> !file.isDirectory()).collect(Collectors.toList())) {
                copyFile(codigo, file);
            }
        }
    }

    public static void removeFiles (String codigo, List<String> files){
        File[] filesOnFolder = new File(getPathFromCode(codigo)).listFiles();

        if (filesOnFolder != null) {
            Arrays.stream(filesOnFolder)
                    .filter(file -> file.isFile() && files.contains(file.getName()))
                    .forEach(file -> {
                        if (!file.delete()) {
                            logger.error("Falha ao deletar: " + file.getAbsolutePath());
                        }
                    });
        }
    }

    private static void copyFile(String codigo, File file) throws IOException {
        Path destination = Paths.get(String.format("%s/%s", getPathFromCode(codigo), file.getName())).toAbsolutePath();
        Path source = file.getAbsoluteFile().toPath();
        if(!source.equals(destination)) Files.copy(source, destination);
    }

    public static List<Documento> getEntriesByCodigo(String codigo){
        return getEntries().stream().filter(d -> d.getCodigo().equals(codigo)).collect(Collectors.toList());
    }

    public static List<Documento> getEntriesBySubClass(SubClasse subClasse){
        return getEntries().stream().filter(d -> d.getSubClasseProducao().getCode().equals(subClasse.getCode())).collect(Collectors.toList());
    }

    public static List<Documento> getEntries(){
        List<Documento> documentos = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(getRepoPath())) {
            paths.filter(Files::isRegularFile).filter((path) -> path.toString().endsWith("json")).forEach(javaPath -> documentos.add(getDoc(javaPath)));
        } catch (Exception e) {
            logger.error("Erro ao listar documentos no repositório", e);
        }
        return documentos;
    }

    public static HashSet<String> getDocCodeSet(){
        HashSet<String> documentCodes = new HashSet<>();
        try (Stream<Path> paths = Files.walk(getRepoPath())) {
            paths.filter(Files::isRegularFile).filter((path) -> path.toString().endsWith("json")).forEach(javaPath -> documentCodes.add(getDoc(javaPath).getCodigo()));
        } catch (Exception e) {
            logger.error("Erro ao coletar códigos de documentos", e);
        }
        return documentCodes;
    }

    private static Path getRepoPath(){
        String projectPath = System.getProperty("selected.project.path");
        if (projectPath == null) {
            throw new IllegalStateException("Project path is not set. Please select a project first.");
        }
        Path path = Paths.get(projectPath, "documents");
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return path;
    }

    private static Documento getDoc(Path javaFile) {
        try {
            Documento doc = JsonGenerator.GenerateDocFromJson(new String(Files.readAllBytes(javaFile)));
            return doc;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getPathFromCode(String code) {
        String[] splitCode = code.split("\\.");
        String path = getRepoPath().toString();
        for (String sub : splitCode){
            path += "/"+sub;
        }
        return path;
    }

}
