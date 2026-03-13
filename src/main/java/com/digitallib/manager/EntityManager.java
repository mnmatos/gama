package com.digitallib.manager;

import com.digitallib.JsonGenerator;
import com.digitallib.exception.EntityNotFoundException;
import com.digitallib.model.entity.Entity;
import com.digitallib.model.entity.EntityType;
import com.digitallib.utils.RobustFileDeleter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.digitallib.JsonGenerator.GenerateJsonFromDoc;

public class EntityManager {

    private static final Logger logger = LogManager.getLogger(EntityManager.class);

    public static final String ENTITIES_FOLDER_NAME = "repo/entities";

    private static Path getRepoPath(){
        String projectPath = System.getProperty("selected.project.path");
        if (projectPath == null) {
            throw new IllegalStateException("Project path is not set. Please select a project first.");
        }
        Path path = Paths.get(projectPath, ENTITIES_FOLDER_NAME);
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return path;
    }

    public static void addEntry(Entity entity){
        try {
            entity.setId(getLatestCode());
            String jsonText = GenerateJsonFromDoc(entity);
            saveFiles(entity, jsonText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateEntry(Entity entity){
        try {
            String jsonText = GenerateJsonFromDoc(entity);
            saveFiles(entity, jsonText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeEntry(String code){
        try {
            RobustFileDeleter.delete(getFilePath(code));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getLatestCode() throws IOException {
        Optional<Integer> max = getPathStream().filter((path) -> path.toString().endsWith("json") && isNumeric(getFiledName(path)))
                .map(javaPath -> Integer.valueOf(getFiledName(javaPath)))
                .max(Comparator.naturalOrder());
        if(!max.isPresent()) return "0";
        return String.valueOf(max.get()+1);
    }

    private static String getFiledName(Path path) {
        return path.getFileName().toString().split("\\.")[0];
    }

    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    private static void saveFiles(Entity entity, String jsonText) throws IOException {
        Files.write(getFilePath(entity.getId()), jsonText.getBytes());
    }

    private static Path getFilePath(String id) {
        return Paths.get(String.format("%s/%s.json", getRepoPath(), id));
    }

    public static Entity getEntryById (String codigo) throws EntityNotFoundException {
        if (codigo == null) return null;
        List<Entity> entitiesList = getEntries().stream().filter(d -> d.getId().startsWith(codigo)).collect(Collectors.toList());
        if(entitiesList.size()==0) throw new EntityNotFoundException("Entity not found for code " + codigo);
        return entitiesList.get(0);
    }

    public static List<Entity> getEntriesByType (EntityType type){
        return getEntries().stream().filter(d -> d.getType().equals(type)).collect(Collectors.toList());
    }

    public static List<Entity> getEntries (){
        List<Entity> entries = new ArrayList<>();
        try {
            Stream<Path> paths = getPathStream();
            paths.filter(Files::isRegularFile).filter((path) -> path.toString().endsWith("json")).forEach(javaPath -> entries.add(getDoc(javaPath)));
        } catch (Exception e) {
            logger.error("Erro ao listar entidades", e);
        }
        return entries;
    }

    private static Stream<Path> getPathStream() throws IOException {
        return Files.walk(getRepoPath());
    }

    private static Entity getDoc(Path javaFile) {
        try {
            return JsonGenerator.GenerateEntityFromJson(new String(Files.readAllBytes(javaFile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
