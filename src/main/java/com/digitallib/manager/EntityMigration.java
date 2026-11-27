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
 * Classe responsavel pela migracao automatica de entidades AUTOR para PESSOA.
 * Executada uma unica vez durante a inicializacao do aplicativo.
 * Trata casos de IDs duplicadas renomeando o arquivo se necessario.
 */
public class EntityMigration {

    private static final Logger logger = LogManager.getLogger(EntityMigration.class);
    private static final String MIGRATION_MARKER_FILE = ".migration_completed";
    private static final String BACKUP_DIR = ".backup_pre_migration";
    private static final Pattern TYPE_AUTOR_PATTERN = Pattern.compile("\\\"type\\\"\\s*:\\s*\\\"AUTOR\\\"");
    private static final Pattern TYPE_PESSOA_PATTERN = Pattern.compile("\\\"type\\\"\\s*:\\s*\\\"PESSOA\\\"");

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
                logger.info("Migracao de entidades ja foi executada anteriormente. Pulando...");
                return;
            }

            logger.info("Iniciando migracao automatica de entidades AUTOR para PESSOA...");

            // Backup fora do repositorio de entidades para nao entrar em scans recursivos.
            Path backupDir = repoPath.resolveSibling(BACKUP_DIR);
            Files.createDirectories(backupDir);

            List<Path> migratedFiles = new ArrayList<>();
            Map<String, String> idMappings = new HashMap<>();
            List<Path> failedFiles = new ArrayList<>();

            // Procura por arquivos JSON antigos com tipo AUTOR
            try (Stream<Path> paths = Files.walk(repoPath)) {
                paths.filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".json"))
                        .forEach(jsonPath -> {
                            try {
                                String jsonContent = new String(Files.readAllBytes(jsonPath), StandardCharsets.UTF_8);
                                if (TYPE_AUTOR_PATTERN.matcher(jsonContent).find()) {
                                    migrateFile(jsonPath, repoPath, backupDir, migratedFiles, idMappings);
                                }
                            } catch (Exception e) {
                                failedFiles.add(jsonPath);
                                logger.error("Erro ao processar arquivo para migracao: " + jsonPath, e);
                            }
                        });
            }

            if (!failedFiles.isEmpty()) {
                throw new IOException("Migracao incompleta. Arquivos com falha: " + failedFiles.size());
            }

            if (!idMappings.isEmpty()) {
                logger.info("Atualizando referencias de autores em documentos...");
                Path docRepoPath = repoPath.getParent().resolve("documents");
                if (Files.exists(docRepoPath)) {
                    DocumentMigrationHelper.updateDocumentReferences(docRepoPath, idMappings);
                } else {
                    logger.info("Repositorio de documentos nao encontrado em: " + docRepoPath);
                }
            }

            // Cria arquivo marcador de migração
            Files.write(migrationMarker,
                    ("Migracao executada em: " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "\n" +
                            "Arquivos migrados: " + migratedFiles.size() + "\n" +
                            "IDs renomeadas: " + idMappings.size()).getBytes(StandardCharsets.UTF_8));

            logger.info("Migracao concluida com sucesso. Total de entidades migradas: " + migratedFiles.size());
            if (!idMappings.isEmpty()) {
                logger.info("IDs renomeadas (com documentos atualizados): " + idMappings.size());
            }
            if (!migratedFiles.isEmpty()) {
                logger.info("Backup dos arquivos antigos criado em: " + backupDir);
            }

        } catch (IOException e) {
            logger.error("Erro durante a migracao de entidades", e);
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
    private static void migrateFile(Path jsonPath, Path repoPath, Path backupDir, List<Path> migratedFiles, Map<String, String> idMappings) throws IOException {
        String jsonContent = new String(Files.readAllBytes(jsonPath), StandardCharsets.UTF_8);
        String fileName = jsonPath.getFileName().toString();

        // Extrai o ID do arquivo
        String currentId = fileName.replace(".json", "");

        // Cria backup do arquivo original
        Path backupPath = backupDir.resolve(fileName);
        Files.write(backupPath, jsonContent.getBytes(StandardCharsets.UTF_8));

        // Realiza conversão de AUTOR para PESSOA diretamente no JSON
        String migratedJson = TYPE_AUTOR_PATTERN.matcher(jsonContent).replaceAll("\"type\":\"PESSOA\"");

        // Verifica se já existe um arquivo PESSOA com o mesmo ID
        Path targetPath = jsonPath;
        String newId = currentId;

        if (idJaExisteComTipoPessoa(repoPath, currentId)) {
            // Encontra um novo ID disponível
            newId = findAvailableId(repoPath, currentId);
            targetPath = repoPath.resolve(newId + ".json");
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
            return TYPE_PESSOA_PATTERN.matcher(content).find();
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
        try {
            int baseNumber = Integer.parseInt(baseId);
            int newNumber = baseNumber + 1;
            while (Files.exists(repoPath.resolve(newNumber + ".json"))) {
                newNumber++;
            }
            return String.valueOf(newNumber);
        } catch (NumberFormatException e) {
            String candidate = baseId + "_migrated";
            int suffix = 1;
            while (Files.exists(repoPath.resolve(candidate + ".json"))) {
                candidate = baseId + "_migrated_" + suffix++;
            }
            return candidate;
        }
    }

    /**
     * Atualiza o campo ID no JSON.
     */
    private static String atualizarIdNoJson(String json, String novoId) {
        Pattern pattern = Pattern.compile("\\\"id\\\"\\s*:\\s*\\\"[^\\\"]*\\\"");
        Matcher matcher = pattern.matcher(json);
        return matcher.replaceFirst("\"id\":\"" + novoId + "\"");
    }
}
