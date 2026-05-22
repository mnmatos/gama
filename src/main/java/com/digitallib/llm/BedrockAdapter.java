package com.digitallib.llm;
import com.digitallib.model.LlmSettings;
import com.digitallib.model.TextBlock;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.http.SdkHttpRequest;
import java.net.URI;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.*;
import java.util.List;
public class BedrockAdapter implements LlmAdapter {
    private final LlmSettings settings;
    private final ObjectMapper mapper = new ObjectMapper();
    public BedrockAdapter(LlmSettings settings) { this.settings = settings; }
    private BedrockRuntimeClient buildClient() {
        String apiKey = settings.getBedrockApiKey();
        String endpointUrl = settings.getBedrockEndpointUrl();
        boolean hasEndpoint = endpointUrl != null && !endpointUrl.isBlank();
        // Priority 1: Bearer API key (bedrock-api-key-...)
        if (apiKey != null && !apiKey.isBlank()) {
            BedrockRuntimeClient client = BedrockRuntimeClient.builder()
                    .region(Region.of(settings.getBedrockRegion()))
                    .credentialsProvider(AnonymousCredentialsProvider.create())
                    .applyMutation(b -> { if (hasEndpoint) b.endpointOverride(URI.create(endpointUrl)); })
                    .overrideConfiguration(c -> c.addExecutionInterceptor(new ExecutionInterceptor() {
                        @Override
                        public SdkHttpRequest modifyHttpRequest(
                                Context.ModifyHttpRequest ctx, ExecutionAttributes attrs) {
                            return ctx.httpRequest().toBuilder()
                                    .putHeader("Authorization", "Bearer " + apiKey)
                                    .build();
                        }
                    }))
                    .build();
            return client;
        }
        // Priority 2: Explicit IAM credentials
        AwsCredentialsProvider credentialsProvider;
        String keyId = settings.getBedrockAccessKeyId();
        String secret = settings.getBedrockSecretAccessKey();
        String sessionToken = settings.getBedrockSessionToken();
        if (keyId != null && !keyId.isBlank() && secret != null && !secret.isBlank()) {
            if (sessionToken != null && !sessionToken.isBlank()) {
                credentialsProvider = StaticCredentialsProvider.create(
                        AwsSessionCredentials.create(keyId, secret, sessionToken));
            } else {
                credentialsProvider = StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(keyId, secret));
            }
        } else {
            // Priority 3: Default chain (env vars / ~/.aws/credentials / instance profile)
            credentialsProvider = DefaultCredentialsProvider.create();
        }
        return BedrockRuntimeClient.builder()
                .region(Region.of(settings.getBedrockRegion()))
                .credentialsProvider(credentialsProvider)
                .applyMutation(b -> { if (hasEndpoint) b.endpointOverride(URI.create(endpointUrl)); })
                .build();
    }
    @Override
    public List<TextBlock> transcribe(byte[] imageBytes, String mimeType) throws LlmException {
        try (BedrockRuntimeClient client = buildClient()) {
            ImageSource imgSrc = ImageSource.builder()
                    .bytes(SdkBytes.fromByteArray(imageBytes))
                    .build();
            ImageBlock imgBlock = ImageBlock.builder()
                    .format(toImageFormat(mimeType))
                    .source(imgSrc)
                    .build();
            ContentBlock imageContent = ContentBlock.builder().image(imgBlock).build();
            ContentBlock textContent = ContentBlock.builder()
                    .text(ResponseParser.TRANSCRIPTION_PROMPT)
                    .build();
            Message userMsg = Message.builder()
                    .role(ConversationRole.USER)
                    .content(imageContent, textContent)
                    .build();
            ConverseResponse response = client.converse(ConverseRequest.builder()
                    .modelId(settings.getBedrockModelId())
                    .messages(userMsg)
                    .build());
            String text = response.output().message().content().stream()
                    .filter(b -> b.text() != null)
                    .map(ContentBlock::text)
                    .findFirst().orElse("");
            return ResponseParser.parse(text);
        } catch (Exception e) { throw new LlmException("Bedrock transcription failed", e); }
    }
    @Override
    public String testConnection() throws LlmException {
        try (BedrockRuntimeClient client = buildClient()) {
            Message msg = Message.builder().role(ConversationRole.USER)
                    .content(ContentBlock.builder().text("ping").build()).build();
            client.converse(ConverseRequest.builder()
                    .modelId(settings.getBedrockModelId())
                    .messages(msg)
                    .build());
            return "Bedrock OK";
        } catch (Exception e) { throw new LlmException("Bedrock connection test failed", e); }
    }
    private ImageFormat toImageFormat(String mimeType) {
        switch (mimeType.toLowerCase()) {
            case "image/png": return ImageFormat.PNG;
            case "image/gif": return ImageFormat.GIF;
            case "image/webp": return ImageFormat.WEBP;
            default: return ImageFormat.JPEG;
        }
    }
}
