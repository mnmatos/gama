package com.digitallib.swing;

import com.digitallib.DocumentCreator;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.Documento;
import com.digitallib.model.MultiSourcedDocument;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class GroupedDocumentListButtonCellPanel extends JPanel {

    RefreshAction action;
    String code;
    Documento doc;
    MultiSourcedDocument group;

    private final Action editAction = new AbstractAction("editar") {
        public void actionPerformed(ActionEvent e) {
            editItem();
        }
    };

    private final Action removeAction = new AbstractAction("remover") {
        public void actionPerformed(ActionEvent e) {
            int result = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), "Você tem certeza que quer remover o " + code, "Atenção!!",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                Documento doc = RepositoryManager.getEntriesByCodigo(code).get(0);
                doc.setGrupo(null);
                RepositoryManager.updateEntry(doc);
                group.getDocuments().removeIf(code -> code.equals(doc.getCodigo()));
                action.perform();
            }
        }
    };

    private final JButton editButton = new JButton(editAction);
    private final JButton removeButton = new JButton(removeAction);

    public GroupedDocumentListButtonCellPanel(String code, MultiSourcedDocument group, RefreshAction action) {
        this.code = code;
        this.action = action;
        this.group = group;
        this.doc = RepositoryManager.getEntriesByCodigo(code).get(0);

        setLayout(new BorderLayout(0, 0));
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 0));
        panel.add(editButton);
        panel.add(removeButton);
        add(panel);
    }

    public void editItem() {
        DocumentCreator dialog = new DocumentCreator(doc);
        dialog.pack();
        dialog.setVisible(true);
        action.perform();
    }

}
