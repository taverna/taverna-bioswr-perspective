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

import javax.swing.table.DefaultTableCellRenderer;
import net.sf.taverna.t2.uiexts.bioswr.model.BioswrOntology;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.BioswrIcons;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * @author Dmitry Repchevsky
 */

public class ServicePermissionsCellRenderer extends DefaultTableCellRenderer {

    private String username;
    
    public void setUsername(final String username) {
        this.username = username;
    }
    
    @Override
    public void setValue(Object value) {
        if (username != null && !username.isEmpty() && value instanceof OWLNamedIndividual) {
            OWLNamedIndividual service = (OWLNamedIndividual)value;
            BioswrOntology ontology = BioswrOntology.getInstance();

            String owner = ontology.getDefinedBy(service.asOWLNamedIndividual().getIRI());
            setIcon(username.equals(owner) ? BioswrIcons.LOGGED_ICON : BioswrIcons.UNLOGGED_ICON);
        } else {
            setIcon(BioswrIcons.UNLOGGED_ICON);
        }
    }
}
