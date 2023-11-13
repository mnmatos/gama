package com.digitallib.manager;

import com.digitallib.JsonGenerator;
import com.digitallib.model.DataDocumento;
import com.digitallib.model.Documento;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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

    public static final String REPO = "repo/documents/";
    public static void addEntry(Documento documento, List<File> files){
        try {
            String jsonText = GenerateJsonFromDoc(documento);
            Files.createDirectories(Paths.get(getPathFromCode(documento.getCodigo())));
            saveFiles(documento, files, jsonText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void updateEntry(Documento documento, List<File> files){
        try {
            String jsonText = GenerateJsonFromDoc(documento);
            saveFiles(documento, files, jsonText);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    private static void saveFiles(Documento documento, List<File> files, String jsonText) throws IOException {
        Files.write(Paths.get(String.format("%s/%s.json", getPathFromCode(documento.getCodigo()), documento.getCodigo())), jsonText.getBytes());
        if(files != null) {
            for (File file : files.stream().filter(file -> !file.isDirectory()).collect(Collectors.toList())) {
                Files.copy(file.getAbsoluteFile().toPath(), Paths.get(String.format("%s/%s", getPathFromCode(documento.getCodigo()), file.getName())));
            }
        }
    }

    public static List<Documento> getEntriesByCodigo(String codigo){
        return getEntries().stream().filter(d -> d.getCodigo().equals(codigo)).collect(Collectors.toList());
    }

    public static List<Documento> getEntries(){
        List<Documento> documentos = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(REPO))) {
            paths.filter(Files::isRegularFile).filter((path) -> path.toString().endsWith("json")).forEach(javaPath -> documentos.add(getDoc(javaPath)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentos;
    }

    public static HashSet<String> getDocCodeSet(){
        HashSet<String> documentCodes = new HashSet<>();
        try (Stream<Path> paths = Files.walk(Paths.get(REPO))) {
            paths.filter(Files::isRegularFile).filter((path) -> path.toString().endsWith("json")).forEach(javaPath -> documentCodes.add(getDoc(javaPath).getCodigo()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return documentCodes;
    }
    private static Documento getDoc(Path javaFile) {
        try {
//            aplicarRetrocompatibilidadeInstituicao(javaFile);
            Documento doc = JsonGenerator.GenerateDocFromJson(new String(Files.readAllBytes(javaFile)));
//            aplicarRetrocompatibilidadeData(doc);
            updateEntry(doc, new ArrayList<>());
            return doc;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void aplicarRetrocompatibilidadeInstituicao(Path javaFile) throws IOException {
        Charset charset = StandardCharsets.UTF_8;
        String content = new String(Files.readAllBytes(javaFile), charset);
        if(!content.contains("instituicao_custodia")) {
            content = content.replaceAll("encontrado_em", "instituicao_custodia");
            content = content.replaceAll("periodico", "encontrado_em");
            Files.write(javaFile, content.getBytes(charset));
        }
    }

    public static void aplicarRetrocompatibilidadeData(Documento documento) {
        if(documento.getDataDocumento() == null) {
            documento.setDataDocumento(new DataDocumento());
            if (documento.getDataAproximada() != null) {
                documento.getDataDocumento().setDataIncerta(true);
                documento.getDataDocumento().setAno(documento.getDataAproximada());
            } else if (documento.getData() != null) {
                documento.getDataDocumento().setDataIncerta(false);
                documento.getDataDocumento().setAno(String.valueOf(documento.getData().getYear()));
                documento.getDataDocumento().setMes(documento.getData().getMonthValue());
                documento.getDataDocumento().setDia(documento.getData().getDayOfMonth());
            }
        }
    }

    public static String getPathFromCode(String code) {
        String[] splitCode = code.split("\\.");
        String path = REPO;
        for (String sub : splitCode){
            if (path.equals(REPO)) path += sub;
            else path += "/"+sub;
        }
        return path;
    }

}
