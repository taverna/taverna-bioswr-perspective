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
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolTip;
import javax.swing.OverlayLayout;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.sf.taverna.t2.uiexts.bioswr.model.BioswrOntology;
import net.sf.taverna.t2.uiexts.bioswr.ui.dnd.AnnotationTransferable;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.BioswrIcons;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.BlockablePanel;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.HtmlToolTip;
import net.sf.taverna.t2.uiexts.bioswr.ui.worker.AddAnnotationWorker;
import net.sf.taverna.t2.uiexts.bioswr.ui.worker.RemoveAnnotationWorker;
import org.inb.bsc.wsdl20.IdentifiableComponent;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * @author Dmitry Repchevsky
 */

public class AnnotationsPanel extends BlockablePanel implements DropTargetListener {
    
    public final static String WSDL_DESCRIPTION = "wsdl.description";
    
    private final AnnotationsListModel model;
    private final JList<OWLAnnotationAssertionAxiom> list;
    
    private final Box annotationsPanel;
    private final JScrollPane bScroll;
    private JTextArea comment;
    private final JButton button;
    
    private final AnnotationCellRenderer renderer;
    
    private Font font;
    
    private OWLNamedIndividual service;
    private IdentifiableComponent wsdlComponent;
    private PasswordAuthentication authentication;
    
    public AnnotationsPanel() {
        setLayout(new OverlayLayout(this));
        setDropTarget(new DropTarget(this, this));

        font = UIManager.getFont("TitledBorder.font");
        if (font != null) {
            font = font.deriveFont(font.getSize2D() - 1);
        }
            
        list = new JList(model = new AnnotationsListModel()) {
            @Override
            public JToolTip createToolTip() {
                return new HtmlToolTip(60000);
            }
        };
        list.setOpaque(false);
        list.setCellRenderer(renderer = new AnnotationCellRenderer());
        
        MouseAdapter adapter = new MouseAdapter() {
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (wsdlComponent != null && 
                    authentication != null && 
                    !e.isPopupTrigger()) {
                    final Point p = e.getPoint();
                    if (p.x < renderer.getIcon().getIconWidth() + renderer.getIconTextGap()) {
                        final String userName = authentication.getUserName();
                        final int index = list.locationToIndex(p);
                        OWLAnnotationAssertionAxiom axiom = model.get(index);

                        Set<OWLAnnotation> annotations = axiom.getAnnotations();
                        for (OWLAnnotation annotation : annotations) {
                            final OWLAnnotationProperty property = annotation.getProperty();
                            if (property != null && OWLRDFVocabulary.RDFS_IS_DEFINED_BY.getIRI().equals(property.getIRI())) {
                                OWLAnnotationValue owlValue = annotation.getValue();
                                if (owlValue instanceof OWLLiteral) {
                                    OWLLiteral literal = (OWLLiteral)annotation.getValue();
                                    if (userName.equals(literal.getLiteral())) {
                                        removeAnnotation(axiom.getAnnotation());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            @Override
            public void mouseMoved(MouseEvent e) {
                final Point p = e.getPoint();
                renderer.setSelectionIndex(p.x < renderer.getIcon().getIconWidth() + renderer.getIconTextGap() ? list.locationToIndex(p) : -1);
                list.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                renderer.setSelectionIndex(-1);
                list.repaint();
            }
        };
        list.addMouseListener(adapter);
        list.addMouseMotionListener(adapter);
        
        JScrollPane aScroll = new JScrollPane(list, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        aScroll.setAlignmentX(Component.RIGHT_ALIGNMENT);
        aScroll.setBorder(null);
        
        comment = new JTextArea(3, 0);
        
        bScroll = new JScrollPane(comment);
        bScroll.setOpaque(false);
        bScroll.setVisible(false);
        bScroll.setAlignmentX(Component.RIGHT_ALIGNMENT);
        bScroll.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1), "rdfs:comment", TitledBorder.LEFT, TitledBorder.CENTER, font, Color.DARK_GRAY));
        
        button = new JButton(ResourceBundle.getBundle("resources/messages").getString("button.add.comment"));
        button.setEnabled(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(0,6,0,6));
        button.setAlignmentX(Component.RIGHT_ALIGNMENT);
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addRDFSComment();
            }
        });
        
        comment.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                button.setEnabled(comment.getDocument().getLength() > 0);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                button.setEnabled(comment.getDocument().getLength() > 0);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                button.setEnabled(comment.getDocument().getLength() > 0);
            }
        });

        annotationsPanel = new Box(BoxLayout.Y_AXIS) {
            @Override
            public void paintBorder(Graphics g) {
                super.paintBorder(g);
                g.drawImage(BioswrIcons.SEMANTICS_TINY_ICON.getImage(), 0, 0, 12, 12, null);
            }            
        };
        
        annotationsPanel.add(aScroll);
        annotationsPanel.add(bScroll);
        annotationsPanel.add(button);
    }
    
    public void setService(OWLNamedIndividual service) {
        this.service = service;
    }

    public void reload() {
        setWSDLComponent(wsdlComponent);
    }

    public void setWSDLComponent(IdentifiableComponent wsdlComponent) {
        this.wsdlComponent = wsdlComponent;
        
        if (wsdlComponent == null) {
            model.clear();
            annotationsPanel.setBorder(null);
            remove(annotationsPanel);
        } else {
            showRDFSComments();
                    
            final String id = wsdlComponent.getFragmentIdentifier().toString();
            model.setIndividual(IRI.create(id));
            
            list.setVisibleRowCount(Math.min(4, model.getSize()));
            
            final String title = MessageFormat.format(ResourceBundle.getBundle("resources/messages").getString("border.title.annotations"), id);
            annotationsPanel.setBorder(new TitledBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1), title, TitledBorder.LEFT, TitledBorder.CENTER, font, Color.DARK_GRAY));

            if (getComponentsInLayer(DEFAULT_LAYER).length == 0) {
                add(annotationsPanel, JLayeredPane.DEFAULT_LAYER);
            }
        }
        
        revalidate();
    }
    
    public void setAuthentication(final PasswordAuthentication authentication) {
        this.authentication = authentication;
        renderer.setUsername(authentication == null ? null : authentication.getUserName());
        showRDFSComments();
        revalidate();
    }

    private void showRDFSComments() {
        if (service == null || wsdlComponent == null || authentication == null) {
            bScroll.setVisible(false);
            button.setVisible(false);
        } else {
            final BioswrOntology ontology = BioswrOntology.getInstance();
            final String definedBy = ontology.getDefinedBy(service.getIRI());
            final boolean visible = definedBy != null && definedBy.equals(authentication.getUserName());
            bScroll.setVisible(visible);
            button.setVisible(visible);
        }
    }
    
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
        if (wsdlComponent == null || authentication == null || isBlocked()) {
            dtde.rejectDrag();
        }

        Transferable tr = dtde.getTransferable();
        if (tr.isDataFlavorSupported(AnnotationTransferable.ANNOTATION)) {
            try {
                OWLAnnotation annotation = (OWLAnnotation)tr.getTransferData(AnnotationTransferable.ANNOTATION);
                OWLAnnotationProperty property = annotation.getProperty();
                OWLAnnotationValue value = annotation.getValue();
                
                for (int i = 0, n = model.size(); i < n; i++) {
                    OWLAnnotationAssertionAxiom axiom = model.get(i);
                    if (property.getIRI().equals(axiom.getProperty().getIRI()) &&
                        value.equals(axiom.getValue())) {
                        dtde.rejectDrag();
                    }
                }
            }
            catch (Exception ex) {
                Logger.getLogger(AnnotationsPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {}

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {}

    @Override
    public void dragExit(DropTargetEvent dte) {}

    @Override
    public void drop(DropTargetDropEvent dtde) {
        Transferable tr = dtde.getTransferable();
        if (tr.isDataFlavorSupported(AnnotationTransferable.ANNOTATION)) {
            try {
                OWLAnnotation annotation = (OWLAnnotation)tr.getTransferData(AnnotationTransferable.ANNOTATION);
                addAnnotation(annotation);
            }
            catch (Exception ex) {
                Logger.getLogger(AnnotationsPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return super.getPreferredSize();
    }
            
    private void addRDFSComment() {
        final OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
        final OWLLiteral literal =  dataFactory.getOWLLiteral(comment.getText(), OWL2Datatype.XSD_STRING);
        final OWLAnnotationProperty rdfsCommentProperty = dataFactory.getRDFSComment();
        final OWLAnnotation annotation = dataFactory.getOWLAnnotation(rdfsCommentProperty, literal);
        addAnnotation(annotation);
    }
    
    private void addAnnotation(OWLAnnotation annotation) {
        IRI subject = IRI.create(wsdlComponent.getFragmentIdentifier().toString());
        OWLAnnotationProperty property = annotation.getProperty();
        
        BioswrOntology ontology = BioswrOntology.getInstance();
        OWLOntology owlOntology = ontology.getOntology();
        if (owlOntology != null) {
            // check if the annotation is already in the ontology
            Set<OWLEntity> entities = owlOntology.getEntitiesInSignature(subject);
            for (OWLEntity entity : entities) {
                Set<OWLAnnotation> annotations = entity.getAnnotations(owlOntology, property);
                if (annotations.contains(annotation)) {
                    return;
                }
            }
        }
        
        final AddAnnotationWorker worker = new AddAnnotationWorker(subject, annotation, authentication);
        worker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Object value = evt.getNewValue();

                if (SwingWorker.StateValue.DONE.equals(value)) {
                    try {
                        OWLAnnotationAssertionAxiom axiom = worker.get();
                        if (axiom != null) {
                            comment.setText("");
                            model.add(model.getSize(), axiom);
                            BioswrOntology ontology = BioswrOntology.getInstance();
                            IRI wsdlLocation = ontology.getWSDLLocation(service);
                                if (wsdlLocation == null) {
                                    Logger.getLogger(AnnotationsPanel.class.getSimpleName()).log(Level.WARNING, "no 'wsdlLocation' property is defined for the service {0}", service.getIRI().toString());
                                } else {
                                    // reload annotated wsdl from the server
                                    try {
                                        URL url = wsdlLocation.toURI().toURL();
                                        firePropertyChange(WSDL_DESCRIPTION, null, url);
                                    } catch (MalformedURLException ex) {
                                        Logger.getLogger(AnnotationsPanel.class.getSimpleName()).log(Level.SEVERE, null, ex);
                                    }
                                }                            
                            list.setVisibleRowCount(Math.min(4, model.getSize()));
                            revalidate();
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(AnnotationsPanel.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(AnnotationsPanel.class.getName()).log(Level.SEVERE, null, ex);
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
    
    private void removeAnnotation(final OWLAnnotation annotation) {
        IRI subject = IRI.create(wsdlComponent.getFragmentIdentifier().toString());
        
        final RemoveAnnotationWorker worker = new RemoveAnnotationWorker(subject, annotation, authentication);
        worker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Object value = evt.getNewValue();

                if (SwingWorker.StateValue.DONE.equals(value)) {
                    try {
                        OWLAnnotationAssertionAxiom axiom = worker.get();
                        for (int i = 0, n = model.getSize(); i < n; i++) {
                            OWLAnnotationAssertionAxiom a = model.get(i);
                            if (a.getProperty().equals(axiom.getProperty()) &&
                                a.getValue().equals(axiom.getValue())) {
                                model.remove(i);

                                BioswrOntology ontology = BioswrOntology.getInstance();
                                IRI wsdlLocation = ontology.getWSDLLocation(service);
                                if (wsdlLocation == null) {
                                    Logger.getLogger(AnnotationsPanel.class.getSimpleName()).log(Level.WARNING, "no 'wsdlLocation' property is defined for the service {0}", service.getIRI().toString());
                                } else {
                                    // reload annotated wsdl from the server
                                    try {
                                        URL url = wsdlLocation.toURI().toURL();
                                        firePropertyChange(WSDL_DESCRIPTION, null, wsdlLocation);
                                    } catch (MalformedURLException ex) {
                                        Logger.getLogger(AnnotationsPanel.class.getSimpleName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                list.setVisibleRowCount(Math.min(4, model.getSize()));
                                revalidate();

                                break;
                            }
                        }
                    } catch (InterruptedException ex) {
                        Logger.getLogger(AnnotationsPanel.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ExecutionException ex) {
                        Logger.getLogger(AnnotationsPanel.class.getName()).log(Level.SEVERE, null, ex);
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
