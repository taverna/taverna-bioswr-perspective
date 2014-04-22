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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;

/**
 * @author Dmitry Repchevsky
 */

public class WaitPainter extends Timer implements ActionListener {
    final private static int N_BARS = 8;
    
    private Component c;

    private int tick;

    public WaitPainter(Component c) {
        super(80, null);
        this.c = c;
        this.addActionListener(this);
    }

    public void paint(Graphics g) {
        if (isRunning()) {
            tick = tick > N_BARS ? 0 : tick + 1;

            final int width = c.getWidth();
            final int height = c.getHeight();
            
            final int wStep = width >>> 3;
            final int hStep = height >>> 4;
            
            final int wStart = wStep * 3;
            final int hStart = hStep * 7;
            
            final int bWidth = 2 * wStep / N_BARS;
            final int bHeight = hStep * 2;
            
            final int arc = bWidth / 3;

            for (int i = 0; i < N_BARS; i++) {
                final int bStart = wStart + i * bWidth + (int)(bWidth * .2);
                
                int rgb = Color.HSBtoRGB((float)Math.max(N_BARS + i - tick, N_BARS + tick - i) / N_BARS, 1, 1f) & 0x00FFFFFF | 0x11000000;
                
                g.setColor(Color.LIGHT_GRAY);
                g.drawRoundRect(bStart, hStart, (int)(bWidth * .8), bHeight, arc, arc);

                g.setColor(new Color(rgb, true));
                
                g.fillRoundRect(bStart , hStart, (int)(bWidth * .8), bHeight, arc, arc);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        c.repaint();
    }
}
