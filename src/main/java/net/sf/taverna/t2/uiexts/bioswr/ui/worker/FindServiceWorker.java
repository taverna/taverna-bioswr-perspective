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

import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import net.sf.taverna.t2.uiexts.bioswr.config.BioswrConfig;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author Dmitry Repchevsky
 */

public class FindServiceWorker extends SwingWorker<URL, Object> {
    private final static String WSDL_LOCATION_IRI = "http://www.w3.org/ns/wsdl-instance#wsdlLocation";
    
    private final String iri;
    public FindServiceWorker(String iri) {
        this.iri = iri;
    }
    
    @Override
    protected URL doInBackground() throws Exception {

        StringBuilder url = new StringBuilder(BioswrConfig.BIOSWR_SERVER_URI + "/rest/sparql/?query=");
        StringBuilder query = new StringBuilder();
        query.append("DESCRIBE <").append(iri).append(">");
        url.append(URLEncoder.encode(query.toString(), "UTF-8"));
        
        OWLOntologyManager m = OWLManager.createOWLOntologyManager();
        OWLOntology o = m.loadOntology(IRI.create(url.toString()));
        
        Set<OWLEntity> entities = o.getEntitiesInSignature(IRI.create(iri));
        if (entities.isEmpty()) {
            Logger.getLogger(FindServiceWorker.class.getName()).log(Level.SEVERE, "no service found : {0}", iri);
        } else {
            OWLEntity entity = entities.iterator().next();
            OWLAnnotationProperty locationProperty = o.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(IRI.create(WSDL_LOCATION_IRI));
            Set<OWLAnnotation> locations = entity.getAnnotations(o, locationProperty);
            if (locations.isEmpty()) {
                Logger.getLogger(FindServiceWorker.class.getName()).log(Level.SEVERE, "no location found for service : {0}", iri);
            } else {
                OWLAnnotation location = locations.iterator().next();
                OWLAnnotationValue locationValue = location.getValue();
                if (locationValue instanceof IRI) {
                    final IRI iri = (IRI)locationValue;
                    return iri.toURI().toURL();
                }
            }
        }
        return null;
    }    
}
