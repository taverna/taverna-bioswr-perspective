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

package net.sf.taverna.t2.uiexts.bioswr.ui.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * @author Dmitry Repchevsky
 */

public class CollapsablePanel extends JPanel 
                              implements ActionListener, MouseListener {
    private final Icon collapseIcon;
    private final Icon expandIcon;
    
    private JButton button;
    private JComponent header;
    
    protected JComponent panel;
    
    public CollapsablePanel(JComponent panel) {
        this(panel, null, true);
    }
    
    public CollapsablePanel(JComponent panel, JComponent header, boolean expanded) {
        super(new GridBagLayout());
        this.panel = panel;
        
        collapseIcon = BioswrIcons.COLLAPSE_ICON;
        expandIcon = BioswrIcons.EXPAND_ICON;
        
        GridBagConstraints c = new GridBagConstraints();
        
        c.gridx = 0;
        c.gridy = 0;
        
        button = new JButton(expanded ? collapseIcon : expandIcon);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.addActionListener(this);

        add(button, c);
        
        
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        
        this.header = header != null ? header : new JSeparator();
        this.header.addMouseListener(this);
        add(this.header, c);
        
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        panel.setVisible(expanded);
        add(panel, c);
    }

    @Override
    public Dimension getMinimumSize() {
        Dimension prf = this.getPreferredSize();
        Dimension min = super.getMinimumSize();

        return new Dimension(min.width, prf.height);
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension prf = this.getPreferredSize();
        Dimension max = super.getMaximumSize();

        return new Dimension(max.width, prf.height);
    }

    /**
     * Expands or collapses the component body.
     * 
     * @param expand - switches the state if null.
     */
    public void setExpanded(Boolean expand) {
        boolean v = expand == null ? !panel.isVisible() : expand;
        panel.setVisible(v);
        button.setIcon(v ? collapseIcon : expandIcon);
        
        revalidate();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        setExpanded(null);
    }
    
    @Override public void mouseClicked(MouseEvent e) {
        setExpanded(null);
    }
    
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}

    @Override
    public void mouseEntered(MouseEvent e) {
        header.setForeground(Color.BLUE);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        header.setForeground(Color.BLACK);
    }
}
