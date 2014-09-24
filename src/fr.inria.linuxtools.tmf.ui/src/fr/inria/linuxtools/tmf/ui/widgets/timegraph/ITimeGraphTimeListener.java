/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package fr.inria.linuxtools.tmf.ui.widgets.timegraph;

import java.util.EventListener;

/**
 * A listener which is notified when a timegraph changes its selected time.
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public interface ITimeGraphTimeListener extends EventListener {

    /**
     * Notifies that the timegraph selected time has changed.
     *
     * @param event event object describing details
     */
    void timeSelected(TimeGraphTimeEvent event);
}
