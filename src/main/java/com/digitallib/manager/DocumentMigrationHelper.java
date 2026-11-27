package com.digitallib.manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Classe helper para atualizar referências de autores em documentos
 * após a migração de IDs de entidades AUTOR → PESSOA.
 *
 * Procura por IDs em listas JSON como "autores" e "autoresPubli" e os substitui
 * quando um autor foi renomeado durante a migração.
 */
public class DocumentMigrationHelper {

    private static final Logger logger = LogManager.getLogger(DocumentMigrationHelper.class);

    /**
     * Atualiza documentos que referenciam autores que foram renomeados durante migração.
     *
     * @param docRepoPath Caminho do repositório de documentos
     * @param idMappings Mapa de IDs antigos para novos (oldId → newId)
     */
    public static void updateDocumentReferences(Path docRepoPath, Map<String, String> idMappings) {
        if (idMappings.isEmpty()) {
            return;
        }

        logger.info("Atualizando referências de autores em documentos (mapeamento: " + idMappings.size() + " IDs)");

        try {
            Files.walk(docRepoPath)
                 .filter(Files::isRegularFile)
                 .filter(path -> path.toString().endsWith(".json"))
                 .forEach(docPath -> {
                     try {
                         updateDocumentFile(docPath, idMappings);
                     } catch (IOException e) {
                         logger.error("Erro ao atualizar documento: " + docPath, e);
                     }
                 });
        } catch (IOException e) {
            logger.error("Erro ao percorrer documentos", e);
        }
    }

    /**
     * Atualiza um arquivo de documento específico, substituindo IDs de autores mapeados.
     */
    private static void updateDocumentFile(Path docPath, Map<String, String> idMappings) throws IOException {
        String jsonContent = new String(Files.readAllBytes(docPath), StandardCharsets.UTF_8);
        String updatedJson = jsonContent;
        int substituicoes = 0;

        // Substitui cada ID antigo pelo novo
        for (Map.Entry<String, String> mapping : idMappings.entrySet()) {
            String oldId = mapping.getKey();
            String newId = mapping.getValue();

            // Padrão: "oldId" dentro de arrays JSON (autores, autoresPubli, etc)
            // Procura por "oldId" como valor string em arrays
            String pattern = "\"" + Pattern.quote(oldId) + "\"";
            String replacement = "\"" + newId + "\"";

            int beforeLength = updatedJson.length();
            updatedJson = updatedJson.replaceAll(pattern, replacement);
            int afterLength = updatedJson.length();

            // Calcula quantas substituições foram feitas
            if (beforeLength != afterLength) {
                int subs = (beforeLength - afterLength) / (oldId.length() - newId.length());
                substituicoes += Math.abs(subs);
            }
        }

        // Salva o arquivo se foi modificado
        if (!updatedJson.equals(jsonContent)) {
            Files.write(docPath, updatedJson.getBytes(StandardCharsets.UTF_8));
            logger.info("Documento atualizado: " + docPath.getFileName() + " (" + substituicoes + " referências atualizadas)");
        }
    }
}

