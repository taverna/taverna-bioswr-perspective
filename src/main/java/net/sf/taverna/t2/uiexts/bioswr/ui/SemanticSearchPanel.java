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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.DecoratorBorder;

/**
 * @author Dmitry Repchevsky
 */

public class SemanticSearchPanel extends JPanel {
    
    private final static Color EDIT_FIELD_COLOR = new Color(0xEF,0xFB,0xFB);
    
    public SemanticSearchPanel(final TableRowSorter<TableModel> sorter, final ServiceFilter filter) {
        super(new BorderLayout());
        
        setBorder(new DecoratorBorder(Color.LIGHT_GRAY, 10, new Insets(2,2,2,2)));
        setBackground(TagLabel.BACKGROUND);
        
        final JLabel label = new JLabel(ResourceBundle.getBundle("resources/messages").getString("label.search"));
        label.setBorder(new EmptyBorder(0, 3, 0, 3));

        final PlainDocument document = new PlainDocument();
        document.addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                search();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                search();
            }
            
            private void search() {
                try {
                    filter.setSearchQuery(document.getText(0, document.getLength()));
                    sorter.setRowFilter(filter);
                } catch (BadLocationException ex) {
                    Logger.getLogger(SemanticSearchPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        final JTextField textField = new JTextField(document, "", 20);
        textField.setBorder(new EmptyBorder(0, 0, 0, 3));
        textField.setBackground(EDIT_FIELD_COLOR);

        add(label, BorderLayout.WEST);
        add(textField, BorderLayout.EAST);
    }
}
