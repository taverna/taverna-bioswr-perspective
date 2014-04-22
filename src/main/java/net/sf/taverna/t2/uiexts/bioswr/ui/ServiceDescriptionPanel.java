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

import net.sf.taverna.t2.uiexts.bioswr.ui.util.BioswrIcons;
import java.awt.Component;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.BlockablePanel;
import net.sf.taverna.t2.uiexts.bioswr.ui.worker.GetServiceDescriptionWorker;

/**
 * @author Dmitry Repchevsky
 */

public class ServiceDescriptionPanel extends BlockablePanel {
    
    private URL location;
    private GetServiceDescriptionWorker worker;

    private final JButton button;
    private final JScrollPane scrollPane;
    private final JTextPane text;
    
    public ServiceDescriptionPanel() {
        text = new JTextPane();
        text.setEditable(false);

        scrollPane = new JScrollPane(text);
        scrollPane.getViewport().addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                layoutOverlayPanels();
            }
        });

        button = new JButton(BioswrIcons.WSDL_TAB_ICON);
        button.setBorder(null);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);

        add(scrollPane, JLayeredPane.DEFAULT_LAYER);
        add(button, JLayeredPane.PALETTE_LAYER);
    }

    public void reload() {
        setWSDLLocation(location, true);
    }
    
    public void setWSDLLocation(URL location) {
        setWSDLLocation(location, false);
    }
    
    private void setWSDLLocation(URL location, boolean reload) {
        this.location = location;

        unblock();

        if (worker != null && !worker.isDone()) {
            worker.cancel(false);
        }

        if (location == null) {
            text.setText("");
        } else {
            worker = new GetServiceDescriptionWorker(location, reload);
            worker.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    SwingWorker<String, Object> task = (SwingWorker)evt.getSource();

                    Object value = evt.getNewValue();

                    if (SwingWorker.StateValue.DONE.equals(value)) {
                        try {
                            String description = task.get();
                            text.setText(description);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ServiceDescriptionPanel.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ExecutionException ex) {
                            Logger.getLogger(ServiceDescriptionPanel.class.getName()).log(Level.SEVERE, null, ex);
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
    
    @Override
    public void doLayout() {
        super.doLayout();

        Dimension size = getSize();
        scrollPane.setSize(size);
        scrollPane.doLayout();

        layoutOverlayPanels();
    }

    private void layoutOverlayPanels() {
        Component[] components = getComponentsInLayer(JLayeredPane.DEFAULT_LAYER);
        JScrollPane visible = (JScrollPane)components[0];
        Dimension dim = visible.getViewport().getExtentSize();
        moveButton(dim.width, dim.height);
    }

    private void moveButton(int width, int height) {
        Dimension bPaneSize = button.getPreferredSize();
        button.setBounds(Math.max(3, width - bPaneSize.width - 3), Math.max(3, height - bPaneSize.height - 3), bPaneSize.width, bPaneSize.height);
    }
}
