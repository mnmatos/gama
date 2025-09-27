package com.digitallib.swing;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class DocumentListButtonEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

    RefreshAction action;

    public DocumentListButtonEditor(RefreshAction action) {
        this.action = action;
    }

    @Override
    public Object getCellEditorValue() {
        return null;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return new DocumentListButtonCellPanel((String) value, action);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        return new DocumentListButtonCellPanel((String) value, action);
    }

}