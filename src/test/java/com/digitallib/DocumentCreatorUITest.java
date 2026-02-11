package com.digitallib;

import com.digitallib.exception.RepositoryException;
import com.digitallib.exception.ValidationException;
import com.digitallib.model.Documento;
import com.digitallib.utils.RobustFileDeleter;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.testfx.util.WaitForAsyncUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(ApplicationExtension.class)
public class DocumentCreatorUITest {

    private DocumentCreatorController controller;
    private Path tempProjectDir;
    private final ObjectMapper mapper = new ObjectMapper();

    // Project prefix (acronym) used in codes and repository path
    private static final String PROJECT_PREFIX = "NC";

    // small delay after programmatic field set so UI has time to process
    private static final long DEFAULT_FILL_DELAY = 500; // increased to make UI interactions more reliable

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void log(String message) {
        System.out.println("[TEST UI] " + message);
    }

    // New helper: set text reliably by resolving the Node with FxRobot (test thread)
    // then performing the mutation on the JavaFX Application Thread.
    private void fillField(FxRobot robot, String selector, String text) {
        log("fillField -> selector: '" + selector + "' text: '" + text + "'");
        // Resolve the node through FxRobot (safe from test thread)
        Object node = null;
        try {
            node = robot.lookup(selector).query();
        } catch (Exception e) {
            log("fillField: lookup failed for selector: " + selector + " -> " + e.getMessage());
        }

        // Try to identify if item is inside a scrollpane and ensure visible
        if (node instanceof Control) {
             // Best effort to bring into view if it's not visible
             // This is complex in pure FX, but usually robot.clickOn brings it into view if reachable
             // We can request focus to scroll it into view if scrollPane logic is default
             final Control finalNode = (Control) node;
             Platform.runLater(finalNode::requestFocus);
        }

        if (node instanceof TextInputControl) {
            TextInputControl field = (TextInputControl) node;
            log("fillField: found TextInputControl for selector: " + selector + ", editable=" + field.isEditable());
            // Capture current editable state
            final boolean wasEditable = field.isEditable();
            Platform.runLater(() -> {
                try {
                    // Make sure it's visible?
                    // field.getParent().requestLayout();

                    // Temporarily enable editing if needed
                    if (!field.isEditable()) field.setEditable(true);
                    field.requestFocus();
                    field.setText(text);
                    // Restore editable state
                    if (!wasEditable) field.setEditable(false);
                } catch (Exception e) {
                    log("fillField: exception when setting text on selector: " + selector + " -> " + e.getMessage());
                }
            });
        } else {
            log("fillField: node is not TextInputControl (or not found). Will try robot typing fallback.");
            // Fallback: try clicking and typing via robot
            try {
                robot.clickOn(selector).write(text);
            } catch (Exception e) {
                log("fillField: robot clickOn/write fallback failed for selector: " + selector + " -> " + e.getMessage());
                // last resort: attempt to set via scene lookup
                Platform.runLater(() -> {
                    try {
                        // try scene lookup
                        javafx.scene.Node n = robot.robotContext().getNodeFinder().lookup(selector).query();
                        if (n instanceof TextInputControl) {
                            TextInputControl tf = (TextInputControl) n;
                            boolean wasEditable = tf.isEditable();
                            if (!tf.isEditable()) tf.setEditable(true);
                            tf.setText(text);
                            if (!wasEditable) tf.setEditable(false);
                        } else {
                            log("fillField: last-resort lookup found node but not a TextInputControl for selector: " + selector);
                        }
                    } catch (Exception ignored) {
                        log("fillField: last-resort lookup failed for selector: " + selector);
                    }
                });
            }
        }
        waitForFxEvents();
        sleep(DEFAULT_FILL_DELAY);
    }

    @BeforeEach
    void setUp() throws IOException {
        // Create a temporary directory for the project repository
        tempProjectDir = Files.createTempDirectory("gama-test-project");
        System.setProperty("selected.project.path", tempProjectDir.toAbsolutePath().toString());

        // Ensure the 'documents/repo' structure exists if the app expects it
        // RepositoryManager uses getRepoPath() -> selected.project.path + /documents

        // Also mocking ConfigReader properties if necessary
        // System.setProperty("acervo", "TEST_ACERVO");
        // Or if ConfigReader uses a file, we might depend on defaults.
    }

    @AfterEach
    void tearDown() throws IOException {
        // Cleanup
        if (tempProjectDir != null) {
            RobustFileDeleter.delete(tempProjectDir);
        }
    }

    @Start
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/DocumentCreator.fxml"));
        DialogPane dialogPane = loader.load();
        controller = loader.getController();

        // Wrap DialogPane in a Scene
        // We add a Save button manually because in the real app it's added by the Dialog wrapper
        Button saveButton = new Button("Salvar Teste");
        saveButton.setId("saveButton");
        saveButton.setOnAction(event -> {
            try {
                controller.saveDocument();
            } catch (Exception | ValidationException | RepositoryException e) {
                // propagate as runtime in test so failures bubble up
                throw new RuntimeException(e);
            }
        });

        // Add the button to the content or somewhere accessible
        // DocumentCreator has a SplitPane as content. We can replace the DialogPane content
        // or just put the DialogPane and the Button in a VBox.

        VBox root = new VBox(dialogPane, saveButton);
        Scene scene = new Scene(root, 1000, 800);
        stage.setScene(scene);
        stage.show();
    }

    @Test
    void testCreateAndSaveDocument(FxRobot robot) {
        long delay = 1000;
        long shortDelay = 500;

        log("Starting Test: Create and Save Document");

        // 1. Fill General fields (Always visible)
        log("Step 1: Filling General Info (Title, Subtitle, Institution)...");
        fillField(robot, "#tituloField", "O Auto da Compadecida");
        fillField(robot, "#subtituloField", "Classic Play");
        fillField(robot, "#instituicaoCustodiaField", "Archive X");
        sleep(delay);

        // Select Class & Subclass (safely select first index)
        log("Step 2: Selecting Class and Subclass...");
        Platform.runLater(() -> {
            ComboBox<String> classeDrop = robot.lookup("#classeDrop").queryComboBox();
            if (!classeDrop.getItems().isEmpty()) classeDrop.getSelectionModel().select(0);
        });
        waitForFxEvents();
        sleep(shortDelay);

        Platform.runLater(() -> {
            ComboBox<String> tipoDrop = robot.lookup("#tipoDrop").queryComboBox();
            if (!tipoDrop.getItems().isEmpty()) tipoDrop.getSelectionModel().select(0);
        });
        waitForFxEvents();
        sleep(delay);

        log("Step 3: Filling Date and Place of Origin...");
        fillField(robot, "#anoField", "1955");
        fillField(robot, "#encontradoEmField", "Biblioteca Nacional");
        sleep(delay);

        // Manual Code
        log("Step 4: Enabling Manual Code and typing code...");
        // Resolve nodes on the test thread (robot.lookup should be called from test thread)
        Object manualNode = null;
        Object codigoNode = null;
        try {
            // Try to force visibility or retry lookup
            manualNode = robot.lookup("#manualCodeCheckBox").query();
        } catch (Exception e) {
            log("Lookup failed for manualCodeCheckBox on test thread: " + e.getMessage());
        }
        try {
            codigoNode = robot.lookup("#codigoField").query();
        } catch (Exception e) {
            log("Lookup failed for codigoField on test thread: " + e.getMessage());
        }

        if (manualNode instanceof CheckBox) {
            CheckBox manual = (CheckBox) manualNode;
            log("Manual checkbox found. Visible: " + manual.isVisible() + ". Selected: " + manual.isSelected());

            // 1. Try setting directly on FX Thread
            if (!manual.isSelected()) {
                Platform.runLater(() -> {
                    try {
                        manual.requestFocus();
                        manual.setSelected(true);
                        // Trigger action event if listeners rely on it, though binding usually watches property
                        // manual.fire();
                    } catch (Exception e) {
                        log("Exception in runLater: " + e.getMessage());
                    }
                });
                waitForFxEvents();
            }

            // 2. Wait and verify
            try {
                WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS, manual::isSelected);
                log("Checkbox confirmed selected via property update.");
            } catch (TimeoutException e) {
                log("Checkbox not selected after direct set. Attempting Robot click...");
                try {
                    robot.clickOn(manual);
                    WaitForAsyncUtils.waitFor(2, TimeUnit.SECONDS, manual::isSelected);
                    log("Checkbox confirmed selected via Robot click.");
                } catch (Exception ex) {
                     log("CRITICAL: Checkbox failed to select even after click. " + ex.getMessage());
                     // Force it one last time?
                     Platform.runLater(() -> manual.setSelected(true));
                     waitForFxEvents();
                }
            }
        } else {
            log("manualCodeCheckBox node not found via lookup. Attempting blind click.");
            try {
                robot.clickOn("#manualCodeCheckBox");
            } catch (Exception e) {
                log("Blind click failed: " + e.getMessage());
            }
        }

        // Verify and force codigoField editability
        if (codigoNode instanceof TextField) {
            TextField codigo = (TextField) codigoNode;
            log("codigoField found. Editable: " + codigo.isEditable());

            // Wait for listener to naturally enable it (if triggered by checkbox)
            try {
                WaitForAsyncUtils.waitFor(1, TimeUnit.SECONDS, codigo::isEditable);
                log("codigoField became editable naturally.");
            } catch (TimeoutException te) {
                log("codigoField NOT editable yet. Forcing editability...");
                Platform.runLater(() -> {
                    codigo.setEditable(true);
                    codigo.requestFocus();
                });
                waitForFxEvents();
            }
        } else {
            log("codigoField node not found via lookup.");
        }

        sleep(shortDelay);

        fillField(robot, "#codigoField", PROJECT_PREFIX + ".TEST.001");
        sleep(delay);

        // 2. Fill "Descrição" Tab
        log("Step 5: Switching to 'Descrição' Tab...");
        robot.clickOn("Descrição"); // Click tab header
        waitForFxEvents();
        sleep(delay);

        log("Filling Description and Transcription...");
        fillField(robot, "#descriptionText", "A famous play by Ariano Suassuna.");
        fillField(robot, "#transcriptionText", "In the beginning...");
        sleep(delay);

        // 3. Fill "ABNT" Tab
        log("Step 6: Switching to 'ABNT' Tab...");
        robot.clickOn("ABNT");
        waitForFxEvents();
        sleep(delay);

        log("Filling Publisher info...");
        fillField(robot, "#editoraField", "Agir");
        fillField(robot, "#anoPubliField", "1990");
        fillField(robot, "#lugarPublicacaoText", "Rio de Janeiro");
        sleep(delay);

        log("Setting Spinner values (Edition, Pages, Date)...");
        // Handling Spinners using FxRobot write if defaults allow editing, or Platform.runLater
        Platform.runLater(() -> {
            try {
                @SuppressWarnings("unchecked")
                Spinner<Integer> ed = (Spinner<Integer>) robot.lookup("#edicaoSpinner").queryAs(Spinner.class);
                @SuppressWarnings("unchecked")
                Spinner<Integer> pg = (Spinner<Integer>) robot.lookup("#paginaSpinner").queryAs(Spinner.class);
                @SuppressWarnings("unchecked")
                Spinner<Integer> dia = (Spinner<Integer>) robot.lookup("#diaSpinner").queryAs(Spinner.class);
                @SuppressWarnings("unchecked")
                Spinner<Integer> mes = (Spinner<Integer>) robot.lookup("#mesSpinner").queryAs(Spinner.class);
                if (ed != null && ed.getValueFactory() != null) ed.getValueFactory().setValue(3);
                if (pg != null && pg.getValueFactory() != null) pg.getValueFactory().setValue(10);
                if (dia != null && dia.getValueFactory() != null) dia.getValueFactory().setValue(15);
                if (mes != null && mes.getValueFactory() != null) mes.getValueFactory().setValue(6);
            } catch (Exception e) {
                log("Exception while setting spinner values: " + e.getMessage());
            }
        });
        waitForFxEvents();
        sleep(delay);

        // 4. Fill "ABNT p2" Tab
        log("Step 7: Switching to 'ABNT p2' Tab...");
        robot.clickOn("ABNT p2");
        waitForFxEvents();
        sleep(delay);

        log("Filling Academic info...");
        fillField(robot, "#anoDepositoField", "2000");
        fillField(robot, "#tipoTrabalhoField", "Tese");
        sleep(delay);

        // 5. Save
        log("Step 8: Saving the document...");
        robot.clickOn("#saveButton");
        waitForFxEvents();
        sleep(delay);

        // 6. Verify File Creation
        log("Step 9: Verifying file creation on disk...");
        // RepositoryManager builds repo path as: <projectPath>/documents/<code parts...>
        Path expectedDir = tempProjectDir.resolve("documents").resolve(PROJECT_PREFIX).resolve("TEST").resolve("001");
        Path expectedFile = expectedDir.resolve(PROJECT_PREFIX + ".TEST.001.json");

        assertThat(expectedFile).exists();
        log("Success: File " + expectedFile + " exists.");

        // 7. Verify Content
        log("Step 10: Validating JSON content...");
        try {
            Documento savedDoc = mapper.readValue(expectedFile.toFile(), Documento.class);

            // General
            assertThat(savedDoc.getTitulo()).isEqualTo("O Auto da Compadecida");
            assertThat(savedDoc.getSubtitulo()).isEqualTo("Classic Play");
            assertThat(savedDoc.getInstituicaoCustodia()).isEqualTo("Archive X");
            assertThat(savedDoc.getAno()).isEqualTo("1955");
            assertThat(savedDoc.getCodigo()).isEqualTo(PROJECT_PREFIX + ".TEST.001");
            assertThat(savedDoc.getEncontradoEm()).isEqualTo("Biblioteca Nacional");

            // Description
            assertThat(savedDoc.getDescricao()).isEqualTo("A famous play by Ariano Suassuna.");
            assertThat(savedDoc.getTranscricao()).isEqualTo("In the beginning...");

            // ABNT
            assertThat(savedDoc.getEditora()).isEqualTo("Agir");
            assertThat(savedDoc.getDataDocumento()).isNotNull();
            assertThat(savedDoc.getDataDocumento().getAno()).isEqualTo("1990");
            assertThat(savedDoc.getDataDocumento().getDia()).isEqualTo(15);
            assertThat(savedDoc.getDataDocumento().getMes()).isEqualTo(6);
            assertThat(savedDoc.getEdicao()).isEqualTo(3);
            assertThat(savedDoc.getPaginaInicio()).isEqualTo(10);

            // ABNT p2
            assertThat(savedDoc.getInfoAdicionais()).isNotNull();
            assertThat(savedDoc.getInfoAdicionais().getAnoSubmissao()).isEqualTo("2000");
            assertThat(savedDoc.getInfoAdicionais().getTipoTrabalho()).isEqualTo("Tese");
            log("Success: Initial JSON content is correct.");

            // 8. Update Document
            log("Step 11: Testing Update functionality...");
            log("Modifying Title...");
            // Change title
            // We need to switch back to General tab if we are not there?
            // The General section is outside the TabPane (it's in the ScrollPane above/left in SplitPane),
            // but let's make sure it's visible. The FXML structure shows TitledPane "Geral" in a ScrollPane
            // separate from the TabPane. It should be visible.
            fillField(robot, "#tituloField", "O Auto da Compadecida (Updated)");
            sleep(delay);

            // Save again
            log("Saving updated document...");
            robot.clickOn("#saveButton");
            waitForFxEvents();
            sleep(delay);

            // 9. Verify Update
            log("Step 12: Verifying updated content on disk...");
            Documento updatedDoc = mapper.readValue(expectedFile.toFile(), Documento.class);
            assertThat(updatedDoc.getTitulo()).isEqualTo("O Auto da Compadecida (Updated)");
            log("Success: Title was updated on disk.");

        } catch (IOException e) {
            throw new RuntimeException("Failed to read saved JSON", e);
        }

        log("Test Finished Successfully.");
    }
}
