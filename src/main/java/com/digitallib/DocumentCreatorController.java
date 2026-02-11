package com.digitallib;

import com.digitallib.code.CodeManager;
import com.digitallib.exception.EntityNotFoundException;
import com.digitallib.exception.RepositoryException;
import com.digitallib.exception.ValidationException;
import com.digitallib.manager.CategoryManager;
import com.digitallib.manager.EntityManager;
import com.digitallib.manager.MultiSourcedDocumentManager;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.*;
import com.digitallib.model.entity.Entity;
import com.digitallib.model.entity.EntityType;
import com.digitallib.utils.ConfigReader;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.application.Platform;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.geometry.Rectangle2D;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;

public class DocumentCreatorController implements Initializable {

    private static final Logger logger = LogManager.getLogger(DocumentCreatorController.class);

    @FXML private TabPane tabPane;
    @FXML private Tab teatroTab;
    @FXML private DialogPane rootPane;
    @FXML private TextField tituloField;
    @FXML private CheckBox manualCodeCheckBox;
    @FXML private TextField codigoField;
    @FXML private ComboBox<String> classeDrop;
    @FXML private ComboBox<String> tipoDrop;
    @FXML private ComboBox<String> tipoNbrDrop;
    @FXML private TextField anoField;
    @FXML private Spinner<Integer> mesSpinner;
    @FXML private Spinner<Integer> diaSpinner;
    @FXML private CheckBox dataIncertaCheckBox;
    @FXML private TextField encontradoEmField;
    @FXML private TextField instituicaoCustodiaField;
    @FXML private CheckBox ineditoCheckBox;
    @FXML private CheckBox danosAoSuporteCheckBox;
    @FXML private Spinner<Integer> paginaSpinner;
    @FXML private Spinner<Integer> numPaginaSpinner;
    @FXML private Spinner<Integer> colunaSpinner;

    @FXML private TextField tituloPublicacaoField;
    @FXML private TextField subTituloPublicacaoField;
    @FXML private TextField editoraField;
    @FXML private TextField anoPubliField;
    @FXML private Spinner<Integer> edicaoSpinner;
    @FXML private Spinner<Integer> numPubliSpinner;
    @FXML private TextField volumeField;
    @FXML private TextField lugarPublicacaoText;
    @FXML private TextField disponivelField;
    @FXML private TextField acessoField;
    @FXML private TextField doiField;

    @FXML private ListView<String> authorList;
    @FXML private ListView<String> authorPubliList;
    @FXML private ListView<String> listaCitacao;

    @FXML private TextArea descriptionText;
    @FXML private TextArea transcriptionText;
    @FXML private TreeView<String> fileTree;

    @FXML private Spinner<Integer> teatroPersonagemSpinner;
    @FXML private Spinner<Integer> teatroAtoSpinner;
    @FXML private Spinner<Integer> teatroCenaSpinner;
    @FXML private TextArea teatroResumoText;
    @FXML private TextField teatroKeywordInput;
    @FXML private ListView<String> teatroKeywordList;

    @FXML private ComboBox<TIPO_RELACAO> linkDropDown;
    @FXML private Button adicionarButton;
    @FXML private Button removeLinkButton;
    @FXML private TextField linkCodeField;
    @FXML private ListView<Relacao> linkList;
    @FXML private TextField testimonyField;
    @FXML private TextField subtituloField;
    @FXML private TextField anoDepositoField;
    @FXML private TextField tipoTrabalhoField;
    @FXML private TextField grauField;
    @FXML private TextField cursoField;
    @FXML private TextField entrevistadorField;
    @FXML private TextField publicadoPorField;
    @FXML private TextField destinatarioField;
    @FXML private TextField descricaoFisicaField;
    @FXML private TextField infoComplementarField;

    private Documento documento;
    private final CategoryManager categoryManager = new CategoryManager();
    private final Map<String, String> authorMap = new HashMap<>(); // Name -> ID
    private final Map<String, String> authorPubliMap = new HashMap<>();
    private final Map<String, String> citacoesMap = new HashMap<>();

    private Entity lugarPublicacao;
    private List<File> files = new ArrayList<>();
    private String editedDocCode;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeSpinners();
        initializeDropdowns();
        initializeLinks();

         // Make codigoField editable only when manualCodeCheckBox is selected
        if (manualCodeCheckBox != null && codigoField != null) {
            // set initial editable state
            codigoField.setEditable(manualCodeCheckBox.isSelected());
            // toggle editable on change
            manualCodeCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                codigoField.setEditable(newVal);
                if (newVal) {
                    // focus the field so the user can start typing immediately
                    Platform.runLater(() -> {
                        try {
                            codigoField.requestFocus();
                        } catch (Exception ignored) {}
                    });
                }
            });
        }

        // Hide Teatro tab by default
        tabPane.getTabs().remove(teatroTab);

        // Resize dialog/window to use more of the screen when available
        Platform.runLater(() -> {
            try {
                if (rootPane != null && rootPane.getScene() != null) {
                    Window window = rootPane.getScene().getWindow();
                    if (window != null) {
                        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
                        double maxW = bounds.getWidth() * 0.9; // use up to 90% of screen width
                        double maxH = bounds.getHeight() * 0.9; // use up to 90% of screen height
                        // Use up to 90% of the screen (no fixed caps)
                        double targetW = Math.max(rootPane.getPrefWidth(), maxW);
                        double targetH = Math.max(rootPane.getPrefHeight(), maxH);
                        window.setWidth(targetW);
                        window.setHeight(targetH);
                        window.centerOnScreen();
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to resize dialog window", e);
            }
        });
    }

    private void initializeLinks() {
        linkDropDown.setItems(FXCollections.observableArrayList(TIPO_RELACAO.values()));
        linkDropDown.getSelectionModel().select(0);
    }

    private void initializeSpinners() {
        mesSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 12, 1));
        diaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, 1));
        paginaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0));
        numPaginaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0));
        colunaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 1));

        edicaoSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        numPubliSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10000, 0));

        teatroPersonagemSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        teatroAtoSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));
        teatroCenaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 50, 0));
    }

    private void initializeDropdowns() {
        classeDrop.setItems(FXCollections.observableArrayList(categoryManager.getClasseAsStringArray()));
        classeDrop.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            refreshSubClassDialog(classeDrop.getSelectionModel().getSelectedIndex());
        });

        if (!classeDrop.getItems().isEmpty()) {
            classeDrop.getSelectionModel().select(0);
        }

        tipoDrop.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            // Check for 'teatro_info' in extensions of subclass
            checkForTeatro(classeDrop.getSelectionModel().getSelectedIndex());
        });

        tipoNbrDrop.setItems(FXCollections.observableArrayList(TipoNBR.getAsStringList()));
    }

    private void refreshSubClassDialog(int index) {
        List<String> subs = new ArrayList<>();
        List<SubClasse> subClasses = categoryManager.getSubClassesForIndex(index);
        if (subClasses != null) {
            for (SubClasse sub : subClasses) {
                subs.add(sub.getDesc());
            }
        }
        tipoDrop.setItems(FXCollections.observableArrayList(subs));
        if (!subs.isEmpty()) tipoDrop.getSelectionModel().select(0);
    }

    private void checkForTeatro(int classIndex) {
         int subIndex = tipoDrop.getSelectionModel().getSelectedIndex();
         if (subIndex < 0) subIndex = 0;

         List<SubClasse> subs = categoryManager.getSubClassesForIndex(classIndex);
         if (subs != null && subIndex < subs.size()) {
             SubClasse sub = subs.get(subIndex);
             boolean isTeatro = false;
             if (sub.getExtensions() != null) {
                 for (String ext : sub.getExtensions()) {
                     if ("teatro_info".equals(ext)) {
                         isTeatro = true;
                         break;
                     }
                 }
             }

             if (isTeatro) {
                 if (!tabPane.getTabs().contains(teatroTab)) {
                     tabPane.getTabs().add(teatroTab);
                 }
             } else {
                 tabPane.getTabs().remove(teatroTab);
             }
         }
    }

    public void setDocumento(Documento doc) {
        this.documento = doc;
        if (doc != null) {
            this.editedDocCode = doc.getCodigo();
            tituloField.setText(doc.getTitulo());
            codigoField.setText(doc.getCodigo());
            subtituloField.setText(doc.getSubtitulo());
            manualCodeCheckBox.setSelected(doc.isCodigoManual());

            // Set Classe and Type
            if (doc.getClasseProducao() != null) {
                classeDrop.getSelectionModel().select(doc.getClasseProducao().getDesc());
            }
            if (doc.getSubClasseProducao() != null) {
                tipoDrop.getSelectionModel().select(doc.getSubClasseProducao().getDesc());
            }

            // Dates
            if (doc.getDataDocumento() != null) {
                anoPubliField.setText(doc.getDataDocumento().getAno());
                if (doc.getDataDocumento().getMes() != null) mesSpinner.getValueFactory().setValue(doc.getDataDocumento().getMes());
                if (doc.getDataDocumento().getDia() != null) diaSpinner.getValueFactory().setValue(doc.getDataDocumento().getDia());
                dataIncertaCheckBox.setSelected(doc.getDataDocumento().isDataIncerta());
            }

            encontradoEmField.setText(doc.getEncontradoEm());
            instituicaoCustodiaField.setText(doc.getInstituicaoCustodia());
            ineditoCheckBox.setSelected(doc.isInedito());
            danosAoSuporteCheckBox.setSelected(doc.isDanosSuporte());

            // Pagination
            paginaSpinner.getValueFactory().setValue(doc.getPaginaInicio() == null ? 0 : doc.getPaginaInicio());
            numPaginaSpinner.getValueFactory().setValue(doc.getNumPagina() == null ? 0 : doc.getNumPagina());
            colunaSpinner.getValueFactory().setValue(doc.getColuna() == null ? 1 : doc.getColuna());

            descriptionText.setText(doc.getDescricao());
            transcriptionText.setText(doc.getTranscricao());


            // Authors
            if (doc.getAutores() != null) {
                for (String authorId : doc.getAutores()) {
                     try {
                         Entity e = EntityManager.getEntryById(authorId);
                         authorMap.put(e.getName(), e.getId());
                         authorList.getItems().add(e.getName());
                     } catch (Exception | EntityNotFoundException e) {
                         logger.error("Failed to load author for id: " + authorId, e);
                     }
                }
            }

            if (doc.getAutoresPubli() != null) {
                 for (String authorId : doc.getAutoresPubli()) {
                     try {
                         Entity e = EntityManager.getEntryById(authorId);
                         authorPubliMap.put(e.getName(), e.getId());
                         authorPubliList.getItems().add(e.getName());
                     } catch (Exception | EntityNotFoundException e) {
                         logger.error("Failed to load publi author for id: " + authorId, e);
                     }
                 }
            }

             if (doc.getCitacoes() != null) {
                 for (String citacaoId : doc.getCitacoes()) {
                     try {
                         Entity e = EntityManager.getEntryById(citacaoId);
                         citacoesMap.put(e.getName(), e.getId());
                         listaCitacao.getItems().add(e.getName());
                     } catch (Exception | EntityNotFoundException e) {
                         logger.error("Failed to load citation for id: " + citacaoId, e);
                     }
                 }
             }

            if (doc.getLugarPublicacao() != null) {
                try {
                    lugarPublicacao = EntityManager.getEntryById(doc.getLugarPublicacao());
                    lugarPublicacaoText.setText(lugarPublicacao.getName());
                } catch (Exception | EntityNotFoundException e) {
                    logger.error("Failed to load publication place", e);
                }
            }

            if (doc.getTipoNbr() != null) {
                tipoNbrDrop.getSelectionModel().select(doc.getTipoNbr().print());
            }

            tituloPublicacaoField.setText(doc.getTituloPublicacao());
            subTituloPublicacaoField.setText(doc.getSubtituloPublicacao());
            if (doc.getEdicao() != null) edicaoSpinner.getValueFactory().setValue(doc.getEdicao());
            editoraField.setText(doc.getEditora());
            anoField.setText(doc.getAno());
            volumeField.setText(doc.getVolume());
            numPubliSpinner.getValueFactory().setValue(doc.getNumPublicacao() == null ? 0 : doc.getNumPublicacao());
            disponivelField.setText(doc.getDisponivelEm());

            if (doc.getAcessoEm() != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                acessoField.setText(doc.getAcessoEm().format(formatter));
            }

            if (doc.getInfoAdicionais() != null) {
                InfoAdicionais info = doc.getInfoAdicionais();
                anoDepositoField.setText(info.getAnoSubmissao());
                tipoTrabalhoField.setText(info.getTipoTrabalho());
                grauField.setText(info.getGrau());
                cursoField.setText(info.getCurso());
                doiField.setText(info.getDoi());
                entrevistadorField.setText(info.getEntrevistador());
                publicadoPorField.setText(info.getPublicadoPor());
                destinatarioField.setText(info.getDestinatario());
                descricaoFisicaField.setText(info.getDescricaoFisica());
                infoComplementarField.setText(info.getInfoComplementares());
            }

            // Load other fields...
            if (doc.getTextoTeatro() != null) {
                loadTeatro(doc.getTextoTeatro());
            }
            testimonyField.setText(doc.getTestemunho());

            // Links
            if (doc.getTrabalhosRelacionados() != null) {
                for (Relacao rel : doc.getTrabalhosRelacionados()) {
                    linkList.getItems().add(rel);
                }
            }

            refreshFileTree();
        }
    }

    private void loadTeatro(TextoTeatral teatro) {
         teatroPersonagemSpinner.getValueFactory().setValue(teatro.getQuantidadePersonagem());
         teatroAtoSpinner.getValueFactory().setValue(teatro.getAtos());
         teatroCenaSpinner.getValueFactory().setValue(teatro.getCenas());
         teatroResumoText.setText(teatro.getResumo());
         if (teatro.getPalavrasChave() != null) {
             teatroKeywordList.getItems().setAll(teatro.getPalavrasChave());
         }
    }

    public Documento getDocumento() throws ValidationException {
        if (documento == null) documento = new Documento();
        Classe classe = categoryManager.getClasseForIndex(classeDrop.getSelectionModel().getSelectedIndex());

        documento.setTitulo(tituloField.getText());
        documento.setSubtitulo(subtituloField.getText());
        documento.setCodigoManual(manualCodeCheckBox.isSelected());
        documento.setClasseProducao(classe);

        List<SubClasse> subs = categoryManager.getSubClassesForIndex(classeDrop.getSelectionModel().getSelectedIndex());
        if (subs != null && !subs.isEmpty()) {
            documento.setSubClasseProducao(subs.get(tipoDrop.getSelectionModel().getSelectedIndex()));
        }

        // Date
        DataDocumento data = new DataDocumento();
        data.setAno(anoPubliField.getText());
        data.setMes(mesSpinner.getValue());
        data.setDia(diaSpinner.getValue());
        data.setDataIncerta(dataIncertaCheckBox.isSelected());
        documento.setDataDocumento(data);

        documento.setEncontradoEm(encontradoEmField.getText());
        documento.setInstituicaoCustodia(instituicaoCustodiaField.getText());
        documento.setInedito(ineditoCheckBox.isSelected());
        documento.setDanosSuporte(danosAoSuporteCheckBox.isSelected());

        documento.setPaginaInicio(paginaSpinner.getValue());
        documento.setNumPagina(numPaginaSpinner.getValue());
        documento.setColuna(colunaSpinner.getValue());

        documento.setDescricao(descriptionText.getText());
        documento.setTranscricao(transcriptionText.getText());

        documento.setAutores(new ArrayList<>(authorMap.values()));
        documento.setCitacoes(new ArrayList<>(citacoesMap.values()));
        documento.setTrabalhosRelacionados(new ArrayList<>(linkList.getItems()));

        if (lugarPublicacao != null) {
             documento.setLugarPublicacao(lugarPublicacao.getId());
        }

        if(tipoNbrDrop.getSelectionModel().getSelectedIndex() >= 0) documento.setTipoNbr(TipoNBR.fromPosition(tipoNbrDrop.getSelectionModel().getSelectedIndex()));
        documento.setTituloPublicacao(tituloPublicacaoField.getText());
        documento.setSubtituloPublicacao(subTituloPublicacaoField.getText());
        documento.setAutoresPubli(new ArrayList<>(authorPubliMap.values()));
        documento.setEdicao(edicaoSpinner.getValue());
        documento.setEditora(editoraField.getText());
        documento.setAno(anoField.getText());
        documento.setVolume(volumeField.getText());
        documento.setNumPublicacao(numPubliSpinner.getValue());
        documento.setDisponivelEm(disponivelField.getText());

        if (acessoField.getText() != null && !acessoField.getText().isEmpty()) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                documento.setAcessoEm(LocalDate.parse(acessoField.getText(), formatter));
            } catch (DateTimeParseException e) {
                 logger.error("Invalid date format for access date", e);
                 throw new ValidationException("Data 'Acesso em' inválida. Use o formato dd/MM/yyyy");
            }
        }

        InfoAdicionais info = new InfoAdicionais();
        info.setAnoSubmissao(anoDepositoField.getText());
        info.setTipoTrabalho(tipoTrabalhoField.getText());
        info.setGrau(grauField.getText());
        info.setCurso(cursoField.getText());
        info.setDoi(doiField.getText());
        info.setEntrevistador(entrevistadorField.getText());
        info.setPublicadoPor(publicadoPorField.getText());
        info.setDestinatario(destinatarioField.getText());
        info.setDescricaoFisica(descricaoFisicaField.getText());
        info.setInfoComplementares(infoComplementarField.getText());
        documento.setInfoAdicionais(info);

        documento.setTestemunho(testimonyField.getText());

        // Teatro
        if (tabPane.getTabs().contains(teatroTab)) {
            TextoTeatral teatro = new TextoTeatral();
            teatro.setQuantidadePersonagem(teatroPersonagemSpinner.getValue());
            teatro.setAtos(teatroAtoSpinner.getValue());
            teatro.setCenas(teatroCenaSpinner.getValue());
            teatro.setResumo(teatroResumoText.getText());
            teatro.setPalavrasChave(new ArrayList<>(teatroKeywordList.getItems()));
            documento.setTextoTeatro(teatro);
        } else {
            documento.setTextoTeatro(null);
        }

        return documento;
    }

    public void saveDocument() throws Exception, ValidationException, RepositoryException {
        getDocumento(); // updates 'documento' from fields

        if (editedDocCode != null) {
            updateDoc(documento);
        } else {
            generateNewDoc(documento);
        }
    }

    private void updateDoc(Documento documento) throws Exception, ValidationException, RepositoryException {
        String newCode;
        if (manualCodeCheckBox.isSelected()) {
            newCode = codigoField.getText();
        } else {
            newCode = CodeManager.getCodeGenerator().generateCodeWithoutAppendix(documento);
        }

        String oldCode = editedDocCode;
        if (oldCode.matches(".*\\.\\d{3}$")) {
             oldCode = oldCode.substring(0, oldCode.length() - 4);
        }

        if (oldCode.equals(newCode)) {
            documento.setCodigo(editedDocCode);
            RepositoryManager.updateEntry(documento);
        } else {
            // Confirmation dialog would be shown in UI thread usually, assuming confirmed for now or throw exception to ask UI
             Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
             alert.setTitle("Atenção!!");
             alert.setHeaderText("Mudança de Código");
             alert.setContentText(String.format("Suas alterações irão mudar o código deste documento de %s para %s \nTodos os anexos prévios também serão perdidos. Você tem certeza?", editedDocCode, newCode));

             Optional<ButtonType> result = alert.showAndWait();
             if (result.isPresent() && result.get() == ButtonType.OK) {
                 String finalCode = generateNewDoc(documento);
                 if (documento.getGrupo() != null) updateGroup(documento, editedDocCode, finalCode);
                 RepositoryManager.removeEntry(editedDocCode);
             } else {
                 throw new ValidationException("Operação cancelada pelo usuário.");
             }
        }
    }

    private void updateGroup(Documento doc, String oldCode, String newCode) throws RepositoryException {
            MultiSourcedDocument group = MultiSourcedDocumentManager.getEntryById(doc.getGrupo());
            group.getDocuments().remove(oldCode);
            group.getDocuments().add(newCode);
            MultiSourcedDocumentManager.updateEntry(group);
    }

    private String generateNewDoc(Documento documento) throws ValidationException {
        String code;
        if (manualCodeCheckBox.isSelected()) {
            code = codigoField.getText();
             // Validation logic
             String acervoPrefix = ConfigReader.getProperty("acervo");
             if (code == null) throw new ValidationException("Código inválido: vazio");
             if (code.matches(".*\\s+.*")) throw new ValidationException("Código inválido. Não deve conter espaços.");
             if (!code.contains(".")) throw new ValidationException("Código inválido. Deve conter o separador '.'.");
             if (acervoPrefix != null && !acervoPrefix.isEmpty()) {
                if (!code.startsWith(acervoPrefix)) {
                     throw new ValidationException(String.format("Código inválido. Deve começar com o prefixo configurado: %s", acervoPrefix));
                }
             }
             documento.setCodigo(code);
        } else {
            code = CodeManager.getCodeGenerator().generateCode(documento);
        }
        RepositoryManager.addEntry(documento, files);
        return code;
    }

    @FXML private void handleSelectLugarPublicacao() {
         List<String> ids = openEntitySelector(EntityType.LOCAL);
         if (!ids.isEmpty()) {
             try {
                lugarPublicacao = EntityManager.getEntryById(ids.get(0));
                lugarPublicacaoText.setText(lugarPublicacao.getName());
             } catch (Exception | EntityNotFoundException e) { logger.error("Failed to select place of publication", e); }
         }
    }

    @FXML private void handleAddAuthor() {
         List<String> ids = openEntitySelector(EntityType.PESSOA);
         for (String id : ids) {
             try {
                 Entity e = EntityManager.getEntryById(id);
                 if (!authorMap.containsKey(e.getName())) {
                     authorMap.put(e.getName(), e.getId());
                     authorList.getItems().add(e.getName());
                 }
             } catch (Exception | EntityNotFoundException e) {}
         }
    }

    @FXML private void handleRemoveAuthor() {
         String selected = authorList.getSelectionModel().getSelectedItem();
         if (selected != null) {
             authorList.getItems().remove(selected);
             authorMap.remove(selected);
         }
    }

    @FXML private void handleAddAuthorPubli() {
        List<String> ids = openEntitySelector(EntityType.PESSOA);
         for (String id : ids) {
             try {
                 Entity e = EntityManager.getEntryById(id);
                 if (!authorPubliMap.containsKey(e.getName())) {
                     authorPubliMap.put(e.getName(), e.getId());
                     authorPubliList.getItems().add(e.getName());
                 }
             } catch (Exception | EntityNotFoundException e) {}
         }
    }

    @FXML private void handleRemoveAuthorPubli() {
        String selected = authorPubliList.getSelectionModel().getSelectedItem();
         if (selected != null) {
             authorPubliList.getItems().remove(selected);
             authorPubliMap.remove(selected);
         }
    }

    @FXML private void handleAddRefPessoa() {
        addCitations(EntityType.PESSOA);
    }
    @FXML private void handleAddRefInst() {
        addCitations(EntityType.INSTITUICAO);
    }
    @FXML private void handleAddRefLocal() {
        addCitations(EntityType.LOCAL);
    }

    private void addCitations(EntityType type) {
        List<String> ids = openEntitySelector(type);
        for (String id : ids) {
             try {
                 Entity e = EntityManager.getEntryById(id);
                 if (!citacoesMap.containsKey(e.getName())) {
                     citacoesMap.put(e.getName(), e.getId());
                     listaCitacao.getItems().add(e.getName());
                 }
             } catch (Exception | EntityNotFoundException e) {}
        }
    }

    private List<String> openEntitySelector(EntityType type) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/EntitySelector.fxml"));
            DialogPane pane = loader.load();
            EntitySelectorController controller = loader.getController();
            controller.setEntityType(type);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Selecionar " + type.name());
            dialog.setResizable(true);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                return controller.getSelectedIds();
            }
        } catch (Exception e) {
            logger.error("Failed to open EntitySelector for type: " + type, e);
        }
        return Collections.emptyList();
    }

    @FXML private void handleAttachFiles() {
        FileChooser chooser = new FileChooser();
        List<File> selectedFiles = chooser.showOpenMultipleDialog(tabPane.getScene().getWindow());
        if (selectedFiles != null) {
            if (editedDocCode != null) {
                try {
                    RepositoryManager.saveFiles(editedDocCode, selectedFiles);
                    if (documento.getArquivos() == null) documento.setArquivos(new ArrayList<>());
                    for(File f : selectedFiles) {
                        documento.getArquivos().add(f.getName());
                    }
                } catch (IOException e) {
                    logger.error("Failed to save files", e);
                }
            } else {
                files.addAll(selectedFiles);
            }
            refreshFileTree();
        }
    }

    private void refreshFileTree() {
        TreeItem<String> root = new TreeItem<>("Arquivos");

        // Use a wrapper object or user data to store the actual File object
        // For simplicity, we can use a map or just check the list on selection

        for (File f : files) {
            TreeItem<String> item = new TreeItem<>(f.getName());
            root.getChildren().add(item);
        }
        if (documento != null && documento.getArquivos() != null) {
             for (String existingFile : documento.getArquivos()) {
                 TreeItem<String> item = new TreeItem<>(existingFile + " (salvo)");
                 root.getChildren().add(item);
             }
        }
        fileTree.setRoot(root);
        fileTree.setShowRoot(false);

        fileTree.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                TreeItem<String> item = fileTree.getSelectionModel().getSelectedItem();
                if (item != null) {
                     handleFileAction(item);
                }
            }
        });
    }

    private void handleFileAction(TreeItem<String> item) {
        String name = item.getValue();
        File file = null;
        boolean isSaved = false;

        if (name.endsWith(" (salvo)")) {
            String cleanName = name.replace(" (salvo)", "");
            // Construct path from repository
            if (editedDocCode != null) {
                 java.nio.file.Path p = Path.of(RepositoryManager.getPathFromCode(editedDocCode));
                 if (p != null) {
                     file = p.resolve(cleanName).toFile();
                     isSaved = true;
                 }
            }
        } else {
            for (File f : files) {
                if (f.getName().equals(name)) {
                    file = f;
                    break;
                }
            }
        }

        if (file != null && file.exists()) {
             openFileActionDialog(file, isSaved);
        }
    }

    private void openFileActionDialog(File file, boolean isSaved) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/FileActionDialog.fxml"));
            DialogPane pane = loader.load();
            FileActionDialogController controller = loader.getController();

            Runnable onRemove = () -> {
                if (isSaved) {
                    RepositoryManager.removeFiles(editedDocCode, Collections.singletonList(file.getName()));
                    // Also update document object?
                    if (documento.getArquivos() != null) documento.getArquivos().remove(file.getName());
                } else {
                    files.remove(file);
                }
                refreshFileTree();
            };

            controller.setFile(file, isSaved ? editedDocCode : null, onRemove);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Ações de Arquivo");
            dialog.showAndWait();

        } catch (IOException e) {
            logger.error("Failed to open file action dialog for file: " + file, e);
        }
    }

    @FXML private void handleSelectRelatedDoc() {
         // Use existing DocumentSelector
         try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/digitallib/DocumentSelector.fxml"));
            DialogPane pane = loader.load();
            DocumentSelectorController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(pane);
            dialog.setTitle("Selecionar Documento");
            dialog.setResizable(true);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                List<String> codes = controller.getSelectedDocCodes();
                if (!codes.isEmpty()) {
                    linkCodeField.setText(codes.get(0));
                }
            }
        } catch (Exception e) {
            logger.error("Failed to open DocumentSelector", e);
        }
    }

    @FXML private void handleAddLink() {
        TIPO_RELACAO tipo = linkDropDown.getSelectionModel().getSelectedItem();
        String code = linkCodeField.getText();
        if (code != null && !code.isEmpty()) {
             // Handle "code -> code" split if manual input had noise, otherwise just code
             String cleanCode = code.split("->")[0].trim();
             linkList.getItems().add(new Relacao(cleanCode, tipo));
             linkCodeField.clear();
        }
    }

    @FXML private void handleRemoveLink() {
        int idx = linkList.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            linkList.getItems().remove(idx);
        }
    }

    @FXML private void handleTeatroAddKeyword() {
        String kw = teatroKeywordInput.getText().trim();
        if (!kw.isEmpty()) {
            teatroKeywordList.getItems().add(kw);
            teatroKeywordInput.clear();
        }
    }

    @FXML private void handleTeatroRemoveKeyword() {
        int idx = teatroKeywordList.getSelectionModel().getSelectedIndex();
        if (idx >= 0) teatroKeywordList.getItems().remove(idx);
    }

}

