package com.digitallib.transcription;

import com.digitallib.model.TextBlock;
import java.util.List;

/**
 * Common interface for all transcription engines — whether AI/LLM-based or
 * classic OCR tools such as Tesseract.
 */
public interface TranscriptionAdapter {

    /**
     * Transcribe the given image bytes and return a list of TextBlocks.
     *
     * @param imageBytes raw bytes of the image file
     * @param mimeType   MIME type e.g. "image/jpeg", "image/png"
     * @return parsed list of TextBlock objects
     * @throws TranscriptionException on any transcription error
     */
    List<TextBlock> transcribe(byte[] imageBytes, String mimeType) throws TranscriptionException;

    /**
     * Test that the adapter can reach the configured endpoint / binary.
     *
     * @return a success message string
     * @throws TranscriptionException on failure
     */
    String testConnection() throws TranscriptionException;
}

