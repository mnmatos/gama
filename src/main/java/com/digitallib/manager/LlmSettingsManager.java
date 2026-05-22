package com.digitallib.manager;
import com.digitallib.model.LlmSettings;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.io.IOException;
/**
 * Manages loading and saving of LLM provider settings from llm-settings.json
 * located in the current project directory.
 */
public class LlmSettingsManager {
    private static final Logger logger = LogManager.getLogger(LlmSettingsManager.class);
    private static final String SETTINGS_FILE = "llm-settings.json";
    private static final String MASKED = "***";
    private static LlmSettingsManager instance;
    private final ObjectMapper mapper = new ObjectMapper();
    private LlmSettings cached;
    private LlmSettingsManager() {}
    public static synchronized LlmSettingsManager getInstance() {
        if (instance == null) {
            instance = new LlmSettingsManager();
        }
        return instance;
    }
    public static void reset() {
        instance = null;
    }
    private File getSettingsFile() {
        String projectPath = System.getProperty("selected.project.path");
        if (projectPath == null) throw new IllegalStateException("No project selected");
        return new File(projectPath, SETTINGS_FILE);
    }
    public LlmSettings load() {
        if (cached != null) return cached;
        File file = getSettingsFile();
        if (!file.exists()) {
            cached = new LlmSettings();
            return cached;
        }
        try {
            cached = mapper.readValue(file, LlmSettings.class);
        } catch (IOException e) {
            logger.error("Failed to read llm-settings.json", e);
            cached = new LlmSettings();
        }
        return cached;
    }
    /**
     * Saves settings. If a key field contains "***" or is blank, the previously stored value is preserved.
     */
    public void save(LlmSettings incoming) {
        LlmSettings existing = load();
        if (isMasked(incoming.getAnthropicApiKey())) incoming.setAnthropicApiKey(existing.getAnthropicApiKey());
        if (isMasked(incoming.getOpenaiApiKey())) incoming.setOpenaiApiKey(existing.getOpenaiApiKey());
        if (isMasked(incoming.getBedrockAccessKeyId())) incoming.setBedrockAccessKeyId(existing.getBedrockAccessKeyId());
        if (isMasked(incoming.getBedrockSecretAccessKey())) incoming.setBedrockSecretAccessKey(existing.getBedrockSecretAccessKey());
        if (isMasked(incoming.getBedrockSessionToken())) incoming.setBedrockSessionToken(existing.getBedrockSessionToken());
        if (isMasked(incoming.getBedrockApiKey())) incoming.setBedrockApiKey(existing.getBedrockApiKey());
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(getSettingsFile(), incoming);
            cached = incoming;
        } catch (IOException e) {
            logger.error("Failed to save llm-settings.json", e);
            throw new RuntimeException("Failed to save LLM settings", e);
        }
    }
    private boolean isMasked(String value) {
        return value == null || value.isBlank() || MASKED.equals(value);
    }
    /** Returns a copy of the settings with API keys masked for display in UI. */
    public LlmSettings loadMasked() {
        LlmSettings s = load();
        LlmSettings masked = new LlmSettings();
        masked.setProvider(s.getProvider());
        masked.setAnthropicApiKey(s.getAnthropicApiKey() != null ? MASKED : null);
        masked.setAnthropicModel(s.getAnthropicModel());
        masked.setOpenaiApiKey(s.getOpenaiApiKey() != null ? MASKED : null);
        masked.setOpenaiModel(s.getOpenaiModel());
        masked.setBedrockRegion(s.getBedrockRegion());
        masked.setBedrockModelId(s.getBedrockModelId());
        masked.setBedrockEndpointUrl(s.getBedrockEndpointUrl());
        masked.setBedrockAccessKeyId(s.getBedrockAccessKeyId() != null && !s.getBedrockAccessKeyId().isBlank() ? MASKED : null);
        masked.setBedrockSecretAccessKey(s.getBedrockSecretAccessKey() != null && !s.getBedrockSecretAccessKey().isBlank() ? MASKED : null);
        masked.setBedrockSessionToken(s.getBedrockSessionToken() != null && !s.getBedrockSessionToken().isBlank() ? MASKED : null);
        masked.setBedrockApiKey(s.getBedrockApiKey() != null && !s.getBedrockApiKey().isBlank() ? MASKED : null);
        masked.setOllamaBaseUrl(s.getOllamaBaseUrl());
        masked.setOllamaModel(s.getOllamaModel());
        masked.setLmStudioBaseUrl(s.getLmStudioBaseUrl());
        masked.setLmStudioModel(s.getLmStudioModel());
        masked.setTranscriptionTool(s.getTranscriptionTool());
        masked.setTessdataPath(s.getTessdataPath());
        masked.setOcrLanguage(s.getOcrLanguage());
        return masked;
    }
}