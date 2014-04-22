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
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JLayeredPane;
import javax.swing.JTextField;
import javax.swing.text.PlainDocument;

/**
 * @author Dmitry Repchevsky
 */

public abstract class AbstractFilterCellHeader extends JLayeredPane {
    
    protected final JTextField editorComponent;
    
    public AbstractFilterCellHeader(PlainDocument document) {
        setOpaque(false);
        
        editorComponent = new JTextField(document, "", 10);
        editorComponent.setBackground(new Color(0xEF,0xFB,0xFB));
        editorComponent.setMaximumSize(editorComponent.getPreferredSize());
        editorComponent.setMargin(new Insets(0,1,0,1));

        add(editorComponent, Integer.valueOf(1)); // add the editor over the renderer
    }
    
    @Override
    public boolean isOptimizedDrawingEnabled() {
        return false;
    }

    @Override
    public void validate() {}
    
    @Override
    public void invalidate() {}
    
    
    @Override
    public void repaint() {}
    @Override
    public void repaint(Rectangle r) {}
    @Override
    public void repaint(long tm, int x, int y, int width, int height) {}
}
