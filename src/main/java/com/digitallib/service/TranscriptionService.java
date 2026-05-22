package com.digitallib.service;
import com.digitallib.exception.RepositoryException;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.*;
import com.digitallib.transcription.TranscriptionAdapter;
import com.digitallib.transcription.TranscriptionAdapterFactory;
import com.digitallib.transcription.TranscriptionException;
import javafx.application.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
public class TranscriptionService {
    private static final Logger logger = LogManager.getLogger(TranscriptionService.class);
    private static final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "transcription");
        t.setDaemon(true);
        return t;
    });
    private static TranscriptionService instance;
    private TranscriptionService() {}
    public static synchronized TranscriptionService getInstance() {
        if (instance == null) instance = new TranscriptionService();
        return instance;
    }
    /**
     * Resolves the full path of an image file belonging to a document.
     * Uses RepositoryManager.getPathFromCode (splits code on ".").
     */
    public Path resolveImagePath(String codigo, String imageFilename) {
        return Paths.get(RepositoryManager.getPathFromCode(codigo), imageFilename);
    }
    /**
     * Returns the MIME type for a given filename based on its extension.
     */
    public static String mimeTypeOf(String filename) {
        String lower = filename.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".gif")) return "image/gif";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".tif") || lower.endsWith(".tiff")) return "image/tiff";
        return "image/jpeg";
    }
    /**
     * Runs LLM transcription asynchronously off the JavaFX thread.
     * Persists an intermediate PROCESSING record, then a final DONE/ERROR record.
     * Calls onDone / onError on the JavaFX Application Thread via Platform.runLater().
     */
    public void transcribeAsync(
            Documento doc,
            String imageFilename,
            LlmSettings settings,
            Consumer<TranscriptionRecord> onDone,
            Consumer<Throwable> onError) {
        // Initialise or reuse record
        TranscriptionRecord record = doc.getTranscriptions().getOrDefault(
                imageFilename, new TranscriptionRecord(imageFilename));
        record.setStatus(TranscriptionStatus.PROCESSING);
        record.setErrorMessage(null);
        doc.getTranscriptions().put(imageFilename, record);
        persistQuietly(doc);
        executor.submit(() -> {
            try {
                Path imgPath = resolveImagePath(doc.getCodigo(), imageFilename);
                byte[] bytes = Files.readAllBytes(imgPath);
                String mime = mimeTypeOf(imageFilename);
                TranscriptionAdapter adapter = TranscriptionAdapterFactory.getAdapter(settings);
                List<TextBlock> blocks = adapter.transcribe(bytes, mime);
                record.setBlocks(blocks);
                record.setStatus(TranscriptionStatus.DONE);
                record.setLlmProvider(resolveEngineLabel(settings));
                record.setLlmModel(resolveModel(settings));
                record.setUpdatedAt(LocalDateTime.now());
                if (record.getCreatedAt() == null) record.setCreatedAt(LocalDateTime.now());
                doc.getTranscriptions().put(imageFilename, record);
                persistQuietly(doc);
                Platform.runLater(() -> onDone.accept(record));
            } catch (IOException | TranscriptionException e) {
                logger.error("Transcription failed for {}/{}", doc.getCodigo(), imageFilename, e);
                record.setStatus(TranscriptionStatus.ERROR);
                record.setErrorMessage(e.getMessage());
                record.setUpdatedAt(LocalDateTime.now());
                doc.getTranscriptions().put(imageFilename, record);
                persistQuietly(doc);
                Platform.runLater(() -> onError.accept(e));
            }
        });
    }
    private void persistQuietly(Documento doc) {
        try {
            RepositoryManager.updateEntry(doc);
        } catch (RepositoryException e) {
            logger.error("Failed to persist document {} after transcription update", doc.getCodigo(), e);
        }
    }
    /** Human-readable engine name stored in the TranscriptionRecord. */
    private String resolveEngineLabel(LlmSettings s) {
        if ("ocr".equalsIgnoreCase(s.getTranscriptionTool())) return "Tesseract OCR";
        return s.getProvider() != null ? s.getProvider() : "llm";
    }
    private String resolveModel(LlmSettings s) {
        if ("ocr".equalsIgnoreCase(s.getTranscriptionTool()))
            return "tesseract (" + (s.getOcrLanguage() != null ? s.getOcrLanguage() : "por") + ")";
        if (s.getProvider() == null) return "";
        switch (s.getProvider().toLowerCase(Locale.ROOT)) {
            case "anthropic": return s.getAnthropicModel();
            case "openai":    return s.getOpenaiModel();
            case "bedrock":   return s.getBedrockModelId();
            case "ollama":    return s.getOllamaModel();
            case "lmstudio":  return s.getLmStudioModel();
            default: return "";
        }
    }
}
