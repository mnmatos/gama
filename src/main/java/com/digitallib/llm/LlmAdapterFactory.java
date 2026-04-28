package com.digitallib.llm;
import com.digitallib.model.LlmSettings;
public class LlmAdapterFactory {
    private LlmAdapterFactory() {}
    public static LlmAdapter getAdapter(LlmSettings settings) {
        if (settings == null) throw new IllegalArgumentException("LlmSettings must not be null");
        String provider = settings.getProvider() == null ? "anthropic" : settings.getProvider().toLowerCase();
        switch (provider) {
            case "anthropic": return new AnthropicAdapter(settings);
            case "openai":    return OpenAiCompatibleAdapter.openAi(settings);
            case "bedrock":   return new BedrockAdapter(settings);
            case "ollama":    return OpenAiCompatibleAdapter.ollama(settings);
            case "lmstudio":  return OpenAiCompatibleAdapter.lmStudio(settings);
            default: throw new IllegalArgumentException("Unknown LLM provider: " + provider);
        }
    }
}
