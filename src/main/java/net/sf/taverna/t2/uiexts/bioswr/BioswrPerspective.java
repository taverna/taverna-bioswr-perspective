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

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.uiexts.bioswr.ui.util.BioswrIcons;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.Workbench;
import net.sf.taverna.t2.workbench.ui.zaria.PerspectiveSPI;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import uk.org.taverna.commons.services.ServiceRegistry;

/**
 * @author Dmitry Repchevsky
 */

public class BioswrPerspective implements PerspectiveSPI, EventHandler {
    private BioswrPerspectiveComponent component;
    
    private Workbench workbench;
    private ServiceRegistry serviceRegistry;
    
    private EditManager editManager;
    private MenuManager menuManager;
    private SelectionManager selectionManager;

    @Override
    public String getID() {
        return BioswrPerspective.class.getName();
    }

    @Override
    public JComponent getPanel() {
        if (component == null) {
            component = new BioswrPerspectiveComponent(workbench, serviceRegistry, editManager, menuManager, selectionManager);
        }
        return component;
    }

    @Override
    public ImageIcon getButtonIcon() {
        return BioswrIcons.BIOSWR_ICON;
    }

    @Override
    public String getText() {
        return "BioSWR";
        //return "<html><body width='100px'><span style='color:#0080FF'>BioSWR</span></body></html>";
    }

    @Override
    public int positionHint() {
        return 40;
    }

    @Override
    public void handleEvent(Event event) {
        // doo nothing por ahora
    }

    public void setWorkbench(Workbench workbench) {
        this.workbench = workbench;
    }
    
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setSelectionManager(SelectionManager selectionManager) {
        this.selectionManager = selectionManager;
    }

    public void setMenuManager(MenuManager menuManager) {
        this.menuManager = menuManager;
    }

    public void setEditManager(EditManager editManager) {
        this.editManager = editManager;
    }
}
