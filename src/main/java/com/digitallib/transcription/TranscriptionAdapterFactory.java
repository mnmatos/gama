package com.digitallib.transcription;

import com.digitallib.llm.LlmAdapterFactory;
import com.digitallib.model.LlmSettings;
import com.digitallib.ocr.TesseractOcrAdapter;

/**
 * Single entry point that resolves the right {@link TranscriptionAdapter}
 * for the current settings, regardless of whether the engine is an
 * AI/LLM provider or a local OCR tool.
 */
public class TranscriptionAdapterFactory {

    private TranscriptionAdapterFactory() {}

    /**
     * Returns the appropriate {@link TranscriptionAdapter} based on
     * {@link LlmSettings#getTranscriptionTool()}:
     * <ul>
     *   <li>{@code "ocr"} → Tesseract OCR (offline, no API key required)</li>
     *   <li>{@code "llm"} (default) → whichever LLM provider is configured</li>
     * </ul>
     */
    public static TranscriptionAdapter getAdapter(LlmSettings settings) {
        if (settings == null) throw new IllegalArgumentException("LlmSettings must not be null");
        String tool = settings.getTranscriptionTool();
        if ("ocr".equalsIgnoreCase(tool)) {
            return new TesseractOcrAdapter(settings);
        }
        // Default: delegate to the LLM adapter and bridge the exception type
        return new LlmAdapterBridge(LlmAdapterFactory.getAdapter(settings));
    }
}

