package com.digitallib.manager;

import com.digitallib.model.export.FichaExportConfig;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton manager for {@link FichaExportConfig} objects.
 * Persists the full list to {@code ficha-export-configs.json} in the selected project directory.
 * Returns an empty list on first load — no auto-generated presets.
 */
public class FichaExportConfigManager {

    private static final Logger logger = LogManager.getLogger(FichaExportConfigManager.class);
    private static final String CONFIG_FILE = "ficha-export-configs.json";

    private static FichaExportConfigManager instance;
    private final ObjectMapper mapper = new ObjectMapper();
    private List<FichaExportConfig> cached;

    private FichaExportConfigManager() {}

    public static synchronized FichaExportConfigManager getInstance() {
        if (instance == null) instance = new FichaExportConfigManager();
        return instance;
    }

    /** Call after changing the selected project so the cache is invalidated. */
    public static void reset() {
        instance = null;
    }

    private File getConfigFile() {
        String projectPath = System.getProperty("selected.project.path");
        if (projectPath == null) throw new IllegalStateException("No project selected");
        return new File(projectPath, CONFIG_FILE);
    }

    /** Returns the cached list, loading from disk on first call. Never null. */
    public List<FichaExportConfig> load() {
        if (cached != null) return cached;
        File file = getConfigFile();
        if (!file.exists()) {
            cached = new ArrayList<>();
            return cached;
        }
        try {
            cached = mapper.readValue(file, new TypeReference<>() {});
        } catch (IOException e) {
            logger.error("Failed to read " + CONFIG_FILE, e);
            cached = new ArrayList<>();
        }
        return cached;
    }

    /** Persists the supplied list to disk and updates the cache. */
    public void save(List<FichaExportConfig> configs) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(getConfigFile(), configs);
            cached = new ArrayList<>(configs);
        } catch (IOException e) {
            logger.error("Failed to save " + CONFIG_FILE, e);
            throw new RuntimeException("Failed to save ficha export configs", e);
        }
    }

    /**
     * Reads a single {@link FichaExportConfig} from the given JSON file.
     * Does NOT add it to the saved list — the caller must call {@link #save} after adding it.
     */
    public FichaExportConfig importFromFile(File file) throws IOException {
        return mapper.readValue(file, FichaExportConfig.class);
    }

    /** Writes a single {@link FichaExportConfig} to the given file. */
    public void exportToFile(FichaExportConfig config, File file) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, config);
    }
}

