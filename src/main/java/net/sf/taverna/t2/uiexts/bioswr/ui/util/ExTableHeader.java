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

package net.sf.taverna.t2.uiexts.bioswr.ui.util;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Enumeration;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * @author Dmitry Repchevsky
 */

public class ExTableHeader extends JTableHeader {
    
    public ExTableHeader(TableColumnModel model) {
        super(new ExTableColumnModel(model));
    }
    
    @Override
    public ExTableColumnModel getColumnModel() {
        return (ExTableColumnModel) super.getColumnModel();
    }
    
    @Override
    public void setColumnModel(TableColumnModel columnModel) {
        super.setColumnModel(new ExTableColumnModel(columnModel));
    }
    
    @Override
    protected ExTableColumnModel createDefaultColumnModel() {
        final TableColumnModel model = super.createDefaultColumnModel();
        return new ExTableColumnModel(model);
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        super.processMouseMotionEvent(e);

        // just stop editing on column resize/rearrange
        final ExTableColumnModel model = getColumnModel();
        final int event_id = e.getID();
        if (MouseEvent.MOUSE_DRAGGED == event_id) {
            if (getResizingColumn() != null || getDraggedColumn() != null) {
                for (int i = 0, n = model.getColumnCount(); i < n; i++) {
                    final ExTableColumn column = model.getColumn(i);
                    final TableCellEditor editor = column.getHeaderEditor();
                    if (editor != null) {
                        editor.stopCellEditing();
                    }
                }
            }
        }
    }
    
    @Override
    protected void processMouseEvent(MouseEvent e) {
        super.processMouseEvent(e);

        final Point p = e.getPoint();
        final ExTableColumnModel model = getColumnModel();
        final int index = model.getColumnIndexAtX(p.x);
        if (index >= 0) {
            final ExTableColumn column = model.getColumn(index);
            final TableCellEditor editor = column.getHeaderEditor();
            if (editor != null) {
                final Component editorComponent;
                final int event_id = e.getID();
                if (MouseEvent.MOUSE_PRESSED == event_id) {
                    if (!editor.isCellEditable(e)) {
                        return;
                    }

                    editorComponent = editor.getTableCellEditorComponent(getTable(), column.getHeaderValue(), true, -1, index);
                    editor.addCellEditorListener(new CellEditorListener() {
                        @Override
                        public void editingStopped(ChangeEvent e) {
                            Object value = editor.getCellEditorValue();
                            column.setHeaderValue(value);
                            cleanup();
                        }

                        @Override
                        public void editingCanceled(ChangeEvent e) {
                            cleanup();
                        }

                        private void cleanup() {
                            editor.removeCellEditorListener(this);
                            requestFocusInWindow();
                            remove(editorComponent);
                            repaint(getHeaderRect(index));
                        }
                    });

                    editorComponent.setBounds(getHeaderRect(index));
                    add(editorComponent);
                    editorComponent.validate();
                    editorComponent.requestFocusInWindow();
                } else if (MouseEvent.MOUSE_CLICKED == event_id || MouseEvent.MOUSE_RELEASED == event_id){
                    editorComponent = editor.getTableCellEditorComponent(getTable(), column.getHeaderValue(), true, -1, index);
                } else {
                    return;
                }

                Point p2 = SwingUtilities.convertPoint(this, p, editorComponent);
                Component dispatchComponent = SwingUtilities.getDeepestComponentAt(editorComponent, p2.x, p2.y);
                if (dispatchComponent != null) {
                    MouseEvent e2 = SwingUtilities.convertMouseEvent(this, e, dispatchComponent);
                    dispatchComponent.dispatchEvent(e2);
                }
            }
        }
    }
    
    public static class ExTableColumnModel implements TableColumnModel {
        private TableColumnModel model;
        
        public ExTableColumnModel(TableColumnModel model) {
            this.model = model;
            extendColumns();
        }
        
        /*
         * Method replaces all model columns with extended versions.
         */
        private void extendColumns() {
            for (int i = 0, n = model.getColumnCount(); i < n; i++) {
                TableColumn col = model.getColumn(0);
                model.removeColumn(col);
                addColumn(col);
            }
        }
        
        @Override
        public void addColumn(TableColumn column) {
            if (column instanceof ExTableColumn) {
                model.addColumn(column);
            } else {
                model.addColumn(new ExTableColumn(column));
            }
        }

        @Override
        public void removeColumn(TableColumn column) {
            model.removeColumn(column);
        }

        @Override
        public void moveColumn(int columnIndex, int newIndex) {
            model.moveColumn(columnIndex, newIndex);
        }

        @Override
        public void setColumnMargin(int newMargin) {
            model.setColumnMargin(newMargin);
        }

        @Override
        public int getColumnCount() {
            return model.getColumnCount();
        }

        @Override
        public Enumeration<TableColumn> getColumns() {
            return model.getColumns();
        }

        @Override
        public int getColumnIndex(Object columnIdentifier) {
            return model.getColumnIndex(columnIdentifier);
        }

        @Override
        public ExTableColumn getColumn(int columnIndex) {
            return (ExTableColumn) model.getColumn(columnIndex);
        }

        @Override
        public int getColumnMargin() {
            return model.getColumnMargin();
        }

        @Override
        public int getColumnIndexAtX(int xPosition) {
            return model.getColumnIndexAtX(xPosition);
        }

        @Override
        public int getTotalColumnWidth() {
            return model.getTotalColumnWidth();
        }

        @Override
        public void setColumnSelectionAllowed(boolean flag) {
            model.setColumnSelectionAllowed(flag);
        }

        @Override
        public boolean getColumnSelectionAllowed() {
            return model.getColumnSelectionAllowed();
        }

        @Override
        public int[] getSelectedColumns() {
            return model.getSelectedColumns();
        }

        @Override
        public int getSelectedColumnCount() {
            return model.getSelectedColumnCount();
        }

        @Override
        public void setSelectionModel(ListSelectionModel newModel) {
            model.setSelectionModel(newModel);
        }

        @Override
        public ListSelectionModel getSelectionModel() {
            return model.getSelectionModel();
        }

        @Override
        public void addColumnModelListener(TableColumnModelListener x) {
            model.addColumnModelListener(x);
        }

        @Override
        public void removeColumnModelListener(TableColumnModelListener x) {
            model.removeColumnModelListener(x);
        }
    }
    
    public static class ExTableColumn extends TableColumn {
        protected TableCellEditor headerEditor;
    
        public ExTableColumn() {}
        
        public ExTableColumn(TableColumn column) {
            modelIndex = column.getModelIndex();
            identifier = column.getIdentifier();
            cellEditor = column.getCellEditor();
            cellRenderer = column.getCellRenderer();
            headerRenderer = column.getHeaderRenderer();
            headerValue = column.getHeaderValue();
            isResizable = column.getResizable();
            width = column.getWidth();
            minWidth = column.getMinWidth();
            maxWidth = column.getMaxWidth();
        }
        
        public TableCellEditor getHeaderEditor() {
            return headerEditor;
        }

        public void setHeaderEditor(TableCellEditor headerEditor){
            this.headerEditor = headerEditor;
        }
    }
}
