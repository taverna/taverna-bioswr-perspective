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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Dmitry Repchevsky
 */

public class ServiceDescriptionModel {
    
    private final static ConcurrentHashMap<URL, SoftReference<String>> descriptions = new ConcurrentHashMap<URL, SoftReference<String>>();
    
    public static String getWSDL20(URL location) throws IOException, MalformedURLException {
        return getWSDL20(location, false);
    }
    
    public static String getWSDL20(URL location, boolean reload) throws IOException, MalformedURLException {
        if (!reload) {
            final SoftReference<String> ref = descriptions.get(location);
            if (ref != null){
                final String description = ref.get();
                if (description != null) {
                    return description;
                }
            }
        }
        synchronized(ServiceDescriptionModel.class) {
            HttpURLConnection connection = (HttpURLConnection)location.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/wsdl+xml");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            try {
                StringBuilder sb = new StringBuilder();
                String ln;
                while((ln = reader.readLine()) != null) {
                    sb.append(ln).append('\n');
                }
                String description = sb.toString();
                descriptions.put(location, new SoftReference<String>(description));
                return description;
            } finally {
                reader.close();
            }
        }
    }
    
    public static void clean(URL location) {
        if (location != null) {
            descriptions.remove(location);
        }
    }
}
