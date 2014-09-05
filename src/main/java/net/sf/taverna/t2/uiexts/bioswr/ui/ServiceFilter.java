/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.taverna.t2.uiexts.bioswr.ui;

import java.util.Map;
import java.util.Set;
import javax.swing.RowFilter;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import net.sf.taverna.t2.uiexts.bioswr.model.BioswrOntology;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 *
 * @author bscuser
 */
public class ServiceFilter extends RowFilter<TableModel, Integer> {
    private final static OWLAnnotationProperty MODEL_REFERENCE_PROPERTY = OWLManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create("http://www.w3.org/ns/sawsdl#modelReference"));
    
    private final PlainDocument document;
    private String semanticReference;

    public ServiceFilter(PlainDocument document) {
        this.document = document;
    }

    public void setSemanticReference(String semanticReference) {
        this.semanticReference = semanticReference;
    }

    @Override
    public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
        if (semanticReference == null && document.getLength() == 0) {
            return true;
        }
        
        OWLNamedIndividual service = (OWLNamedIndividual)entry.getValue(2);
        return checkLabel(service) && checkModelReference(service);
    }

    private boolean checkLabel(OWLNamedIndividual service) {
        try {
            final String filter = document.getText(0, document.getLength());

            String label = BioswrOntology.getInstance().getLabel(service.getIRI());
            if (label == null) {
                label = service.getIRI().toString();
            }
            return label.toUpperCase().contains(filter.toUpperCase());
        } catch(BadLocationException ex) {
            return false;
        }
    }

    private boolean checkModelReference(OWLNamedIndividual individual) {
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
