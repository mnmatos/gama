package com.digitallib;
import com.digitallib.llm.LlmAdapter;
import com.digitallib.llm.LlmAdapterFactory;
import com.digitallib.llm.LlmException;
import com.digitallib.manager.LlmSettingsManager;
import com.digitallib.model.LlmSettings;
import com.digitallib.ocr.TesseractOcrAdapter;
import com.digitallib.transcription.TranscriptionException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.concurrent.Executors;
public class LlmSettingsController {
    @FXML private RadioButton radioLlm;
    @FXML private RadioButton radioOcr;
    @FXML private ToggleGroup transcriptionToolGroup;
    @FXML private TitledPane llmSection;
    @FXML private TitledPane ocrSection;
    @FXML private Button btnTestLlm;
    @FXML private Button btnTestOcr;
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
    // OCR
    @FXML private TextField tessdataPath;
    @FXML private TextField ocrLanguage;
    @FXML private Label statusLabel;
    @FXML
    public void initialize() {
        activeProviderCombo.getItems().setAll("anthropic","openai","bedrock","ollama","lmstudio");
        anthropicModel.getItems().setAll("claude-3-5-sonnet-20241022","claude-3-opus-20240229","claude-3-haiku-20240307");
        openaiModel.getItems().setAll("gpt-4o","gpt-4o-mini","gpt-4-turbo");
        LlmSettings s = LlmSettingsManager.getInstance().loadMasked();
        // Transcription tool
        boolean isOcr = "ocr".equalsIgnoreCase(s.getTranscriptionTool());
        radioOcr.setSelected(isOcr);
        radioLlm.setSelected(!isOcr);
        updateSectionVisibility(isOcr);
        radioLlm.setOnAction(e -> updateSectionVisibility(false));
        radioOcr.setOnAction(e -> updateSectionVisibility(true));
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
        tessdataPath.setText(s.getTessdataPath() != null ? s.getTessdataPath() : "");
        ocrLanguage.setText(s.getOcrLanguage() != null ? s.getOcrLanguage() : "por");
        updatePaneStates(activeProviderCombo.getValue());
        activeProviderCombo.valueProperty().addListener((obs, old, val) -> updatePaneStates(val));
    }
    private void updateSectionVisibility(boolean ocrSelected) {
        llmSection.setVisible(!ocrSelected);
        llmSection.setManaged(!ocrSelected);
        ocrSection.setVisible(ocrSelected);
        ocrSection.setManaged(ocrSelected);
        btnTestLlm.setVisible(!ocrSelected);
        btnTestLlm.setManaged(!ocrSelected);
        btnTestOcr.setVisible(ocrSelected);
        btnTestOcr.setManaged(ocrSelected);
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
    private void handleBrowseTessdata() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Selecionar pasta tessdata");
        if (!tessdataPath.getText().isBlank()) {
            File f = new File(tessdataPath.getText());
            if (f.exists()) dc.setInitialDirectory(f);
        }
        File dir = dc.showDialog(tessdataPath.getScene().getWindow());
        if (dir != null) tessdataPath.setText(dir.getAbsolutePath());
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
        statusLabel.setText("Testando conexao LLM...");
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
                Platform.runLater(() -> { statusLabel.setText("OK: " + msg); statusLabel.setStyle("-fx-text-fill: green;"); });
            } catch (LlmException e) {
                Platform.runLater(() -> { statusLabel.setText("Falha: " + e.getMessage()); statusLabel.setStyle("-fx-text-fill: red;"); });
            }
        });
    }
    @FXML
    private void handleTestOcr() {
        statusLabel.setText("Testando Tesseract OCR...");
        statusLabel.setStyle("-fx-text-fill: gray;");
        LlmSettings s = buildFromForm();
        Executors.newSingleThreadExecutor(r -> { Thread t = new Thread(r); t.setDaemon(true); return t; })
                .submit(() -> {
            try {
                String msg = new TesseractOcrAdapter(s).testConnection();
                Platform.runLater(() -> { statusLabel.setText("OK: " + msg); statusLabel.setStyle("-fx-text-fill: green;"); });
            } catch (TranscriptionException e) {
                Platform.runLater(() -> { statusLabel.setText("Falha OCR: " + e.getMessage()); statusLabel.setStyle("-fx-text-fill: red;"); });
            }
        });
    }
    @FXML
    private void handleClose() {
        ((Stage) activeProviderCombo.getScene().getWindow()).close();
    }
    private LlmSettings buildFromForm() {
        LlmSettings s = new LlmSettings();
        s.setTranscriptionTool(radioOcr.isSelected() ? "ocr" : "llm");
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
        s.setTessdataPath(tessdataPath.getText());
        s.setOcrLanguage(ocrLanguage.getText());
        return s;
    }
    private boolean isMasked(String value) {
        return value == null || value.isBlank() || "***".equals(value);
    }
}
