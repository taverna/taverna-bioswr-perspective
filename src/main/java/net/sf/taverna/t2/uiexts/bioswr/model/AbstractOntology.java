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

package net.sf.taverna.t2.uiexts.bioswr.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author Dmitry Repchevsky
 */

public abstract class AbstractOntology {
    
    private OWLOntology owlOntology;
    
    public abstract String getOntologyURI();
    
    public final synchronized OWLOntology getOntology() {
        return owlOntology != null ? owlOntology : loadOntology();
    }
    
    public final synchronized OWLOntology loadOntology() {
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        try {
            owlOntology = m.loadOntology(IRI.create(getOntologyURI()));
        } catch (OWLOntologyCreationException ex) {
            Logger.getLogger(AbstractOntology.class.getName()).log(Level.SEVERE, null, ex);
        }
        return owlOntology;
    }

    public synchronized List<OWLAnnotationAssertionAxiom> getAnnotations(IRI iri) {
        final List<OWLAnnotationAssertionAxiom> annotations = new ArrayList<OWLAnnotationAssertionAxiom>();
        final OWLOntology ontology = getOntology();
        if (ontology != null) {
            final Set<OWLOntology> ontologies = ontology.getOWLOntologyManager().getOntologies();
            for (OWLOntology o : ontologies) {
                final Set<OWLAnnotationAssertionAxiom> axioms = o.getAnnotationAssertionAxioms(iri);
                for(OWLAnnotationAssertionAxiom axiom : axioms) {
                    annotations.add(axiom);
                }
            }
        }
        return annotations;
    }
    
    /**
     * Returns rdfs:label annotation value for the axiom.
     * 
     * @param iri axiom resource identifier 
     * 
     * @return the value of rdfs:label of the axiom or null
     */
    public synchronized String getLabel(final IRI iri) {
        final OWLOntology ontology = getOntology();
        if (ontology != null) {
            final Set<OWLOntology> ontologies = ontology.getOWLOntologyManager().getOntologies();
            for (OWLOntology o : ontologies) {
                final Set<OWLAnnotationAssertionAxiom> axioms = o.getAnnotationAssertionAxioms(iri);
                for(OWLAnnotationAssertionAxiom axiom : axioms) {
                    final OWLAnnotationProperty property = axiom.getProperty();
                    if (property.isLabel()) {
                        final OWLAnnotationValue owlAnnotationValue = axiom.getValue();
                        if (owlAnnotationValue instanceof OWLLiteral) {
                            final OWLLiteral owlLiteral = (OWLLiteral)owlAnnotationValue;
                            return owlLiteral.getLiteral();
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public synchronized String getComment(final IRI iri) {
        final OWLOntology ontology = getOntology();
        if (ontology != null) {
            final Set<OWLOntology> ontologies = owlOntology.getOWLOntologyManager().getOntologies();
            for (OWLOntology o : ontologies) {
                final Set<OWLAnnotationAssertionAxiom> axioms = o.getAnnotationAssertionAxioms(iri);
                for(OWLAnnotationAssertionAxiom axiom : axioms) {
                    final OWLAnnotationProperty property = axiom.getProperty();
                    if (property.isComment()) {
                        final OWLAnnotationValue owlAnnotationValue = axiom.getValue();
                        if (owlAnnotationValue instanceof OWLLiteral) {
                            final OWLLiteral owlLiteral = (OWLLiteral)owlAnnotationValue;
                            return owlLiteral.getLiteral();
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public synchronized String getDefinedBy(final IRI iri) {
        final OWLOntology ontology = getOntology();
        if (ontology != null) {
            IRI isDefinedByIRI = ontology.getOWLOntologyManager().getOWLDataFactory().getRDFSIsDefinedBy().getIRI();
            final Set<OWLOntology> ontologies = ontology.getOWLOntologyManager().getOntologies();
            for (OWLOntology o : ontologies) {
                final Set<OWLAnnotationAssertionAxiom> axioms = o.getAnnotationAssertionAxioms(iri);
                for(OWLAnnotationAssertionAxiom axiom : axioms) {
                    final OWLAnnotationProperty property = axiom.getProperty();
                    if (isDefinedByIRI.equals(property.getIRI())) {
                        final OWLAnnotationValue owlAnnotationValue = axiom.getValue();
                        if (owlAnnotationValue instanceof OWLLiteral) {
                            final OWLLiteral owlLiteral = (OWLLiteral)owlAnnotationValue;
                            return owlLiteral.getLiteral();
                        }
                    }
                }
            }
        }
        return null;
    }
}
