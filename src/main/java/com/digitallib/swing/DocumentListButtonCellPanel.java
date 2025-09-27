package com.digitallib.swing;

import com.digitallib.DocumentCreator;
import com.digitallib.MultiSourceDocumentList;
import com.digitallib.manager.RepositoryManager;
import com.digitallib.model.Documento;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DocumentListButtonCellPanel extends JPanel {

    RefreshAction action;
    String code;
    Documento doc;

    private final Action editAction = new AbstractAction("editar") {
        public void actionPerformed(ActionEvent e) {
            editItem(code);
        }
    };
    private final Action editGroupAction = new AbstractAction("Tradição") {
        public void actionPerformed(ActionEvent e) {
            editGroup(code);
        }
    };
    private final Action removeAction = new AbstractAction("remover") {
        public void actionPerformed(ActionEvent e) {
            int result = JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), "Você tem certeza que quer remover o " + code, "Atenção!!",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                RepositoryManager.removeEntry(code);
                action.perform();
            }
        }
    };

    private final JButton editGroupButton = new JButton(editGroupAction);
    private final JButton editButton = new JButton(editAction);
    private final JButton removeButton = new JButton(removeAction);

    public DocumentListButtonCellPanel(String code, RefreshAction action) {
        this.code = code;
        this.action = action;
        setLayout(new BorderLayout(0, 0));
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1, 0));
        doc = RepositoryManager.getEntriesByCodigo(code).get(0);
        if(doc.getGrupo()!=null) panel.add(editGroupButton);
        panel.add(editButton);
        panel.add(removeButton);
        add(panel);
    }

    public void editItem(String code) {
        DocumentCreator dialog = new DocumentCreator(doc);
        dialog.pack();
        dialog.setVisible(true);
        action.perform();
    }
    public void editGroup(String code) {
        MultiSourceDocumentList dialog = new MultiSourceDocumentList(doc.getGrupo());
        dialog.pack();
        dialog.setVisible(true);
        action.perform();
    }

}
