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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import net.sf.taverna.t2.uiexts.bioswr.model.BioswrOntology;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.BlockablePanel;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.ExTableHeader;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.ExTableHeader.ExTableColumn;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.ExTableHeader.ExTableColumnModel;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.HtmlToolTip;
import net.sf.taverna.t2.uiexts.bioswr.ui.worker.GetAllServicesWorker;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Dmitry Repchevsky
 */

public class WSDLTablePane extends BlockablePanel {
    
    public final WSDLTable table;
    
    public WSDLTablePane() {
        
        table = new WSDLTable();
        table.setFillsViewportHeight(true);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, JLayeredPane.DEFAULT_LAYER);

        GetAllServicesWorker worker = new GetAllServicesWorker();
        worker.addPropertyChangeListener(table);
        worker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (SwingWorker.StateValue.DONE.equals(evt.getNewValue())) {
                    unblock();
                }
            }
        });
        worker.execute();
        block();
    }

    public void setUsername(final String username) {
        table.setUsername(username);
    }
    
    public static class WSDLTable extends JTable implements PropertyChangeListener {
        
        private final ServicePermissionsCellRenderer permissionsRenderer;
        private final ServiceCellRenderer serviceCellRenderer;
        
        public WSDLTable() {
            super(new WSDLTableModel());

            setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            ExTableHeader header = new ExTableHeader(getTableHeader().getColumnModel());
            header.setReorderingAllowed(false);
            setTableHeader(header);
            
            ExTableColumnModel headerColumnModel = header.getColumnModel();
            ExTableColumn hcol2 = headerColumnModel.getColumn(2);
            
            final PlainDocument document = new PlainDocument();
         
            HeaderFilterCellEditor headerCellEditor = new HeaderFilterCellEditor(document);
            HeaderFilterCellRenderer headerCellRenderer = new HeaderFilterCellRenderer();
            
            hcol2.setHeaderEditor(headerCellEditor);
            hcol2.setHeaderRenderer(headerCellRenderer);

            hcol2.setHeaderValue(null); // by default the value is set to a column name
            
            TableColumn col0 = getColumnModel().getColumn(0);
            col0.setCellRenderer(permissionsRenderer = new ServicePermissionsCellRenderer());
            col0.setMinWidth(20);
            col0.setMaxWidth(20);
            col0.setPreferredWidth(20);
            col0.setResizable(false);
            
            TableColumn col1 = getColumnModel().getColumn(1);
            col1.setCellRenderer(new ServiceTypeCellRenderer());
            col1.setMinWidth(20);
            col1.setMaxWidth(20);
            col1.setPreferredWidth(20);
            col1.setResizable(false);

            TableColumn col2 = getColumnModel().getColumn(2);
            col2.setCellRenderer(serviceCellRenderer = new ServiceCellRenderer());

            TableColumn col3 = getColumnModel().getColumn(3);
            col3.setCellRenderer(new ServiceEndpointCellRenderer());
            
            final ServiceFilter filter = new ServiceFilter(document);
            final TableRowSorter<TableModel> sorter = new TableRowSorter(this.getModel());
            sorter.setRowFilter(filter);
            sorter.setSortable(2, false);
            setRowSorter(sorter);

            document.setDocumentFilter(new DocumentFilter() {
                @Override
                public void remove(DocumentFilter.FilterBypass fb, int offset, int length) 
                        throws BadLocationException {
                    fb.remove(offset, length);
                    if (length != 0) {
                        updateTableModel();
                    }
                }
                @Override
                public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) 
                        throws BadLocationException {
                    fb.insertString(offset, string, attr);
                    if (string.length() > 0) {
                        updateTableModel();
                    }
                }
                @Override
                public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                             throws BadLocationException {
                    
                    final boolean eq = text.equals(fb.getDocument().getText(offset, length));
                    fb.replace(offset, length, text, attrs);
                    if (!eq) {
                        updateTableModel();
                    }                    
                }
                private void updateTableModel() throws BadLocationException {
                    serviceCellRenderer.setFilter(document.getText(0, document.getLength()));
                    sorter.setRowFilter(filter);
                }
            });
        }
        
        public void setUsername(final String username) {
            permissionsRenderer.setUsername(username);
            repaint();
        }

        @Override
        public JToolTip createToolTip() {
            return new HtmlToolTip();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            SwingWorker<OWLOntology, Object> task = (SwingWorker)evt.getSource();

            Object value = evt.getNewValue();

            if (SwingWorker.StateValue.DONE.equals(value)) {
                try {
                    ((WSDLTableModel)getModel()).setOntology(task.get());
                } catch (Exception ex) {
                    Logger.getLogger(WSDLTableModel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public static class ServiceFilter extends RowFilter<TableModel, Integer> {
        private final PlainDocument document;

        public ServiceFilter(PlainDocument document) {
            this.document = document;
        }
        
        @Override
        public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
            final int length = document.getLength();
            if (length == 0) {
                return true;
            }
            try {
                final String filter = document.getText(0, length);
            
                OWLNamedIndividual service = (OWLNamedIndividual)entry.getValue(2);
                String label = BioswrOntology.getInstance().getLabel(service.getIRI());
                if (label == null) {
                    label = service.getIRI().toString();
                }
                return label.toUpperCase().contains(filter.toUpperCase());
            } catch(BadLocationException ex) {
                return false;
            }
        }
    }
}
