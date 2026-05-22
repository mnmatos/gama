package com.digitallib.llm;
import com.digitallib.model.LlmSettings;
import com.digitallib.model.TextBlock;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
/**
 * OpenAI-format adapter — works for OpenAI directly, Ollama and LM Studio via configurable baseUrl.
 */
public class OpenAiCompatibleAdapter implements LlmAdapter {
    private final String baseUrl;
    private final String apiKey;
    private final String model;
    private final int testTimeoutSeconds;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    private final ObjectMapper mapper = new ObjectMapper();
    /** OpenAI direct */
    public static OpenAiCompatibleAdapter openAi(LlmSettings s) {
        return new OpenAiCompatibleAdapter("https://api.openai.com/v1", s.getOpenaiApiKey(), s.getOpenaiModel(), 15);
    }
    /** Ollama local */
    public static OpenAiCompatibleAdapter ollama(LlmSettings s) {
        return new OpenAiCompatibleAdapter(s.getOllamaBaseUrl() + "/v1", "ollama", s.getOllamaModel(), 60);
    }
    /** LM Studio local */
    public static OpenAiCompatibleAdapter lmStudio(LlmSettings s) {
        return new OpenAiCompatibleAdapter(s.getLmStudioBaseUrl() + "/v1", "lm-studio", s.getLmStudioModel(), 60);
    }
    public OpenAiCompatibleAdapter(String baseUrl, String apiKey, String model) {
        this(baseUrl, apiKey, model, 15);
    }
    public OpenAiCompatibleAdapter(String baseUrl, String apiKey, String model, int testTimeoutSeconds) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.model = model;
        this.testTimeoutSeconds = testTimeoutSeconds;
    }
    @Override
    public List<TextBlock> transcribe(byte[] imageBytes, String mimeType) throws LlmException {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            body.put("max_tokens", 4096);
            ArrayNode messages = body.putArray("messages");
            ObjectNode msg = messages.addObject();
            msg.put("role", "user");
            ArrayNode content = msg.putArray("content");
            ObjectNode imgPart = content.addObject();
            imgPart.put("type", "image_url");
            imgPart.putObject("image_url").put("url",
                    "data:" + mimeType + ";base64," + ResponseParser.toBase64(imageBytes));
            ObjectNode txtPart = content.addObject();
            txtPart.put("type", "text");
            txtPart.put("text", ResponseParser.TRANSCRIPTION_PROMPT);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .timeout(Duration.ofSeconds(180))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new LlmException("API error " + resp.statusCode() + ": " + resp.body());
            }
            String text = mapper.readTree(resp.body())
                    .path("choices").get(0).path("message").path("content").asText();
            return ResponseParser.parse(text);
        } catch (LlmException e) { throw e; }
        catch (Exception e) { throw new LlmException("OpenAI-compatible transcription failed", e); }
    }
    @Override
    public String testConnection() throws LlmException {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            body.put("max_tokens", 5);
            ArrayNode msgs = body.putArray("messages");
            ObjectNode m = msgs.addObject();
            m.put("role", "user");
            m.putArray("content").addObject().put("type", "text").put("text", "ping");
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .timeout(Duration.ofSeconds(testTimeoutSeconds))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) return "OK (" + baseUrl + ")";
            throw new LlmException("Returned " + resp.statusCode());
        } catch (LlmException e) { throw e; }
        catch (Exception e) { throw new LlmException("Connection test failed: " + e.getMessage(), e); }
    }
}
