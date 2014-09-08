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
import java.awt.FlowLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import net.sf.taverna.t2.uiexts.bioswr.model.BioswrOntology;
import net.sf.taverna.t2.uiexts.bioswr.model.EdamOntology;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Dmitry Repchevsky
 */

public class SemanticTagsPanel extends JPanel implements MouseListener {
    
    private final TableRowSorter<TableModel> sorter;
    private final ServiceFilter filter;

    private TagLabel selected;
    
    public SemanticTagsPanel(TableRowSorter<TableModel> sorter, ServiceFilter filter) {
        super(new FlowLayout(FlowLayout.LEFT, 2, 2));
        setBorder(null);
        
        this.sorter = sorter;
        this.filter = filter;

        init();
    }
    
    private void init() {
        removeAll();

        add(new SemanticSearchPanel(sorter, filter));
        
        OWLOntology ontology = EdamOntology.getInstance().getOntology();
        
        List<String> references = BioswrOntology.getInstance().getReferences(null);
        
        for (int i = 0, n = Math.min(references.size(), 20); i < n; i++) {
            final String modelReference = references.get(i);
            
            IRI modelReferenceIRI = IRI.create(modelReference);
            Set<OWLEntity> subjects = ontology.getEntitiesInSignature(modelReferenceIRI, true);
            if (!subjects.isEmpty()) {
                TagLabel tagLabel = new TagLabel(modelReference);
                tagLabel.addMouseListener(this);
                
                OWLEntity subject = subjects.iterator().next();
                Set<OWLOntology> extOntologies = ontology.getOWLOntologyManager().getOntologies();
                for (OWLOntology extOntology : extOntologies) {
                    Set<OWLAnnotation> annotations = subject.getAnnotations(extOntology);
                    for (OWLAnnotation annotation : annotations) {
                        OWLAnnotationProperty property = annotation.getProperty();
                        if (property.isLabel()) {
                            OWLLiteral val = (OWLLiteral)annotation.getValue();
                            tagLabel.setText(val.getLiteral());
                        } else if (property.isComment()) {
                            OWLLiteral val = (OWLLiteral)annotation.getValue();
                            StringBuilder tooltipText = new StringBuilder();
                            tooltipText.append("<div style='width:320px;'>").append(modelReferenceIRI)
                                    .append("<br/>").append(val.getLiteral()).append("</div>");
                            tagLabel.setToolTipText(tooltipText.toString());
                        }
                    }
                }
                add(tagLabel);
            }
        }
    }

    @Override
    public Dimension getMinimumSize() {
        final Component label = getComponent(1);
        return new Dimension(0, label.getPreferredSize().height * 2 + 6);
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (selected != e.getComponent()) {
            if (selected != null) {
                selected.setSelected(false);
            }
            selected = (TagLabel)e.getComponent();
            selected.setSelected(true);
            filter.setSemanticReference(selected.reference);
        } else if (selected != null){
            selected.setSelected(false);
            selected = null;
            filter.setSemanticReference(null);
        }
        
        sorter.setRowFilter(filter); // rebuild service table
        
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {}

    @Override
    public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {
        e.getComponent().setBackground(TagLabel.BACKGROUND_HIGHLITED);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        e.getComponent().setBackground(TagLabel.BACKGROUND);
    }
}
