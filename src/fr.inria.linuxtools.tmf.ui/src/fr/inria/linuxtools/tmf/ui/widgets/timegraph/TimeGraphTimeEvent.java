/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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

import java.util.EventObject;

/**
 * Time selection event
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TimeGraphTimeEvent extends EventObject {

    /**
     * Default serial version UID for this class.
     * @since 1.0
     */
    private static final long serialVersionUID = 1L;

    /**
     * The selection begin time.
     */
    private final long fBeginTime;

    /**
     * The selection end time.
     */
    private final long fEndTime;

    /**
     * Standard constructor
     *
     * @param source
     *            The source of this event
     * @param beginTime
     *            The selection begin time
     * @param endTime
     *            The selection end time
     * @since 2.1
     */
    public TimeGraphTimeEvent(Object source, long beginTime, long endTime) {
        super(source);
        fBeginTime = beginTime;
        fEndTime = endTime;
    }

    /**
     * @return the selection begin time
     * @since 2.1
     */
    public long getBeginTime() {
        return fBeginTime;
    }

    /**
     * @return the selection end time
     * @since 2.1
     */
    public long getEndTime() {
        return fEndTime;
    }

}
