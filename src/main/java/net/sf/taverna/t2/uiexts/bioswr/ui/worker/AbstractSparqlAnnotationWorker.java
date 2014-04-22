package net.sf.taverna.t2.uiexts.bioswr.ui.worker;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;
import javax.swing.SwingWorker;
import net.sf.taverna.t2.uiexts.bioswr.config.BioswrConfig;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;

/**
 * @author Dmitry Repchevsky
 */

public abstract class AbstractSparqlAnnotationWorker extends SwingWorker<OWLAnnotationAssertionAxiom, Object> {
    protected final static String SPARQL_ENDPOINT = BioswrConfig.BIOSWR_SERVER_URI + "/rest/sparql/";
    
    private final IRI subject;
    private final OWLAnnotation annotation;
    private final PasswordAuthentication credentials;

    protected AbstractSparqlAnnotationWorker(IRI subject, OWLAnnotation annotation, PasswordAuthentication credentials) {
        this.subject = subject;
        this.annotation = annotation;
        this.credentials = credentials;
    }
    
    public abstract String getQueryTemplate();
    
    @Override
    protected OWLAnnotationAssertionAxiom doInBackground() throws Exception {
        final String value;
        OWLAnnotationValue owlAnnotationValue = annotation.getValue();
        if (owlAnnotationValue instanceof OWLLiteral) {
            // encode literal value for SPARQL (A.7 Escape sequences in strings)
            final OWLLiteral literal = (OWLLiteral)owlAnnotationValue;
            value = "\"" + encodeLiteral(literal.getLiteral()) + "\"^^" + literal.getDatatype().getIRI().toQuotedString();
        } else {
            value = owlAnnotationValue.toString();
        }
        
        final String query = String.format(getQueryTemplate(), subject.toQuotedString(), 
                annotation.getProperty().getIRI().toQuotedString(), value);
        
        
        HttpClient client = new HttpClient();
        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(credentials.getUserName(), new String(credentials.getPassword())));

        PostMethod post = new PostMethod(SPARQL_ENDPOINT);
        post.setDoAuthentication(true);

        post.setRequestEntity(new StringRequestEntity(query, "application/sparql-update", "UTF-8"));
 	try {
            final int status = client.executeMethod(post);
            if (status != HttpURLConnection.HTTP_OK) {
                throw new IOException(HttpStatus.getStatusText(status));
            }
        } finally {
            post.releaseConnection();
 	}

        OWLDataFactory factory = OWLManager.getOWLDataFactory();
        OWLAnnotation author = factory.getOWLAnnotation(factory.getRDFSIsDefinedBy(), factory.getOWLLiteral(credentials.getUserName()));
        OWLAnnotationAssertionAxiom axiom = factory.getOWLAnnotationAssertionAxiom(subject, annotation, new HashSet(Arrays.asList(author)));

        return axiom;
    }
    
    private static String encodeLiteral(String value) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0, n = value.length(); i < n; i++) {
            final char ch = value.charAt(i);
            switch(ch) {
                case 0x0009: sb.append("\\t");break;
                case 0x000A: sb.append("\\n");break;
                case 0x000D: sb.append("\\r");break;
                case 0x0008: sb.append("\\b");break;
                case 0x000C: sb.append("\\f");break;
                case 0x0022: sb.append("\\\"");break;
                case 0x0027: sb.append("\\\'");break;
                case 0x005C: sb.append("\\\\");break;
                default: sb.append(ch);
            }
        }
        return sb.toString();
    }
}
