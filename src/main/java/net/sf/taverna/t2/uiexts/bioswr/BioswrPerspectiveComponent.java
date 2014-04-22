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

package net.sf.taverna.t2.uiexts.bioswr;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.uiexts.bioswr.model.BioswrOntology;
import net.sf.taverna.t2.uiexts.bioswr.ui.AnnotationsPanel;
import net.sf.taverna.t2.uiexts.bioswr.ui.AnnotationsTreePanel;
import net.sf.taverna.t2.uiexts.bioswr.ui.LoginPanel;
import net.sf.taverna.t2.uiexts.bioswr.ui.ServiceDescriptionPanel;
import net.sf.taverna.t2.uiexts.bioswr.ui.WSDLTablePane;
import net.sf.taverna.t2.uiexts.bioswr.ui.WSDLTreePanel;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.CollapsablePanel;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.Workbench;
import org.inb.bsc.wsdl20.IdentifiableComponent;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import uk.org.taverna.commons.services.ServiceRegistry;

/**
 * @author Dmitry Repchevsky
 */

public class BioswrPerspectiveComponent extends JPanel {

    private JLabel header;
    private final LoginPanel loginPanel;
    private final AnnotationsPanel annotationsPanel;
    final ServiceDescriptionPanel serviceDescriptionPane;
    private CollapsablePanel collapsablePanel;
    
    public BioswrPerspectiveComponent(Workbench workbench, ServiceRegistry serviceRegistry, 
                                      EditManager editManager, MenuManager menuManager, SelectionManager selectionManager) {
        super (new GridBagLayout());

        serviceDescriptionPane = new ServiceDescriptionPanel();
        serviceDescriptionPane.setPreferredSize(new Dimension(Integer.MAX_VALUE, 200));

        annotationsPanel = new AnnotationsPanel();
        
        // AnnotationsPanel fires "wsdl.description" change event when annotations are changed
        annotationsPanel.addPropertyChangeListener("wsdl.description", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                serviceDescriptionPane.reload();
            }
        });
        final WSDLTablePane wsdlTable = new WSDLTablePane();
        
        final AnnotationsTreePanel annotationsTreePanel = new AnnotationsTreePanel();

        // AnnotationsTreePanel fires "wsdl.component" change event when annotation is added
        annotationsTreePanel.addPropertyChangeListener("wsdl.component", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                annotationsPanel.reload();
                serviceDescriptionPane.reload();
            }
        });
                
        final WSDLTreePanel wsdlTreePanel = new WSDLTreePanel(workbench, serviceRegistry, editManager, menuManager, selectionManager);
        wsdlTreePanel.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getNewLeadSelectionPath();
                if (path == null) {
                    annotationsPanel.setWSDLComponent(null);
                    annotationsTreePanel.setWSDLComponent(null);
                } else {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    IdentifiableComponent wsdlComponent = (IdentifiableComponent)node.getUserObject();
                    annotationsPanel.setWSDLComponent(wsdlComponent);
                    annotationsTreePanel.setWSDLComponent(wsdlComponent);
                }
            }
        });

        JSplitPane hSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        hSplit.setContinuousLayout(true);
        hSplit.setDividerSize(2);
        hSplit.setResizeWeight(0.8);
        hSplit.setDividerLocation(0.8);

        hSplit.setLeftComponent(wsdlTable);
        
        JSplitPane vSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        vSplit.setContinuousLayout(true);
        vSplit.setDividerSize(2);
        vSplit.setResizeWeight(0.5);
        vSplit.setDividerLocation(0.5);
        vSplit.setTopComponent(wsdlTreePanel);
        vSplit.setBottomComponent(annotationsTreePanel);
        
        hSplit.setRightComponent(vSplit);
        
        header = new JLabel("", JLabel.RIGHT);
        collapsablePanel = new CollapsablePanel(serviceDescriptionPane, header, false);

        loginPanel = new LoginPanel();
        loginPanel.addPropertyChangeListener(LoginPanel.AUTHORIZED, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Boolean isAuthorized = (Boolean)evt.getNewValue();

                wsdlTable.setUsername(isAuthorized != null && isAuthorized ? loginPanel.getUsername() : null);
                
                PasswordAuthentication authentication = isAuthorized != null && isAuthorized ? new PasswordAuthentication(loginPanel.getUsername(), loginPanel.getPassword().toCharArray()) : null;
                annotationsTreePanel.setAuthentication(authentication);
                annotationsPanel.setAuthentication(authentication);
            }
        });
                
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        add(loginPanel, c);
        
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        add(hSplit, c);

        c.gridy = 2;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(annotationsPanel, c);

        c.gridy = 3;
        add(collapsablePanel, c);
        
        wsdlTable.table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if(!e.getValueIsAdjusting()) {
                    collapsablePanel.setExpanded(true);

                    final int idx = wsdlTable.table.getSelectedRow();
                    if (idx >= 0) {
                        final OWLNamedIndividual service = (OWLNamedIndividual)wsdlTable.table.getValueAt(idx, 1);
                        
                        wsdlTreePanel.setService(service);
                        annotationsTreePanel.setService(service);
                        annotationsPanel.setService(service);
                        
                        BioswrOntology ontology = BioswrOntology.getInstance();
                        IRI wsdlLocation = ontology.getWSDLLocation(service);
                        if (wsdlLocation == null) {
                            header.setText(null);
                            annotationsPanel.setService(service);
                            serviceDescriptionPane.setWSDLLocation(null);
                        } else {
                            header.setText(wsdlLocation.toString());
                            try {
                                final URL url = wsdlLocation.toURI().toURL();
                                serviceDescriptionPane.setWSDLLocation(url);
                            } catch (MalformedURLException ex) {
                                serviceDescriptionPane.setWSDLLocation(null);
                                Logger.getLogger(BioswrPerspectiveComponent.class.getName()).log(Level.WARNING, "wsdlLocation value must be an URL", ex);
                            }
                        }
                    }
                }
            }
        });
    }
}
