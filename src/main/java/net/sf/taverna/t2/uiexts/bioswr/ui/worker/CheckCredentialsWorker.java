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

import java.net.HttpURLConnection;
import java.net.URI;
import javax.swing.SwingWorker;
import net.sf.taverna.t2.uiexts.bioswr.config.BioswrConfig;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * @author Dmitry Repchevsky
 */

public class CheckCredentialsWorker extends SwingWorker<Boolean, Object> {
    
    private final static URI uri = URI.create(BioswrConfig.BIOSWR_SERVER_URI + "/rest/service/deregister/00000000000000000000000000000000");
        
    private final String username;
    private final String password;
    
    //private final String authorization;

    public CheckCredentialsWorker(String username, String password) {
        this.username = username;
        this.password = password;

//        String credentials = username + ":" + password;        
//        authorization = "Basic " + DatatypeConverter.printBase64Binary(credentials.getBytes());
    }

    @Override
    protected Boolean doInBackground() throws Exception {
        HttpClient client = new HttpClient();
        client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

        GetMethod get = new GetMethod(uri.toString());
        get.setDoAuthentication(true);

 	try {
            final int status = client.executeMethod( get ); 	
            return status != HttpURLConnection.HTTP_UNAUTHORIZED;
 	} finally {
            get.releaseConnection();
 	}
 
//        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
//        connection.setAllowUserInteraction(false);
//        connection.setRequestProperty ("Authorization", authorization);
//        connection.connect();
//        return connection.getResponseCode() != HttpURLConnection.HTTP_UNAUTHORIZED;
    }
}
