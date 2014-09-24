/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package fr.inria.linuxtools.statesystem.core.exceptions;

/**
 * Exception thrown by the state system if a query is done on it after it has
 * been disposed.
 *
 * @author Alexandre Montplaisir
 * @since 3.0
 */
public class StateSystemDisposedException extends Exception {

    private static final long serialVersionUID = 7896041701818620084L;

    /**
     * Create a new simple StateSystemDisposedException.
     */
    public StateSystemDisposedException() {
        super();
    }

    /**
     * Create a new StateSystemDisposedException based on a previous one.
     *
     * @param e
     *            The previous exception
     */
    public StateSystemDisposedException(Throwable e) {
        super(e);
    }

}
