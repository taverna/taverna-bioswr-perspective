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
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import net.sf.taverna.t2.uiexts.bioswr.model.EdamOntology;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.BioswrIcons;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * @author Dmitry Repchevsky
 */

public class AnnotationCellRenderer extends JLabel implements ListCellRenderer<OWLAnnotationAssertionAxiom> {

    private final static String TOOLTIP_TEXT = "<div style='color: #0000FF; border-bottom: 1px solid #0000AA'>%s</div>" +
                                               "<div style='width:600px; color: #000000'>%s</div>";
    
    private int index = -1;
    private String username;

    private final OWLAnnotationProperty idDefinedByProperty;
    
    public AnnotationCellRenderer() {
        setOpaque(false);
        setIcon(BioswrIcons.DUMMY12_ICON);
        idDefinedByProperty = OWLManager.getOWLDataFactory().getRDFSIsDefinedBy();
    }

    public void setSelectionIndex(int index) {
        this.index = index;
    }

    public void setUsername(final String username) {
        this.username = username;
    }    
    
    @Override
    public Component getListCellRendererComponent(JList<? extends OWLAnnotationAssertionAxiom> list, OWLAnnotationAssertionAxiom value, int index, boolean isSelected, boolean cellHasFocus) {
        OWLAnnotationValue owlAnnotationValue = value.getValue();
        if (owlAnnotationValue instanceof OWLLiteral) {
            String definedBy = null;
            Set<OWLAnnotation> annotations = value.getAnnotations(idDefinedByProperty);
            for (OWLAnnotation annotation : annotations) {
                OWLAnnotationValue owlValue = annotation.getValue();
                if (owlValue instanceof OWLLiteral) {
                    OWLLiteral val = (OWLLiteral)annotation.getValue();
                    definedBy = val.getLiteral();
                }
            }
            
            OWLLiteral owlLiteral = (OWLLiteral)owlAnnotationValue;

            StringBuilder sb = new StringBuilder();
            sb.append("<html><span style='white-space:nowrap'>");
            if (definedBy != null && definedBy.equals(username)) {
                setIcon(this.index == index ? BioswrIcons.DELETE2_ICON : BioswrIcons.DELETE1_ICON);
                sb.append("<b>");
            } else {
                setIcon(BioswrIcons.DUMMY12_ICON);
            }

            String property = value.getProperty().getIRI().toString();
            sb.append("<span style='color:blue'>").append(property).append("</span>").
                append(" \'").append(owlLiteral.getLiteral()).append('\'');

            final String comments;
            if ("http://www.w3.org/ns/sawsdl#modelReference".equals(property)) {
                IRI ref = IRI.create(owlLiteral.getLiteral());
                EdamOntology ontology = EdamOntology.getInstance();
                final String label = ontology.getLabel(ref);
                if (label != null) {
                    sb.append(" (").append(label).append(')');
                    property = label;
                }
                comments = ontology.getComment(ref);
            } else {
                comments = owlLiteral.getLiteral();
            }

            if (definedBy != null) {
                sb.append(" definedBy ").append(definedBy);
                if (definedBy.equals(username)) {
                    sb.append("</b>");
                }
            }

            sb.append("</span></html>");
            setText(sb.toString());
            if (this.index == index) {
                setToolTipText(ResourceBundle.getBundle("resources/messages").getString("icon.tooltip.remove.annotation"));
            } else {
                setToolTipText(comments == null ? null : String.format(TOOLTIP_TEXT, property, comments));
            }
        } else {
            setText("");
            setToolTipText(null);
        }
        return this;
    }
}
