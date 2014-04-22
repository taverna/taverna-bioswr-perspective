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
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import net.sf.taverna.t2.uiexts.bioswr.model.EdamOntology;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.BioswrIcons;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;

/**
 * @author Dmitry Repchevsky
 */

public class AnnotationsTreeCellRenderer extends DefaultTreeCellRenderer {
    
    private String filter;
    
    public AnnotationsTreeCellRenderer() {
        setLeafIcon(BioswrIcons.ENTITY_ICON);
        setOpenIcon(BioswrIcons.ENTITY_ICON);
        setClosedIcon(BioswrIcons.ENTITY_ICON);
    }
    
    public String getFilter() {
        return filter;
    }
    
    public void setFilter(String filter) {
        this.filter = filter;
    }
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                         Object value,
                                         boolean sel,
                                         boolean expanded,
                                         boolean leaf,
                                         int row,
                                         boolean hasFocus) {
        if (value != null && value instanceof DefaultMutableTreeNode) {
            final DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;
            final OWLClass clazz = (OWLClass)node.getUserObject();
            if (clazz != null) {
                final IRI iri = clazz.getIRI();

                EdamOntology ontology = EdamOntology.getInstance();

                final String comment = ontology.getComment(iri);
                if (comment == null) {
                    setToolTipText(null);
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<div style='width:320px;'>").append(comment).append("</div>");
                    setToolTipText(sb.toString());
                }
                

                String label = ontology.getLabel(iri);
                if (label == null) {
                    value = iri;
                } else {
                    if (filter != null) {
                        final int l = filter.length();
                        if (l > 0) {
                            String label2 = label.toLowerCase();
                            StringBuilder sb = new StringBuilder("<html>");
                            int idx1 = 0;
                            int idx2;
                            while ((idx2 = label2.indexOf(filter, idx1)) >= 0) {
                                sb.append(label.substring(idx1, idx2));
                                idx1 = idx2 + l;
                                sb.append("<span style='color: red'>").append(label.substring(idx2, idx1)).append("</span>");
                            }
                            sb.append(label.substring(idx1)).append("</html>");
                            label = sb.toString();
                        }
                    }
                    value = label;
                }
            }
        }
        
        return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    }
}
