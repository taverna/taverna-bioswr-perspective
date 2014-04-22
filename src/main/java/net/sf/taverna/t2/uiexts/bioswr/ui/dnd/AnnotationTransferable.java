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

package net.sf.taverna.t2.uiexts.bioswr.ui.dnd;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.BioswrIcons;
import org.semanticweb.owlapi.model.OWLAnnotation;

/**
 * @author Dmitry Repchevsky
 */

public class AnnotationTransferable implements Transferable {
    
    public final static Cursor SEMANTIC_CURSOR_STD = Toolkit.getDefaultToolkit().createCustomCursor(BioswrIcons.SEMANTICS_CURSOR_STD_ICON.getImage(), new Point(0, 0), "annotation_cursor");
    public final static Cursor SEMANTIC_CURSOR_BLC = Toolkit.getDefaultToolkit().createCustomCursor(BioswrIcons.SEMANTICS_CURSOR_BLC_ICON.getImage(), new Point(0, 0), "annotation_blocked_cursor");
    
    public final static DataFlavor ANNOTATION = new DataFlavor(OWLAnnotation.class, "application/java-object; class=org.semanticweb.owlapi.model.OWLAnnotation");

    private final static DataFlavor[] FLAVORS = { ANNOTATION };

    private OWLAnnotation annotation;
    private DataFlavor flavor;

    public AnnotationTransferable(OWLAnnotation annotation) {
        this.annotation = annotation;
    }

    public AnnotationTransferable(OWLAnnotation annotation, DataFlavor flavor) {
        this(annotation);
        this.flavor = flavor;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return flavor != null ? new DataFlavor[] {flavor} : FLAVORS;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (this.flavor != null) {
            return this.flavor.equals(flavor);
        }
        return ANNOTATION.equals(flavor);
    }

    @Override
    public OWLAnnotation getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (isDataFlavorSupported(flavor)) {
            return annotation;
        }
        throw new UnsupportedFlavorException(flavor);
    }
    
    public static Cursor getSemanticCursor() {
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension dim = toolkit.getBestCursorSize(32, 32);
        BufferedImage img = new BufferedImage(dim.width,dim.height, BufferedImage.TYPE_INT_ARGB);
        img.createGraphics().drawImage(BioswrIcons.SEMANTICS_CURSOR_STD_ICON.getImage(), 0, 0, dim.width, dim.height, null);
        return Toolkit.getDefaultToolkit().createCustomCursor(img, new Point(0, 0), "annotation_cursor");
    }
}
