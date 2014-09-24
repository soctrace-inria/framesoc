/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package fr.inria.linuxtools.tmf.core.callstack;

import org.eclipse.osgi.util.NLS;

/**
 * Message bundle for the call stack state provider.
 *
 * @since 3.0
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "fr.inria.linuxtools.tmf.core.callstack"; //$NON-NLS-1$

    /**
     * The value popped from a 'func_exit' event doesn't match the current
     * function name.
     */
    public static String CallStackStateProvider_UnmatchedPoppedValue;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
