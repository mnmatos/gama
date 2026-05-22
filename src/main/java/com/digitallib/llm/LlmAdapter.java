package com.digitallib.llm;
import com.digitallib.model.TextBlock;
import java.util.List;
/**
 * Common interface for all LLM transcription adapters.
 */
public interface LlmAdapter {
    /**
     * Transcribe the given image bytes and return a list of TextBlocks.
     *
     * @param imageBytes raw bytes of the image file
     * @param mimeType   MIME type e.g. "image/jpeg", "image/png"
     * @return parsed list of TextBlock objects
     * @throws LlmException on network, authentication or parsing errors
     */
    List<TextBlock> transcribe(byte[] imageBytes, String mimeType) throws LlmException;
    /**
     * Test that the adapter can reach the configured endpoint.
     *
     * @return a success message string
     * @throws LlmException on failure
     */
    String testConnection() throws LlmException;
}