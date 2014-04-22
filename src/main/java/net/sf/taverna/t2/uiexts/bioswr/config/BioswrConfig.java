package net.sf.taverna.t2.uiexts.bioswr.config;

import java.io.IOException;
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

import java.util.Properties;

/**
 * @author Dmitry Repchevsky
 */

public class BioswrConfig {
    public final static String BIOSWR_SERVER_URI;
    static {
        String uri;
        try {
            final Properties config = new Properties();
            config.load(BioswrConfig.class.getClassLoader().getResourceAsStream("resources/config.properties"));
            uri = config.getProperty("bioswr.server.url");
        } catch (IOException ex) {
            uri = null;
        }
        BIOSWR_SERVER_URI = uri != null ? uri : "http://inb.bsc.es/BioSWR";
    }
}
