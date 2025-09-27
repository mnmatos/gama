package com.digitallib.swing;

import com.digitallib.model.MultiSourcedDocument;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class GroupedDocumentListButtonEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

    RefreshAction action;
    MultiSourcedDocument group;

    public GroupedDocumentListButtonEditor(MultiSourcedDocument doc, RefreshAction action) {
        this.action = action;
        this.group = doc;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return new GroupedDocumentListButtonCellPanel((String) value, group, action);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return new GroupedDocumentListButtonCellPanel((String) value, group, action);
    }

}