package com.digitallib;

import com.digitallib.exception.EntityNotFoundException;
import com.digitallib.exception.ValidationException;
import com.digitallib.manager.EntityManager;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.*;
import com.digitallib.model.entity.Entity;
import com.digitallib.model.entity.EntityType;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static com.digitallib.manager.RepositoryManager.getPathFromCode;

public class DocumentCreator extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField tituloField;
    private JTextField encontradoEmField;
    private JTextField anoField;
    private JSpinner paginaSpinner;
    private JSpinner numPaginaSpinner;
    private JSpinner colunaSpinner;
    private JCheckBox ineditoCheckBox;
    private JCheckBox danosAoSuporteCheckBox;
    private JSpinner numPubliSpinner;
    private JButton addReferenciaPessoaButton;
    private JScrollPane referenciaScrollPanel;
    private JList listaCitacao;
    private JButton AddReferenciaLocalButton;
    private JButton attachFilesButton;
    private JComboBox classe_drop;
    private JComboBox tipo_drop;
    private JTextField instituicaoCustodiaField;
    private JCheckBox dataIncertaCheckBox;
    private JButton addReferenciaInstituicaoButton;
    private JSpinner mesSpinner;
    private JSpinner diaSpinner;
    private JTextField anoVolumeField;
    private JPanel painelBotoes;
    private JPanel painelPrincipal;
    private JPanel painelPublicacao;
    private JPanel painelData;
    private JPanel panelBotoesRef;
    private JPanel referenciaContentPainel;
    private JPanel refPanel;
    private JTextField lugarPublicacaoText;
    private JButton localPublicacaoButton;
    private JScrollPane painelScroll;
    private JScrollPane arquivoPanel;
    private JPanel painelGeral;
    private JTextField monografiaField;
    private JPanel eletronicoPanel;
    private JTextField disponivelField;
    private JTextField acessoField;
    private JComboBox tipoNbrDrop;
    private JButton addAuthorButton;
    private JList authorList;
    private JTree fileTree;

    private Entity lugarPublicacao;

    JFileChooser jfc = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
    List<File> files = new ArrayList<>();
    DefaultListModel pessoaLocalListModel = new DefaultListModel();

    DefaultListModel authorListModel = new DefaultListModel();
    Map<String, String> citacoes = new HashMap<>();
    List<String> filteredSubClassOptions = new ArrayList<>();

    Map<String, String> authorMap = new HashMap<>();

    String editedDocCode = null;


    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode root;

    private Logger logger = LogManager.getLogger();

    public DocumentCreator(Documento document) {
        this();
        //General
        editedDocCode = document.getCodigo();
        classe_drop.setSelectedIndex(document.getClasseProducao().getPosition());
        tipo_drop.setSelectedItem(document.getSubClasseProducao().print());
        tituloField.setText(document.getTitulo());
        encontradoEmField.setText(document.getEncontradoEm());
        instituicaoCustodiaField.setText(document.getInstituicaoCustodia());
        ineditoCheckBox.setSelected(document.isInedito());
        danosAoSuporteCheckBox.setSelected(document.isDanosSuporte());
        addCitacoes(document.getCitacoes());
        if (document.getAutores() != null) addAutores(document.getAutores());

        setLugarPublicacao(document.getLugarPublicacao());

        //Publication
        if (document.getTipoNbr() != null) tipoNbrDrop.setSelectedItem(document.getTipoNbr().print());
        monografiaField.setText(document.getMonografia());
        anoVolumeField.setText(document.getAnoVolume());
        numPubliSpinner.setValue(document.getNumPublicacao());
        paginaSpinner.setValue(document.getPaginaInicio());
        numPaginaSpinner.setValue(document.getNumPagina());
        colunaSpinner.setValue(document.getColuna());
        disponivelField.setText(document.getDisponivelEm());
        if (document.getAcessoEm() != null) acessoField.setText(getDate(document.getAcessoEm()));
        LoadDateFields(document);

        File folder = new File(getPathFromCode(document.getCodigo()));
        root = new DefaultMutableTreeNode(new FileNode(folder));
        treeModel = new DefaultTreeModel(root);

        for (File file : folder.listFiles()) {
            if (!file.getName().equals(document.getCodigo() + ".json")) {
                files.add(file);
            }
        }
        fileTree.setModel(treeModel);
        refreshArquivoList();
    }

    private void setLugarPublicacao(String codigo) {
        try {
            lugarPublicacao = EntityManager.getEntryById(codigo);
            if (lugarPublicacao != null) lugarPublicacaoText.setText(lugarPublicacao.getName());
        } catch (EntityNotFoundException e) {
            logger.error(e);
        }
    }

    public class FileNode {

        private File file;

        public FileNode(File file) {
            this.file = file;
        }

        @Override
        public String toString() {
            String name = file.getName();
            if (name.equals("")) {
                return file.getAbsolutePath();
            } else {
                return name;
            }
        }
    }

    private void LoadDateFields(Documento document) {
        if (document.getDataDocumento() != null) {
            DataDocumento data = document.getDataDocumento();
            if (data.getAno() != null) anoField.setText(data.getAno());
            if (data.getMes() != null) mesSpinner.setValue(data.getMes());
            if (data.getDia() != null) diaSpinner.setValue(data.getDia());
            dataIncertaCheckBox.setSelected(document.getDataDocumento().isDataIncerta());
        }
    }

    public DocumentCreator() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        listaCitacao.setModel(pessoaLocalListModel);
        authorList.setModel(authorListModel);

        addReferenciaPessoaButton.addActionListener(e -> {
            getCitacaoFromDialog(EntityType.PESSOA);
        });

        addReferenciaInstituicaoButton.addActionListener(e -> {
            getCitacaoFromDialog(EntityType.INSTITUICAO);
        });

        AddReferenciaLocalButton.addActionListener(e -> {
            getCitacaoFromDialog(EntityType.LOCAL);
        });

        localPublicacaoButton.addActionListener(e -> {
            getLugarPublicacaoFromDialog();
        });

        addAuthorButton.addActionListener(e -> {
            getAuthorFromDialog();
        });

        jfc.setDialogTitle("Multiple file and directory selection:");
        jfc.setMultiSelectionEnabled(true);
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        attachFilesButton.addActionListener(e -> {
            int returnValue = jfc.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                files.addAll(Arrays.asList(jfc.getSelectedFiles()));
                logger.info("\n- - - - - - - - - - -\n");
                logger.info("Files Found\n");
                refreshArquivoList();
            }
        });

        initializeClasseDropdown();
        initializeTipoNbrDropdown();

        listaCitacao.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    citacoes.remove(listaCitacao.getSelectedValue());
                    pessoaLocalListModel.remove(listaCitacao.getSelectedIndex());
                }
            }
        });

        authorList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if (e.getClickCount() == 2) {
                    authorMap.remove(authorList.getSelectedValue());
                    authorListModel.remove(authorList.getSelectedIndex());
                }
            }
        });

        fileTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                if (e.getClickCount() == 2) {
                    if (editedDocCode != null) {
                        if (fileTree.getSelectionPath().equals(fileTree.getPathForRow(0))) {
                            String path = getPathFromCode(editedDocCode);
                            File fileFolder = new File(path);
                            try {
                                Process p = new ProcessBuilder("explorer.exe", "/select," + fileFolder.getCanonicalPath()).start();
                            } catch (IOException ex) {
                                logger.error(ex);
                            }
                        } else {
                            String fileName = fileTree.getSelectionPath().getPath()[1].toString();
                            logger.debug("selected:" + fileName);
                            if (editedDocCode == null) {
                                files.remove(fileName);
                                refreshArquivoList();
                            } else {
                                OpenFileRemovalConfirmation(fileName);
                            }
                        }
                    } else {
                        logger.error("Folder info is null");
                    }
                }
            }
        });
    }

    private void OpenFileRemovalConfirmation(String fileName) {
        int result = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), String.format("Você tem certeza que qeur remover o arquivo %s?", fileName), "Atenção!!",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            File[] filesOnFolder = new File(getPathFromCode(editedDocCode)).listFiles();
            for (File file : filesOnFolder) {
                if (file.getName().equals(fileName)) {
                    file.delete();
                }
            }

            files.removeIf(file -> file.getName().equals(fileName));
            refreshArquivoList();
        }
    }

    private void getLugarPublicacaoFromDialog() {
        EntitySelector dialog = new EntitySelector(EntityType.LOCAL);
        dialog.pack();
        List<String> codigos = dialog.showDialog();
        if (codigos.size() > 0) {
            setLugarPublicacao(codigos.get(0));
        }
    }

    private void getAuthorFromDialog() {
        EntitySelector dialog = new EntitySelector(EntityType.PESSOA);
        dialog.pack();
        List<String> codigos = dialog.showDialog();
        if (codigos.size() > 0) {
            for (String cod : codigos) {
                Entity author = null;
                try {
                    author = EntityManager.getEntryById(cod);
                    authorMap.put(author.getName(), author.getId());
                } catch (EntityNotFoundException e) {
                    logger.error(e);
                }
                if (author != null) authorListModel.add(authorListModel.size(), author.getName());
            }
        }
    }

    private void getCitacaoFromDialog(EntityType tipo) {
        EntitySelector dialog = new EntitySelector(tipo);
        dialog.pack();
        addCitacoes(dialog.showDialog());
    }

    private void refreshArquivoList() {
        root.removeAllChildren();
        files.forEach(x -> {
            if (x.isFile()) {
                logger.info(x.getName());
                DefaultMutableTreeNode child = new DefaultMutableTreeNode(new FileNode(x));
                root.add(child);
            }
        });
        treeModel.reload();
    }

    private void initializeClasseDropdown() {

        final DefaultComboBoxModel model = new DefaultComboBoxModel(ClasseProducao.getAsStringList());
        classe_drop.setModel(model);
        refreshSubClassDialog(classe_drop.getSelectedIndex() + 1);
        classe_drop.addActionListener(e -> {
            refreshSubClassDialog(classe_drop.getSelectedIndex() + 1);
        });
    }

    private void initializeTipoNbrDropdown() {
        final DefaultComboBoxModel model = new DefaultComboBoxModel(TipoNBR.getAsStringList());
        tipoNbrDrop.setModel(model);
    }

    private void refreshSubClassDialog(int index) {
        filteredSubClassOptions.clear();
        for (Map.Entry<String, SubClasseProducao> entry : SubClasseProducao.getSubClasseMap().entrySet()) {
            if (entry.getKey().startsWith(String.format("%02d", index)))
                filteredSubClassOptions.add(entry.getValue().print());
        }
        final DefaultComboBoxModel model = new DefaultComboBoxModel(filteredSubClassOptions.toArray());
        tipo_drop.setModel(model);
    }

    private void addCitacoes(List<String> codigos) {
        for (String novoCodigo : codigos) {
            Entity entity = null;
            try {
                entity = EntityManager.getEntryById(novoCodigo);
                citacoes.put(entity.getName(), novoCodigo);
            } catch (EntityNotFoundException e) {
                logger.error(e);
            }
        }
        pessoaLocalListModel.clear();
        for (Map.Entry<String, String> codigo : citacoes.entrySet()) {
            try {
                Entity entity = EntityManager.getEntryById(codigo.getValue());
                pessoaLocalListModel.add(0, entity.getName());
            } catch (EntityNotFoundException e) {
                logger.error(e);
            }
        }
    }

    private void addAutores(List<String> authorList) {
        Map<String, Entity> entityMap = new HashMap<>();
        for (String cod : authorList) {
            try {
                Entity entity = EntityManager.getEntryById(cod);
                authorMap.put(entity.getName(), cod);
                entityMap.put(entity.getName(), entity);
            } catch (EntityNotFoundException e) {
                logger.error(e);
            }
        }
        authorListModel.clear();
        for (Map.Entry<String, String> codigo : authorMap.entrySet()) {
            authorListModel.add(authorListModel.size(), entityMap.get(codigo.getValue()));
        }
    }

    private void onOK() {
        try {
            createDoc();
        } catch (ValidationException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
            return;
        }
        dispose();
    }

    private void createDoc() throws ValidationException {
        Documento documento = new Documento();

        //General
        documento.setClasseProducao(ClasseProducao.fromPosition(classe_drop.getSelectedIndex()));
        documento.setSubClasseProducao(SubClasseProducao.forCode(tipo_drop.getSelectedItem().toString().substring(0, 3)));
        documento.setTitulo(tituloField.getText());
        documento.setAutores(new ArrayList<>(authorMap.values()));
        documento.setEncontradoEm(encontradoEmField.getText());
        documento.setInstituicaoCustodia(instituicaoCustodiaField.getText());
        documento.setInedito(ineditoCheckBox.isSelected());
        documento.setDanosSuporte(danosAoSuporteCheckBox.isSelected());
        if (lugarPublicacao != null) documento.setLugarPublicacao(lugarPublicacao.getId());

        //Publication
        documento.setTipoNbr(TipoNBR.fromPosition(tipoNbrDrop.getSelectedIndex()));
        documento.setMonografia(monografiaField.getText());
        documento.setAnoVolume(anoVolumeField.getText());
        documento.setNumPublicacao(getSpinnerIntValue(numPubliSpinner));
        documento.setPaginaInicio(getSpinnerIntValue(paginaSpinner));
        documento.setNumPagina(getSpinnerIntValue(numPaginaSpinner));
        documento.setDisponivelEm(disponivelField.getText());
        setDateFields(documento);
        setDataOnDoc(documento);

        documento.setColuna(getSpinnerIntValue(colunaSpinner));


        documento.setCitacoes(new ArrayList<>(citacoes.values()));
        documento.setArquivos(Arrays.stream(files.toArray()).map(o -> o instanceof File ? ((File) o).getName() : "").collect(Collectors.toList()));
        if (editedDocCode != null) {
            checkIfNewCode(documento);
        } else {
            generateNewDoc(documento);
        }
    }

    private void setDateFields(Documento documento) throws ValidationException {
        try {
            if (!acessoField.getText().isEmpty()) setDate(documento);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Disponível em inválido. Use o padrão dd/mm/yyyy");
        }
    }

    private void checkIfNewCode(Documento documento) {
        String newCode = documento.generateCodigoWithoutAppendix();
        if (editedDocCode.contains(newCode)) {
            documento.setCodigo(editedDocCode);
            RepositoryManager.updateEntry(documento, files);
        } else {
            int result = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), String.format("Suas alterações irão mudar o código deste documento de %s para %s \nTodos os anexos prévios também serão pedidos. Você tem certeza?", editedDocCode, newCode), "Atenção!!",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                generateNewDoc(documento);
                RepositoryManager.removeEntry(editedDocCode);
            }
        }
    }

    private void setDate(Documento documento) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        formatter = formatter.withLocale(Locale.ROOT);
        documento.setAcessoEm(LocalDate.parse(acessoField.getText(), formatter));
    }

    private String getDate(LocalDate data) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        formatter = formatter.withLocale(Locale.ROOT);
        return formatter.format(data);
    }

    private void setDataOnDoc(Documento documento) {
        DataDocumento dataDocumento = new DataDocumento();
        dataDocumento.setDataIncerta(dataIncertaCheckBox.isSelected());
        dataDocumento.setAno(anoField.getText());
        dataDocumento.setMes((Integer) mesSpinner.getValue());
        dataDocumento.setDia((Integer) diaSpinner.getValue());
        documento.setDataDocumento(dataDocumento);
    }

    private void generateNewDoc(Documento documento) {
        documento.generateCodigo();
        RepositoryManager.addEntry(documento, files);
    }

    private Integer getSpinnerIntValue(JSpinner paginaSpinner) {
        try {
            paginaSpinner.commitEdit();
        } catch (ParseException e) {
            logger.error("Erro ao parsear spinner" + e.getMessage());
        }
        return (Integer) paginaSpinner.getValue();
    }

    private LocalDate getDateFromInput(String text) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(text, formatter);
        } catch (DateTimeParseException e) {
            logger.error("Could not parse date: " + text);
        }
        return null;
    }

    private String getInputFromDate(LocalDate dateTime) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return dateTime.format(formatter);
        } catch (DateTimeParseException e) {
            logger.error("Could not parse date time");
        }
        return null;
    }

    private void onCancel() {
        dispose();
    }

    public static void main(String[] args) {
        DocumentCreator dialog = new DocumentCreator();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        painelBotoes = new JPanel();
        painelBotoes.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(painelBotoes, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        painelBotoes.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        painelBotoes.add(panel1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setText("OK");
        panel1.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel1.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        painelScroll = new JScrollPane();
        contentPane.add(painelScroll, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        painelPrincipal = new JPanel();
        painelPrincipal.setLayout(new GridLayoutManager(10, 12, new Insets(0, 0, 20, 0), -1, -1));
        painelScroll.setViewportView(painelPrincipal);
        painelGeral = new JPanel();
        painelGeral.setLayout(new GridLayoutManager(9, 3, new Insets(10, 10, 10, 10), -1, -1));
        painelPrincipal.add(painelGeral, new GridConstraints(0, 0, 8, 11, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        painelGeral.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label1 = new JLabel();
        label1.setText("Classe do documento");
        painelGeral.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label2 = new JLabel();
        label2.setText("Subclasse");
        painelGeral.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Título");
        painelGeral.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label4 = new JLabel();
        label4.setText("Instituição de custódia");
        painelGeral.add(label4, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label5 = new JLabel();
        label5.setText("Encontrado em");
        painelGeral.add(label5, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label6 = new JLabel();
        label6.setText("Coluna");
        painelGeral.add(label6, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tipo_drop = new JComboBox();
        painelGeral.add(tipo_drop, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        classe_drop = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        classe_drop.setModel(defaultComboBoxModel1);
        painelGeral.add(classe_drop, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tituloField = new JTextField();
        painelGeral.add(tituloField, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        instituicaoCustodiaField = new JTextField();
        painelGeral.add(instituicaoCustodiaField, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        encontradoEmField = new JTextField();
        encontradoEmField.setText("");
        painelGeral.add(encontradoEmField, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        colunaSpinner = new JSpinner();
        painelGeral.add(colunaSpinner, new GridConstraints(6, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        danosAoSuporteCheckBox = new JCheckBox();
        danosAoSuporteCheckBox.setHorizontalAlignment(0);
        danosAoSuporteCheckBox.setText("Danos ao suporte");
        painelGeral.add(danosAoSuporteCheckBox, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        ineditoCheckBox = new JCheckBox();
        ineditoCheckBox.setText("Inédito");
        painelGeral.add(ineditoCheckBox, new GridConstraints(7, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label7 = new JLabel();
        label7.setText("Autor(a)");
        painelGeral.add(label7, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addAuthorButton = new JButton();
        addAuthorButton.setText("Adicionar Autor");
        painelGeral.add(addAuthorButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JScrollPane scrollPane1 = new JScrollPane();
        painelGeral.add(scrollPane1, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, 1, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        authorList = new JList();
        scrollPane1.setViewportView(authorList);
        final Spacer spacer2 = new Spacer();
        painelGeral.add(spacer2, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_CAN_GROW, null, null, new Dimension(-1, 50), 0, false));
        refPanel = new JPanel();
        refPanel.setLayout(new GridLayoutManager(3, 2, new Insets(10, 10, 10, 10), -1, -1));
        painelPrincipal.add(refPanel, new GridConstraints(8, 0, 1, 11, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        refPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        panelBotoesRef = new JPanel();
        panelBotoesRef.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        refPanel.add(panelBotoesRef, new GridConstraints(1, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(3, 1, new Insets(10, 10, 10, 10), -1, -1));
        panel2.setBackground(new Color(-5657691));
        panelBotoesRef.add(panel2, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        AddReferenciaLocalButton = new JButton();
        AddReferenciaLocalButton.setText("Adicionar Local");
        panel2.add(AddReferenciaLocalButton, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addReferenciaPessoaButton = new JButton();
        addReferenciaPessoaButton.setText("Adicionar Pessoa");
        panel2.add(addReferenciaPessoaButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        addReferenciaInstituicaoButton = new JButton();
        addReferenciaInstituicaoButton.setText("Adicionar Instituição");
        panel2.add(addReferenciaInstituicaoButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        referenciaContentPainel = new JPanel();
        referenciaContentPainel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        refPanel.add(referenciaContentPainel, new GridConstraints(1, 1, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        referenciaScrollPanel = new JScrollPane();
        referenciaContentPainel.add(referenciaScrollPanel, new GridConstraints(0, 0, 2, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        listaCitacao = new JList();
        referenciaScrollPanel.setViewportView(listaCitacao);
        final JLabel label8 = new JLabel();
        label8.setText("Citações");
        refPanel.add(label8, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        painelPublicacao = new JPanel();
        painelPublicacao.setLayout(new GridLayoutManager(10, 6, new Insets(10, 10, 10, 10), -1, -1));
        painelPrincipal.add(painelPublicacao, new GridConstraints(0, 11, 8, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        painelPublicacao.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label9 = new JLabel();
        label9.setText("Ano/Volume");
        painelPublicacao.add(label9, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        anoVolumeField = new JTextField();
        painelPublicacao.add(anoVolumeField, new GridConstraints(4, 3, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer3 = new Spacer();
        painelPublicacao.add(spacer3, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label10 = new JLabel();
        label10.setText("Número de publicação");
        painelPublicacao.add(label10, new GridConstraints(5, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numPubliSpinner = new JSpinner();
        painelPublicacao.add(numPubliSpinner, new GridConstraints(5, 3, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        painelData = new JPanel();
        painelData.setLayout(new GridLayoutManager(3, 5, new Insets(10, 10, 10, 10), -1, -1));
        painelPublicacao.add(painelData, new GridConstraints(8, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        painelData.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        anoField = new JTextField();
        painelData.add(anoField, new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        dataIncertaCheckBox = new JCheckBox();
        dataIncertaCheckBox.setText("Data incerta?");
        painelData.add(dataIncertaCheckBox, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label11 = new JLabel();
        label11.setText("Ano");
        painelData.add(label11, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer4 = new Spacer();
        painelData.add(spacer4, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label12 = new JLabel();
        label12.setText("DATA");
        painelData.add(label12, new GridConstraints(0, 0, 1, 5, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label13 = new JLabel();
        label13.setText("Dia");
        painelData.add(label13, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        diaSpinner = new JSpinner();
        painelData.add(diaSpinner, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label14 = new JLabel();
        label14.setText("Mês");
        painelData.add(label14, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        mesSpinner = new JSpinner();
        painelData.add(mesSpinner, new GridConstraints(1, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer5 = new Spacer();
        painelData.add(spacer5, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JLabel label15 = new JLabel();
        label15.setText("Página/folha");
        painelPublicacao.add(label15, new GridConstraints(6, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        paginaSpinner = new JSpinner();
        painelPublicacao.add(paginaSpinner, new GridConstraints(6, 3, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label16 = new JLabel();
        label16.setText("Quantidade de páginas/folhas");
        painelPublicacao.add(label16, new GridConstraints(7, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numPaginaSpinner = new JSpinner();
        painelPublicacao.add(numPaginaSpinner, new GridConstraints(7, 3, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label17 = new JLabel();
        label17.setText("Informações de publicação e monografia");
        painelPublicacao.add(label17, new GridConstraints(0, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label18 = new JLabel();
        label18.setText("Lugar");
        painelPublicacao.add(label18, new GridConstraints(3, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        lugarPublicacaoText = new JTextField();
        lugarPublicacaoText.setEnabled(false);
        painelPublicacao.add(lugarPublicacaoText, new GridConstraints(3, 3, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        localPublicacaoButton = new JButton();
        localPublicacaoButton.setText("Selecionar lugar");
        painelPublicacao.add(localPublicacaoButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label19 = new JLabel();
        label19.setText("Monografia/Publicação periódica");
        painelPublicacao.add(label19, new GridConstraints(2, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        monografiaField = new JTextField();
        monografiaField.setText("");
        painelPublicacao.add(monografiaField, new GridConstraints(2, 3, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        eletronicoPanel = new JPanel();
        eletronicoPanel.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        painelPublicacao.add(eletronicoPanel, new GridConstraints(9, 0, 1, 6, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JLabel label20 = new JLabel();
        label20.setText("Disponível em");
        eletronicoPanel.add(label20, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label21 = new JLabel();
        label21.setText("Meio Eletrônico");
        eletronicoPanel.add(label21, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label22 = new JLabel();
        label22.setText("Acesso em");
        eletronicoPanel.add(label22, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        disponivelField = new JTextField();
        disponivelField.setText("");
        eletronicoPanel.add(disponivelField, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final Spacer spacer6 = new Spacer();
        eletronicoPanel.add(spacer6, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        acessoField = new JTextField();
        eletronicoPanel.add(acessoField, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label23 = new JLabel();
        label23.setText("Tipo NBR");
        painelPublicacao.add(label23, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tipoNbrDrop = new JComboBox();
        painelPublicacao.add(tipoNbrDrop, new GridConstraints(1, 3, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        arquivoPanel = new JScrollPane();
        painelPrincipal.add(arquivoPanel, new GridConstraints(8, 11, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        fileTree = new JTree();
        arquivoPanel.setViewportView(fileTree);
        attachFilesButton = new JButton();
        attachFilesButton.setText("Anexar arquivos");
        painelPrincipal.add(attachFilesButton, new GridConstraints(9, 11, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        label14.setLabelFor(mesSpinner);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }

}
