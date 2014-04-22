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
import java.awt.Dimension;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ResourceBundle;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.BioswrIcons;
import net.sf.taverna.t2.uiexts.bioswr.ui.worker.CheckCredentialsWorker;

/**
 * @author Dmitry Repchevsky
 */

public class LoginPanel extends Box implements DocumentListener {
    
    public final static String AUTHORIZED = "authorized";
    
    private final Color RED = new Color(0xFF,0xEE,0xEE);
    private final Color GREEN = new Color(0xEE,0xFF,0xEE);
    
    private final JTextField username;
    private final JPasswordField password;
    private final JLabel label;
    
    private boolean authenticated;
    
    private CheckCredentialsWorker worker;
    
    public LoginPanel() {
        super(BoxLayout.X_AXIS);
        
        username = new JTextField(16);
        password = new JPasswordField(16);
        label = new JLabel(BioswrIcons.UNLOGGED_ICON);
        
        final Insets insets = new Insets(0,1,0,1);

        username.setMargin(insets);
        password.setMargin(insets);
        
        username.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 4, 1, 2), username.getBorder()));
        password.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 4, 1, 2), password.getBorder()));
        
        username.getDocument().addDocumentListener(this);
        password.getDocument().addDocumentListener(this);

        add(new JLabel(ResourceBundle.getBundle("resources/messages").getString("label.user")));
        add(username);
        
        add(new JLabel(ResourceBundle.getBundle("resources/messages").getString("label.password")));
        add(password);
        
        add(label);
    }

    public String getUsername() {
        return username.getText();
    }
    
    public String getPassword() {
        return new String(password.getPassword());
    }
    
    public boolean isAuthenticated() {
        return authenticated;
    }
    
    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }
    
    @Override
    public void insertUpdate(DocumentEvent e) {
        checkCredentials();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        checkCredentials();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        checkCredentials();
    }
    
    private void checkCredentials() {
        if (worker != null && !worker.isDone()) {
            worker.cancel(false);
        }
        
        worker = new CheckCredentialsWorker(getUsername(), getPassword());
        worker.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                SwingWorker<Boolean, Object> task = (SwingWorker)evt.getSource();

                Object value = evt.getNewValue();

                if (SwingWorker.StateValue.DONE.equals(value)) {
                    try {
                        final Boolean isAuthenticated = task.get();
                        final boolean authenticated = isAuthenticated != null && isAuthenticated;
                        
                        if (LoginPanel.this.authenticated != authenticated) {
                            LoginPanel.this.firePropertyChange(AUTHORIZED, LoginPanel.this.authenticated, authenticated);
                            LoginPanel.this.authenticated = authenticated;
                            
                            username.setBackground(authenticated ? GREEN : RED);
                            password.setBackground(authenticated ? GREEN : RED);
                            label.setIcon(authenticated ? BioswrIcons.LOGGED_ICON : BioswrIcons.UNLOGGED_ICON);
                        }
                    } catch (ExecutionException ex) {
                        Logger.getLogger(LoginPanel.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LoginPanel.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (CancellationException ex) {}
                }
            }
        });

        worker.execute();
    }
}
