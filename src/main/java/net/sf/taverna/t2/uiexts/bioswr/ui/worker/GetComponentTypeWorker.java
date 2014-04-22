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

package net.sf.taverna.t2.uiexts.bioswr.ui.worker;

import java.util.Set;
import javax.swing.SwingWorker;
import net.sf.taverna.t2.uiexts.bioswr.model.BioswrOntology;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Dmitry Repchevsky
 */

public class GetComponentTypeWorker extends SwingWorker<String, Object> {

    private final IRI wsdlComponent; 
    
    public GetComponentTypeWorker(IRI wsdlComponent) {
        this.wsdlComponent = wsdlComponent;
    }
    
    @Override
    protected String doInBackground() throws Exception {
        OWLOntology ontology = BioswrOntology.getInstance().getOntology();
        Set<OWLEntity> entities = ontology.getEntitiesInSignature(wsdlComponent, true);
        if (!entities.isEmpty()) {
            OWLEntity entity = entities.iterator().next();
            if (entity.isOWLNamedIndividual()) {
                OWLNamedIndividual individual = entity.asOWLNamedIndividual();
                Set<OWLClassExpression> types = individual.getTypes(ontology);
                for (OWLClassExpression type : types) {
                    if (!type.isAnonymous()) {
                        return type.asOWLClass().getIRI().toString();
                    }
                }
            }
        }
        return null;
    }
}