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

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import net.sf.taverna.t2.uiexts.bioswr.model.EdamOntology;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author Dmitry Repchevsky
 */

public class AnnotationsTreeModel extends DefaultTreeModel {
    
    private String type;

    public AnnotationsTreeModel() {
        super(new DefaultMutableTreeNode());
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type == null) {
            if (this.type != null) {
                this.type = null;
                nodeStructureChanged(root);
            }
        } else if (!type.equals(this.type)) {
            OWLOntology ontology = EdamOntology.getInstance().getOntology();
            if (ontology != null) {
                String iri;
                switch(type) {
                    case "http://www.w3.org/ns/wsdl-rdf#Interface": iri = "http://edamontology.org/topic_0003";break;
                    case "http://www.w3.org/ns/wsdl-rdf#InterfaceOperation": iri = "http://edamontology.org/operation_0004";break;
                    case "http://www.w3.org/ns/wsdl-rdf#InputMessage": iri = "http://edamontology.org/data_0006";break;
                    case "http://www.w3.org/ns/wsdl-rdf#OutputMessage": iri = "http://edamontology.org/data_0006";break;
                    default: return;
                }

                Set<OWLEntity> entities = ontology.getEntitiesInSignature(IRI.create(iri), true);
                if (entities.isEmpty()) {
                    Logger.getLogger(AnnotationsTreeModel.class.getName()).log(Level.SEVERE, "EDAM concept {0} not found", iri);
                } else {
                    OWLEntity entity = entities.iterator().next();
                    if (entity.isOWLClass()) {
                        this.type = type;
                        DefaultMutableTreeNode raiz = (DefaultMutableTreeNode)root;
                        raiz.removeAllChildren();
                        addNode(ontology.getOWLOntologyManager(), entity.asOWLClass(), raiz, null);
                        nodeStructureChanged(root);
                    } else {
                        Logger.getLogger(AnnotationsTreeModel.class.getName()).log(Level.SEVERE, "EDAM concept {0} is not a class", iri);
                    }
                }
            }
        }
    }
    
    public void setFilter(String filter) {
        if (type != null) {
            final OWLOntology ontology = EdamOntology.getInstance().getOntology();
            if (ontology != null) {
                for (int i = root.getChildCount() - 1; i >= 0; i--) {
                    final DefaultMutableTreeNode child = (DefaultMutableTreeNode)root.getChildAt(i);
                    final OWLClass clazz = (OWLClass)child.getUserObject();
                    child.removeFromParent();
                    addNode(ontology.getOWLOntologyManager(), clazz, (DefaultMutableTreeNode) root, filter);
                }
                nodeStructureChanged(root);
            }
        }
    }
    
    private void addNode(OWLOntologyManager m, OWLClass clazz, DefaultMutableTreeNode parent, String filter) {
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(clazz);

        for (OWLClassExpression e : clazz.getSubClasses(m.getOntologies())) {
            if (!e.isAnonymous()) {
                addNode(m, e.asOWLClass(), child, filter);
            }
        }
        
        if (filter != null && filter.length() > 0 && child.isLeaf() && parent != root) {
            final IRI iri = clazz.getIRI();
            String label = EdamOntology.getInstance().getLabel(iri);
            if (label == null) {
                label = iri.toString();
            }
            if (!label.toUpperCase().contains(filter.toUpperCase())) {
                return;
            }
        }
        
        parent.add(child);
    }
}
