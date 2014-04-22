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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.util.Arrays;
import java.util.HashSet;
import javax.swing.SwingWorker;
import net.sf.taverna.t2.uiexts.bioswr.model.BioswrOntology;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.semanticweb.owlapi.io.RDFLiteral;
import org.semanticweb.owlapi.io.RDFTriple;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.SimpleRenderer;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

/**
 * @author Dmitry Repchevsky
 */

public class AddAnnotationWorker extends AbstractSparqlAnnotationWorker {

    private final static String INSERT_QUERY = "INSERT DATA { %s %s %s }";
    
    public AddAnnotationWorker(IRI subject, OWLAnnotation annotation, PasswordAuthentication credentials) {
        super(subject, annotation, credentials);
    }

    @Override
    protected OWLAnnotationAssertionAxiom doInBackground() throws Exception {
        OWLAnnotationAssertionAxiom axiom = super.doInBackground();
        
        BioswrOntology ontology = BioswrOntology.getInstance();
        OWLOntology owlOntology = ontology.getOntology();
        OWLOntologyManager manager = owlOntology.getOWLOntologyManager();

        manager.addAxiom(owlOntology, axiom);
        return axiom;
    }    

    @Override
    public String getQueryTemplate() {
        return INSERT_QUERY;
    }
}
