package com.digitallib;

import com.digitallib.exporter.docx.FichaExporter;
import com.digitallib.exporter.docx.InventarioExporter;
import com.digitallib.manager.CategoryManager;
import com.digitallib.manager.EntityManager;
import com.digitallib.manager.MultiSourcedDocumentManager;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.manager.index.DocByEntityIndexManager;
import com.digitallib.manager.index.EntityIndexManager;
import com.digitallib.manager.index.SubClassIndexManager;
import com.digitallib.model.Documento;
import com.digitallib.model.MultiSourcedDocument;
import com.digitallib.model.ui.Filter;
import com.digitallib.swing.DocumentListButtonEditor;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DocumentList extends JDialog {
    public static final String ACTIONS_HEADER = "Ações";
    private JTable tabela_poemas;
    private JPanel contentPanel;
    private JButton add_button;
    private JTextField filtroCodigo;
    private JLabel numDocField;
    private JComboBox classeFilter;
    private JComboBox testemunhoFilter;
    private JLabel testemunhoLabel;
    private JPanel buttonBanel;
    private JScrollPane tablePanel;
    private JPanel FilterPanel;
    private JMenuBar topMenuBar;

    private List<Filter> filterList = new ArrayList<>();

    private Logger logger = LogManager.getLogger();
    CategoryManager categoryManager = new CategoryManager();

    JMenu fileMenu = new JMenu("Arquivo");
    JMenu exportMenu = new JMenu("Exportar");
    JMenuItem addMenuItem = new JMenuItem("Adicionar mono-testemunhal");
    JMenuItem addPoliMenuItem = new JMenuItem("Adicionar poli-testemunhal");
    JMenuItem exportInventoryMenuItem = new JMenuItem("Exportar Inventário");
    JMenuItem exportCatalogMenuItem = new JMenuItem("Exportar Ficha-catálogo");

    public DocumentList() {
        setContentPane(contentPanel);
        setModal(true);
        refreshTable();

        initializeMenu();

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPanel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);


        add_button.addActionListener(e -> {
            addItem();
        });

        initializeFilters();

    }

    private void initializeMenu() {
        fileMenu.add(addMenuItem);
        fileMenu.add(addPoliMenuItem);
        topMenuBar.add(fileMenu);
        exportMenu.add(exportInventoryMenuItem);
        exportMenu.add(exportCatalogMenuItem);
        topMenuBar.add(exportMenu);

        addMenuItem.addActionListener(e -> {
            addItem();
        });

        addPoliMenuItem.addActionListener(e -> {

            String title = JOptionPane.showInputDialog(
                    null,
                    "Digite o Título",
                    "Criar Poli-testemunhal",
                    JOptionPane.QUESTION_MESSAGE
            );

            if (title != null && !title.trim().isEmpty()) {
                MultiSourcedDocument doc = new MultiSourcedDocument();
                doc.setTitulo(title);
                addPoliItem(MultiSourcedDocumentManager.addEntry(doc));
            } else {
                System.out.println("Nenhum nome de grupo foi inserido.");
            }
        });

        exportInventoryMenuItem.addActionListener(e -> {
            exportAsInventory();
        });

        exportCatalogMenuItem.addActionListener(e -> {
            exportAsCatalog();
        });
    }

    private void exportAsCatalog() {
        FichaExporter exporter = new FichaExporter();
        exporter.export(getDocumentos());
    }

    private void exportAsInventory() {
        InventarioExporter docxExporter = new InventarioExporter();
        docxExporter.export(getDocumentos());
    }

    private void initializeFilters() {
        filtroCodigo.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshTable();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshTable();
            }

            public void changedUpdate(DocumentEvent e) {
                refreshTable();
            }
        });
        initializeClasseDropdown();
        refreshFilters();
        testemunhoFilter.addActionListener(e -> refreshTable());
        toogleTestemunhoFilter();
    }

    private void refreshIndexes(List<Documento> documents) {
        new SubClassIndexManager().updateIndex(documents);
        new DocByEntityIndexManager().updateIndex(documents);
        new EntityIndexManager().updateIndex(EntityManager.getEntries());
    }

    private void refreshFilters() {
        Filter filterCodigo = new Filter("Código", d -> filtroCodigo.getText().equals("") || d.getCodigo().startsWith(filtroCodigo.getText()));
        Filter filterClasse = new Filter("Série", d -> classeFilter.getSelectedIndex() < 1 || d.getClasseProducao().equals(categoryManager.getClasseForIndex(classeFilter.getSelectedIndex() - 1)));
        Filter filterTestemunho = new Filter("Testemunho", d -> testemunhoFilter.getSelectedIndex() < 1 || (d.isInedito() == (testemunhoFilter.getSelectedIndex() == 1)));
        filterList.add(filterCodigo);
        filterList.add(filterClasse);
        filterList.add(filterTestemunho);
    }

    private void initializeClasseDropdown() {
        final DefaultComboBoxModel model = new DefaultComboBoxModel(categoryManager.getClasseAndCodeAsStringArray());
        classeFilter.setModel(model);
        classeFilter.addActionListener(e -> {
            toogleTestemunhoFilter();
            refreshTable();
        });
    }

    private void toogleTestemunhoFilter() {
        if (classeFilter.getSelectedIndex() == 1) {
            testemunhoFilter.setVisible(true);
            testemunhoLabel.setVisible(true);
        } else {
            testemunhoFilter.setSelectedIndex(0);
            testemunhoFilter.setVisible(false);
            testemunhoLabel.setVisible(false);
        }
    }

    private void refreshTable() {
        String[] colunas = {"Tradição", "Código", "Título", "Série", "Encontrado em", ACTIONS_HEADER};

        List<Documento> documentos = getDocumentosFiltrados();
        numDocField.setText(String.format("Número de documentos: %d", documentos.size()));

        String[][] dados = new String[documentos.size()][colunas.length];

        List<MultiSourcedDocument> multiSourcedDocuments = MultiSourcedDocumentManager.getEntries();
        Map<String, String> multiSourcedDocumentMap = multiSourcedDocuments.stream()
                .collect(Collectors.toMap(MultiSourcedDocument::getId, MultiSourcedDocument::getTitulo));

        for (int i = 0; i < documentos.size(); i++) {
            dados[i][0] = multiSourcedDocumentMap.get(documentos.get(i).getGrupo());
            dados[i][1] = documentos.get(i).getCodigo();
            dados[i][2] = documentos.get(i).getTitulo();
            dados[i][3] = documentos.get(i).getClasseProducao().getDesc();
            dados[i][4] = documentos.get(i).getEncontradoEm();
            dados[i][5] = documentos.get(i).getCodigo();
        }

        DefaultTableModel tableModel = new DefaultTableModel(dados, colunas);
        tabela_poemas.setModel(tableModel);


        DocumentListButtonEditor editor = new DocumentListButtonEditor(() -> refreshTable());
        tabela_poemas.getColumn(ACTIONS_HEADER).setCellRenderer(editor);
        tabela_poemas.getColumn(ACTIONS_HEADER).setCellEditor(editor);
        refreshIndexes(documentos);
    }

    private List<Documento> getDocumentosFiltrados() {
        List<Documento> documentos = getDocumentos();

        refreshFilters();
        for (Filter filter : filterList) {
            documentos = filter.filter(documentos);
        }

        return documentos;
    }

    private static List<Documento> getDocumentos() {
        List<Documento> documentos = RepositoryManager.getEntries();
        return documentos;
    }

    public static void main() {
        DocumentList dialog = new DocumentList();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }

    public void addItem() {
        DocumentCreator dialog = new DocumentCreator();
        dialog.pack();
        dialog.setVisible(true);
        refreshTable();
    }

    public void addPoliItem(String docCode) {
        MultiSourceDocumentList dialog = new MultiSourceDocumentList(docCode);
        dialog.pack();
        dialog.setVisible(true);
        refreshTable();
    }

    private void onCancel() {
        dispose();
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
        contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayoutManager(5, 2, new Insets(10, 20, 10, 20), -1, -1));
        contentPanel.setMinimumSize(new Dimension(1600, 800));
        contentPanel.setPreferredSize(new Dimension(1600, 800));
        buttonBanel = new JPanel();
        buttonBanel.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPanel.add(buttonBanel, new GridConstraints(4, 0, 1, 2, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        add_button = new JButton();
        add_button.setText("Adicionar");
        buttonBanel.add(add_button, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        buttonBanel.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        numDocField = new JLabel();
        numDocField.setText("Label");
        buttonBanel.add(numDocField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JLabel label1 = new JLabel();
        label1.setText("Documentos");
        contentPanel.add(label1, new GridConstraints(1, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        tablePanel = new JScrollPane();
        contentPanel.add(tablePanel, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        tabela_poemas = new JTable();
        tabela_poemas.setAutoCreateRowSorter(true);
        tabela_poemas.setEnabled(true);
        tabela_poemas.putClientProperty("JTable.autoStartsEdit", Boolean.FALSE);
        tablePanel.setViewportView(tabela_poemas);
        FilterPanel = new JPanel();
        FilterPanel.setLayout(new GridLayoutManager(1, 9, new Insets(20, 20, 20, 20), -1, -1));
        contentPanel.add(FilterPanel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        FilterPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), null, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, null, null));
        final JLabel label2 = new JLabel();
        label2.setText("Código");
        FilterPanel.add(label2, new GridConstraints(0, 7, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        filtroCodigo = new JTextField();
        FilterPanel.add(filtroCodigo, new GridConstraints(0, 8, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JLabel label3 = new JLabel();
        label3.setText("Série");
        FilterPanel.add(label3, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        classeFilter = new JComboBox();
        FilterPanel.add(classeFilter, new GridConstraints(0, 5, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer2 = new Spacer();
        FilterPanel.add(spacer2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        FilterPanel.add(panel1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        testemunhoFilter = new JComboBox();
        final DefaultComboBoxModel defaultComboBoxModel1 = new DefaultComboBoxModel();
        defaultComboBoxModel1.addElement("Inéditos e Éditos");
        defaultComboBoxModel1.addElement("Inéditos");
        defaultComboBoxModel1.addElement("Éditos");
        testemunhoFilter.setModel(defaultComboBoxModel1);
        panel1.add(testemunhoFilter, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        testemunhoLabel = new JLabel();
        testemunhoLabel.setText("Testemunho");
        FilterPanel.add(testemunhoLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        topMenuBar = new JMenuBar();
        contentPanel.add(topMenuBar, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPanel;
    }

}
