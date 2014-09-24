/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Changed to updated histogram data model
 *   Francois Chouinard - Reformat histogram labels on format change
 *   Patrick Tasse - Support selection range
 *   Xavier Raynaud - Support multi-trace coloring
 *******************************************************************************/

package fr.inria.linuxtools.tmf.ui.views.histogram;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import fr.inria.linuxtools.tmf.core.signal.TmfSignalHandler;
import fr.inria.linuxtools.tmf.core.signal.TmfSignalManager;
import fr.inria.linuxtools.tmf.core.signal.TmfTimestampFormatUpdateSignal;
import fr.inria.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import fr.inria.linuxtools.tmf.core.timestamp.TmfTimestamp;
import fr.inria.linuxtools.tmf.core.timestamp.TmfTimestampDelta;
import fr.inria.linuxtools.tmf.core.timestamp.TmfTimestampFormat;
import fr.inria.linuxtools.tmf.ui.views.TmfView;

/**
 * Re-usable histogram widget.
 *
 * It has the following features:
 * <ul>
 * <li>Y-axis labels displaying min/max count values
 * <li>X-axis labels displaying time range
 * <li>a histogram displaying the distribution of values over time (note that
 * the histogram might not necessarily fill the whole canvas)
 * </ul>
 * The widget also has 2 'markers' to identify:
 * <ul>
 * <li>a red dashed line over the bar that contains the currently selected event
 * <li>a dark red dashed line that delimits the right end of the histogram (if
 * it doesn't fill the canvas)
 * </ul>
 * Clicking on the histogram will select the current event at the mouse
 * location.
 * <p>
 * Once the histogram is selected, there is some limited keyboard support:
 * <ul>
 * <li>Home: go to the first histogram bar
 * <li>End: go to the last histogram bar
 * <li>Left: go to the previous histogram
 * <li>Right: go to the next histogram bar
 * </ul>
 * Finally, when the mouse hovers over the histogram, a tool tip showing the
 * following information about the corresponding histogram bar time range:
 * <ul>
 * <li>start of the time range
 * <li>end of the time range
 * <li>number of events in that time range
 * </ul>
 *
 * @version 1.1
 * @author Francois Chouinard
 */
public abstract class Histogram implements ControlListener, PaintListener, KeyListener, MouseListener, MouseMoveListener, MouseTrackListener, IHistogramModelListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // Histogram colors

    // System colors, they do not need to be disposed
    private final Color fBackgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    private final Color fSelectionForegroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
    private final Color fSelectionBackgroundColor = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
    private final Color fLastEventColor = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);

    // Application colors, they need to be disposed
    private final Color[] fHistoBarColors = new Color[] {new Color(Display.getDefault(), 90, 90, 255), // blue
            new Color(Display.getDefault(), 0, 240, 0), // green
            new Color(Display.getDefault(), 255, 0, 0), // red
            new Color(Display.getDefault(), 0, 255, 255), // cyan
            new Color(Display.getDefault(), 255, 80, 255), // magenta
            new Color(Display.getDefault(), 200, 200, 0), // yellow
            new Color(Display.getDefault(), 200, 150, 0), // brown
            new Color(Display.getDefault(), 150, 255, 150), // light green
            new Color(Display.getDefault(), 200, 80, 80), // dark red
            new Color(Display.getDefault(), 30, 150, 150), // dark cyan
            new Color(Display.getDefault(), 200, 200, 255), // light blue
            new Color(Display.getDefault(), 0, 120, 0), // dark green
            new Color(Display.getDefault(), 255, 150, 150), // lighter red
            new Color(Display.getDefault(), 140, 80, 140), // dark magenta
            new Color(Display.getDefault(), 150, 100, 50), // brown
            new Color(Display.getDefault(), 255, 80, 80), // light red
            new Color(Display.getDefault(), 200, 200, 200), // light grey
            new Color(Display.getDefault(), 255, 200, 80), // orange
            new Color(Display.getDefault(), 255, 255, 80), // pale yellow
            new Color(Display.getDefault(), 255, 200, 200), // pale red
            new Color(Display.getDefault(), 255, 200, 255), // pale magenta
            new Color(Display.getDefault(), 255, 255, 200), // pale pale yellow
            new Color(Display.getDefault(), 200, 255, 255), // pale pale blue
    };
    private final Color fTimeRangeColor = new Color(Display.getCurrent(), 255, 128, 0);
    private final Color fLostEventColor = new Color(Display.getCurrent(), 208, 62, 120);

    // Drag states
    /**
     * No drag in progress
     * @since 2.2
     */
    protected final int DRAG_NONE = 0;
    /**
     * Drag the selection
     * @since 2.2
     */
    protected final int DRAG_SELECTION = 1;
    /**
     * Drag the time range
     * @since 2.2
     */
    protected final int DRAG_RANGE = 2;
    /**
     * Drag the zoom range
     * @since 2.2
     */
    protected final int DRAG_ZOOM = 3;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The parent TMF view.
     */
    protected TmfView fParentView;

    private Composite fComposite;
    private Font fFont;

    // Histogram text fields
    private Label fMaxNbEventsLabel;
    private Label fMinNbEventsLabel;
    private Label fTimeRangeStartLabel;
    private Label fTimeRangeEndLabel;

    /**
     * Histogram drawing area
     */
    protected Canvas fCanvas;

    /**
     * The histogram data model.
     */
    protected final HistogramDataModel fDataModel;

    /**
     * The histogram data model scaled to current resolution and screen width.
     */
    protected HistogramScaledData fScaledData;

    /**
     * The current event value
     */
    protected long fCurrentEventTime = 0L;

    /**
     * The current selection begin time
     */
    private long fSelectionBegin = 0L;

    /**
     * The current selection end time
     */
    private long fSelectionEnd = 0L;

    /**
     * The drag state
     * @see #DRAG_NONE
     * @see #DRAG_SELECTION
     * @see #DRAG_RANGE
     * @see #DRAG_ZOOM
     * @since 2.2
     */
    protected int fDragState = DRAG_NONE;

    /**
     * The button that started a mouse drag, or 0 if no drag in progress
     * @since 2.2
     */
    protected int fDragButton = 0;

    /**
     * The bucket display offset
     */
    private int fOffset = 0;

    /**
     * show the traces or not
     * @since 3.0
     */
    static boolean showTraces = true;

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    /**
     * Full constructor.
     *
     * @param view A reference to the parent TMF view.
     * @param parent A parent composite
     */
    public Histogram(final TmfView view, final Composite parent) {
        fParentView = view;

        fComposite = createWidget(parent);
        fDataModel = new HistogramDataModel();
        fDataModel.addHistogramListener(this);
        clear();

        fCanvas.addControlListener(this);
        fCanvas.addPaintListener(this);
        fCanvas.addKeyListener(this);
        fCanvas.addMouseListener(this);
        fCanvas.addMouseTrackListener(this);
        fCanvas.addMouseMoveListener(this);

        TmfSignalManager.register(this);
    }

    /**
     * Dispose resources and unregisters listeners.
     */
    public void dispose() {
        TmfSignalManager.deregister(this);
        fLostEventColor.dispose();
        for (Color c : fHistoBarColors) {
            c.dispose();
        }
        fTimeRangeColor.dispose();
        fFont.dispose();
        fDataModel.removeHistogramListener(this);
        fDataModel.dispose();
    }

    private Composite createWidget(final Composite parent) {

        fFont = adjustFont(parent);

        final int initalWidth = 10;

        // --------------------------------------------------------------------
        // Define the histogram
        // --------------------------------------------------------------------

        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.marginTop = 0;
        gridLayout.horizontalSpacing = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginLeft = 0;
        gridLayout.marginRight = 0;
        final Composite composite = new Composite(parent, SWT.FILL);
        composite.setLayout(gridLayout);

        // Use all the horizontal space
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        composite.setLayoutData(gridData);

        // Y-axis max event
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.RIGHT;
        gridData.verticalAlignment = SWT.TOP;
        fMaxNbEventsLabel = new Label(composite, SWT.RIGHT);
        fMaxNbEventsLabel.setFont(fFont);
        fMaxNbEventsLabel.setText("0"); //$NON-NLS-1$
        fMaxNbEventsLabel.setLayoutData(gridData);

        // Histogram itself
        Composite canvasComposite = new Composite(composite, SWT.BORDER);
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.verticalSpan = 2;
        gridData.horizontalAlignment = SWT.FILL;
        gridData.verticalAlignment = SWT.FILL;
        gridData.heightHint = 0;
        gridData.widthHint = 0;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        canvasComposite.setLayoutData(gridData);
        canvasComposite.setLayout(new FillLayout());
        fCanvas = new Canvas(canvasComposite, SWT.DOUBLE_BUFFERED);
        fCanvas.addDisposeListener(new DisposeListener() {
            @Override
            public void widgetDisposed(DisposeEvent e) {
                Object image = fCanvas.getData(IMAGE_KEY);
                if (image instanceof Image) {
                    ((Image) image).dispose();
                }
            }
        });

        // Y-axis min event (always 0...)
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.RIGHT;
        gridData.verticalAlignment = SWT.BOTTOM;
        fMinNbEventsLabel = new Label(composite, SWT.RIGHT);
        fMinNbEventsLabel.setFont(fFont);
        fMinNbEventsLabel.setText("0"); //$NON-NLS-1$
        fMinNbEventsLabel.setLayoutData(gridData);

        // Dummy cell
        gridData = new GridData(initalWidth, SWT.DEFAULT);
        gridData.horizontalAlignment = SWT.RIGHT;
        gridData.verticalAlignment = SWT.BOTTOM;
        final Label dummyLabel = new Label(composite, SWT.NONE);
        dummyLabel.setLayoutData(gridData);

        // Window range start time
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.LEFT;
        gridData.verticalAlignment = SWT.BOTTOM;
        fTimeRangeStartLabel = new Label(composite, SWT.NONE);
        fTimeRangeStartLabel.setFont(fFont);
        fTimeRangeStartLabel.setLayoutData(gridData);

        // Window range end time
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.RIGHT;
        gridData.verticalAlignment = SWT.BOTTOM;
        fTimeRangeEndLabel = new Label(composite, SWT.NONE);
        fTimeRangeEndLabel.setFont(fFont);
        fTimeRangeEndLabel.setLayoutData(gridData);

        return composite;
    }

    private static Font adjustFont(final Composite composite) {
        // Reduce font size for a more pleasing rendering
        final int fontSizeAdjustment = -2;
        final Font font = composite.getFont();
        final FontData fontData = font.getFontData()[0];
        return new Font(font.getDevice(), fontData.getName(), fontData.getHeight() + fontSizeAdjustment, fontData.getStyle());
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the start time (equal first bucket time)
     * @return the start time.
     */
    public long getStartTime() {
        return fDataModel.getFirstBucketTime();
    }

    /**
     * Returns the end time.
     * @return the end time.
     */
    public long getEndTime() {
        return fDataModel.getEndTime();
    }

    /**
     * Returns the time limit (end of last bucket)
     * @return the time limit.
     */
    public long getTimeLimit() {
        return fDataModel.getTimeLimit();
    }

    /**
     * Returns a data model reference.
     * @return data model.
     */
    public HistogramDataModel getDataModel() {
        return fDataModel;
    }

    /**
     * Set the max number events to be displayed
     *
     * @param maxNbEvents
     *            the maximum number of events
     */
    void setMaxNbEvents(long maxNbEvents) {
        fMaxNbEventsLabel.setText(Long.toString(maxNbEvents));
        fMaxNbEventsLabel.getParent().layout();
        fCanvas.redraw();
    }

    /**
     * Return <code>true</code> if the traces must be displayed in the histogram,
     * <code>false</code> otherwise.
     * @return whether the traces should be displayed
     * @since 3.0
     */
    public boolean showTraces() {
        return showTraces && fDataModel.getNbTraces() < getMaxNbTraces();
    }

    /**
     * Returns the maximum number of traces the histogram can display with separate colors.
     * If there is more traces, histogram will use only one color to display them.
     * @return the maximum number of traces the histogram can display.
     * @since 3.0
     */
    public int getMaxNbTraces() {
        return fHistoBarColors.length;
    }

    /**
     * Returns the color used to display the trace at the given index.
     * @param traceIndex a trace index
     * @return a {@link Color}
     * @since 3.0
     */
    public Color getTraceColor(int traceIndex) {
        return fHistoBarColors[traceIndex % fHistoBarColors.length];
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    /**
     * Updates the time range.
     * @param startTime A start time
     * @param endTime A end time.
     */
    public void updateTimeRange(long startTime, long endTime) {
        if (fDragState == DRAG_NONE) {
            ((HistogramView) fParentView).updateTimeRange(startTime, endTime);
        }
    }

    /**
     * Clear the histogram and reset the data
     */
    public void clear() {
        fDataModel.clear();
        if (fDragState == DRAG_SELECTION) {
            updateSelectionTime();
        }
        fDragState = DRAG_NONE;
        fDragButton = 0;
        synchronized (fDataModel) {
            fScaledData = null;
        }
    }

    /**
     * Sets the current selection time range and refresh the display
     *
     * @param beginTime The begin time of the current selection
     * @param endTime The end time of the current selection
     * @since 2.1
     */
    public void setSelection(final long beginTime, final long endTime) {
        fSelectionBegin = (beginTime > 0) ? beginTime : 0;
        fSelectionEnd = (endTime > 0) ? endTime : 0;
        fDataModel.setSelectionNotifyListeners(beginTime, endTime);
    }

    /**
     * Computes the timestamp of the bucket at [offset]
     *
     * @param offset offset from the left on the histogram
     * @return the start timestamp of the corresponding bucket
     */
    public synchronized long getTimestamp(final int offset) {
        assert offset > 0 && offset < fScaledData.fWidth;
        try {
            return fScaledData.fFirstBucketTime + fScaledData.fBucketDuration * offset;
        } catch (final Exception e) {
            return 0; // TODO: Fix that racing condition (NPE)
        }
    }

    /**
     * Computes the offset of the timestamp in the histogram
     *
     * @param timestamp the timestamp
     * @return the offset of the corresponding bucket (-1 if invalid)
     */
    public synchronized int getOffset(final long timestamp) {
        if (timestamp < fDataModel.getFirstBucketTime() || timestamp > fDataModel.getEndTime()) {
            return -1;
        }
        return (int) ((timestamp - fDataModel.getFirstBucketTime()) / fScaledData.fBucketDuration);
    }

    /**
     * Set the bucket display offset
     *
     * @param offset
     *            the bucket display offset
     * @since 2.2
     */
    protected void setOffset(final int offset) {
        fOffset = offset;
    }

    /**
     * Move the currently selected bar cursor to a non-empty bucket.
     *
     * @param keyCode the SWT key code
     */
    protected void moveCursor(final int keyCode) {

        int index;
        switch (keyCode) {

        case SWT.HOME:
            index = 0;
            while (index < fScaledData.fLastBucket && fScaledData.fData[index].isEmpty()) {
                index++;
            }
            if (index < fScaledData.fLastBucket) {
                fScaledData.fSelectionBeginBucket = index;
            }
            break;

        case SWT.ARROW_RIGHT:
            index = Math.max(0, fScaledData.fSelectionBeginBucket + 1);
            while (index < fScaledData.fWidth && fScaledData.fData[index].isEmpty()) {
                index++;
            }
            if (index < fScaledData.fLastBucket) {
                fScaledData.fSelectionBeginBucket = index;
            }
            break;

        case SWT.END:
            index = fScaledData.fLastBucket;
            while (index >= 0 && fScaledData.fData[index].isEmpty()) {
                index--;
            }
            if (index >= 0) {
                fScaledData.fSelectionBeginBucket = index;
            }
            break;

        case SWT.ARROW_LEFT:
            index = Math.min(fScaledData.fLastBucket - 1, fScaledData.fSelectionBeginBucket - 1);
            while (index >= 0 && fScaledData.fData[index].isEmpty()) {
                index--;
            }
            if (index >= 0) {
                fScaledData.fSelectionBeginBucket = index;
            }
            break;

        default:
            return;
        }

        fScaledData.fSelectionEndBucket = fScaledData.fSelectionBeginBucket;
        fSelectionBegin = getTimestamp(fScaledData.fSelectionBeginBucket);
        fSelectionEnd = fSelectionBegin;
        updateSelectionTime();
    }

    /**
     * Refresh the histogram display
     */
    @Override
    public void modelUpdated() {
        if (!fCanvas.isDisposed() && fCanvas.getDisplay() != null) {
            fCanvas.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (!fCanvas.isDisposed()) {
                        // Retrieve and normalize the data
                        final int canvasWidth = fCanvas.getBounds().width;
                        final int canvasHeight = fCanvas.getBounds().height;
                        if (canvasWidth <= 0 || canvasHeight <= 0) {
                            return;
                        }
                        fDataModel.setSelection(fSelectionBegin, fSelectionEnd);
                        fScaledData = fDataModel.scaleTo(canvasWidth, canvasHeight, 1);
                        synchronized (fDataModel) {
                            if (fScaledData != null) {
                                fCanvas.redraw();
                                // Display histogram and update X-,Y-axis labels
                                updateRangeTextControls();
                                long maxNbEvents = HistogramScaledData.hideLostEvents ? fScaledData.fMaxValue : fScaledData.fMaxCombinedValue;
                                fMaxNbEventsLabel.setText(Long.toString(maxNbEvents));
                                // The Y-axis area might need to be re-sized
                                GridData gd = (GridData) fMaxNbEventsLabel.getLayoutData();
                                gd.widthHint = Math.max(gd.widthHint, fMaxNbEventsLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x);
                                fMaxNbEventsLabel.getParent().layout();
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * Add a mouse wheel listener to the histogram
     * @param listener the mouse wheel listener
     * @since 2.0
     */
    public void addMouseWheelListener(MouseWheelListener listener) {
        fCanvas.addMouseWheelListener(listener);
    }

    /**
     * Remove a mouse wheel listener from the histogram
     * @param listener the mouse wheel listener
     * @since 2.0
     */
    public void removeMouseWheelListener(MouseWheelListener listener) {
        fCanvas.removeMouseWheelListener(listener);
    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private void updateSelectionTime() {
        if (fSelectionBegin > fSelectionEnd) {
            long end = fSelectionBegin;
            fSelectionBegin = fSelectionEnd;
            fSelectionEnd = end;
        }
        ((HistogramView) fParentView).updateSelectionTime(fSelectionBegin, fSelectionEnd);
    }

    /**
     * Update the range text controls
     */
    private void updateRangeTextControls() {
        if (fDataModel.getStartTime() < fDataModel.getEndTime()) {
            fTimeRangeStartLabel.setText(TmfTimestampFormat.getDefaulTimeFormat().format(fDataModel.getStartTime()));
            fTimeRangeEndLabel.setText(TmfTimestampFormat.getDefaulTimeFormat().format(fDataModel.getEndTime()));
        } else {
            fTimeRangeStartLabel.setText(""); //$NON-NLS-1$
            fTimeRangeEndLabel.setText(""); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // PaintListener
    // ------------------------------------------------------------------------
    /**
     * Image key string for the canvas.
     */
    protected final String IMAGE_KEY = "double-buffer-image"; //$NON-NLS-1$

    @Override
    public void paintControl(final PaintEvent event) {

        // Get the geometry
        final int canvasWidth = fCanvas.getBounds().width;
        final int canvasHeight = fCanvas.getBounds().height;

        // Make sure we have something to draw upon
        if (canvasWidth <= 0 || canvasHeight <= 0) {
            return;
        }

        // Retrieve image; re-create only if necessary
        Image image = (Image) fCanvas.getData(IMAGE_KEY);
        if (image == null || image.getBounds().width != canvasWidth || image.getBounds().height != canvasHeight) {
            if (image != null) {
                image.dispose();
            }
            image = new Image(event.display, canvasWidth, canvasHeight);
            fCanvas.setData(IMAGE_KEY, image);
        }

        // Draw the histogram on its canvas
        final GC imageGC = new GC(image);
        formatImage(imageGC, image);
        event.gc.drawImage(image, 0, 0);
        imageGC.dispose();
    }

    private void formatImage(final GC imageGC, final Image image) {

        if (fScaledData == null) {
            return;
        }

        final HistogramScaledData scaledData = new HistogramScaledData(fScaledData);

        try {
            // Get drawing boundaries
            final int width = image.getBounds().width;
            final int height = image.getBounds().height;

            // Clear the drawing area
            imageGC.setBackground(fBackgroundColor);
            imageGC.fillRectangle(0, 0, image.getBounds().width + 1, image.getBounds().height + 1);

            // Draw the histogram bars
            final int limit = width < scaledData.fWidth ? width : scaledData.fWidth;
            double factor = HistogramScaledData.hideLostEvents ? scaledData.fScalingFactor : scaledData.fScalingFactorCombined;
            final boolean showTracesColors = showTraces();
            for (int i = 0; i < limit; i++) {
                HistogramBucket hb = scaledData.fData[i];
                int totalNbEvents = hb.getNbEvents();
                int value = (int) Math.ceil(totalNbEvents * factor);
                int x = i + fOffset;

                // in Linux, the last pixel in a line is not drawn,
                // so draw lost events first, one pixel too far
                if (!HistogramScaledData.hideLostEvents) {
                    imageGC.setForeground(fLostEventColor);
                    final int lostEventValue = (int) Math.ceil(scaledData.fLostEventsData[i] * factor);
                    if (lostEventValue != 0) {
                        // drawing a line is inclusive, so we should remove 1 from y2
                        // but we don't because Linux
                        imageGC.drawLine(x, height - value - lostEventValue, x, height - value);
                    }
                }

                // then draw normal events second, to overwrite that extra pixel
                if (!hb.isEmpty()) {
                    if (showTracesColors) {
                        for (int traceIndex = 0; traceIndex < hb.getNbTraces(); traceIndex++) {
                            int nbEventsForTrace = hb.getNbEvent(traceIndex);
                            if (nbEventsForTrace > 0) {
                                Color c = fHistoBarColors[traceIndex % fHistoBarColors.length];
                                imageGC.setForeground(c);
                                imageGC.drawLine(x, height - value, x, height);
                                totalNbEvents -= nbEventsForTrace;
                                value = (int) Math.ceil(totalNbEvents * scaledData.fScalingFactor);
                            }
                        }
                    } else {
                        Color c = fHistoBarColors[0];
                        imageGC.setForeground(c);
                        imageGC.drawLine(x, height - value, x, height);
                    }
                }
            }

            // Draw the selection bars
            int alpha = imageGC.getAlpha();
            imageGC.setAlpha(100);
            imageGC.setForeground(fSelectionForegroundColor);
            imageGC.setBackground(fSelectionBackgroundColor);
            final int beginBucket = scaledData.fSelectionBeginBucket + fOffset;
            if (beginBucket >= 0 && beginBucket < limit) {
                imageGC.drawLine(beginBucket, 0, beginBucket, height);
            }
            final int endBucket = scaledData.fSelectionEndBucket + fOffset;
            if (endBucket >= 0 && endBucket < limit && endBucket != beginBucket) {
                imageGC.drawLine(endBucket, 0, endBucket, height);
            }
            if (Math.abs(endBucket - beginBucket) > 1) {
                if (endBucket > beginBucket) {
                    imageGC.fillRectangle(beginBucket + 1, 0, endBucket - beginBucket - 1, height);
                } else {
                    imageGC.fillRectangle(endBucket + 1, 0, beginBucket - endBucket - 1, height);
                }
            }
            imageGC.setAlpha(alpha);

            // Add a dashed line as a delimiter
            int delimiterIndex = (int) ((getDataModel().getEndTime() - scaledData.getFirstBucketTime()) / scaledData.fBucketDuration) + 1;
            drawDelimiter(imageGC, fLastEventColor, height, delimiterIndex);

            // Fill the area to the right of delimiter with background color
            imageGC.setBackground(fComposite.getBackground());
            imageGC.fillRectangle(delimiterIndex + 1, 0, width - (delimiterIndex + 1), height);

        } catch (final Exception e) {
            // Do nothing
        }
    }

    private static void drawDelimiter(final GC imageGC, final Color color,
            final int height, final int index) {
        imageGC.setBackground(color);
        final int dash = height / 4;
        imageGC.fillRectangle(index, 0 * dash, 1, dash - 1);
        imageGC.fillRectangle(index, 1 * dash, 1, dash - 1);
        imageGC.fillRectangle(index, 2 * dash, 1, dash - 1);
        imageGC.fillRectangle(index, 3 * dash, 1, height - 3 * dash);
    }

    /**
     * Draw a time range window
     *
     * @param imageGC
     *            the GC
     * @param rangeStartTime
     *            the range start time
     * @param rangeDuration
     *            the range duration
     * @since 2.2
     */
    protected void drawTimeRangeWindow(GC imageGC, long rangeStartTime, long rangeDuration) {

        if (fScaledData == null) {
            return;
        }

        // Map times to histogram coordinates
        long bucketSpan = Math.max(fScaledData.fBucketDuration, 1);
        long startTime = Math.min(rangeStartTime, rangeStartTime + rangeDuration);
        int rangeWidth = (int) (Math.abs(rangeDuration) / bucketSpan);

        int left = (int) ((startTime - fDataModel.getFirstBucketTime()) / bucketSpan);
        int right = left + rangeWidth;
        int center = (left + right) / 2;
        int height = fCanvas.getSize().y;
        int arc = Math.min(15, rangeWidth);

        // Draw the selection window
        imageGC.setForeground(fTimeRangeColor);
        imageGC.setLineWidth(1);
        imageGC.setLineStyle(SWT.LINE_SOLID);
        imageGC.drawRoundRectangle(left, 0, rangeWidth, height - 1, arc, arc);

        // Fill the selection window
        imageGC.setBackground(fTimeRangeColor);
        imageGC.setAlpha(35);
        imageGC.fillRoundRectangle(left + 1, 1, rangeWidth - 1, height - 2, arc, arc);
        imageGC.setAlpha(255);

        // Draw the cross hair
        imageGC.setForeground(fTimeRangeColor);
        imageGC.setLineWidth(1);
        imageGC.setLineStyle(SWT.LINE_SOLID);

        int chHalfWidth = ((rangeWidth < 60) ? (rangeWidth * 2) / 3 : 40) / 2;
        imageGC.drawLine(center - chHalfWidth, height / 2, center + chHalfWidth, height / 2);
        imageGC.drawLine(center, (height / 2) - chHalfWidth, center, (height / 2) + chHalfWidth);
    }

    // ------------------------------------------------------------------------
    // KeyListener
    // ------------------------------------------------------------------------

    @Override
    public void keyPressed(final KeyEvent event) {
        moveCursor(event.keyCode);
    }

    @Override
    public void keyReleased(final KeyEvent event) {
    }

    // ------------------------------------------------------------------------
    // MouseListener
    // ------------------------------------------------------------------------

    @Override
    public void mouseDoubleClick(final MouseEvent event) {
    }

    @Override
    public void mouseDown(final MouseEvent event) {
        if (fScaledData != null && event.button == 1 && fDragState == DRAG_NONE && fDataModel.getStartTime() < fDataModel.getEndTime()) {
            fDragState = DRAG_SELECTION;
            fDragButton = event.button;
            if ((event.stateMask & SWT.MODIFIER_MASK) == SWT.SHIFT) {
                if (Math.abs(event.x - fScaledData.fSelectionBeginBucket) < Math.abs(event.x - fScaledData.fSelectionEndBucket)) {
                    fScaledData.fSelectionBeginBucket = fScaledData.fSelectionEndBucket;
                    fSelectionBegin = fSelectionEnd;
                }
                fSelectionEnd = Math.min(getTimestamp(event.x), getEndTime());
                fScaledData.fSelectionEndBucket = (int) ((fSelectionEnd - fScaledData.fFirstBucketTime) / fScaledData.fBucketDuration);
            } else {
                fSelectionBegin = Math.min(getTimestamp(event.x), getEndTime());
                fScaledData.fSelectionBeginBucket = (int) ((fSelectionBegin - fScaledData.fFirstBucketTime) / fScaledData.fBucketDuration);
                fSelectionEnd = fSelectionBegin;
                fScaledData.fSelectionEndBucket = fScaledData.fSelectionBeginBucket;
            }
            fCanvas.redraw();
        }
    }

    @Override
    public void mouseUp(final MouseEvent event) {
        if (fDragState == DRAG_SELECTION && event.button == fDragButton) {
            fDragState = DRAG_NONE;
            fDragButton = 0;
            updateSelectionTime();
        }
    }

    // ------------------------------------------------------------------------
    // MouseMoveListener
    // ------------------------------------------------------------------------

    /**
     * @since 2.2
     */
    @Override
    public void mouseMove(MouseEvent event) {
        if (fDragState == DRAG_SELECTION && fDataModel.getStartTime() < fDataModel.getEndTime()) {
            fSelectionEnd = Math.max(getStartTime(), Math.min(getEndTime(), getTimestamp(event.x)));
            fScaledData.fSelectionEndBucket = (int) ((fSelectionEnd - fScaledData.fFirstBucketTime) / fScaledData.fBucketDuration);
            fCanvas.redraw();
        }
    }

    // ------------------------------------------------------------------------
    // MouseTrackListener
    // ------------------------------------------------------------------------

    @Override
    public void mouseEnter(final MouseEvent event) {
    }

    @Override
    public void mouseExit(final MouseEvent event) {
    }

    @Override
    public void mouseHover(final MouseEvent event) {
        if (fDataModel.getStartTime() < fDataModel.getEndTime() && fScaledData != null) {
            int delimiterIndex = (int) ((fDataModel.getEndTime() - fScaledData.getFirstBucketTime()) / fScaledData.fBucketDuration) + 1;
            if (event.x < delimiterIndex) {
                final String tooltip = formatToolTipLabel(event.x - fOffset);
                fCanvas.setToolTipText(tooltip);
                return;
            }
        }
        fCanvas.setToolTipText(null);
    }

    private String formatToolTipLabel(final int index) {
        long startTime = fScaledData.getBucketStartTime(index);
        // negative values are possible if time values came into the model in decreasing order
        if (startTime < 0) {
            startTime = 0;
        }
        final long endTime = fScaledData.getBucketEndTime(index);
        final int nbEvents = (index >= 0) ? fScaledData.fData[index].getNbEvents() : 0;
        final String newLine = System.getProperty("line.separator"); //$NON-NLS-1$
        final StringBuffer buffer = new StringBuffer();
        int selectionBeginBucket = Math.min(fScaledData.fSelectionBeginBucket, fScaledData.fSelectionEndBucket);
        int selectionEndBucket = Math.max(fScaledData.fSelectionBeginBucket, fScaledData.fSelectionEndBucket);
        if (selectionBeginBucket <= index && index <= selectionEndBucket && fSelectionBegin != fSelectionEnd) {
            TmfTimestampDelta delta = new TmfTimestampDelta(Math.abs(fSelectionEnd - fSelectionBegin), ITmfTimestamp.NANOSECOND_SCALE);
            buffer.append(NLS.bind(Messages.Histogram_selectionSpanToolTip, delta.toString()));
            buffer.append(newLine);
        }
        buffer.append(NLS.bind(Messages.Histogram_bucketRangeToolTip,
                new TmfTimestamp(startTime, ITmfTimestamp.NANOSECOND_SCALE).toString(),
                new TmfTimestamp(endTime, ITmfTimestamp.NANOSECOND_SCALE).toString()));
        buffer.append(newLine);
        buffer.append(NLS.bind(Messages.Histogram_eventCountToolTip, nbEvents));
        if (!HistogramScaledData.hideLostEvents) {
            final int nbLostEvents = (index >= 0) ? fScaledData.fLostEventsData[index] : 0;
            buffer.append(newLine);
            buffer.append(NLS.bind(Messages.Histogram_lostEventCountToolTip, nbLostEvents));
        }
        return buffer.toString();
    }

    // ------------------------------------------------------------------------
    // ControlListener
    // ------------------------------------------------------------------------

    @Override
    public void controlMoved(final ControlEvent event) {
        fDataModel.complete();
    }

    @Override
    public void controlResized(final ControlEvent event) {
        fDataModel.complete();
    }

    // ------------------------------------------------------------------------
    // Signal Handlers
    // ------------------------------------------------------------------------

    /**
     * Format the timestamp and update the display
     *
     * @param signal
     *            the incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void timestampFormatUpdated(TmfTimestampFormatUpdateSignal signal) {
        updateRangeTextControls();

        fComposite.layout();
    }

}
