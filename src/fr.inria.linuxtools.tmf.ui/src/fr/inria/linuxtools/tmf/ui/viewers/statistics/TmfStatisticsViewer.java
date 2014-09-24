/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Initial API and implementation
 *   Alexandre Montplaisir - Port to ITmfStatistics provider
 *   Patrick Tasse - Support selection range
 *******************************************************************************/

package fr.inria.linuxtools.tmf.ui.viewers.statistics;

import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import fr.inria.linuxtools.statesystem.core.ITmfStateSystem;
import fr.inria.linuxtools.tmf.core.component.TmfComponent;
import fr.inria.linuxtools.tmf.core.request.ITmfEventRequest;
import fr.inria.linuxtools.tmf.core.signal.TmfSignalHandler;
import fr.inria.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import fr.inria.linuxtools.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import fr.inria.linuxtools.tmf.core.statistics.ITmfStatistics;
import fr.inria.linuxtools.tmf.core.statistics.TmfStatisticsEventTypesModule;
import fr.inria.linuxtools.tmf.core.statistics.TmfStatisticsModule;
import fr.inria.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import fr.inria.linuxtools.tmf.core.timestamp.TmfTimeRange;
import fr.inria.linuxtools.tmf.core.trace.ITmfTrace;
import fr.inria.linuxtools.tmf.core.trace.TmfExperiment;
import fr.inria.linuxtools.tmf.core.trace.TmfTraceManager;
import fr.inria.linuxtools.tmf.ui.viewers.TmfViewer;
import fr.inria.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnData;
import fr.inria.linuxtools.tmf.ui.viewers.statistics.model.TmfBaseColumnDataProvider;
import fr.inria.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsFormatter;
import fr.inria.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTree;
import fr.inria.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeManager;
import fr.inria.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import fr.inria.linuxtools.tmf.ui.viewers.statistics.model.TmfTreeContentProvider;

/**
 * A basic viewer to display statistics in the statistics view.
 *
 * It is linked to a single ITmfTrace until its disposal.
 *
 * @author Mathieu Denis
 * @since 2.0
 */
public class TmfStatisticsViewer extends TmfViewer {

    /** Timestamp scale used for all statistics (nanosecond) */
    private static final byte TIME_SCALE = ITmfTimestamp.NANOSECOND_SCALE;

    /** The delay (in ms) between each update in live-reading mode */
    private static final long LIVE_UPDATE_DELAY = 1000;

    /** The actual tree viewer to display */
    private TreeViewer fTreeViewer;

    /** The statistics tree linked to this viewer */
    private TmfStatisticsTree fStatisticsData;

    /** Update range synchronization object */
    private final Object fStatisticsRangeUpdateSyncObj = new Object();

    /** The trace that is displayed by this viewer */
    private ITmfTrace fTrace;

    /** Indicates to process all events */
    private boolean fProcessAll;

    /** View instance counter (for multiple statistics views) */
    private static int fCountInstance = 0;

    /** Number of this instance. Used as an instance ID. */
    private int fInstanceNb;

    /** Object to store the cursor while waiting for the trace to load */
    private Cursor fWaitCursor = null;

    /**
     * Counts the number of times waitCursor() has been called. It avoids
     * removing the waiting cursor, since there may be multiple requests running
     * at the same time.
     */
    private int fWaitCursorCount = 0;

    /** Tells to send a time range request when the trace gets updated. */
    private boolean fSendRangeRequest = true;

    /** Reference to the trace manager */
    private final TmfTraceManager fTraceManager;

    /**
     * Create a basic statistics viewer. To be used in conjunction with
     * {@link TmfStatisticsViewer#init(Composite, String, ITmfTrace)}
     *
     * @param parent
     *            The parent composite that will hold the viewer
     * @param viewerName
     *            The name that will be assigned to this viewer
     * @param trace
     *            The trace that is displayed by this viewer
     * @see TmfComponent
     */
    public TmfStatisticsViewer(Composite parent, String viewerName, ITmfTrace trace) {
        init(parent, viewerName, trace);
        fTraceManager = TmfTraceManager.getInstance();
    }

    /**
     * Initialize the statistics viewer.
     *
     * @param parent
     *            The parent component of the viewer.
     * @param viewerName
     *            The name to give to the viewer.
     * @param trace
     *            The trace that will be displayed by the viewer.
     */
    public void init(Composite parent, String viewerName, ITmfTrace trace) {
        super.init(parent, viewerName);
        // Increment a counter to make sure the tree ID is unique.
        fCountInstance++;
        fInstanceNb = fCountInstance;
        fTrace = trace;

        // The viewer will process all events if he is assigned to an experiment
        fProcessAll = (trace instanceof TmfExperiment);

        initContent(parent);
        initInput();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fWaitCursor != null) {
            fWaitCursor.dispose();
        }

        // Clean the model for this viewer
        TmfStatisticsTreeManager.removeStatTreeRoot(getTreeID());
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handles the signal about new trace range.
     *
     * @param signal
     *            The trace range updated signal
     */
    @TmfSignalHandler
    public void traceRangeUpdated(TmfTraceRangeUpdatedSignal signal) {
        ITmfTrace trace = signal.getTrace();
        // validate
        if (!isListeningTo(trace)) {
            return;
        }

        synchronized (fStatisticsRangeUpdateSyncObj) {
            // Sends the time range request only once from this method.
            if (fSendRangeRequest) {
                fSendRangeRequest = false;
                ITmfTimestamp begin = fTraceManager.getSelectionBeginTime();
                ITmfTimestamp end = fTraceManager.getSelectionEndTime();
                TmfTimeRange timeRange = new TmfTimeRange(begin, end);
                requestTimeRangeData(trace, timeRange);
            }
        }
        requestData(trace, signal.getRange());
    }

    /**
     * Handles the time synch updated signal. It updates the time range
     * statistics.
     *
     * @param signal
     *            Contains the information about the new selected time range.
     * @since 2.1
     */
    @TmfSignalHandler
    public void timeSynchUpdated(TmfTimeSynchSignal signal) {
        if (fTrace == null) {
            return;
        }
        ITmfTimestamp begin = signal.getBeginTime();
        ITmfTimestamp end = signal.getEndTime();
        TmfTimeRange timeRange = new TmfTimeRange(begin, end);
        requestTimeRangeData(fTrace, timeRange);
    }

    // ------------------------------------------------------------------------
    // Class methods
    // ------------------------------------------------------------------------

    /*
     * Returns the primary control associated with this viewer.
     *
     * @return the SWT control which displays this viewer's content
     */
    @Override
    public Control getControl() {
        return fTreeViewer.getControl();
    }

    /**
     * Get the input of the viewer.
     *
     * @return an object representing the input of the statistics viewer.
     */
    public Object getInput() {
        return fTreeViewer.getInput();
    }

    /**
     * This method can be overridden to implement another way of representing
     * the statistics data and to retrieve the information for display.
     *
     * @return a TmfStatisticsData object.
     */
    public TmfStatisticsTree getStatisticData() {
        if (fStatisticsData == null) {
            fStatisticsData = new TmfStatisticsTree();
        }
        return fStatisticsData;
    }

    /**
     * Returns a unique ID based on name to be associated with the statistics
     * tree for this viewer. For a same name, it will always return the same ID.
     *
     * @return a unique statistics tree ID.
     */
    public String getTreeID() {
        return getName() + fInstanceNb;
    }

    @Override
    public void refresh() {
        final Control viewerControl = getControl();
        // Ignore update if disposed
        if (viewerControl.isDisposed()) {
            return;
        }

        viewerControl.getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                if (!viewerControl.isDisposed()) {
                    fTreeViewer.refresh();
                }
            }
        });
    }

    /**
     * Will force a request on the partial event count if one is needed.
     */
    public void sendPartialRequestOnNextUpdate() {
        synchronized (fStatisticsRangeUpdateSyncObj) {
            fSendRangeRequest = true;
        }
    }

    /**
     * Focus on the statistics tree of the viewer
     */
    public void setFocus() {
        fTreeViewer.getTree().setFocus();
    }

    /**
     * Cancels the request if it is not already completed
     *
     * @param request
     *            The request to be canceled
     * @since 3.0
     */
    protected void cancelOngoingRequest(ITmfEventRequest request) {
        if (request != null && !request.isCompleted()) {
            request.cancel();
        }
    }

    /**
     * This method can be overridden to change the representation of the data in
     * the columns.
     *
     * @return An object of type {@link TmfBaseColumnDataProvider}.
     * @since 3.0
     */
    protected TmfBaseColumnDataProvider getColumnDataProvider() {
        return new TmfBaseColumnDataProvider();
    }

    /**
     * Initialize the content that will be drawn in this viewer
     *
     * @param parent
     *            The parent of the control to create
     */
    protected void initContent(Composite parent) {
        final List<TmfBaseColumnData> columnDataList = getColumnDataProvider().getColumnData();

        fTreeViewer = new TreeViewer(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        fTreeViewer.setContentProvider(new TmfTreeContentProvider());
        fTreeViewer.getTree().setHeaderVisible(true);
        fTreeViewer.setUseHashlookup(true);

        // Creates the columns defined by the column data provider
        for (final TmfBaseColumnData columnData : columnDataList) {
            final TreeViewerColumn treeColumn = new TreeViewerColumn(fTreeViewer, columnData.getAlignment());
            treeColumn.getColumn().setText(columnData.getHeader());
            treeColumn.getColumn().setWidth(columnData.getWidth());
            treeColumn.getColumn().setToolTipText(columnData.getTooltip());

            // If is dummy column
            if (columnData == columnDataList.get(TmfBaseColumnDataProvider.StatsColumn.DUMMY.getIndex())) {
                treeColumn.getColumn().setResizable(false);
            }

            // A comparator is defined.
            if (columnData.getComparator() != null) {
                // Adds a listener on the columns header for sorting purpose.
                treeColumn.getColumn().addSelectionListener(new SelectionAdapter() {

                    private ViewerComparator reverseComparator;

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        // Initializes the reverse comparator once.
                        if (reverseComparator == null) {
                            reverseComparator = new ViewerComparator() {
                                @Override
                                public int compare(Viewer viewer, Object e1, Object e2) {
                                    return -1 * columnData.getComparator().compare(viewer, e1, e2);
                                }
                            };
                        }

                        if (fTreeViewer.getTree().getSortDirection() == SWT.UP
                                || fTreeViewer.getTree().getSortColumn() != treeColumn.getColumn()) {
                            /*
                             * Puts the descendant order if the old order was up
                             * or if the selected column has changed.
                             */
                            fTreeViewer.setComparator(columnData.getComparator());
                            fTreeViewer.getTree().setSortDirection(SWT.DOWN);
                        } else {
                            /*
                             * Puts the ascendant ordering if the selected
                             * column hasn't changed.
                             */
                            fTreeViewer.setComparator(reverseComparator);
                            fTreeViewer.getTree().setSortDirection(SWT.UP);
                        }
                        fTreeViewer.getTree().setSortColumn(treeColumn.getColumn());
                    }
                });
            }
            treeColumn.setLabelProvider(columnData.getLabelProvider());
        }

        // Handler that will draw the percentages and the bar charts.
        fTreeViewer.getTree().addListener(SWT.EraseItem, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (columnDataList.get(event.index).getPercentageProvider() != null) {

                    TmfStatisticsTreeNode node = (TmfStatisticsTreeNode) event.item.getData();

                    // If node is hidden, exit immediately.
                    if (TmfBaseColumnDataProvider.HIDDEN_FOLDER_LEVELS.contains(node.getName())) {
                        return;
                    }

                    // Otherwise, get percentage and draw bar and text if applicable.
                    double percentage = columnDataList.get(event.index).getPercentageProvider().getPercentage(node);

                    // The item is selected.
                    if ((event.detail & SWT.SELECTED) > 0) {
                        // Draws our own background to avoid overwriting the bar.
                        event.gc.fillRectangle(event.x, event.y, event.width, event.height);
                        event.detail &= ~SWT.SELECTED;
                    }

                    // Drawing the percentage text
                    // if events are present in top node
                    // and the current node is not the top node
                    // and if is total or partial events column.
                    // If not, exit the method.
                    if (!((event.index == TmfBaseColumnDataProvider.StatsColumn.TOTAL.getIndex() || event.index == TmfBaseColumnDataProvider.StatsColumn.PARTIAL.getIndex())
                    && node != node.getTop())) {
                        return;
                    }

                    long eventValue = event.index == TmfBaseColumnDataProvider.StatsColumn.TOTAL.getIndex() ?
                            node.getTop().getValues().getTotal() : node.getTop().getValues().getPartial();

                    if (eventValue != 0) {

                        int oldAlpha = event.gc.getAlpha();
                        Color oldForeground = event.gc.getForeground();
                        Color oldBackground = event.gc.getBackground();

                        // Bar to draw
                        if (percentage != 0) {
                            /*
                             * Draws a transparent gradient rectangle from the
                             * color of foreground and background.
                             */
                            int barWidth = (int) ((fTreeViewer.getTree().getColumn(event.index).getWidth() - 8) * percentage);
                            event.gc.setAlpha(64);
                            event.gc.setForeground(event.item.getDisplay().getSystemColor(SWT.COLOR_BLUE));
                            event.gc.setBackground(event.item.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
                            event.gc.fillGradientRectangle(event.x, event.y, barWidth, event.height, true);
                            event.gc.drawRectangle(event.x, event.y, barWidth, event.height);

                            // Restore old values
                            event.gc.setBackground(oldBackground);
                            event.gc.setAlpha(oldAlpha);
                            event.detail &= ~SWT.BACKGROUND;

                        }

                        String percentageText = TmfStatisticsFormatter.toPercentageText(percentage);
                        String absoluteNumberText = TmfStatisticsFormatter.toColumnData(node, TmfBaseColumnDataProvider.StatsColumn.getColumn(event.index));

                        if (event.width > event.gc.stringExtent(percentageText).x + event.gc.stringExtent(absoluteNumberText).x) {
                            int textHeight = event.gc.stringExtent(percentageText).y;
                            event.gc.setForeground(event.item.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
                            event.gc.drawText(percentageText, event.x, event.y + (event.height - textHeight) / 2, true);
                        }

                        // Restores old values
                        event.gc.setForeground(oldForeground);

                    }
                }
            }

        });

        // Initializes the comparator parameters
        fTreeViewer.setComparator(columnDataList.get(0).getComparator());
        fTreeViewer.getTree().setSortColumn(fTreeViewer.getTree().getColumn(0));
        fTreeViewer.getTree().setSortDirection(SWT.DOWN);
    }

    /**
     * Initializes the input for the tree viewer.
     */
    protected void initInput() {
        String treeID = getTreeID();
        TmfStatisticsTreeNode statisticsTreeNode;
        if (TmfStatisticsTreeManager.containsTreeRoot(treeID)) {
            // The statistics root is already present
            statisticsTreeNode = TmfStatisticsTreeManager.getStatTreeRoot(treeID);

            // Checks if the trace is already in the statistics tree.
            int numNodeTraces = statisticsTreeNode.getNbChildren();

            ITmfTrace[] traces = TmfTraceManager.getTraceSet(fTrace);
            int numTraces = traces.length;

            if (numTraces == numNodeTraces) {
                boolean same = true;
                /*
                 * Checks if the experiment contains the same traces as when
                 * previously selected.
                 */
                for (int i = 0; i < numTraces; i++) {
                    String traceName = traces[i].getName();
                    if (!statisticsTreeNode.containsChild(traceName)) {
                        same = false;
                        break;
                    }
                }

                if (same) {
                    // No need to reload data, all traces are already loaded
                    fTreeViewer.setInput(statisticsTreeNode);
                    return;
                }
                // Clears the old content to start over
                statisticsTreeNode.reset();
            }
        } else {
            // Creates a new tree
            statisticsTreeNode = TmfStatisticsTreeManager.addStatsTreeRoot(treeID, getStatisticData());
        }

        // Sets the input to a clean data model
        fTreeViewer.setInput(statisticsTreeNode);
    }

    /**
     * Tells if the viewer is listening to a trace.
     *
     * @param trace
     *            The trace that the viewer may be listening
     * @return true if the viewer is listening to the trace, false otherwise
     */
    protected boolean isListeningTo(ITmfTrace trace) {
        if (fProcessAll || trace == fTrace) {
            return true;
        }
        return false;
    }

    /**
     * Called when an trace request has been completed successfully.
     *
     * @param global
     *            Tells if the request is a global or time range (partial)
     *            request.
     */
    protected void modelComplete(boolean global) {
        refresh();
        waitCursor(false);
    }

    /**
     * Called when an trace request has failed or has been cancelled.
     *
     * @param isGlobalRequest
     *            Tells if the request is a global or time range (partial)
     *            request.
     */
    protected void modelIncomplete(boolean isGlobalRequest) {
        if (isGlobalRequest) { // Clean the global statistics
            /*
             * No need to reset the global number of events, since the index of
             * the last requested event is known.
             */
        } else { // Clean the partial statistics
            resetTimeRangeValue();
        }
        refresh();
        waitCursor(false);
    }

    /**
     * Sends the request to the trace for the whole trace
     *
     * @param trace
     *            The trace used to send the request
     * @param timeRange
     *            The range to request to the trace
     */
    protected void requestData(final ITmfTrace trace, final TmfTimeRange timeRange) {
        buildStatisticsTree(trace, timeRange, true);
    }

    /**
     * Sends the time range request from the trace
     *
     * @param trace
     *            The trace used to send the request
     * @param timeRange
     *            The range to request to the trace
     */
    protected void requestTimeRangeData(final ITmfTrace trace, final TmfTimeRange timeRange) {
        buildStatisticsTree(trace, timeRange, false);
    }

    /**
     * Requests all the data of the trace to the state system which contains
     * information about the statistics.
     *
     * Since the viewer may be listening to multiple traces, it may receive an
     * experiment rather than a single trace. The filtering is done with the
     * method {@link #isListeningTo(String trace)}.
     *
     * @param trace
     *            The trace for which a request must be done
     * @param timeRange
     *            The time range that will be requested to the state system
     * @param isGlobal
     *            Tells if the request is for the global event count or the
     *            partial one.
     */
    private void buildStatisticsTree(final ITmfTrace trace, final TmfTimeRange timeRange, final boolean isGlobal) {
        final TmfStatisticsTreeNode statTree = TmfStatisticsTreeManager.getStatTreeRoot(getTreeID());
        final TmfStatisticsTree statsData = TmfStatisticsTreeManager.getStatTree(getTreeID());
        if (statsData == null) {
            return;
        }

        synchronized (statsData) {
            if (isGlobal) {
                statTree.resetGlobalValue();
            } else {
                statTree.resetTimeRangeValue();
            }

            for (final ITmfTrace aTrace : TmfTraceManager.getTraceSet(trace)) {
                if (!isListeningTo(aTrace)) {
                    continue;
                }

                /* Retrieve the statistics object */
                final TmfStatisticsModule statsMod = aTrace.getAnalysisModuleOfClass(TmfStatisticsModule.class, TmfStatisticsModule.ID);
                if (statsMod == null) {
                    /* No statistics module available for this trace */
                    continue;
                }

                /* Run the potentially long queries in a separate thread */
                Thread statsThread = new Thread("Statistics update") { //$NON-NLS-1$
                    @Override
                    public void run() {
                        /* Wait until the analysis is ready to be queried */
                        statsMod.waitForInitialization();
                        ITmfStatistics stats = statsMod.getStatistics();
                        if (stats == null) {
                            /* It should have worked, but didn't */
                            throw new IllegalStateException();
                        }

                        /*
                         * The generic statistics are stored in nanoseconds, so
                         * we must make sure the time range is scaled correctly.
                         */
                        long start = timeRange.getStartTime().normalize(0, TIME_SCALE).getValue();
                        long end = timeRange.getEndTime().normalize(0, TIME_SCALE).getValue();

                        /*
                         * Wait on the state system object we are going to query.
                         *
                         * TODO Eventually this could be exposed through the
                         * TmfStateSystemAnalysisModule directly.
                         */
                        ITmfStateSystem ss = statsMod.getStateSystem(TmfStatisticsEventTypesModule.ID);
                        if (ss == null) {
                            /*
                             * It should be instantiated after the
                             * statsMod.waitForInitialization() above.
                             */
                            throw new IllegalStateException();
                        }

                        /*
                         * Periodically update the statistics while they are
                         * being built (or, if the back-end is already
                         * completely built, it will skip over the while() immediately.
                         */
                        while (!ss.waitUntilBuilt(LIVE_UPDATE_DELAY)) {
                            Map<String, Long> map = stats.getEventTypesInRange(start, end);
                            updateStats(aTrace, isGlobal, map);
                        }
                        /* Query one last time for the final values */
                        Map<String, Long> map = stats.getEventTypesInRange(start, end);
                        updateStats(aTrace, isGlobal, map);
                    }
                };
                statsThread.start();
            }
        }
    }

    /*
     * Update statistics for a given trace
     */
    private void updateStats(ITmfTrace trace, boolean isGlobal, Map<String, Long> eventsPerType) {

        final TmfStatisticsTree statsData = TmfStatisticsTreeManager.getStatTree(getTreeID());
        if (statsData == null) {
            /* The stat tree has been disposed, abort mission. */
            return;
        }

        Map<String, Long> map = eventsPerType;
        String name = trace.getName();


        /*
         * "Global", "partial", "total", etc., it's all very confusing...
         *
         * The base view shows the total count for the trace and for
         * each even types, organized in columns like this:
         *
         *                   |  Global  |  Time range |
         * trace name        |    A     |      B      |
         *    Event Type     |          |             |
         *       <event 1>   |    C     |      D      |
         *       <event 2>   |   ...    |     ...     |
         *         ...       |          |             |
         *
         * Here, we called the cells like this:
         *  A : GlobalTotal
         *  B : TimeRangeTotal
         *  C : GlobalTypeCount(s)
         *  D : TimeRangeTypeCount(s)
         */

        /* Fill in an the event counts (either cells C or D) */
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            statsData.setTypeCount(name, entry.getKey(), isGlobal, entry.getValue());
        }

        /*
         * Calculate the totals (cell A or B, depending if isGlobal). We will
         * use the results of the previous request instead of sending another
         * one.
         */
        long globalTotal = 0;
        for (long val : map.values()) {
            globalTotal += val;
        }
        statsData.setTotal(name, isGlobal, globalTotal);

        modelComplete(isGlobal);
    }

    /**
     * Resets the number of events within the time range
     */
    protected void resetTimeRangeValue() {
        TmfStatisticsTreeNode treeModelRoot = TmfStatisticsTreeManager.getStatTreeRoot(getTreeID());
        if (treeModelRoot != null && treeModelRoot.hasChildren()) {
            treeModelRoot.resetTimeRangeValue();
        }
    }

    /**
     * When the trace is loading the cursor will be different so the user knows
     * that the processing is not finished yet.
     *
     * Calls to this method are stacked.
     *
     * @param waitRequested
     *            Indicates if we need to show the waiting cursor, or the
     *            default one.
     */
    protected void waitCursor(final boolean waitRequested) {
        if ((fTreeViewer == null) || (fTreeViewer.getTree().isDisposed())) {
            return;
        }

        boolean needsUpdate = false;
        Display display = fTreeViewer.getControl().getDisplay();
        if (waitRequested) {
            fWaitCursorCount++;
            if (fWaitCursor == null) { // The cursor hasn't been initialized yet
                fWaitCursor = new Cursor(display, SWT.CURSOR_WAIT);
            }
            if (fWaitCursorCount == 1) { // The cursor is not in waiting mode
                needsUpdate = true;
            }
        } else {
            if (fWaitCursorCount > 0) { // The cursor is in waiting mode
                fWaitCursorCount--;
                if (fWaitCursorCount == 0) { // No more reason to wait
                    // Put back the default cursor
                    needsUpdate = true;
                }
            }
        }

        if (needsUpdate) {
            // Performs the updates on the UI thread
            display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    if ((fTreeViewer != null)
                            && (!fTreeViewer.getTree().isDisposed())) {
                        Cursor cursor = null; // indicates default
                        if (waitRequested) {
                            cursor = fWaitCursor;
                        }
                        fTreeViewer.getControl().setCursor(cursor);
                    }
                }
            });
        }
    }
}
