package com.digitallib.transcription;

import com.digitallib.llm.LlmAdapter;
import com.digitallib.llm.LlmException;
import com.digitallib.model.TextBlock;

import java.util.List;

/**
 * Bridges the legacy {@link LlmAdapter} interface into the broader
 * {@link TranscriptionAdapter} contract so that LLM-based adapters
 * (Anthropic, OpenAI, Bedrock, Ollama, LM Studio) can be used
 * wherever a {@link TranscriptionAdapter} is expected.
 */
public class LlmAdapterBridge implements TranscriptionAdapter {

    private final LlmAdapter delegate;

    public LlmAdapterBridge(LlmAdapter delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<TextBlock> transcribe(byte[] imageBytes, String mimeType) throws TranscriptionException {
        try {
            return delegate.transcribe(imageBytes, mimeType);
        } catch (LlmException e) {
            throw new TranscriptionException(e.getMessage(), e);
        }
    }

    @Override
    public String testConnection() throws TranscriptionException {
        try {
            return delegate.testConnection();
        } catch (LlmException e) {
            throw new TranscriptionException(e.getMessage(), e);
        }
    }
}

