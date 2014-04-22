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

import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.AbstractTableModel;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Dmitry Repchevsky
 */

public class WSDLTableModel extends AbstractTableModel {

    private final static String[] COL_NAMES = {"", "", "name", "url"};
    
    private final static String SERVICE_IRI = "http://www.w3.org/ns/wsdl-rdf#Service";

    private ArrayList<OWLNamedIndividual> services;

    @Override
    public String getColumnName(int column) {
        return COL_NAMES[column];
    }
    
    @Override
    public int getRowCount() {
        return services == null ? 0 : services.size();
    }

    @Override
    public int getColumnCount() {
        return COL_NAMES.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return services.get(rowIndex);
    }

    public final void setOntology(OWLOntology ontology) {
        services = null;
        

        Set<OWLEntity> entities = ontology.getEntitiesInSignature(IRI.create(SERVICE_IRI), true);
        if (entities.isEmpty()) {
            Logger.getLogger(WSDLTableModel.class.getName()).log(Level.SEVERE, "{0} not WSDL 2.0 RDF ontology", ontology.getOntologyID());
        } else {
            OWLEntity entity = entities.iterator().next();
            if (entity.isOWLClass()) {
                services = new ArrayList<OWLNamedIndividual>();

                OWLClass owlClass = entity.asOWLClass();
                Set<OWLIndividual> individuals = owlClass.getIndividuals(ontology.getOWLOntologyManager().getOntologies());
                for (OWLIndividual individual : individuals) {
                    if (individual.isNamed()) {
                        services.add(individual.asOWLNamedIndividual());
                    }
                }
            } else {
                Logger.getLogger(WSDLTableModel.class.getName()).log(Level.SEVERE, "{0} is not a OWL class", entity.getIRI());
            }
        }
        fireTableDataChanged();
    }
}
