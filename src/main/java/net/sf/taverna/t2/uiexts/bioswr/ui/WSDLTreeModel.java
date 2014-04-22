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

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import org.inb.bsc.wsdl20.Interface;
import org.inb.bsc.wsdl20.InterfaceMessageReference;
import org.inb.bsc.wsdl20.InterfaceOperation;
import org.inb.bsc.wsdl20.Service;

/**
 * @author Dmitry Repchevsky
 */

public class WSDLTreeModel extends DefaultTreeModel {

    public WSDLTreeModel() {
        super(new DefaultMutableTreeNode());
    }
    
    public void setService(Service service) {
        DefaultMutableTreeNode raiz = (DefaultMutableTreeNode) super.root;
        raiz.removeAllChildren();
        raiz.setUserObject(service);
        
        Interface _interface = service.getInterface();
        DefaultMutableTreeNode interfaceNode = new DefaultMutableTreeNode(_interface);
        raiz.add(interfaceNode);

        for (InterfaceOperation operation : _interface.getAllInterfaceOperations()) {
            DefaultMutableTreeNode operationNode = new DefaultMutableTreeNode(operation);
            interfaceNode.add(operationNode);

            for (InterfaceMessageReference input : operation.getInputs()) {
                operationNode.add(new DefaultMutableTreeNode(input));
            }

            for (InterfaceMessageReference output : operation.getOutputs()) {
                operationNode.add(new DefaultMutableTreeNode(output));
            }
        }

        nodeStructureChanged(root);
    }
}
