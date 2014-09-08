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
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import net.sf.taverna.t2.uiexts.bioswr.model.BioswrOntology;
import net.sf.taverna.t2.uiexts.bioswr.model.EdamOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Dmitry Repchevsky
 */

public class ServiceFilter extends RowFilter<TableModel, Integer> {
    private final static OWLAnnotationProperty MODEL_REFERENCE_PROPERTY = OWLManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://www.w3.org/ns/sawsdl#modelReference"));
    
    private String query;
    private String serviceName;
    private String semanticReference;
    
    public void setServiceName(String name) {
        serviceName = name;
    }
    
    public void setSearchQuery(String query) {
        this.query = query;
    }

    public void setSemanticReference(String semanticReference) {
        this.semanticReference = semanticReference;
    }

    @Override
    public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
        OWLNamedIndividual service = (OWLNamedIndividual)entry.getValue(2);
        return searchLabel(service) && checkModelReference(service) && searchQuery(service);
    }

    private boolean searchLabel(OWLNamedIndividual service) {
        if (serviceName == null || serviceName.isEmpty()) {
            return true;
        }
        
        String label = BioswrOntology.getInstance().getLabel(service.getIRI());
        if (label == null) {
            label = service.getIRI().toString();
        }

        return label.toUpperCase().contains(serviceName.toUpperCase());
    }

    private boolean searchQuery(OWLNamedIndividual individual) {
        if (query == null || query.isEmpty()) {
            return true;
        }

        OWLOntology services_ontology = BioswrOntology.getInstance().getOntology();
        OWLOntology annotations_ontology = EdamOntology.getInstance().getOntology();
        
        return searchQuery(services_ontology, annotations_ontology, individual);
    }
    
    private boolean searchQuery(OWLOntology services_ontology, OWLOntology annotations_ontology, OWLNamedIndividual individual) {
        
        Set<OWLAnnotation> annotations = individual.getAnnotations(services_ontology);
        for (OWLAnnotation annotation : annotations) {
            OWLAnnotationProperty annotationProperty = annotation.getProperty();
            if (annotationProperty.isComment()) {
                OWLAnnotationValue owlAnnotationValue = annotation.getValue();
                if (owlAnnotationValue instanceof OWLLiteral) {
                    final OWLLiteral value = (OWLLiteral)owlAnnotationValue;
                    final String literal = value.getLiteral();
                    if (literal != null && literal.toLowerCase().contains(query)) {
                        return true;
                    }
                }
            } else if ("http://www.w3.org/ns/sawsdl#modelReference".equals(annotationProperty.getIRI().toString())) {
                OWLAnnotationValue owlAnnotationValue = annotation.getValue();
                if (owlAnnotationValue instanceof OWLLiteral) {
                    final OWLLiteral value = (OWLLiteral)owlAnnotationValue;
                    final String literal = value.getLiteral();
                    if (searchQuery(annotations_ontology, IRI.create(literal))) {
                        return true;
                    }
                }
            }
        }

        Set<OWLObjectPropertyAssertionAxiom> axioms = services_ontology.getObjectPropertyAssertionAxioms(individual);
        for (OWLObjectPropertyAssertionAxiom axiom : axioms) {
            final OWLIndividual object = axiom.getObject();
            if (object.isNamed() && searchQuery(services_ontology, annotations_ontology, (OWLNamedIndividual)object)) {
                return true;
            }
        }
            
        return false;
    }
    
    private boolean searchQuery(OWLOntology annotations_ontology, IRI term) {
        OWLDataFactory dataFactory = annotations_ontology.getOWLOntologyManager().getOWLDataFactory();

        Set<OWLEntity> subjects = annotations_ontology.getEntitiesInSignature(term, true);
        for (OWLEntity subject : subjects) {
            for (OWLOntology o : annotations_ontology.getOWLOntologyManager().getOntologies()) {
                Set<OWLAnnotation> annotations = subject.getAnnotations(o, dataFactory.getRDFSComment());
                annotations.addAll(subject.getAnnotations(o, dataFactory.getRDFSLabel()));
                for (OWLAnnotation annotation : annotations) {
                    OWLAnnotationValue annotationValue = annotation.getValue();
                    if (annotationValue instanceof OWLLiteral) {
                        OWLLiteral owlLiteral = (OWLLiteral)annotationValue;
                        final String literal = owlLiteral.getLiteral();
                        if (literal != null && literal.toLowerCase().contains(query)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
    
    private boolean checkModelReference(OWLNamedIndividual individual) {
        if (semanticReference == null || semanticReference.isEmpty()) {
            return true;
        }

        OWLOntology ontology = BioswrOntology.getInstance().getOntology();
        return checkModelReference(ontology, individual);
    }
                        
    private boolean checkModelReference(OWLOntology ontology, OWLNamedIndividual individual) {
        
        Set<OWLAnnotation> annotations = individual.getAnnotations(ontology, MODEL_REFERENCE_PROPERTY);
        for (OWLAnnotation annotation : annotations) {
            OWLAnnotationValue owlAnnotationValue = annotation.getValue();
            if (owlAnnotationValue instanceof OWLLiteral) {
                final OWLLiteral value = (OWLLiteral)owlAnnotationValue;
                String literal = value.getLiteral();
                if (semanticReference.equals(literal)) {
                    return true;
                }
            }
        }
        
        Set<OWLObjectPropertyAssertionAxiom> axioms = ontology.getObjectPropertyAssertionAxioms(individual);
        for (OWLObjectPropertyAssertionAxiom axiom : axioms) {
            final OWLIndividual object = axiom.getObject();
            if (object.isNamed() && checkModelReference(ontology, (OWLNamedIndividual)object)) {
                return true;
            }
        }
            
        return false;
    }
}
