package com.digitallib.swing;

import com.digitallib.model.Documento;

import javax.print.Doc;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class DocSelectScreenWithSearch extends JDialog {
    private JTextField searchField;
    private JList<String> list;
    private DefaultListModel<String> model;
    private String selectedValue;

    public DocSelectScreenWithSearch(Frame parent, List<Documento> docs) {
        super(parent, "Select an Option", true);
        setLayout(new BorderLayout());

        searchField = new JTextField();
        model = new DefaultListModel<>();
        docs.forEach(doc -> model.addElement(getTextFromDoc(doc)));

        list = new JList<>(model);
        JScrollPane scrollPane = new JScrollPane(list);

        JButton selectButton = new JButton("Selecionar");
        selectButton.addActionListener(e -> {
            selectedValue = list.getSelectedValue();
            dispose();
        });

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filter(); }
            public void removeUpdate(DocumentEvent e) { filter(); }
            public void changedUpdate(DocumentEvent e) { filter(); }

            private void filter() {
                String query = searchField.getText().toLowerCase();
                List<Documento> filtered = docs.stream()
                        .filter(doc -> doc.getTitulo().toLowerCase().contains(query))
                        .collect(Collectors.toList());

                model.clear();
                filtered.forEach(doc -> model.addElement(getTextFromDoc(doc)));
            }
        });

        add(searchField, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(selectButton, BorderLayout.SOUTH);

        setSize(300, 400);
        setLocationRelativeTo(parent);
    }

    private static String getTextFromDoc(Documento doc) {
        return String.format("%s -> %s", doc.getCodigo(), doc.getTitulo());
    }

    public String getSelectedValue() {
        return selectedValue;
    }

    public static String showDialog(Frame parent, List<Documento> documentos) {
        DocSelectScreenWithSearch dialog = new DocSelectScreenWithSearch(parent, documentos);
        dialog.setVisible(true);
        return dialog.getSelectedValue();
    }
}