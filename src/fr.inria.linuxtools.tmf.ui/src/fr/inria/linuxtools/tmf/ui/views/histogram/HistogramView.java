/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   William Bourque - Initial API and implementation
 *   Yuriy Vashchuk - GUI reorganisation, simplification and some related code improvements.
 *   Yuriy Vashchuk - Histograms optimisation.
 *   Yuriy Vashchuk - Histogram Canvas Heritage correction
 *   Francois Chouinard - Cleanup and refactoring
 *   Francois Chouinard - Moved from LTTng to TMF
 *   Patrick Tasse - Update for mouse wheel zoom
 *   Xavier Raynaud - Support multi-trace coloring
 *******************************************************************************/

package fr.inria.linuxtools.tmf.ui.views.histogram;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IActionBars;

import fr.inria.linuxtools.internal.tmf.ui.Activator;
import fr.inria.linuxtools.internal.tmf.ui.ITmfImageConstants;
import fr.inria.linuxtools.tmf.core.request.ITmfEventRequest;
import fr.inria.linuxtools.tmf.core.request.ITmfEventRequest.ExecutionType;
import fr.inria.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import fr.inria.linuxtools.tmf.core.signal.TmfSignalHandler;
import fr.inria.linuxtools.tmf.core.signal.TmfSignalThrottler;
import fr.inria.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import fr.inria.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import fr.inria.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import fr.inria.linuxtools.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import fr.inria.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import fr.inria.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;
import fr.inria.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import fr.inria.linuxtools.tmf.core.timestamp.TmfTimeRange;
import fr.inria.linuxtools.tmf.core.timestamp.TmfTimestamp;
import fr.inria.linuxtools.tmf.core.trace.ITmfTrace;
import fr.inria.linuxtools.tmf.core.trace.TmfTraceManager;
import fr.inria.linuxtools.tmf.ui.views.TmfView;

/**
 * The purpose of this view is to provide graphical time distribution statistics about the trace events.
 * <p>
 * The view is composed of two histograms and two controls:
 * <ul>
 * <li>an event distribution histogram for the whole trace;
 * <li>an event distribution histogram for current time window (window span);
 * <li>the timestamp of the currently selected event;
 * <li>the window span (size of the time window of the smaller histogram).
 * </ul>
 * The histograms x-axis show their respective time range.
 *
 * @version 2.0
 * @author Francois Chouinard
 */
public class HistogramView extends TmfView {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     *  The view ID as defined in plugin.xml
     */
    public static final String ID = "fr.inria.linuxtools.tmf.ui.views.histogram"; //$NON-NLS-1$

    private static final Image LINK_IMG = Activator.getDefault().getImageFromPath("/icons/etool16/link.gif"); //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Parent widget
    private Composite fParent;

    // The current trace
    private ITmfTrace fTrace;

    // Current timestamp/time window - everything in the TIME_SCALE
    private long fTraceStartTime;
    private long fTraceEndTime;
    private long fWindowStartTime;
    private long fWindowEndTime;
    private long fWindowSpan;
    private long fSelectionBeginTime;
    private long fSelectionEndTime;

    // Time controls
    private HistogramTextControl fSelectionStartControl;
    private HistogramTextControl fSelectionEndControl;
    private HistogramTextControl fTimeSpanControl;

    // Link
    private Label fLinkButton;
    private boolean fLinkState;

    // Histogram/request for the full trace range
    private static FullTraceHistogram fFullTraceHistogram;
    private HistogramRequest fFullTraceRequest;

    // Histogram/request for the selected time range
    private static TimeRangeHistogram fTimeRangeHistogram;
    private HistogramRequest fTimeRangeRequest;

    // Legend area
    private Composite fLegendArea;
    private Image[] fLegendImages;

    // Throttlers for the time sync and time-range sync signals
    private final TmfSignalThrottler fTimeSyncThrottle;
    private final TmfSignalThrottler fTimeRangeSyncThrottle;

    // Action for toggle showing the lost events
    private Action hideLostEventsAction;
    // Action for toggle showing the traces
    private Action showTraceAction;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public HistogramView() {
        super(ID);
        fTimeSyncThrottle = new TmfSignalThrottler(this, 200);
        fTimeRangeSyncThrottle = new TmfSignalThrottler(this, 200);
    }

    @Override
    public void dispose() {
        if ((fTimeRangeRequest != null) && !fTimeRangeRequest.isCompleted()) {
            fTimeRangeRequest.cancel();
        }
        if ((fFullTraceRequest != null) && !fFullTraceRequest.isCompleted()) {
            fFullTraceRequest.cancel();
        }
        fFullTraceHistogram.dispose();
        fTimeRangeHistogram.dispose();
        fSelectionStartControl.dispose();
        fSelectionEndControl.dispose();
        fTimeSpanControl.dispose();
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // TmfView
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(Composite parent) {

        fParent = parent;

        // Control labels
        final String selectionStartLabel = Messages.HistogramView_selectionStartLabel;
        final String selectionEndLabel = Messages.HistogramView_selectionEndLabel;
        final String windowSpanLabel = Messages.HistogramView_windowSpanLabel;

        // --------------------------------------------------------------------
        // Set the HistogramView layout
        // --------------------------------------------------------------------

        Composite viewComposite = new Composite(fParent, SWT.FILL);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.horizontalSpacing = 5;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        viewComposite.setLayout(gridLayout);

        // --------------------------------------------------------------------
        // Time controls
        // --------------------------------------------------------------------

        Composite controlsComposite = new Composite(viewComposite, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 5;
        gridLayout.verticalSpacing = 1;
        gridLayout.makeColumnsEqualWidth = false;
        controlsComposite.setLayout(gridLayout);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, false, false);
        controlsComposite.setLayoutData(gridData);

        Composite selectionGroup = new Composite(controlsComposite, SWT.BORDER);
        gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        selectionGroup.setLayout(gridLayout);

        // Selection start control
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.CENTER;
        fSelectionStartControl = new HistogramSelectionStartControl(this, selectionGroup, selectionStartLabel, 0L);
        fSelectionStartControl.setLayoutData(gridData);
        fSelectionStartControl.setValue(Long.MIN_VALUE);

        // Selection end control
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.CENTER;
        fSelectionEndControl = new HistogramSelectionEndControl(this, selectionGroup, selectionEndLabel, 0L);
        fSelectionEndControl.setLayoutData(gridData);
        fSelectionEndControl.setValue(Long.MIN_VALUE);

        // Link button
        gridData = new GridData();
        fLinkButton = new Label(controlsComposite, SWT.NONE);
        fLinkButton.setImage(LINK_IMG);
        fLinkButton.setLayoutData(gridData);
        addLinkButtonListeners();

        // Window span time control
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.CENTER;
        fTimeSpanControl = new HistogramTimeRangeControl(this, controlsComposite, windowSpanLabel, 0L);
        fTimeSpanControl.setLayoutData(gridData);
        fTimeSpanControl.setValue(Long.MIN_VALUE);

        // --------------------------------------------------------------------
        // Time range histogram
        // --------------------------------------------------------------------

        Composite timeRangeComposite = new Composite(viewComposite, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginTop = 5;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginLeft = 5;
        gridLayout.marginRight = 5;
        timeRangeComposite.setLayout(gridLayout);

        // Use remaining horizontal space
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        timeRangeComposite.setLayoutData(gridData);

        // Histogram
        fTimeRangeHistogram = new TimeRangeHistogram(this, timeRangeComposite);

        // --------------------------------------------------------------------
        // Full range histogram
        // --------------------------------------------------------------------

        Composite fullRangeComposite = new Composite(viewComposite, SWT.FILL);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginTop = 5;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginLeft = 5;
        gridLayout.marginRight = 5;
        fullRangeComposite.setLayout(gridLayout);

        // Use remaining horizontal space
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        fullRangeComposite.setLayoutData(gridData);

        // Histogram
        fFullTraceHistogram = new FullTraceHistogram(this, fullRangeComposite);

        fLegendArea = new Composite(viewComposite, SWT.FILL);
        fLegendArea.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 2, 1));
        fLegendArea.setLayout(new RowLayout());

        // Add mouse wheel listener to time span control
        MouseWheelListener listener = fFullTraceHistogram.getZoom();
        fTimeSpanControl.addMouseWheelListener(listener);


        // View Action Handling
        contributeToActionBars();

        ITmfTrace trace = getActiveTrace();
        if (trace != null) {
            traceSelected(new TmfTraceSelectedSignal(this, trace));
        }
    }

    @Override
    public void setFocus() {
        fFullTraceHistogram.fCanvas.setFocus();
    }

    void refresh() {
        fParent.layout(true);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the current trace handled by the view
     *
     * @return the current trace
     * @since 2.0
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Returns the time range of the current selected window (base on default time scale).
     *
     * @return the time range of current selected window.
     * @since 2.0
     */
    public TmfTimeRange getTimeRange() {
        return new TmfTimeRange(
                new TmfTimestamp(fWindowStartTime, ITmfTimestamp.NANOSECOND_SCALE),
                new TmfTimestamp(fWindowEndTime, ITmfTimestamp.NANOSECOND_SCALE));
    }

    /**
     * get the show lost events action
     *
     * @return The action object
     * @since 2.2
     */
    public Action getShowLostEventsAction() {
        if (hideLostEventsAction == null) {
            /* show lost events */
            hideLostEventsAction = new Action(Messages.HistogramView_hideLostEvents, IAction.AS_CHECK_BOX) {
                @Override
                public void run() {
                    HistogramScaledData.hideLostEvents = hideLostEventsAction.isChecked();
                    long maxNbEvents = HistogramScaledData.hideLostEvents ? fFullTraceHistogram.fScaledData.fMaxValue : fFullTraceHistogram.fScaledData.fMaxCombinedValue;
                    fFullTraceHistogram.setMaxNbEvents(maxNbEvents);
                    maxNbEvents = HistogramScaledData.hideLostEvents ? fTimeRangeHistogram.fScaledData.fMaxValue : fTimeRangeHistogram.fScaledData.fMaxCombinedValue;
                    fTimeRangeHistogram.setMaxNbEvents(maxNbEvents);
                }
            };
            hideLostEventsAction.setText(Messages.HistogramView_hideLostEvents);
            hideLostEventsAction.setToolTipText(Messages.HistogramView_hideLostEvents);
            hideLostEventsAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_SHOW_LOST_EVENTS));
        }
        return hideLostEventsAction;
    }

    /**
     * get the show trace action
     *
     * @return The action object
     * @since 3.0
     */
    public Action getShowTraceAction() {
        if (showTraceAction == null) {
            /* show lost events */
            showTraceAction = new Action(Messages.HistogramView_showTraces, IAction.AS_CHECK_BOX) {
                @Override
                public void run() {
                    Histogram.showTraces = showTraceAction.isChecked();
                    fFullTraceHistogram.fCanvas.redraw();
                    fTimeRangeHistogram.fCanvas.redraw();
                    updateLegendArea();
                }
            };
            showTraceAction.setChecked(true);
            showTraceAction.setText(Messages.HistogramView_showTraces);
            showTraceAction.setToolTipText(Messages.HistogramView_showTraces);
            showTraceAction.setImageDescriptor(Activator.getDefault().getImageDescripterFromPath(ITmfImageConstants.IMG_UI_SHOW_HIST_TRACES));
        }
        return showTraceAction;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Broadcast TmfSignal about new current selection time range.
     * @param beginTime the begin time of current selection.
     * @param endTime the end time of current selection.
     */
    void updateSelectionTime(long beginTime, long endTime) {
        updateDisplayedSelectionTime(beginTime, endTime);
        TmfTimestamp beginTs = new TmfTimestamp(beginTime, ITmfTimestamp.NANOSECOND_SCALE);
        TmfTimestamp endTs = new TmfTimestamp(endTime, ITmfTimestamp.NANOSECOND_SCALE);
        TmfTimeSynchSignal signal = new TmfTimeSynchSignal(this, beginTs, endTs);
        fTimeSyncThrottle.queue(signal);
    }

    /**
     * Get selection begin time
     * @return the begin time of current selection
     */
    long getSelectionBegin() {
        return fSelectionBeginTime;
    }

    /**
     * Get selection end time
     * @return the end time of current selection
     */
    long getSelectionEnd() {
        return fSelectionEndTime;
    }

    /**
     * Get the link state
     * @return true if begin and end selection time should be linked
     */
    boolean getLinkState() {
        return fLinkState;
    }

    /**
     * Broadcast TmfSignal about new selection time range.
     * @param startTime the new start time
     * @param endTime the new end time
     */
    void updateTimeRange(long startTime, long endTime) {
        if (fTrace != null) {
            // Build the new time range; keep the current time
            TmfTimeRange timeRange = new TmfTimeRange(
                    new TmfTimestamp(startTime, ITmfTimestamp.NANOSECOND_SCALE),
                    new TmfTimestamp(endTime, ITmfTimestamp.NANOSECOND_SCALE));
            fTimeSpanControl.setValue(endTime - startTime);

            updateDisplayedTimeRange(startTime, endTime);

            // Send the FW signal
            TmfRangeSynchSignal signal = new TmfRangeSynchSignal(this, timeRange);
            fTimeRangeSyncThrottle.queue(signal);
        }
    }

    /**
     * Broadcast TmfSignal about new selected time range.
     * @param newDuration new duration (relative to current start time)
     */
    public synchronized void updateTimeRange(long newDuration) {
        if (fTrace != null) {
            long delta = newDuration - fWindowSpan;
            long newStartTime = fWindowStartTime - (delta / 2);
            setNewRange(newStartTime, newDuration);
        }
    }

    private void setNewRange(long startTime, long duration) {
        long realStart = startTime;

        if (realStart < fTraceStartTime) {
            realStart = fTraceStartTime;
        }

        long endTime = realStart + duration;
        if (endTime > fTraceEndTime) {
            endTime = fTraceEndTime;
            if ((endTime - duration) > fTraceStartTime) {
                realStart = endTime - duration;
            } else {
                realStart = fTraceStartTime;
            }
        }
        updateTimeRange(realStart, endTime);
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handles trace opened signal. Loads histogram if new trace time range is not
     * equal <code>TmfTimeRange.NULL_RANGE</code>
     * @param signal the trace opened signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        assert (signal != null);
        fTrace = signal.getTrace();
        loadTrace();
    }

    /**
     * Handles trace selected signal. Loads histogram if new trace time range is not
     * equal <code>TmfTimeRange.NULL_RANGE</code>
     * @param signal the trace selected signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        assert (signal != null);
        if (fTrace != signal.getTrace()) {
            fTrace = signal.getTrace();
            loadTrace();
        }
    }

    private void loadTrace() {
        initializeHistograms();
        fParent.redraw();
    }

    /**
     * Handles trace closed signal. Clears the view and data model and cancels requests.
     * @param signal the trace closed signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {

        if (signal.getTrace() != fTrace) {
            return;
        }

        // Kill any running request
        if ((fTimeRangeRequest != null) && !fTimeRangeRequest.isCompleted()) {
            fTimeRangeRequest.cancel();
        }
        if ((fFullTraceRequest != null) && !fFullTraceRequest.isCompleted()) {
            fFullTraceRequest.cancel();
        }

        // Initialize the internal data
        fTrace = null;
        fTraceStartTime = 0L;
        fTraceEndTime = 0L;
        fWindowStartTime = 0L;
        fWindowEndTime = 0L;
        fWindowSpan = 0L;
        fSelectionBeginTime = 0L;
        fSelectionEndTime = 0L;

        // Clear the UI widgets
        fFullTraceHistogram.clear();
        fTimeRangeHistogram.clear();
        fSelectionStartControl.setValue(Long.MIN_VALUE);
        fSelectionEndControl.setValue(Long.MIN_VALUE);

        fTimeSpanControl.setValue(Long.MIN_VALUE);

        for (Control c: fLegendArea.getChildren()) {
            c.dispose();
        }
        if (fLegendImages != null) {
            for (Image i: fLegendImages) {
                i.dispose();
            }
        }
        fLegendImages = null;
        fLegendArea.layout();
        fLegendArea.getParent().layout();
    }

    /**
     * Handles trace range updated signal. Extends histogram according to the new time range. If a
     * HistogramRequest is already ongoing, it will be cancelled and a new request with the new range
     * will be issued.
     *
     * @param signal the trace range updated signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceRangeUpdated(TmfTraceRangeUpdatedSignal signal) {

        if (signal.getTrace() != fTrace) {
            return;
        }

        TmfTimeRange fullRange = signal.getRange();

        fTraceStartTime = fullRange.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        fTraceEndTime = fullRange.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

        fFullTraceHistogram.setFullRange(fTraceStartTime, fTraceEndTime);
        fTimeRangeHistogram.setFullRange(fTraceStartTime, fTraceEndTime);

        sendFullRangeRequest(fullRange);
    }

    /**
     * Handles the trace updated signal. Used to update time limits (start and end time)
     * @param signal the trace updated signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
        if (signal.getTrace() != fTrace) {
            return;
        }
        TmfTimeRange fullRange = signal.getTrace().getTimeRange();
        fTraceStartTime = fullRange.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        fTraceEndTime = fullRange.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();

        fFullTraceHistogram.setFullRange(fTraceStartTime, fTraceEndTime);
        fTimeRangeHistogram.setFullRange(fTraceStartTime, fTraceEndTime);

        if ((fFullTraceRequest != null) && fFullTraceRequest.getRange().getEndTime().compareTo(signal.getRange().getEndTime()) < 0) {
            sendFullRangeRequest(fullRange);
        }
}

    /**
     * Handles the current time updated signal. Sets the current time in the time range
     * histogram as well as the full histogram.
     *
     * @param signal the signal to process
     */
    @TmfSignalHandler
    public void currentTimeUpdated(final TmfTimeSynchSignal signal) {
        if (Display.getCurrent() == null) {
            // Make sure the signal is handled in the UI thread
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (fParent.isDisposed()) {
                        return;
                    }
                    currentTimeUpdated(signal);
                }
            });
            return;
        }

        // Update the selected time range
        ITmfTimestamp beginTime = signal.getBeginTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE);
        ITmfTimestamp endTime = signal.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE);
        updateDisplayedSelectionTime(beginTime.getValue(), endTime.getValue());
    }

    /**
     * Updates the current time range in the time range histogram and full range histogram.
     * @param signal the signal to process
     */
    @TmfSignalHandler
    public void timeRangeUpdated(final TmfRangeSynchSignal signal) {
        if (Display.getCurrent() == null) {
            // Make sure the signal is handled in the UI thread
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (fParent.isDisposed()) {
                        return;
                    }
                    timeRangeUpdated(signal);
                }
            });
            return;
        }

        if (fTrace != null) {
            // Validate the time range
            TmfTimeRange range = signal.getCurrentRange().getIntersection(fTrace.getTimeRange());
            if (range == null) {
                return;
            }

            updateDisplayedTimeRange(
                    range.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue(),
                    range.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue());

            // Send the event request to populate the small histogram
            sendTimeRangeRequest(fWindowStartTime, fWindowEndTime);

            fTimeSpanControl.setValue(fWindowSpan);
        }
    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private void initializeHistograms() {
        TmfTimeRange fullRange = updateTraceTimeRange();
        long selectionBeginTime = fTraceManager.getSelectionBeginTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        long selectionEndTime = fTraceManager.getSelectionEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        long startTime = fTraceManager.getCurrentRange().getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        long duration = fTraceManager.getCurrentRange().getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue() - startTime;

        if ((fTimeRangeRequest != null) && !fTimeRangeRequest.isCompleted()) {
            fTimeRangeRequest.cancel();
        }
        fTimeRangeHistogram.clear();
        fTimeRangeHistogram.setFullRange(fTraceStartTime, fTraceEndTime);
        fTimeRangeHistogram.setTimeRange(startTime, duration);
        fTimeRangeHistogram.setSelection(selectionBeginTime, selectionEndTime);
        fTimeRangeHistogram.fDataModel.setTrace(fTrace);

        if ((fFullTraceRequest != null) && !fFullTraceRequest.isCompleted()) {
            fFullTraceRequest.cancel();
        }
        fFullTraceHistogram.clear();
        fFullTraceHistogram.setFullRange(fTraceStartTime, fTraceEndTime);
        fFullTraceHistogram.setTimeRange(startTime, duration);
        fFullTraceHistogram.setSelection(selectionBeginTime, selectionEndTime);
        fFullTraceHistogram.fDataModel.setTrace(fTrace);

        fWindowStartTime = startTime;
        fWindowSpan = duration;
        fWindowEndTime = startTime + duration;

        fSelectionBeginTime = selectionBeginTime;
        fSelectionEndTime = selectionEndTime;
        fSelectionStartControl.setValue(fSelectionBeginTime);
        fSelectionEndControl.setValue(fSelectionEndTime);

        fTimeSpanControl.setValue(duration);

        ITmfTrace[] traces = TmfTraceManager.getTraceSet(fTrace);
        if (traces != null) {
            this.showTraceAction.setEnabled(traces.length < fFullTraceHistogram.getMaxNbTraces());
        }
        updateLegendArea();

        if (!fullRange.equals(TmfTimeRange.NULL_RANGE)) {
            sendTimeRangeRequest(startTime, startTime + duration);
            sendFullRangeRequest(fullRange);
        }
    }

    private void updateLegendArea() {
        for (Control c: fLegendArea.getChildren()) {
            c.dispose();
        }
        if (fLegendImages != null) {
            for (Image i: fLegendImages) {
                i.dispose();
            }
        }
        fLegendImages = null;
        if (fFullTraceHistogram.showTraces()) {
            ITmfTrace[] traces = TmfTraceManager.getTraceSet(fTrace);
            fLegendImages = new Image[traces.length];
            int traceIndex = 0;
            for (ITmfTrace trace : traces) {
                fLegendImages[traceIndex] = new Image(fLegendArea.getDisplay(), 16, 16);
                GC gc = new GC(fLegendImages[traceIndex]);
                gc.setBackground(fFullTraceHistogram.getTraceColor(traceIndex));
                gc.fillRectangle(0, 0, 15, 15);
                gc.setForeground(fLegendArea.getDisplay().getSystemColor(SWT.COLOR_BLACK));
                gc.drawRectangle(0, 0, 15, 15);
                gc.dispose();

                CLabel label = new CLabel(fLegendArea, SWT.NONE);
                label.setText(trace.getName());
                label.setImage(fLegendImages[traceIndex]);
                traceIndex++;
            }
        }
        fLegendArea.layout();
        fLegendArea.getParent().layout();
    }

    private void updateDisplayedSelectionTime(long beginTime, long endTime) {
        fSelectionBeginTime = beginTime;
        fSelectionEndTime = endTime;

        fFullTraceHistogram.setSelection(fSelectionBeginTime, fSelectionEndTime);
        fTimeRangeHistogram.setSelection(fSelectionBeginTime, fSelectionEndTime);
        fSelectionStartControl.setValue(fSelectionBeginTime);
        fSelectionEndControl.setValue(fSelectionEndTime);
    }

    private void updateDisplayedTimeRange(long start, long end) {
        fWindowStartTime = start;
        fWindowEndTime = end;
        fWindowSpan = fWindowEndTime - fWindowStartTime;
        fFullTraceHistogram.setTimeRange(fWindowStartTime, fWindowSpan);
    }

    private TmfTimeRange updateTraceTimeRange() {
        fTraceStartTime = 0L;
        fTraceEndTime = 0L;

        TmfTimeRange timeRange = fTrace.getTimeRange();
        if (!timeRange.equals(TmfTimeRange.NULL_RANGE)) {
            fTraceStartTime = timeRange.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
            fTraceEndTime = timeRange.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        }
        return timeRange;
    }

    private void sendTimeRangeRequest(long startTime, long endTime) {
        if ((fTimeRangeRequest != null) && !fTimeRangeRequest.isCompleted()) {
            fTimeRangeRequest.cancel();
        }
        TmfTimestamp startTS = new TmfTimestamp(startTime, ITmfTimestamp.NANOSECOND_SCALE);
        TmfTimestamp endTS = new TmfTimestamp(endTime, ITmfTimestamp.NANOSECOND_SCALE);
        TmfTimeRange timeRange = new TmfTimeRange(startTS, endTS);

        fTimeRangeHistogram.clear();
        fTimeRangeHistogram.setFullRange(fTraceStartTime, fTraceEndTime);
        fTimeRangeHistogram.setTimeRange(startTime, endTime - startTime);

        int cacheSize = fTrace.getCacheSize();
        fTimeRangeRequest = new HistogramRequest(fTimeRangeHistogram.getDataModel(),
                timeRange, 0, ITmfEventRequest.ALL_DATA, cacheSize, ExecutionType.FOREGROUND, false);
        fTrace.sendRequest(fTimeRangeRequest);
    }

    private void sendFullRangeRequest(TmfTimeRange fullRange) {
        if ((fFullTraceRequest != null) && !fFullTraceRequest.isCompleted()) {
            fFullTraceRequest.cancel();
        }
        int cacheSize = fTrace.getCacheSize();
        fFullTraceRequest = new HistogramRequest(fFullTraceHistogram.getDataModel(),
                fullRange,
                (int) fFullTraceHistogram.fDataModel.getNbEvents(),
                ITmfEventRequest.ALL_DATA,
                cacheSize,
                ExecutionType.BACKGROUND, true);
        fTrace.sendRequest(fFullTraceRequest);
    }

    private void contributeToActionBars() {
        IActionBars bars = getViewSite().getActionBars();
        bars.getToolBarManager().add(getShowLostEventsAction());
        bars.getToolBarManager().add(getShowTraceAction());
        bars.getToolBarManager().add(new Separator());
    }

    private void addLinkButtonListeners() {
        fLinkButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDown(MouseEvent e) {
                fSelectionEndControl.setEnabled(fLinkState);
                fLinkState = !fLinkState;
                fLinkButton.redraw();
            }
        });

        fLinkButton.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                if (fLinkState) {
                    Rectangle r = fLinkButton.getBounds();
                    r.x = -1;
                    r.y = -1;
                    e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
                    e.gc.drawRectangle(r);
                    r.x = 0;
                    r.y = 0;
                    e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_DARK_GRAY));
                    e.gc.drawRectangle(r);
                }
            }
        });
    }
}
