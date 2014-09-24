/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package fr.inria.linuxtools.tmf.core.signal;

import fr.inria.linuxtools.tmf.core.timestamp.TmfTimeRange;
import fr.inria.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Signal indicating a trace has been updated.
 *
 * The trace has been indexed up to the specified range.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfTraceUpdatedSignal extends TmfSignal {

    private final ITmfTrace fTrace;
    private final TmfTimeRange fTimeRange;
    private final long fNbEvents;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param trace
     *            The trace that was updated
     * @param range
     *            The new time range of the trace
     * @param nbEvents
     *            The number of events in the trace
     * @since 3.0
     */
    public TmfTraceUpdatedSignal(Object source, ITmfTrace trace, TmfTimeRange range, long nbEvents) {
        super(source);
        fTrace = trace;
        fTimeRange = range;
        fNbEvents = nbEvents;
    }

    /**
     * @return The trace referred to by this signal
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * @return The time range indicated by this signal
     * @since 2.0
     */
    public TmfTimeRange getRange() {
        return fTimeRange;
    }

    /**
     * Returns the number of events indicated by this signal
     *
     * @return the number of events indicated by this signal
     * @since 3.0
     */
    public long getNbEvents() {
        return fNbEvents;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfTraceUpdatedSignal (" + fTrace.toString() + ", "
                + fTimeRange.toString() + ")]";
    }

}
