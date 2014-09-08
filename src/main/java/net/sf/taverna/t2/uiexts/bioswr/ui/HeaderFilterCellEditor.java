/**
 * *****************************************************************************
 * Copyright (C) 2014 Spanish National Bioinformatics Institute (INB),
 * Barcelona Supercomputing Center and The University of Manchester
 *
 * Modifications to the initial code base are copyright of their respective
 * authors, or their employers as appropriate.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *****************************************************************************
 */

package net.sf.taverna.t2.uiexts.bioswr.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.PlainDocument;

/**
 * @author Dmitry Repchevsky
 */

public class HeaderFilterCellEditor extends AbstractFilterCellHeader implements TableCellEditor, FocusListener {
    
    private final DefaultCellEditor headerCellEditor;
    
    public HeaderFilterCellEditor(PlainDocument document) {
        super(document);

        headerCellEditor = new DefaultCellEditor(editorComponent);
        headerCellEditor.setClickCountToStart(1);
        
        editorComponent.addFocusListener(this);
    }
    
    public JTextField getEditorComponent() {
        return editorComponent;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        headerCellEditor.getTableCellEditorComponent(table, value, isSelected, row, column);

        TableCellRenderer headerCellRenderer = table.getTableHeader().getDefaultRenderer();
        
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        Component rendererComponent = headerCellRenderer.getTableCellRendererComponent(table, table.getModel().getColumnName(tableColumn.getModelIndex()), isSelected, true, row, column);
        
        final Dimension rendererSize = rendererComponent.getPreferredSize();            
        final Dimension editorSize = editorComponent.getPreferredSize();
        editorComponent.setBounds(rendererSize.width, Math.max(0, (rendererSize.height - editorSize.height) / 2), editorSize.width, editorSize.height);

        return this;
    }
    
    @Override
    public Object getCellEditorValue() {
        return headerCellEditor.getCellEditorValue();
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return headerCellEditor.isCellEditable(anEvent);
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return headerCellEditor.shouldSelectCell(anEvent);
    }

    @Override
    public boolean stopCellEditing() {
        return headerCellEditor.stopCellEditing();
    }

    @Override
    public void cancelCellEditing() {
        headerCellEditor.cancelCellEditing();
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        headerCellEditor.addCellEditorListener(l);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        headerCellEditor.removeCellEditorListener(l);
    }

    @Override
    public void focusGained(FocusEvent e) {
    }

    @Override
    public void focusLost(FocusEvent e) {
        headerCellEditor.stopCellEditing();
    }
}
