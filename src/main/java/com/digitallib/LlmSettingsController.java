package com.digitallib;
import com.digitallib.llm.LlmAdapter;
import com.digitallib.llm.LlmAdapterFactory;
import com.digitallib.llm.LlmException;
import com.digitallib.manager.LlmSettingsManager;
import com.digitallib.model.LlmSettings;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.concurrent.Executors;
public class LlmSettingsController {
    @FXML private ComboBox<String> activeProviderCombo;
    // Panes
    @FXML private VBox anthropicPane;
    @FXML private VBox openaiPane;
    @FXML private VBox bedrockPane;
    @FXML private VBox ollamaPane;
    @FXML private VBox lmStudioPane;
    // Anthropic
    @FXML private PasswordField anthropicApiKey;
    @FXML private ComboBox<String> anthropicModel;
    // OpenAI
    @FXML private PasswordField openaiApiKey;
    @FXML private ComboBox<String> openaiModel;
    // Bedrock
    @FXML private TextField bedrockRegion;
    @FXML private TextField bedrockModelId;
    @FXML private TextField bedrockEndpointUrl;
    @FXML private PasswordField bedrockApiKey;
    @FXML private PasswordField bedrockAccessKeyId;
    @FXML private PasswordField bedrockSecretAccessKey;
    @FXML private PasswordField bedrockSessionToken;
    // Ollama
    @FXML private TextField ollamaBaseUrl;
    @FXML private TextField ollamaModel;
    // LM Studio
    @FXML private TextField lmStudioBaseUrl;
    @FXML private TextField lmStudioModel;
    @FXML private Label statusLabel;
    @FXML
    public void initialize() {
        activeProviderCombo.getItems().setAll("anthropic","openai","bedrock","ollama","lmstudio");
        anthropicModel.getItems().setAll("claude-3-5-sonnet-20241022","claude-3-opus-20240229","claude-3-haiku-20240307");
        openaiModel.getItems().setAll("gpt-4o","gpt-4o-mini","gpt-4-turbo");
        LlmSettings s = LlmSettingsManager.getInstance().loadMasked();
        activeProviderCombo.setValue(s.getProvider() != null ? s.getProvider() : "anthropic");
        anthropicApiKey.setText(s.getAnthropicApiKey() != null ? s.getAnthropicApiKey() : "");
        anthropicModel.setValue(s.getAnthropicModel());
        openaiApiKey.setText(s.getOpenaiApiKey() != null ? s.getOpenaiApiKey() : "");
        openaiModel.setValue(s.getOpenaiModel());
        bedrockRegion.setText(s.getBedrockRegion() != null ? s.getBedrockRegion() : "us-east-1");
        bedrockModelId.setText(s.getBedrockModelId() != null ? s.getBedrockModelId() : "");
        bedrockEndpointUrl.setText(s.getBedrockEndpointUrl() != null ? s.getBedrockEndpointUrl() : "");
        bedrockApiKey.setText(s.getBedrockApiKey() != null ? s.getBedrockApiKey() : "");
        bedrockAccessKeyId.setText(s.getBedrockAccessKeyId() != null ? s.getBedrockAccessKeyId() : "");
        bedrockSecretAccessKey.setText(s.getBedrockSecretAccessKey() != null ? s.getBedrockSecretAccessKey() : "");
        bedrockSessionToken.setText(s.getBedrockSessionToken() != null ? s.getBedrockSessionToken() : "");
        ollamaBaseUrl.setText(s.getOllamaBaseUrl() != null ? s.getOllamaBaseUrl() : "http://localhost:11434");
        ollamaModel.setText(s.getOllamaModel() != null ? s.getOllamaModel() : "llava");
        lmStudioBaseUrl.setText(s.getLmStudioBaseUrl() != null ? s.getLmStudioBaseUrl() : "http://localhost:1234");
        lmStudioModel.setText(s.getLmStudioModel() != null ? s.getLmStudioModel() : "");
        // Apply disabled state initially and whenever the combo changes
        updatePaneStates(activeProviderCombo.getValue());
        activeProviderCombo.valueProperty().addListener((obs, old, val) -> updatePaneStates(val));
    }
    /** Disable all panes except the one matching the active provider. */
    private void updatePaneStates(String provider) {
        String p = provider != null ? provider.toLowerCase() : "";
        anthropicPane.setDisable(!"anthropic".equals(p));
        openaiPane.setDisable(!"openai".equals(p));
        bedrockPane.setDisable(!"bedrock".equals(p));
        ollamaPane.setDisable(!"ollama".equals(p));
        lmStudioPane.setDisable(!"lmstudio".equals(p));
    }
    @FXML
    private void handleSave() {
        LlmSettings s = buildFromForm();
        try {
            LlmSettingsManager.getInstance().save(s);
            statusLabel.setText("Configuracoes salvas.");
            statusLabel.setStyle("-fx-text-fill: green;");
        } catch (Exception e) {
            statusLabel.setText("Erro ao salvar: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    @FXML
    private void handleTest() {
        statusLabel.setText("Testando conexao...");
        statusLabel.setStyle("-fx-text-fill: gray;");
        LlmSettings base = LlmSettingsManager.getInstance().load();
        LlmSettings s = buildFromForm();
        if (isMasked(s.getAnthropicApiKey()))        s.setAnthropicApiKey(base.getAnthropicApiKey());
        if (isMasked(s.getOpenaiApiKey()))           s.setOpenaiApiKey(base.getOpenaiApiKey());
        if (isMasked(s.getBedrockApiKey()))          s.setBedrockApiKey(base.getBedrockApiKey());
        if (isMasked(s.getBedrockAccessKeyId()))     s.setBedrockAccessKeyId(base.getBedrockAccessKeyId());
        if (isMasked(s.getBedrockSecretAccessKey())) s.setBedrockSecretAccessKey(base.getBedrockSecretAccessKey());
        if (isMasked(s.getBedrockSessionToken()))    s.setBedrockSessionToken(base.getBedrockSessionToken());
        Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r); t.setDaemon(true); return t; })
                .submit(() -> {
            try {
                LlmAdapter adapter = LlmAdapterFactory.getAdapter(s);
                String msg = adapter.testConnection();
                Platform.runLater(() -> {
                    statusLabel.setText("OK: " + msg);
                    statusLabel.setStyle("-fx-text-fill: green;");
                });
            } catch (LlmException e) {
                Platform.runLater(() -> {
                    statusLabel.setText("Falha: " + e.getMessage());
                    statusLabel.setStyle("-fx-text-fill: red;");
                });
            }
        });
    }
    @FXML
    private void handleClose() {
        ((Stage) activeProviderCombo.getScene().getWindow()).close();
    }
    private LlmSettings buildFromForm() {
        LlmSettings s = new LlmSettings();
        s.setProvider(activeProviderCombo.getValue());
        s.setAnthropicApiKey(anthropicApiKey.getText());
        s.setAnthropicModel(anthropicModel.getValue());
        s.setOpenaiApiKey(openaiApiKey.getText());
        s.setOpenaiModel(openaiModel.getValue());
        s.setBedrockRegion(bedrockRegion.getText());
        s.setBedrockModelId(bedrockModelId.getText());
        s.setBedrockEndpointUrl(bedrockEndpointUrl.getText());
        s.setBedrockApiKey(bedrockApiKey.getText());
        s.setBedrockAccessKeyId(bedrockAccessKeyId.getText());
        s.setBedrockSecretAccessKey(bedrockSecretAccessKey.getText());
        s.setBedrockSessionToken(bedrockSessionToken.getText());
        s.setOllamaBaseUrl(ollamaBaseUrl.getText());
        s.setOllamaModel(ollamaModel.getText());
        s.setLmStudioBaseUrl(lmStudioBaseUrl.getText());
        s.setLmStudioModel(lmStudioModel.getText());
        return s;
    }
    private boolean isMasked(String value) {
        return value == null || value.isBlank() || "***".equals(value);
    }
}
