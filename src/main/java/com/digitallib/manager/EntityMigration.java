package com.digitallib.manager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Classe responsável pela migração automática de entidades AUTOR para PESSOA.
 * Executada uma única vez durante a inicialização do aplicativo.
 * Trata casos de IDs duplicadas renomeando o arquivo se necessário.
 */
public class EntityMigration {

    private static final Logger logger = LogManager.getLogger(EntityMigration.class);
    private static final String MIGRATION_MARKER_FILE = ".migration_completed";
    private static final String BACKUP_DIR = ".backup_pre_migration";

    /**
     * Executa a migração automática de entidades AUTOR para PESSOA.
     * Cria backup dos arquivos antigos e marca a migração como concluída.
     * Trata conflitos de ID renomeando arquivos se necessário.
     *
     * @param repoPath Caminho do repositório de entidades
     */
    public static void performMigration(Path repoPath) {
        try {
            Path migrationMarker = repoPath.resolve(MIGRATION_MARKER_FILE);

            // Verifica se a migração já foi executada
            if (Files.exists(migrationMarker)) {
                logger.info("Migração de entidades já foi executada anteriormente. Pulando...");
                return;
            }

            logger.info("Iniciando migração automática de entidades AUTOR para PESSOA...");

            // Cria diretório de backup
            Path backupDir = repoPath.resolve(BACKUP_DIR);
            Files.createDirectories(backupDir);

            List<Path> migratedFiles = new ArrayList<>();
            Map<String, String> idMappings = new HashMap<>();  // oldId → newId

            // Procura por arquivos JSON antigos com tipo AUTOR
            try (Stream<Path> paths = Files.walk(repoPath)) {
                paths.filter(Files::isRegularFile)
                     .filter(path -> path.toString().endsWith(".json"))
                     .forEach(jsonPath -> {
                         try {
                             String jsonContent = new String(Files.readAllBytes(jsonPath), StandardCharsets.UTF_8);

                             // Verifica se é um arquivo AUTOR antigo
                             if (jsonContent.contains("\"type\":\"AUTOR\"") || jsonContent.contains("\"type\": \"AUTOR\"")) {
                                 migrateFile(jsonPath, repoPath, backupDir, migratedFiles, idMappings);
                             }
                         } catch (IOException e) {
                             logger.error("Erro ao processar arquivo para migração: " + jsonPath, e);
                         }
                     });
            }

            // Atualiza referências em documentos se houver IDs mapeados
            if (!idMappings.isEmpty()) {
                logger.info("Atualizando referências de autores em documentos...");
                Path docRepoPath = repoPath.getParent().resolve("documents");
                if (Files.exists(docRepoPath)) {
                    DocumentMigrationHelper.updateDocumentReferences(docRepoPath, idMappings);
                } else {
                    logger.info("Repositório de documentos não encontrado em: " + docRepoPath);
                }
            }

            // Cria arquivo marcador de migração
            Files.write(migrationMarker,
                       ("Migração executada em: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "\n" +
                        "Arquivos migrados: " + migratedFiles.size() + "\n" +
                        "IDs renomeadas: " + idMappings.size()).getBytes(StandardCharsets.UTF_8));

            logger.info("Migração concluída com sucesso. Total de entidades migradas: " + migratedFiles.size());
            if (!idMappings.isEmpty()) {
                logger.info("IDs renomeadas (com documentos atualizados): " + idMappings.size());
            }
            if (!migratedFiles.isEmpty()) {
                logger.info("Backup dos arquivos antigos criado em: " + backupDir);
            }

        } catch (IOException e) {
            logger.error("Erro durante a migração de entidades", e);
        }
    }

    /**
     * Migra um arquivo individual de AUTOR para PESSOA.
     * Se já existir uma PESSOA com o mesmo ID, renomeia o arquivo AUTOR para um novo ID.
     * Rastreia mapeamento de IDs para atualizar documentos que referenciam esse autor.
     *
     * @param jsonPath Caminho do arquivo JSON a migrar
     * @param repoPath Caminho raiz do repositório
     * @param backupDir Diretório para armazenar backup
     * @param migratedFiles Lista para rastrear arquivos migrados
     * @param idMappings Mapa para rastrear IDs antigos → novos (para atualizar documentos)
     */
    private static void migrateFile(Path jsonPath, Path repoPath, Path backupDir, List<Path> migratedFiles, Map<String, String> idMappings) {
        try {
            String jsonContent = new String(Files.readAllBytes(jsonPath), StandardCharsets.UTF_8);
            String originalContent = jsonContent;
            String fileName = jsonPath.getFileName().toString();

            // Extrai o ID do arquivo
            String currentId = fileName.replace(".json", "");

            // Cria backup do arquivo original
            Path backupPath = backupDir.resolve(fileName);
            Files.write(backupPath, originalContent.getBytes(StandardCharsets.UTF_8));

            // Realiza conversão de AUTOR para PESSOA diretamente no JSON
            String migratedJson = jsonContent.replaceAll("\"type\"\\s*:\\s*\"AUTOR\"", "\"type\":\"PESSOA\"");

            // Verifica se já existe um arquivo PESSOA com o mesmo ID
            Path targetPath = jsonPath;
            String newId = currentId;

            if (idJaExisteComTipoPessoa(repoPath, currentId)) {
                // Encontra um novo ID disponível
                newId = findAvailableId(repoPath, currentId);
                targetPath = repoPath.resolve(newId + ".json");

                // Atualiza o ID no JSON migrado
                migratedJson = atualizarIdNoJson(migratedJson, newId);

                logger.warn("Conflito de ID detectado: " + currentId + ". Entidade AUTOR renomeada para ID: " + newId);
            }

            // Rastreia mapeamento de IDs se houve renomeação
            if (!newId.equals(currentId)) {
                idMappings.put(currentId, newId);
            }

            // Salva o arquivo migrado (pode ser no mesmo caminho ou em caminho novo)
            Files.write(targetPath, migratedJson.getBytes(StandardCharsets.UTF_8));

            // Remove arquivo antigo se foi renomeado
            if (!targetPath.equals(jsonPath)) {
                Files.delete(jsonPath);
            }

            migratedFiles.add(targetPath);
            logger.info("Entidade migrada com sucesso: " + fileName + " -> ID final: " + newId);

        } catch (Exception e) {
            logger.error("Erro ao migrar arquivo: " + jsonPath, e);
        }
    }

    /**
     * Verifica se já existe uma entidade PESSOA com o mesmo ID.
     */
    private static boolean idJaExisteComTipoPessoa(Path repoPath, String id) {
        Path targetPath = repoPath.resolve(id + ".json");
        if (!Files.exists(targetPath)) {
            return false;
        }

        try {
            String content = new String(Files.readAllBytes(targetPath), StandardCharsets.UTF_8);
            // Verifica se é uma PESSOA (não é AUTOR nem outro tipo)
            return content.contains("\"type\":\"PESSOA\"") || content.contains("\"type\": \"PESSOA\"");
        } catch (IOException e) {
            logger.error("Erro ao verificar tipo de entidade: " + id, e);
            return false;
        }
    }

    /**
     * Encontra um novo ID disponível baseado no ID solicitado.
     * Incrementa o número até encontrar um ID não utilizado.
     */
    private static String findAvailableId(Path repoPath, String baseId) throws IOException {
        // Extrai o número do ID
        int baseNumber;
        try {
            baseNumber = Integer.parseInt(baseId);
        } catch (NumberFormatException e) {
            // Se não for numérico, apenas adiciona sufixo
            return baseId + "_migrated";
        }

        int newNumber = baseNumber + 1;
        while (Files.exists(repoPath.resolve(newNumber + ".json"))) {
            newNumber++;
        }

        return String.valueOf(newNumber);
    }

    /**
     * Atualiza o campo ID no JSON.
     */
    private static String atualizarIdNoJson(String json, String novoId) {
        // Substitui o valor do campo "id"
        Pattern pattern = Pattern.compile("\"id\"\\s*:\\s*\"[^\"]*\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.replaceFirst("\"id\":\"" + novoId + "\"");
    }
}

