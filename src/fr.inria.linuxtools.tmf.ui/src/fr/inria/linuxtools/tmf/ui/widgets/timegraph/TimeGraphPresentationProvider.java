/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *   Patrick Tasse - Refactoring
 *   Geneviève Bastien - Add drawing helper methods
 *******************************************************************************/

package fr.inria.linuxtools.tmf.ui.widgets.timegraph;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import fr.inria.linuxtools.internal.tmf.ui.Messages;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.widgets.ITmfTimeGraphDrawingHelper;
import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;

/**
 * Provider class for the time graph provider
 *
 * @version 1.0
 * @author Patrick Tasse
 *
 */
public class TimeGraphPresentationProvider implements ITimeGraphPresentationProvider2 {

    private ITmfTimeGraphDrawingHelper fDrawingHelper;
    private final String fStateTypeName;
    //@Framesoc
    private TimeUnit fTimeUnit = TimeUnit.UNKNOWN;

    // The list of listeners for graph color changes
    private final List<ITimeGraphColorListener> fListeners = new ArrayList<>();

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final int DEFAULT_ITEM_HEIGHT = 19;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Constructor
     * @param stateTypeName  The state type name
     * @since 2.1
     */
    public TimeGraphPresentationProvider(String stateTypeName) {
        fStateTypeName = stateTypeName;
    }

    /**
     * Constructor
     * @since 2.1
     */
    public TimeGraphPresentationProvider() {
        this(Messages.TmfTimeLegend_TRACE_STATES);
    }

    @Override
    public String getStateTypeName() {
        return fStateTypeName;
    }

    /**
     * @see fr.inria.linuxtools.tmf.ui.widgets.timegraph.ITimeGraphPresentationProvider#getStateTypeName(fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry)
     * @since 2.0
     */
    @Override
    public String getStateTypeName(ITimeGraphEntry entry) {
        return null;
    }

    @Override
    public StateItem[] getStateTable() {
        return null;
    }

    @Override
    public Long getStateTableIndex(ITimeEvent event) {
        return 0l;
    }

    /**
     * @since 2.1
     */
    @Override
    public ITmfTimeGraphDrawingHelper getDrawingHelper() {
        return fDrawingHelper;
    }

    /**
     * @since 2.1
     */
    @Override
    public void setDrawingHelper(ITmfTimeGraphDrawingHelper helper) {
        fDrawingHelper = helper;
    }

    @Override
    public void postDrawControl(Rectangle bounds, GC gc) {
        // Override to add own drawing code
    }

    @Override
    public void postDrawEntry(ITimeGraphEntry entry, Rectangle bounds, GC gc) {
        // Override to add own drawing code
    }

    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc) {
        // Override to add own drawing code
    }

    @Override
    public int getItemHeight(ITimeGraphEntry entry) {
        return DEFAULT_ITEM_HEIGHT;
    }

    @Override
    public Image getItemImage(ITimeGraphEntry entry) {
        return null;
    }

    @Override
    public String getEventName(ITimeEvent event) {
        return null;
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event) {
        return null;
    }

    /**
     * @since 2.0
     */
    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime) {
        return getEventHoverToolTipInfo(event);
    }

    /**
     * @since 3.0
     */
    @Override
    public boolean displayTimesInTooltip() {
        return true;
    }

    /**
     * @since 3.0
     */
    @Override
    public void addColorListener(ITimeGraphColorListener listener) {
        if (!fListeners.contains(listener)) {
            fListeners.add(listener);
        }
    }

    /**
     * @since 3.0
     */
    @Override
    public void removeColorListener(ITimeGraphColorListener listener) {
        fListeners.remove(listener);
    }

    /**
     * Notifies listeners of the state table change
     * @since 3.0
     */
    protected void fireColorSettingsChanged() {
        for (ITimeGraphColorListener listener : fListeners) {
            listener.colorSettingsChanged(getStateTable());
        }
    }


    @Override
    public TimeUnit getTimeUnit() {
        return fTimeUnit;
    }

    @Override
    public void setTimeUnit(TimeUnit unit) {
        fTimeUnit = unit;
    }

}
