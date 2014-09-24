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

import fr.inria.linuxtools.internal.tmf.ui.Activator;
import fr.inria.linuxtools.internal.tmf.ui.ITmfImageConstants;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.SDView;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.dialogs.PagesDialog;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.handlers.provider.ISDAdvancedPagingProvider;
import fr.inria.linuxtools.tmf.ui.views.uml2sd.util.Messages;

/**
 * Action class implementation for paging.
 *
 * @version 1.0
 * @author Bernd Hufmann
 */
public class OpenSDPagesDialog extends BaseSDAction {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The action ID.
     */
    public static final String ID = "fr.inria.linuxtools.tmf.ui.views.uml2sd.handlers.sdPaging"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The advanced paging provider reference.
     */
    private final ISDAdvancedPagingProvider fProvider;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param view
     *            The view reference
     * @param provider
     *            The provider
     */
    public OpenSDPagesDialog(SDView view, ISDAdvancedPagingProvider provider) {
        super(view);
        setText(Messages.SequenceDiagram_Pages);
        setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_GOTO_PAGE));
        setId(ID);
        fProvider = provider;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void run() {
        if (getView() == null) {
            return;
        }
        PagesDialog dialog = new PagesDialog(getView(), fProvider);
        dialog.open();
    }
}
