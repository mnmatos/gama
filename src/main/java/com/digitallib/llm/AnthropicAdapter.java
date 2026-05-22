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
public class AnthropicAdapter implements LlmAdapter {
    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String API_VERSION = "2023-06-01";
    private final LlmSettings settings;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
    private final ObjectMapper mapper = new ObjectMapper();
    public AnthropicAdapter(LlmSettings settings) { this.settings = settings; }
    @Override
    public List<TextBlock> transcribe(byte[] imageBytes, String mimeType) throws LlmException {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", settings.getAnthropicModel());
            body.put("max_tokens", 4096);
            ArrayNode messages = body.putArray("messages");
            ObjectNode msg = messages.addObject();
            msg.put("role", "user");
            ArrayNode content = msg.putArray("content");
            ObjectNode imgBlock = content.addObject();
            imgBlock.put("type", "image");
            ObjectNode src = imgBlock.putObject("source");
            src.put("type", "base64");
            src.put("media_type", mimeType);
            src.put("data", ResponseParser.toBase64(imageBytes));
            ObjectNode txtBlock = content.addObject();
            txtBlock.put("type", "text");
            txtBlock.put("text", ResponseParser.TRANSCRIPTION_PROMPT);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", settings.getAnthropicApiKey())
                    .header("anthropic-version", API_VERSION)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .timeout(Duration.ofSeconds(120))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) {
                throw new LlmException("Anthropic API error " + resp.statusCode() + ": " + resp.body());
            }
            String text = mapper.readTree(resp.body()).path("content").get(0).path("text").asText();
            return ResponseParser.parse(text);
        } catch (LlmException e) {
            throw e;
        } catch (Exception e) {
            throw new LlmException("Anthropic transcription failed", e);
        }
    }
    @Override
    public String testConnection() throws LlmException {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", settings.getAnthropicModel());
            body.put("max_tokens", 10);
            ArrayNode messages = body.putArray("messages");
            ObjectNode msg = messages.addObject();
            msg.put("role", "user");
            msg.putArray("content").addObject().put("type", "text").put("text", "ping");
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", settings.getAnthropicApiKey())
                    .header("anthropic-version", API_VERSION)
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .timeout(Duration.ofSeconds(15))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() == 200) return "Anthropic OK";
            throw new LlmException("Anthropic returned " + resp.statusCode());
        } catch (LlmException e) { throw e; }
        catch (Exception e) { throw new LlmException("Anthropic connection test failed", e); }
    }
}
