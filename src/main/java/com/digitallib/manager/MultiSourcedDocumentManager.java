package com.digitallib.manager;

import com.digitallib.JsonGenerator;
import com.digitallib.exception.RepositoryException;
import com.digitallib.model.MultiSourcedDocument;
import com.digitallib.model.SubClasse;

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

public class MultiSourcedDocumentManager {

    private static final Logger logger = LogManager.getLogger(MultiSourcedDocumentManager.class);

    public static final String REPO = "repo/multi";

    public static String addEntry(MultiSourcedDocument multiSourcedDocument){
        try {
            multiSourcedDocument.setId(getLatestCode());
            String jsonText = GenerateJsonFromDoc(multiSourcedDocument);
            Files.createDirectories(Paths.get(REPO));
            saveFiles(multiSourcedDocument, jsonText);
            return multiSourcedDocument.getId();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateEntry(MultiSourcedDocument MultiSourcedDocument){
        try {
            String jsonText = GenerateJsonFromDoc(MultiSourcedDocument);
            saveFiles(MultiSourcedDocument, jsonText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void removeEntry(String code){
        try {
            Files.delete(getFilePath(code));
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

    private static void saveFiles(MultiSourcedDocument MultiSourcedDocument, String jsonText) throws IOException {
        Files.write(getFilePath(MultiSourcedDocument.getId()), jsonText.getBytes());
    }

    private static Path getFilePath(String id) {
        return Paths.get(String.format("%s/%s.json", REPO, id));
    }

    public static MultiSourcedDocument getEntryById (String codigo) throws RepositoryException {
        if (codigo == null) return null;
        List<MultiSourcedDocument> docList = getEntries().stream().filter(d -> d.getId().startsWith(codigo)).collect(Collectors.toList());
        if(docList.size()==0) throw new RepositoryException("MultiSourcedDocument not found for code " + codigo);
        return docList.get(0);
    }

    public static List<MultiSourcedDocument> getEntries (){
        List<MultiSourcedDocument> entries = new ArrayList<>();
        try {
            Stream<Path> paths = getPathStream();
            paths.filter(Files::isRegularFile).filter((path) -> path.toString().endsWith("json")).forEach(javaPath -> entries.add(getDoc(javaPath)));
        } catch (Exception e) {
            logger.error("Erro ao listar MultiSourcedDocuments", e);
        }
        return entries;
    }

    private static Stream<Path> getPathStream() throws IOException {
        if(!Files.exists(Paths.get(REPO))) Files.createDirectories(Paths.get(REPO));
        ;
        Stream<Path> paths = Files.walk(Paths.get(REPO));
        return paths;
    }

    private static MultiSourcedDocument getDoc(Path javaFile) {
        try {
            return JsonGenerator.GenerateMultiSourcedDocumentFromJson(new String(Files.readAllBytes(javaFile)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
