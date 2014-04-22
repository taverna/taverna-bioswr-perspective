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

import java.awt.Component;
import java.awt.Font;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.BioswrIcons;
import org.inb.bsc.wsdl20.ElementDeclaration;
import org.inb.bsc.wsdl20.IdentifiableComponent;
import org.inb.bsc.wsdl20.Interface;
import org.inb.bsc.wsdl20.InterfaceMessageReference;
import org.inb.bsc.wsdl20.InterfaceOperation;
import org.inb.bsc.wsdl20.MessageContentModel;
import org.inb.bsc.wsdl20.MessageDirection;

/**
 * @author Dmitry Repchevsky
 */

public class WSDLTreeCellRenderer extends DefaultTreeCellRenderer {
    @Override
    public Component getTreeCellRendererComponent(JTree tree,
                                         Object value,
                                         boolean sel,
                                         boolean expanded,
                                         boolean leaf,
                                         int row,
                                         boolean hasFocus) {
        if (value != null && value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)value;

            final int level = node.getLevel();
            setFont(tree.getFont().deriveFont(level == 2 ? Font.BOLD : Font.PLAIN));
            
            final IdentifiableComponent component = (IdentifiableComponent) node.getUserObject();
            final String label = WSDLTreeCellRenderer.getLabel(level, component);
            super.getTreeCellRendererComponent(tree, label, sel, expanded, leaf, row, hasFocus);
            setIcon(WSDLTreeCellRenderer.getIcon(level, component));
            
            return this;
        }
        return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    }

    public static ImageIcon getIcon(final int level, final IdentifiableComponent component) {
        switch(level) {
            case 1: return BioswrIcons.SERVICE_ICON;
            case 2: return BioswrIcons.OPERATION_ICON;
            case 3: InterfaceMessageReference message = (InterfaceMessageReference)component;
                    return message.getDirection() == MessageDirection.In ? BioswrIcons.IN_ICON : BioswrIcons.OUT_ICON;
        }
        return null;
    }
    
    public static String getLabel(final int level, final IdentifiableComponent component) {
        switch(level) {
            case 1: Interface _interface = (Interface)component; return _interface.getName().getLocalPart();
            case 2: InterfaceOperation operation = (InterfaceOperation)component; return operation.getName().getLocalPart();
            case 3: InterfaceMessageReference message = (InterfaceMessageReference)component; 
                    ElementDeclaration element = message.getElementDeclaration();
                    if (element == null) {
                        MessageContentModel messageContentModel = message.getMessageContentModel();
                        switch(messageContentModel) {
                            case none: return "{empty content}";
                            case any: return "{unspecified XML content}";
                            case other: return "{non XML content}";
                            default: return "{not found...}";
                        }
                    } else {
                        return element.getName().getLocalPart() + " (" + message.getMessageLabel() + ")";
                    }
             }
        return "";
    }
}
