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
import fr.inria.linuxtools.tmf.ui.views.uml2sd.dialogs.MinMaxDialog;

/**
 * Action class implementation to configure minimum and maximum time range values.
 *
 * @version 1.0
 * @author sveyrier
 *
 */
public class ConfigureMinMax extends BaseSDAction {

    /**
     * Constructor
     * @param view
     *          the sequence diagram view reference
     * @since 2.0
     */
    public ConfigureMinMax(SDView view) {
        super(view);
    }
    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------
    @Override
    public void run() {
        if ((getView() != null) && (getView().getSDWidget() != null)) {
            MinMaxDialog minMax = new MinMaxDialog(getView().getSite().getShell(), getView().getSDWidget());
            minMax.open();
        }
    }
}
