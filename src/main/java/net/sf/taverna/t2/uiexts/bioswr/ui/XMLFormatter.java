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

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Dmitry Repchevsky
 */

public class XMLFormatter {

    public static void format(InputSource input, Result result) throws SAXException, TransformerConfigurationException, TransformerException {
        XMLReader filter = new XMLFilterImpl(XMLReaderFactory.createXMLReader()) {
            
            @Override
            public void startDocument()
                    throws SAXException {
                super.startDocument();
                super.characters(new char[] {'\n'}, 0, 1);
            }
            
            @Override
            public void ignorableWhitespace(char[] ch, int start, int length)
                    throws SAXException {
                // skip whitespaces
            }
            @Override
            public void characters(char[] ch, int start, int length)
                    throws SAXException {
                for (int i = start, n = start + length; i < n; i++) {
                    if (!Character.isWhitespace(ch[i])) {
                        super.characters(ch, start, length);
                        break;
                    }
                }
            }
        };
        SAXSource source = new SAXSource(filter, input);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.transform(source, result);

    }
}
