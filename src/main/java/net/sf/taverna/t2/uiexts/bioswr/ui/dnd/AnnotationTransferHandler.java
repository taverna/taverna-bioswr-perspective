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

package net.sf.taverna.t2.uiexts.bioswr.ui.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * @author Dmitry Repchevsky
 */

public class AnnotationTransferHandler extends TransferHandler {

    @Override
    protected Transferable createTransferable(JComponent c) {
        if (c instanceof JTree) {
            JTree tree = (JTree)c;
            Object o = tree.getLastSelectedPathComponent();
            if (o instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)o;

                Object object = node.getUserObject();
                if (object instanceof OWLClass) {
                    OWLClass clazz = (OWLClass)object;
                    OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
                    OWLDataFactory factory = manager.getOWLDataFactory();
                    OWLAnnotationProperty propery = factory.getOWLAnnotationProperty(IRI.create("http://www.w3.org/ns/sawsdl#modelReference"));
                    OWLLiteral value = factory.getOWLLiteral(clazz.getIRI().toString());
                    OWLAnnotation annotation = factory.getOWLAnnotation(propery, value);
                    return new AnnotationTransferable(annotation);
                }
            }
        }
        return null;
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        for(DataFlavor flavor : support.getDataFlavors()) {
            if (AnnotationTransferable.ANNOTATION.equals(flavor)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }
}
