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

import fr.inria.linuxtools.tmf.ui.views.uml2sd.SDView;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.SDWidget;

/**
 * Action class implementation to move up in the sequence diagram view within a
 * page.
 *
 * @version 1.0
 * @author sveyrier
 */
public class MoveSDUp extends BaseSDAction {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /**
     * The action ID.
     */
    public static final String ID = "fr.inria.linuxtools.tmf.ui.views.uml2sd.handlers.MoveSDUp"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Default constructor
     */
    public MoveSDUp() {
        this(null);
    }

    /**
     * Constructor
     *
     * @param view a sequence diagram view reference
     */
    public MoveSDUp(SDView view) {
        super(view);
        setId(ID);
        setActionDefinitionId(ID);
    }

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------

    @Override
    public void run() {
        if (getView() == null) {
            return;
        }
        SDWidget viewer = getView().getSDWidget();

        if (viewer != null) {
            viewer.scrollBy(0, -viewer.getVisibleHeight());
        }
    }
}
