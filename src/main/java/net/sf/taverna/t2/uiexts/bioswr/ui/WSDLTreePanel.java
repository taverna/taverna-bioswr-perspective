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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import net.sf.taverna.t2.activities.wsdl.servicedescriptions.WSDLServiceDescription;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.uiexts.bioswr.model.BioswrOntology;
import net.sf.taverna.t2.uiexts.bioswr.model.HTTPBindingTemplateService;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.BioswrIcons;
import net.sf.taverna.t2.uiexts.bioswr.ui.worker.GetServiceDescriptionWorker;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.Workbench;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;
import org.inb.bsc.wsdl20.Binding;
import org.inb.bsc.wsdl20.BindingOperation;
import org.inb.bsc.wsdl20.BindingType;
import org.inb.bsc.wsdl20.Description;
import org.inb.bsc.wsdl20.Endpoint;
import org.inb.bsc.wsdl20.InterfaceOperation;
import org.inb.bsc.wsdl20.Service;
import org.inb.bsc.wsdl20.factory.WSDL2Factory;
import org.inb.bsc.wsdl20.xml.WSDL2Reader;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.xml.sax.InputSource;
import uk.org.taverna.commons.services.ServiceRegistry;

/**
 * @author Dmitry Repchevsky
 */

public class WSDLTreePanel extends JTree {
        
    private final Workbench workbench;
    private final ServiceRegistry serviceRegistry;
    private final EditManager editManager;
    private final MenuManager menuManager;
    private final SelectionManager selectionManager;
    
    private GetServiceDescriptionWorker worker;
    
    public WSDLTreePanel(Workbench workbench, ServiceRegistry serviceRegistry, 
                    EditManager editManager, MenuManager menuManager, SelectionManager selectionManager) {
        super(new WSDLTreeModel());
        setRootVisible(false);
        setCellRenderer(new WSDLTreeCellRenderer());
        
        this.workbench = workbench;
        this.serviceRegistry = serviceRegistry;
        this.editManager = editManager;
        this.menuManager = menuManager;
        this.selectionManager = selectionManager;
        
        addMouseListener(new AddOperationListener());
    }

    public void setService(OWLNamedIndividual service) {
        if (worker != null && !worker.isDone()) {
            worker.cancel(false);
        }
        
        BioswrOntology ontology = BioswrOntology.getInstance();
        
        final IRI location = ontology.getWSDLLocation(service);
        if (location == null) {
            ((WSDLTreeModel)getModel()).setService(null);
        } else {
            URL wsdlLocation;
            try {
                wsdlLocation = location.toURI().toURL();
            } catch (MalformedURLException ex) {
                Logger.getLogger(WSDLTreePanel.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            final String serviceName = ontology.getLabel(service.getIRI());
            
            worker = new GetServiceDescriptionWorker(wsdlLocation, false);
            worker.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    SwingWorker<String, Object> task = (SwingWorker)evt.getSource();

                    Object value = evt.getNewValue();

                    if (SwingWorker.StateValue.DONE.equals(value)) {
                        try {
                            String wsdl2 = task.get();
                            
                            WSDL2Factory factory = WSDL2Factory.newInstance();
                            WSDL2Reader reader = factory.getWSLD2Reader();
                            InputSource source = new InputSource(new StringReader(wsdl2));
                            source.setSystemId(location.toString());
                            Description description = reader.read(source);
                            
                            Service service = description.getService(new QName(serviceName));
                            ((WSDLTreeModel)getModel()).setService(service);
                            
                            expandAll(getPathForRow(0));
                        } catch (Exception ex) {
                            Logger.getLogger(WSDLTreePanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            worker.execute();
        }
    }
    
    private void expandAll(TreePath path) {
        TreeNode node = (TreeNode) path.getLastPathComponent();
        if (node.isLeaf()) {
            this.setExpandedState(path, true);
        } else {
            for (int i = 0, n = node.getChildCount(); i < n; i++) {
                TreeNode child = node.getChildAt(i);
                expandAll(path.pathByAddingChild(child));
            }
        }
    }
            
    public class AddOperationListener extends MouseAdapter {
        
        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup(e);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup(e);
            }
        }

        private void popup(MouseEvent e) {
            final TreePath path = getPathForLocation(e.getX(), e.getY());
            // check if it is an operation (the third level - root/interface(operation)
            if (path != null && path.getPathCount() == 3) {

                final DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                if (node != null) {
                    final InterfaceOperation operation = (InterfaceOperation)node.getUserObject();
                    final QName operationName = operation.getName();

                    JPopupMenu popup = new JPopupMenu();
                    JMenuItem menuItem01 = new JMenuItem(
                            MessageFormat.format(ResourceBundle.getBundle("resources/messages").getString("menu.item.add.operation"), 
                                    operationName.getLocalPart()), BioswrIcons.WORKFLOW_ICON);

                    menuItem01.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            final DefaultMutableTreeNode root = (DefaultMutableTreeNode)getModel().getRoot();
                            final Service service = (Service)root.getUserObject();
                            Description description = service.getParentElement();
                            URI documentBaseURI = description.getDocumentBaseURI();
                            List<Endpoint> endpoints = service.getEndpoints();
                            for (Endpoint endpoint : endpoints) {
                                Binding binding = endpoint.getBinding();
                                URI bindingType = binding.getType();
                                if (bindingType != null) {
                                    BindingOperation bindingOperation = binding.getBindingOperation(operationName);
                                    if (BindingType.SOAP.URI.equals(bindingType)) {
                                        WSDLServiceDescription item = new WSDLServiceDescription(null);
                                        item.setOperation(operationName.getLocalPart());
                                        item.setURI(documentBaseURI);
                                        WorkflowView.importServiceDescription(item, false, editManager, menuManager, selectionManager, serviceRegistry);
                                    } else if (BindingType.HTTP.URI.equals(bindingType)) {
                                        HTTPBindingTemplateService item = new HTTPBindingTemplateService(bindingOperation);
                                        WorkflowView.importServiceDescription(item, false, editManager, menuManager, selectionManager, serviceRegistry);
                                    } else {
                                        continue;
                                    }
                                    break;
                                }
                            }
//                                selectionManager.setSelectedPerspective(workbench);
                        }
                    });

                    popup.add(menuItem01);
                    popup.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        }
    }
}
