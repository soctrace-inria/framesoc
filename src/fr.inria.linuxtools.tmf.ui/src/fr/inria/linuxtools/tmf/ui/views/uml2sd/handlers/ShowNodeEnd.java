/**********************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation, Ericsson
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Bernd Hufmann - Updated for TMF
 **********************************************************************/

package fr.inria.linuxtools.tmf.ui.views.uml2sd.handlers;

import java.util.Iterator;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;

import fr.inria.linuxtools.internal.tmf.ui.Activator;
import fr.inria.linuxtools.internal.tmf.ui.ITmfImageConstants;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.SDView;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.SDWidget;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.core.GraphNode;

/**
 * Action class implementation to show end of a graph node.
 *
 * @version 1.0
 * @author sveyrier
 */
public class ShowNodeEnd extends BaseSDAction {

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public ShowNodeEnd() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param view The sequence diagram view reference
     * @since 2.0
     */
    public ShowNodeEnd(SDView view) {
        super(view);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_NODE_END));
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void run() {
        if (getView() == null) {
            return;
        }

        SDWidget sdWidget = getView().getSDWidget();

        if (sdWidget == null) {
            return;
        }

        ISelectionProvider selProvider = sdWidget.getSelectionProvider();
        ISelection sel = selProvider.getSelection();
        Object selectedNode = null;

        Iterator<Object> it = ((StructuredSelection) sel).iterator();
        while (it.hasNext()) {
            selectedNode = it.next();
        }

        if (selectedNode != null) {
            GraphNode node = (GraphNode) selectedNode;
            if ((node.getX() + node.getWidth()) * sdWidget.getZoomFactor() < sdWidget.getContentsX() + sdWidget.getVisibleWidth() / 2) {
                sdWidget.ensureVisible(Math.round((node.getX() + node.getWidth()) * sdWidget.getZoomFactor()) - sdWidget.getVisibleWidth() / 2, Math.round((node.getY() + node.getHeight()) * sdWidget.getZoomFactor()));
            } else {
                sdWidget.ensureVisible(Math.round((node.getX() + node.getWidth()) * sdWidget.getZoomFactor() + sdWidget.getVisibleWidth() / (float) 2), Math.round((node.getY() + node.getHeight()) * sdWidget.getZoomFactor()));
            }
        }
    }
}
