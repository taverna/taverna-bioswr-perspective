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
import java.awt.Graphics;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JToolTip;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.DecoratorBorder;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.HtmlToolTip;

/**
 * @author Dmitry Repchevsky
 */

public class TagLabel extends JLabel {
    
    public final static Color BACKGROUND = new Color(0xE0ECF8);
    public final static Color BACKGROUND_HIGHLITED = new Color(0xA3CEF8);
    public final static Color BACKGROUND_SELECTED = new Color(0xF6E3CE);
    
    public final static Color FOREGROUND = new Color(0x407EED);
    
    public final String reference;
    
    public boolean selected;
    
    public TagLabel(String reference) {
        this.reference = reference;
        
        setForeground(FOREGROUND);
        setBackground(BACKGROUND);
        setBorder(new DecoratorBorder(Color.LIGHT_GRAY, 10, new Insets(2,2,2,2)));
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    @Override
    public JToolTip createToolTip() {
        return new HtmlToolTip();
    }
        
    @Override
    public void paint(Graphics g) {
        paintBorder(g);
        g.setColor(selected ? BACKGROUND_SELECTED : getBackground());
        g.fillRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1, 10, 10);
        paintComponent(g);
    }
}
