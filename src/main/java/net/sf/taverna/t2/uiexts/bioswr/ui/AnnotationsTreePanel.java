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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.ToolTipManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import net.sf.taverna.t2.uiexts.bioswr.model.BioswrOntology;
import net.sf.taverna.t2.uiexts.bioswr.ui.dnd.AnnotationTransferable;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.BioswrIcons;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.BlockablePanel;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.HtmlToolTip;
import net.sf.taverna.t2.uiexts.bioswr.ui.worker.AddAnnotationWorker;
import net.sf.taverna.t2.uiexts.bioswr.ui.worker.GetComponentTypeWorker;
import org.inb.bsc.wsdl20.IdentifiableComponent;
import org.inb.bsc.wsdl20.Interface;
import org.inb.bsc.wsdl20.InterfaceMessageReference;
import org.inb.bsc.wsdl20.InterfaceOperation;
import org.inb.bsc.wsdl20.MessageDirection;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

/**
 * @author Dmitry Repchevsky
 */

public class AnnotationsTreePanel extends BlockablePanel
        implements DragGestureListener, DragSourceListener {
    
    public final static String WSDL_CIMPONENT = "wsdl.component";
    
    private String owner;
    private PasswordAuthentication authentication;
    
    private IdentifiableComponent wsdlComponent;

    private final JTree tree;
    private final AnnotationsTreeModel model;
    
    private final JScrollPane scrollPane;
    private final AnnotationsTreeCellRenderer renderer;
    
    private final JTextField textField;
    private final JLabel warningLabel;
    
    private DragSource dragSource;
    
    private GetComponentTypeWorker worker;
    
    public AnnotationsTreePanel() {
        tree = new JTree(model = new AnnotationsTreeModel()) {
            @Override
            public JToolTip createToolTip() {
                return new HtmlToolTip(60000);
            }
        };
        tree.setCellRenderer(renderer = new AnnotationsTreeCellRenderer());
        tree.setRootVisible(false);
        tree.addMouseListener(new AddAnnotationListener());
        
        dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(tree, DnDConstants.ACTION_MOVE, this);
        
        ToolTipManager.sharedInstance().registerComponent(tree);
        
        scrollPane = new JScrollPane(tree);
        scrollPane.getViewport().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                layoutOverlayPanels();
            }
        });
        
        textField = new JTextField();
        textField.setMargin(new Insets(0,1,0,1));
        textField.setBackground(new Color(0xEF,0xFB,0xFB));
        textField.setVisible(false);
        textField.getDocument().addDocumentListener(new FilterDocumentListener());
        
        warningLabel = new JLabel(BioswrIcons.WARNING_ICON) {
            @Override
            public JToolTip createToolTip() {
                return new HtmlToolTip();
            }
        };
        warningLabel.setVisible(false);
        
        add(scrollPane, JLayeredPane.DEFAULT_LAYER);
        add(textField, JLayeredPane.PALETTE_LAYER);
        add(warningLabel, JLayeredPane.PALETTE_LAYER);
    }
    
    public void setService(OWLNamedIndividual service) {
        owner = BioswrOntology.getInstance().getDefinedBy(service.getIRI());
    }
    
    public void setWSDLComponent(IdentifiableComponent wsdlComponent) {
        this.wsdlComponent = wsdlComponent;
        setWarningIcon();
        
        if (worker != null && !worker.isDone()) {
            worker.cancel(false);
        }
        
        if (wsdlComponent == null) {
            textField.setText(null);
            model.setType(null);
        } else {
            worker = new GetComponentTypeWorker(IRI.create(wsdlComponent.getFragmentIdentifier().toString()));
            worker.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    SwingWorker<String, Object> task = (SwingWorker)evt.getSource();

                    Object value = evt.getNewValue();

                    if (SwingWorker.StateValue.DONE.equals(value)) {
                        try {
                            String type = task.get();
                            if (type == null) {
                                model.setType(null);
                                textField.setText(null);
                            } else if (!type.equals(model.getType())) {
                                model.setType(null);
                                textField.setText(null);
                                model.setType(type);
                            }

                            tree.expandPath(tree.getPathForRow(0));
                            textField.setVisible(true);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(AnnotationsTreePanel.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ExecutionException ex) {
                            Logger.getLogger(AnnotationsTreePanel.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (CancellationException ex) {
                        } finally {
                            unblock();
                        }
                    }
                }
            });
            block();
            worker.execute();
        }
    }
    
    public void setAuthentication(final PasswordAuthentication authentication) {
        this.authentication = authentication;
        setWarningIcon();
    }
    
    private void setWarningIcon() {
        if (wsdlComponent == null) {
            warningLabel.setIcon(null);
            warningLabel.setToolTipText(null);
        } else {
            final String subject = URI.create(wsdlComponent.getFragmentIdentifier().toString()).getFragment();
            if (owner != null && authentication != null && owner.equals(authentication.getUserName())) {
                final String tooltip = MessageFormat.format(ResourceBundle.getBundle("resources/messages")
                                            .getString("label.tooltip.info"), subject);
                warningLabel.setToolTipText(tooltip);
                warningLabel.setIcon(BioswrIcons.SEMANTICS_ICON);
            } else {
                final String tooltip = MessageFormat.format(ResourceBundle.getBundle("resources/messages")
                                            .getString("label.tooltip.no.authentication.warning"), subject);
                warningLabel.setToolTipText(tooltip);
                warningLabel.setIcon(BioswrIcons.WARNING_ICON);
            }            
        }
        warningLabel.setVisible(true);
    }
    
    @Override
    public void doLayout() {
        super.doLayout();

        Dimension size = getSize();
        scrollPane.setSize(size);
        scrollPane.doLayout();

        layoutOverlayPanels();
    }

    @Override
    public void dragGestureRecognized(DragGestureEvent dge) {
        if (owner != null && authentication != null && owner.equals(authentication.getUserName())) {
            Point clickPoint = dge.getDragOrigin();
            TreePath path = tree.getPathForLocation(clickPoint.x, clickPoint.y);
            if (path != null) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                Object object = node.getUserObject();
                if (object instanceof OWLClass) {
                    OWLClass clazz = (OWLClass)object;
                    OWLDataFactory factory = OWLManager.getOWLDataFactory();
                    OWLAnnotationProperty property = factory.getOWLAnnotationProperty(IRI.create("http://www.w3.org/ns/sawsdl#modelReference"));
                    OWLLiteral value = factory.getOWLLiteral(clazz.getIRI().toString());
                    OWLAnnotation annotation = factory.getOWLAnnotation(property, value);
                    Transferable trans = new AnnotationTransferable(annotation);
                    dragSource.startDrag(dge, AnnotationTransferable.SEMANTIC_CURSOR_BLC, trans, this);
                }
            }
        }
    }

    @Override
    public void dragEnter(DragSourceDragEvent dsde) {
        DragSourceContext ctx = dsde.getDragSourceContext();

        ctx.setCursor(AnnotationTransferable.SEMANTIC_CURSOR_STD);
    }

    @Override
    public void dragOver(DragSourceDragEvent dsde) {}

    @Override
    public void dropActionChanged(DragSourceDragEvent dsde) {
    }

    @Override
    public void dragExit(DragSourceEvent dse) {
        DragSourceContext ctx = dse.getDragSourceContext();
        ctx.setCursor(AnnotationTransferable.SEMANTIC_CURSOR_BLC);
    }

    @Override
    public void dragDropEnd(DragSourceDropEvent dsde) {
    }

    private void layoutOverlayPanels() {
        Component[] components = getComponentsInLayer(JLayeredPane.DEFAULT_LAYER);
        JScrollPane visible = (JScrollPane)components[0];
        Dimension dim = visible.getViewport().getExtentSize();
        Dimension textSize = textField.getPreferredSize();
        Dimension labelSize = warningLabel.getPreferredSize();
        
        textField.setBounds(dim.width/2, Math.min(2, dim.height - textSize.height), dim.width/2, textSize.height);
        warningLabel.setBounds(Math.max(2, dim.width - labelSize.width - 2), Math.max(2, dim.height - labelSize.height - 2), labelSize.width, labelSize.height);
    }
    
    private void expandAll(TreePath path) {
        TreeNode node = (TreeNode) path.getLastPathComponent();
        if (!node.isLeaf()) {
            for (int i = 0, n = node.getChildCount(); i < n; i++) {
                TreeNode child = node.getChildAt(i);
                expandAll(path.pathByAddingChild(child));
            }
            tree.expandPath(path);
        }
    }
    
    private void filter(String filter) {
        renderer.setFilter(filter);
        model.setFilter(filter);
        
        final TreePath path = tree.getPathForRow(0);
        if (filter.isEmpty()) {
            tree.expandPath(path);
        } else {
            expandAll(path);
        }
    }
    
    public class FilterDocumentListener implements DocumentListener {
        @Override public void insertUpdate(DocumentEvent e) {
            filter(textField.getText());
        }
        @Override public void removeUpdate(DocumentEvent e) {
            filter(textField.getText());
        }
        @Override public void changedUpdate(DocumentEvent e) {
            filter(textField.getText());
        }
    }
    
    public class AddAnnotationListener extends MouseAdapter {
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
            if (wsdlComponent == null || authentication == null) {
                return;
            }
            
            final TreePath path = tree.getPathForLocation(e.getX(), e.getY());
            if (path != null) {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                if (node != null) {
                    final OWLClass clazz = (OWLClass)node.getUserObject();
                    if (clazz != null) {
                        final BioswrOntology ontology = BioswrOntology.getInstance();
                        final OWLOntology owlOntology = ontology.getOntology();
                        if (owlOntology != null) {
                            final OWLDataFactory factory = owlOntology.getOWLOntologyManager().getOWLDataFactory();
                            final OWLAnnotationProperty property = factory.getOWLAnnotationProperty(IRI.create("http://www.w3.org/ns/sawsdl#modelReference"));
                            final OWLLiteral value = factory.getOWLLiteral(clazz.getIRI().toString());
                            final OWLAnnotation annotation = factory.getOWLAnnotation(property, value);
                            final IRI subject = IRI.create(wsdlComponent.getFragmentIdentifier().toString());
                            final Set<OWLEntity> entities = owlOntology.getEntitiesInSignature(subject);
                            for (OWLEntity entity : entities) {
                                Set<OWLAnnotation> annotations = entity.getAnnotations(owlOntology, property);
                                if (annotations.contains(annotation)) {
                                    return;
                                }
                            }
                            
                            StringBuilder sb = new StringBuilder();
                            sb.append("<html><body>");
                            
                            final int level;
                            if (wsdlComponent instanceof Interface) {
                                level = 1;
                                sb.append(ResourceBundle.getBundle("resources/messages").getString("menu.item.annotate.interface"));
                                sb.append(" <img style='vertical-align: bottom;' src='").append(AnnotationsTreePanel.class.getClassLoader().getResource("/img/service.png")).append("'/>");
                            } else if (wsdlComponent instanceof InterfaceOperation) {
                                level = 2;
                                sb.append(ResourceBundle.getBundle("resources/messages").getString("menu.item.annotate.operation"));
                                sb.append(" <img src='").append(AnnotationsTreePanel.class.getClassLoader().getResource("/img/operation.png")).append("'/>");
                            } else if (wsdlComponent instanceof InterfaceMessageReference) {
                                level = 3;
                                sb.append(ResourceBundle.getBundle("resources/messages").getString("menu.item.annotate.message"));
                                InterfaceMessageReference message = (InterfaceMessageReference)wsdlComponent;
                                sb.append(" <img src='").append(AnnotationsTreePanel.class.getClassLoader()
                                        .getResource(message.getDirection() == MessageDirection.In ? "/img/input.png": "/img/output.png")).append("'/>");
                            } else {
                                level = 0;
                            }
                            sb.append(WSDLTreeCellRenderer.getLabel(level, wsdlComponent)).append("</body></html>");
                            
                            JPopupMenu popup = new JPopupMenu();
                            JMenuItem menuItem01 = new JMenuItem(sb.toString(), BioswrIcons.SAWSDL_ICON);

                            menuItem01.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    final AddAnnotationWorker worker = new AddAnnotationWorker(subject, annotation, authentication);
                                    worker.addPropertyChangeListener(new PropertyChangeListener() {
                                        @Override
                                        public void propertyChange(PropertyChangeEvent evt) {
                                            Object value = evt.getNewValue();

                                            if (SwingWorker.StateValue.DONE.equals(value)) {
                                                try {
                                                    OWLAnnotationAssertionAxiom axiom = worker.get();
                                                    if (axiom != null) {
                                                        firePropertyChange(WSDL_CIMPONENT, null, wsdlComponent);
                                                    }
                                                } catch (InterruptedException ex) {
                                                    Logger.getLogger(AnnotationsPanel.class.getName()).log(Level.SEVERE, null, ex);
                                                } catch (ExecutionException ex) {
                                                    Logger.getLogger(AnnotationsPanel.class.getName()).log(Level.SEVERE, null, ex);
                                                } finally {
                                                    unblock();
                                                }
                                            }
                                        }
                                    });
                                    block();
                                    worker.execute();
                                }
                            });
                            popup.add(menuItem01);
                            popup.show(e.getComponent(), e.getX(), e.getY());
                        }
                    }
                }
            }
        }
    }
}
