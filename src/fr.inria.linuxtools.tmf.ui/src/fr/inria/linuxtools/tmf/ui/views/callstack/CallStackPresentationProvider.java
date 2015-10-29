/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package fr.inria.linuxtools.tmf.ui.views.callstack;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import fr.inria.linuxtools.internal.tmf.ui.Activator;
import fr.inria.linuxtools.statesystem.core.ITmfStateSystem;
import fr.inria.linuxtools.statesystem.core.exceptions.AttributeNotFoundException;
import fr.inria.linuxtools.statesystem.core.exceptions.StateSystemDisposedException;
import fr.inria.linuxtools.statesystem.core.exceptions.TimeRangeException;
import fr.inria.linuxtools.statesystem.core.statevalue.ITmfStateValue;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.StateItem;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils;

/**
 * Presentation provider for the Call Stack view, based on the generic TMF
 * presentation provider.
 *
 * @author Patrick Tasse
 * @since 2.0
 */
public class CallStackPresentationProvider extends TimeGraphPresentationProvider {

    /** Number of colors used for call stack events */
    public static final int NUM_COLORS = 360;

    private final CallStackView fView;

    private enum State {
        MULTIPLE (new RGB(100, 100, 100)),
        EXEC     (new RGB(0, 200, 0));

        private final RGB rgb;

        private State (RGB rgb) {
            this.rgb = rgb;
        }
    }

    /**
     * Constructor
     *
     * @param view
     *            The callstack view that will contain the time events
     * @since 3.0
     */
    public CallStackPresentationProvider(CallStackView view) {
        fView = view;
    }

    @Override
    public String getStateTypeName() {
        // Empty string since no generic name
        return ""; //$NON-NLS-1$
    }

    @Override
    public String getStateTypeName(ITimeGraphEntry entry) {
        return ""; //$NON-NLS-1$
    }

    @Override
    public StateItem[] getStateTable() {
        final float saturation = 0.6f;
        final float brightness = 0.6f;
        StateItem[] stateTable = new StateItem[NUM_COLORS + 1];
        stateTable[0] = new StateItem(State.MULTIPLE.rgb, State.MULTIPLE.toString());
        for (int i = 0; i < NUM_COLORS; i++) {
            RGB rgb = new RGB(i, saturation, brightness);
            stateTable[i + 1] = new StateItem(rgb, State.EXEC.toString());
        }
        return stateTable;
    }

    @Override
    public Long getStateTableIndex(ITimeEvent event) {
        if (event instanceof CallStackEvent) {
            CallStackEvent callStackEvent = (CallStackEvent) event;
            return callStackEvent.getValue() + 1l;
        } else if (event instanceof NullTimeEvent) {
            return INVISIBLE;
        }
        return (long) State.MULTIPLE.ordinal();
    }

    @Override
    public String getEventName(ITimeEvent event) {
        if (event instanceof CallStackEvent) {
            CallStackEntry entry = (CallStackEntry) event.getEntry();
            ITmfStateSystem ss = CallStackView.getCallStackStateSystem(entry.getTrace());
            if (ss == null) {
                return null;
            }
            try {
                ITmfStateValue value = ss.querySingleState(event.getTime(), entry.getQuark()).getStateValue();
                if (!value.isNull()) {
                    String address = value.toString();
                    return fView.getFunctionName(address);
                }
            } catch (AttributeNotFoundException e) {
                Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
            } catch (TimeRangeException e) {
                Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
            } catch (StateSystemDisposedException e) {
                /* Ignored */
            }
            return null;
        }
        return State.MULTIPLE.toString();
    }

    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc) {
        if (bounds.width <= gc.getFontMetrics().getAverageCharWidth()) {
            return;
        }
        if (!(event instanceof CallStackEvent)) {
            return;
        }
        CallStackEntry entry = (CallStackEntry) event.getEntry();
        ITmfStateSystem ss = CallStackView.getCallStackStateSystem(entry.getTrace());
        if (ss == null) {
            return;
        }
        try {
            ITmfStateValue value = ss.querySingleState(event.getTime(), entry.getQuark()).getStateValue();
            if (!value.isNull()) {
                String address = value.toString();
                String name = fView.getFunctionName(address);
                gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
                Utils.drawText(gc, name, bounds.x, bounds.y - 2, bounds.width, true, true);
            }
        } catch (AttributeNotFoundException e) {
            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
        } catch (TimeRangeException e) {
            Activator.getDefault().logError("Error querying state system", e); //$NON-NLS-1$
        } catch (StateSystemDisposedException e) {
            /* Ignored */
        }
    }

}
