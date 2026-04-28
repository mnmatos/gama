package com.digitallib.model;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LlmSettings {
    @JsonProperty("provider")
    private String provider = "anthropic";
    @JsonProperty("anthropicApiKey")
    private String anthropicApiKey;
    @JsonProperty("anthropicModel")
    private String anthropicModel = "claude-3-5-sonnet-20241022";
    @JsonProperty("openaiApiKey")
    private String openaiApiKey;
    @JsonProperty("openaiModel")
    private String openaiModel = "gpt-4o";
    @JsonProperty("bedrockRegion")
    private String bedrockRegion = "us-east-1";
    @JsonProperty("bedrockModelId")
    private String bedrockModelId = "anthropic.claude-3-5-sonnet-20241022-v2:0";
    @JsonProperty("bedrockEndpointUrl")
    private String bedrockEndpointUrl;
    @JsonProperty("bedrockAccessKeyId")
    private String bedrockAccessKeyId;
    @JsonProperty("bedrockSecretAccessKey")
    private String bedrockSecretAccessKey;
    @JsonProperty("bedrockSessionToken")
    private String bedrockSessionToken;
    @JsonProperty("bedrockApiKey")
    private String bedrockApiKey;
    @JsonProperty("ollamaBaseUrl")
    private String ollamaBaseUrl = "http://localhost:11434";
    @JsonProperty("ollamaModel")
    private String ollamaModel = "llava";
    @JsonProperty("lmStudioBaseUrl")
    private String lmStudioBaseUrl = "http://localhost:1234";
    @JsonProperty("lmStudioModel")
    private String lmStudioModel;
    public LlmSettings() {}
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getAnthropicApiKey() { return anthropicApiKey; }
    public void setAnthropicApiKey(String anthropicApiKey) { this.anthropicApiKey = anthropicApiKey; }
    public String getAnthropicModel() { return anthropicModel; }
    public void setAnthropicModel(String anthropicModel) { this.anthropicModel = anthropicModel; }
    public String getOpenaiApiKey() { return openaiApiKey; }
    public void setOpenaiApiKey(String openaiApiKey) { this.openaiApiKey = openaiApiKey; }
    public String getOpenaiModel() { return openaiModel; }
    public void setOpenaiModel(String openaiModel) { this.openaiModel = openaiModel; }
    public String getBedrockRegion() { return bedrockRegion; }
    public void setBedrockRegion(String bedrockRegion) { this.bedrockRegion = bedrockRegion; }
    public String getBedrockModelId() { return bedrockModelId; }
    public void setBedrockModelId(String bedrockModelId) { this.bedrockModelId = bedrockModelId; }
    public String getBedrockEndpointUrl() { return bedrockEndpointUrl; }
    public void setBedrockEndpointUrl(String bedrockEndpointUrl) { this.bedrockEndpointUrl = bedrockEndpointUrl; }
    public String getBedrockAccessKeyId() { return bedrockAccessKeyId; }
    public void setBedrockAccessKeyId(String bedrockAccessKeyId) { this.bedrockAccessKeyId = bedrockAccessKeyId; }
    public String getBedrockSecretAccessKey() { return bedrockSecretAccessKey; }
    public void setBedrockSecretAccessKey(String bedrockSecretAccessKey) { this.bedrockSecretAccessKey = bedrockSecretAccessKey; }
    public String getBedrockSessionToken() { return bedrockSessionToken; }
    public void setBedrockSessionToken(String bedrockSessionToken) { this.bedrockSessionToken = bedrockSessionToken; }
    public String getBedrockApiKey() { return bedrockApiKey; }
    public void setBedrockApiKey(String bedrockApiKey) { this.bedrockApiKey = bedrockApiKey; }
    public String getOllamaBaseUrl() { return ollamaBaseUrl; }
    public void setOllamaBaseUrl(String ollamaBaseUrl) { this.ollamaBaseUrl = ollamaBaseUrl; }
    public String getOllamaModel() { return ollamaModel; }
    public void setOllamaModel(String ollamaModel) { this.ollamaModel = ollamaModel; }
    public String getLmStudioBaseUrl() { return lmStudioBaseUrl; }
    public void setLmStudioBaseUrl(String lmStudioBaseUrl) { this.lmStudioBaseUrl = lmStudioBaseUrl; }
    public String getLmStudioModel() { return lmStudioModel; }
    public void setLmStudioModel(String lmStudioModel) { this.lmStudioModel = lmStudioModel; }
}