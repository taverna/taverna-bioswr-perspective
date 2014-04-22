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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.CellRendererPane;
import javax.swing.JLayeredPane;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.PlainDocument;

/**
 * @author Dmitry Repchevsky
 */

public class HeaderFilterCellRenderer extends AbstractFilterCellHeader implements TableCellRenderer {

    private TableCellRenderer headerCellRenderer;
    private Component rendererComponent;
    
    public HeaderFilterCellRenderer() {
        super(new PlainDocument());
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        headerCellRenderer = table.getTableHeader().getDefaultRenderer();
        
        // remove rendererComponent if already added.
        for (Component c : getComponentsInLayer(JLayeredPane.DEFAULT_LAYER)) {
            remove(c);
        }
        
        TableColumn tableColumn = table.getColumnModel().getColumn(column);
        rendererComponent = headerCellRenderer.getTableCellRendererComponent(table, table.getModel().getColumnName(tableColumn.getModelIndex()), isSelected, hasFocus, row, column);
        add(rendererComponent, JLayeredPane.DEFAULT_LAYER);
        
        final Dimension rendererSize = rendererComponent.getPreferredSize();
        
        editorComponent.setText(value == null ? "" : value.toString());
        final Dimension editorSize = editorComponent.getPreferredSize();
        editorComponent.setBounds(rendererSize.width, Math.max(0, (rendererSize.height - editorSize.height) / 2), editorSize.width, editorSize.height);

        return this;
    }
    
    @Override
    public void validate() {
        super.validate();
        if (rendererComponent != null) {
            rendererComponent.setBounds(0, 0, this.getWidth(), this.getHeight());
        }
    }

    @Override
    public void paint(Graphics g) {
        Container parent = getParent();
        if (parent instanceof CellRendererPane) {
            // before paining JTableHeader incapsulates renderers into CellRendererPane
            CellRendererPane pane = (CellRendererPane)parent;
            pane.paintComponent(g, rendererComponent, pane.getParent(), rendererComponent.getBounds());
            pane.paintComponent(g, editorComponent, pane.getParent(), editorComponent.getBounds());
        } else {
            super.paint(g);
        }
    }
}
