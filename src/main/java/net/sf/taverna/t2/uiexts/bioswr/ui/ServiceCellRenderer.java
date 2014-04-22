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

import net.sf.taverna.t2.uiexts.bioswr.model.BioswrOntology;
import javax.swing.table.DefaultTableCellRenderer;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * @author Dmitry Repchevsky
 */

public class ServiceCellRenderer extends DefaultTableCellRenderer {
    
    private String filter;
    
    public void setFilter(String filter) {
        this.filter = filter;
    }

    @Override
    public void setValue(Object value) {
        if (value instanceof OWLNamedIndividual) {
            OWLNamedIndividual service =(OWLNamedIndividual)value;
            String label = BioswrOntology.getInstance().getLabel(service.getIRI());
            if (label == null) {
                label = service.getIRI().toString();
            }
            if (filter == null || filter.isEmpty()) {
                value = label;
            } else {
                final int l = filter.length();
                if (l > 0) {
                    String label2 = label.toLowerCase();
                    StringBuilder sb = new StringBuilder("<html>");
                    int idx1 = 0;
                    int idx2;
                    while ((idx2 = label2.indexOf(filter, idx1)) >= 0) {
                        sb.append(label.substring(idx1, idx2));
                        idx1 = idx2 + l;
                        sb.append("<span style='color: red'>").append(label.substring(idx2, idx1)).append("</span>");
                    }
                    sb.append(label.substring(idx1)).append("</html>");
                    value = sb;
                }
            }

            String comment = BioswrOntology.getInstance().getComment(service.getIRI());
            if (comment == null) {
                OWLIndividual _interface = BioswrOntology.getInstance().getServiceInterface(service);
                if (_interface.isNamed()) {
                    comment = BioswrOntology.getInstance().getComment(_interface.asOWLNamedIndividual().getIRI());
                }
            }
            setToolTipText(comment);
        } else {
            setToolTipText(null);
        }
        setText(value.toString());
    }
}
