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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import net.sf.taverna.t2.uiexts.bioswr.config.BioswrConfig;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

/**
 * @author Dmitry Repchevsky
 */

public class BioswrOntology extends AbstractOntology {

    protected final static String URI = BioswrConfig.BIOSWR_SERVER_URI + "/rest/service/";
    
    private final static String WSDL_LOCATION_IRI = "http://www.w3.org/ns/wsdl-instance#wsdlLocation";
    
    protected final static String ENDPOINT_PROPERTY_IRI = "http://www.w3.org/ns/wsdl-rdf#endpoint";
    protected final static String ADDRESS_PROPERTY_IRI = "http://www.w3.org/ns/wsdl-rdf#address";
    protected final static String IMPLEMENTS_PROPERTY_IRI = "http://www.w3.org/ns/wsdl-rdf#implements";
    
    protected final static String BINDING_PROPERTY_IRI = "http://www.w3.org/ns/wsdl-rdf#usesBinding";
    protected final static String MODEL_REF_PROPERTY = "http://www.w3.org/ns/sawsdl#modelReference";

    protected final static String WSOAP = "http://www.w3.org/ns/wsdl/soap";
    protected final static String WHTTP = "http://www.w3.org/ns/wsdl/http";
    
    private static BioswrOntology ontology;
    
    @Override
    public String getOntologyURI() {
        return URI;
    }
    
    public static synchronized BioswrOntology getInstance() {
        if (ontology == null) {
            ontology = new BioswrOntology();
        }
        return ontology;
    }

    public IRI getWSDLLocation(OWLNamedIndividual service) {
        OWLAnnotationProperty locationProperty = getOntology().getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(IRI.create(WSDL_LOCATION_IRI));
        Set<OWLAnnotation> locations = service.getAnnotations(getOntology(), locationProperty);

        return getWSDLLocationValue(locations);
    }
    
    private IRI getWSDLLocationValue(Set<OWLAnnotation> locations) {
        for (OWLAnnotation location : locations) {
            OWLAnnotationValue locationValue = location.getValue();
            if (locationValue instanceof IRI) {
                return (IRI)locationValue;
            } else if (locationValue instanceof OWLLiteral) {
                OWLLiteral literal = (OWLLiteral)locationValue;
                OWLDatatype datatype = literal.getDatatype();
                if (datatype.isBuiltIn()) {
                    OWL2Datatype owl2Datatype = datatype.getBuiltInDatatype();
                    if (OWL2Datatype.Category.CAT_URI == owl2Datatype.getCategory()) {
                        return IRI.create(literal.getLiteral());
                    }
                }
            }
        }
        return null;
    }
    
    public OWLIndividual getServiceInterface(OWLIndividual service) {
        final OWLOntology o = getOntology();
        final OWLObjectProperty implementsProperty = o.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(IRI.create(IMPLEMENTS_PROPERTY_IRI));
        final Set<OWLIndividual> interfaces = service.getObjectPropertyValues(implementsProperty, o);
        if (interfaces.isEmpty()) {
            return null;
        } else {
            return interfaces.iterator().next();
        }
    }    
    
    public OWLNamedIndividual getServiceEndpoint(OWLNamedIndividual service) {
        final OWLOntology o = getOntology();
        final OWLObjectProperty endpointProperty = o.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(IRI.create(ENDPOINT_PROPERTY_IRI));
        final Set<OWLIndividual> endpoints = service.getObjectPropertyValues(endpointProperty, o);
        for (OWLIndividual endpoint : endpoints) {
            if (endpoint.isNamed()) {
                return endpoint.asOWLNamedIndividual();
            }
        }
        return null;
    }
    
    public IRI getEndpointAddress(OWLNamedIndividual endpoint) {
        final OWLOntology o = getOntology();
        final OWLObjectProperty addressProperty = o.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(IRI.create(ADDRESS_PROPERTY_IRI));
        final Set<OWLIndividual> addresses = endpoint.getObjectPropertyValues(addressProperty, o);
        for (OWLIndividual address : addresses) {
            if (address.isNamed()) {
                return address.asOWLNamedIndividual().getIRI();
            }
        }
        return null;
    }
    
    public synchronized OWLNamedIndividual getBinding(OWLNamedIndividual service) {
        OWLIndividual endpoint = getServiceEndpoint(service);
        if (endpoint != null) {
            final OWLOntology o = getOntology();
            final OWLObjectProperty bindingProperty = o.getOWLOntologyManager().getOWLDataFactory().getOWLObjectProperty(IRI.create(BINDING_PROPERTY_IRI));
            final Set<OWLIndividual> bindings = endpoint.getObjectPropertyValues(bindingProperty, o);
            for (OWLIndividual binding : bindings) {
                if (binding.isNamed()) {
                    return binding.asOWLNamedIndividual();
                }
            }
        }
        return null;
    }
    
    public boolean isSOAPBinding(OWLIndividual binding) {
        final IRI iri = getBindingType(binding);
        return iri != null && WSOAP.equals(iri.toString());
    }

    public boolean isHTTPBinding(OWLIndividual binding) {
        final IRI iri = getBindingType(binding);
        return iri != null && WHTTP.equals(iri.toString());
    }

    public IRI getBindingType(OWLIndividual binding) {
        final OWLOntology o = getOntology();
        final Set<OWLClassExpression> types = binding.getTypes(o.getOWLOntologyManager().getOntologies());
        for (OWLClassExpression type : types) {
            if (!type.isAnonymous()) {
                return type.asOWLClass().getIRI();
            }
        }
        return null;
    }
    
    /**
     * Returns most frequent SAWSDL annotations found (ordered list) 
     * @param type
     * @return 
     */
    public List<String> getReferences(final String type) {
        final Map<String, Integer> map = new HashMap();
        
        OWLOntology owlOntology = ontology.getOntology();
        OWLAnnotationProperty modelReferenceProperty = OWLManager.getOWLDataFactory().getOWLAnnotationProperty(IRI.create(MODEL_REF_PROPERTY));
        Set<OWLNamedIndividual> individuals = owlOntology.getIndividualsInSignature();
        for (OWLNamedIndividual individual : individuals) {
            final String fragment = individual.getIRI().toURI().getFragment();
            if (fragment != null) {
                if (type == null || type.isEmpty() ||
                   (fragment.length() > type.length() &&
                    fragment.startsWith(type)) &&
                    fragment.charAt(type.length()) == '(') {
                    Set<OWLAnnotation> annotations = individual.getAnnotations(owlOntology, modelReferenceProperty);
                    for (OWLAnnotation annotation : annotations) {
                        OWLAnnotationValue owlAnnotationValue = annotation.getValue();
                        if (owlAnnotationValue instanceof OWLLiteral) {
                            final OWLLiteral value = (OWLLiteral)owlAnnotationValue;
                            String literal = value.getLiteral();
                            Integer count = map.get(literal);
                            if (count == null) {
                                map.put(literal, 1);
                            } else {
                                map.put(literal, count + 1);
                            }
                        }
                    }
                }
            }
        }
        ArrayList<String> references = new ArrayList(map.keySet());
        Collections.sort(references, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                Integer i1 = map.get(s1);
                Integer i2 = map.get(s2);
                final int c = i2.compareTo(i1);
                return c == 0 ? s1.compareToIgnoreCase(s2) : c;
            }
        });
        return references;
    } 

}
