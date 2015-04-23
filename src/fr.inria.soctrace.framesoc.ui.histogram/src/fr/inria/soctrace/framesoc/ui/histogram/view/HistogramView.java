/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.histogram.view;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.IntervalMarker;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.Range;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.jfree.ui.RectangleInsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.histogram.loaders.DensityHistogramLoader;
import fr.inria.soctrace.framesoc.ui.histogram.model.HistogramLoaderDataset;
import fr.inria.soctrace.framesoc.ui.model.ColorsChangeDescriptor;
import fr.inria.soctrace.framesoc.ui.model.GanttTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.PieTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TableTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPart;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.framesoc.ui.providers.SquareIconLabelProvider;
import fr.inria.soctrace.framesoc.ui.treefilter.FilterDataManager;
import fr.inria.soctrace.framesoc.ui.treefilter.FilterDimension;
import fr.inria.soctrace.framesoc.ui.treefilter.FilterDimensionData;
import fr.inria.soctrace.framesoc.ui.treefilter.ProducerFilterData;
import fr.inria.soctrace.framesoc.ui.treefilter.TypeFilterData;
import fr.inria.soctrace.framesoc.ui.utils.TimeBar;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.model.utils.TimestampFormat;
import fr.inria.soctrace.lib.model.utils.TimestampFormat.TickDescriptor;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Framesoc Bar Chart view.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class HistogramView extends FramesocPart {

	private class HistogramFilterData extends FilterDataManager {

		public HistogramFilterData(FilterDimensionData dimension) {
			super(dimension);
		}

		@Override
		public void reloadAfterChange() {
			loadHistogram(currentShownTrace, loadedInterval);
		}
	}

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = FramesocViews.HISTOGRAM_VIEW_ID;

	/**
	 * Logger
	 */
	public static final Logger logger = LoggerFactory.getLogger(HistogramView.class);

	/*
	 * Constants
	 */
	private static final String TOOLTIP_FORMAT = "bin central timestamp: {1}, events: {2}";
	private static final String HISTOGRAM_TITLE = "";
	private static final String X_LABEL = "";
	private static final String Y_LABEL = "";
	private static final boolean HAS_LEGEND = false;
	private static final boolean HAS_TOOLTIPS = true;
	private static final boolean HAS_URLS = true;
	private static final boolean USE_BUFFER = true;
	private static final Color BACKGROUND_PAINT = new Color(255, 255, 255);
	private static final Color DOMAIN_GRIDLINE_PAINT = new Color(230, 230, 230);
	private static final Color RANGE_GRIDLINE_PAINT = new Color(200, 200, 200);
	private static final Color MARKER_OUTLINE_PAINT = new Color(0, 0, 255);
	private static final int TIMESTAMP_MAX_SIZE = 130;
	private static final long BUILD_UPDATE_TIMEOUT = 300;
	private static final int TOTAL_WORK = 1000;
	private static final int NO_STATUS = -1;
	private static final Cursor ARROW_CURSOR = new Cursor(Display.getDefault(), SWT.CURSOR_ARROW);
	private static final Cursor IBEAM_CURSOR = new Cursor(Display.getDefault(), SWT.CURSOR_IBEAM);

	private final Font TICK_LABEL_FONT = new Font("Tahoma", 0, 11);
	private final Font LABEL_FONT = new Font("Tahoma", 0, 12);
	private final TimestampFormat X_FORMAT = new TimestampFormat();
	private final DecimalFormat Y_FORMAT = new DecimalFormat("0");
	private final XYToolTipGenerator TOOLTIP_GENERATOR = new StandardXYToolTipGenerator(
			TOOLTIP_FORMAT, X_FORMAT, Y_FORMAT);

	/*
	 * UI components
	 */
	private Composite compositeChart;
	private ChartComposite chartFrame;
	protected XYPlot plot;
	private TimeBar timeBar;
	private IntervalMarker marker;

	private List<SquareIconLabelProvider> labelProviders = new ArrayList<>();

	/*
	 * Loading data and configuration
	 */
	private TimeInterval requestedInterval;
	private TimeInterval loadedInterval;
	private Map<FilterDimension, HistogramFilterData> configurationMap;
	private HistogramLoaderDataset dataset;

	/*
	 * Timestamp management
	 */
	private int numberOfTicks = 10;
	private IStatusLineManager statusLineManager;

	/*
	 * Selection
	 */
	private boolean activeSelection = false;
	private boolean dragInProgress = false;
	private long selectedTs0 = Long.MAX_VALUE;
	private long selectedTs1 = Long.MIN_VALUE;

	public HistogramView() {
		super();
		topics.addTopic(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED);
		topics.registerAll();
		configurationMap = new HashMap<>();
		configurationMap.put(FilterDimension.PRODUCERS, new HistogramFilterData(
				new ProducerFilterData()));
		configurationMap.put(FilterDimension.TYPE, new HistogramFilterData(new TypeFilterData()));
	}

	/* Uncomment this to use the window builder */
	// public void createPartControl(Composite parent) {
	// createFramesocPartControl(parent);
	// }

	@Override
	public void createFramesocPartControl(Composite parent) {

		statusLineManager = getViewSite().getActionBars().getStatusLineManager();

		// parent layout
		GridLayout gl_parent = new GridLayout(1, false);
		gl_parent.verticalSpacing = 2;
		gl_parent.marginWidth = 0;
		gl_parent.horizontalSpacing = 0;
		gl_parent.marginHeight = 0;
		parent.setLayout(gl_parent);

		// Chart Composite
		compositeChart = new Composite(parent, SWT.BORDER);
		compositeChart.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		FillLayout fl_compositeChart = new FillLayout(SWT.HORIZONTAL);
		compositeChart.setLayout(fl_compositeChart);
		compositeChart.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				int width = Math.max(compositeChart.getSize().x - 40, 1);
				numberOfTicks = Math.max(width / TIMESTAMP_MAX_SIZE, 1);
				refresh(false, false, true);
			}
		});

		// Time management bar
		Composite timeComposite = new Composite(parent, SWT.BORDER);
		timeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayout gl_timeComposite = new GridLayout(1, false);
		gl_timeComposite.horizontalSpacing = 0;
		timeComposite.setLayout(gl_timeComposite);
		// time manager
		timeBar = new TimeBar(timeComposite, SWT.NONE, true, true);
		timeBar.setEnabled(false);
		IStatusLineManager statusLineManager = getViewSite().getActionBars().getStatusLineManager();
		timeBar.setStatusLineManager(statusLineManager);
		timeBar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TimeInterval barInterval = timeBar.getSelection();
				if (marker == null) {
					addNewMarker(barInterval.startTimestamp, barInterval.endTimestamp);
				} else {
					marker.setStartValue(barInterval.startTimestamp);
					marker.setEndValue(barInterval.endTimestamp);
				}
				selectedTs0 = barInterval.startTimestamp;
				selectedTs1 = barInterval.endTimestamp;
			}
		});
		// button to synch the timebar, producers and type with the current loaded data
		timeBar.getSynchButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (loadedInterval != null) {
					timeBar.setSelection(loadedInterval);
					if (marker != null && plot != null) {
						plot.removeDomainMarker(marker);
						marker = null;
					}
				}
			}
		});
		// load button
		timeBar.getLoadButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!timeBar.getSelection().equals(loadedInterval)) {
					loadHistogram(currentShownTrace, timeBar.getSelection());
				}
			}
		});

		// ----------
		// TOOL BAR
		// ----------

		// filters and actions
		initFilterDialogs();
		createActions();

	}

	private void initFilterData(Trace t) {
		for (HistogramFilterData data : configurationMap.values()) {
			try {
				data.setFilterRoots(DensityHistogramLoader.loadDimension(data.getDimension(), t));
			} catch (SoCTraceException e) {
				e.printStackTrace();
			}
		}
	}

	private void initFilterDialogs() {
		for (HistogramFilterData data : configurationMap.values()) {
			data.initFilterDialog(getSite().getShell());
		}
	}

	private void createActions() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();

		// Filters actions
		manager.add(configurationMap.get(FilterDimension.PRODUCERS).initFilterAction());
		manager.add(configurationMap.get(FilterDimension.TYPE).initFilterAction());

		// Separator
		manager.add(new Separator());

		// Framesoc Actions
		TableTraceIntervalAction.add(manager, createTableAction());
		GanttTraceIntervalAction.add(manager, createGanttAction());
		PieTraceIntervalAction.add(manager, createPieAction());

		// disable all actions
		enableActions(false);
	}

	@Override
	public void setFocus() {
		super.setFocus();
	}

	@Override
	public void dispose() {
		plot = null;
		super.dispose();
	}

	protected TraceIntervalDescriptor getIntervalDescriptor() {
		if (currentShownTrace == null || loadedInterval == null)
			return null;
		TraceIntervalDescriptor des = new TraceIntervalDescriptor();
		des.setTrace(currentShownTrace);
		des.setTimeInterval(loadedInterval);
		return des;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void showTrace(final Trace trace, Object data) {

		if (trace == null)
			return;

		initFilterData(trace);

		if (data == null) {
			// called after right click on trace tree menu
			loadHistogram(trace, new TimeInterval(trace.getMinTimestamp(), trace.getMaxTimestamp()));
		} else {
			// called after double click on trace tree or a Framesoc bus message
			// coming from a "show in" action
			TraceIntervalDescriptor des = (TraceIntervalDescriptor) data;
			TimeInterval desInterval = des.getTimeInterval();
			if (desInterval.equals(TimeInterval.NOT_SPECIFIED)) {
				// double click
				if ((currentShownTrace != null && currentShownTrace.equals(trace))) {
					// same trace: keep interval and configuration
					return;
				}
				if (currentShownTrace == null) {
					// load the whole trace
					Trace t = des.getTrace();
					desInterval = new TimeInterval(t.getMinTimestamp(), t.getMaxTimestamp());
				}
			}

			if (loadedInterval == null || (!loadedInterval.equals(desInterval))) {
				loadHistogram(des.getTrace(), desInterval);
			}

		}
	}

	/**
	 * Load the histogram using the current trace and the information in the type and producer
	 * trees.
	 * 
	 * @param trace
	 *            trace to load
	 * @param interval
	 *            time interval to load
	 */
	public void loadHistogram(final Trace trace, final TimeInterval interval) {

		currentShownTrace = trace;
		// set time unit and extrema
		timeBar.setTimeUnit(TimeUnit.getTimeUnit(trace.getTimeUnit()));
		timeBar.setExtrema(trace.getMinTimestamp(), trace.getMaxTimestamp());
		// nothing is loaded so far, so the interval is [start, start] (duration 0)
		loadedInterval = new TimeInterval(interval.startTimestamp, interval.startTimestamp);
		requestedInterval = new TimeInterval(interval);

		Thread showThread = new Thread() {
			@Override
			public void run() {
				// prepare the configuration for the loader
				Map<FilterDimension, List<Integer>> confMap = new HashMap<>();
				for (HistogramFilterData data : configurationMap.values()) {
					confMap.put(data.getDimension(), data.getCheckedId());
				}

				// create a new loader dataset
				dataset = new HistogramLoaderDataset();

				// create loader thread and drawer job
				LoaderThread loaderThread = new LoaderThread(interval,
						confMap.get(FilterDimension.TYPE), confMap.get(FilterDimension.PRODUCERS));
				DrawerJob drawerJob = new DrawerJob("Event Density Chart Job", loaderThread);
				loaderThread.start();
				drawerJob.schedule();
			}
		};
		showThread.start();
	}

	/**
	 * Loader thread.
	 */
	private class LoaderThread extends Thread {

		private final TimeInterval interval;
		private final IProgressMonitor monitor;
		private List<Integer> types;
		private List<Integer> producer;

		public LoaderThread(TimeInterval interval, List<Integer> types, List<Integer> producers) {
			this.interval = interval;
			this.types = types;
			this.producer = producers;
			this.monitor = new NullProgressMonitor();
		}

		@Override
		public void run() {
			DensityHistogramLoader.load(currentShownTrace, interval, types, producer, dataset,
					monitor);
		}

		public void cancel() {
			monitor.setCanceled(true);
		}
	}

	/**
	 * Drawer job.
	 */
	private class DrawerJob extends Job {

		private final LoaderThread loaderThread;

		public DrawerJob(String name, LoaderThread loaderThread) {
			super(name);
			this.loaderThread = loaderThread;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			DeltaManager dm = new DeltaManager();
			dm.start();
			monitor.beginTask("Loading trace " + currentShownTrace.getAlias(), TOTAL_WORK);
			try {
				disableButtons();
				boolean done = false;
				boolean first = true;
				long oldLoadedEnd = 0;
				while (!done) {
					done = dataset.waitUntilDone(BUILD_UPDATE_TIMEOUT);
					if (!dataset.isDirty()) {
						continue;
					}
					if (monitor.isCanceled()) {
						loaderThread.cancel();
						logger.debug("Drawer job cancelled");
						// refresh one last time
						refresh(first, true, false);
						first = false;
						return Status.CANCEL_STATUS;
					}
					oldLoadedEnd = loadedInterval.endTimestamp;
					refresh(first, false, false);
					first = false;

					double delta = loadedInterval.endTimestamp - oldLoadedEnd;
					if (delta > 0) {
						monitor.worked((int) ((delta / requestedInterval.getDuration()) * TOTAL_WORK));
					}
				}
				if (first) {
					// refresh at least once when there is no data.
					refresh(first, false, false);
				}
				return Status.OK_STATUS;
			} finally {
				enableButtons();
				logger.debug(dm.endMessage("finished drawing"));
			}
		}

		private void disableButtons() {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					timeBar.setEnabled(false);
					enableActions(false);
				}
			});
		}

		private void enableButtons() {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					enableActions(true);
					timeBar.setEnabled(true);
				}
			});
		}
	}

	/**
	 * Refresh the UI using the current dataset
	 * 
	 * @param first
	 *            flag indicating if it is the first refresh for a given load
	 */
	private void refresh(final boolean first, final boolean cancelled, final boolean keepZoom) {

		if (dataset == null) {
			return;
		}

		/*
		 * Prepare data
		 */

		DeltaManager dm = new DeltaManager();
		dm.start();
		// get the last snapshot
		HistogramDataset hdataset = dataset.getSnapshot(loadedInterval);
		// if we have not been cancelled, the x range corresponds to the requested interval
		final TimeInterval histogramInterval = new TimeInterval(requestedInterval);
		if (cancelled) {
			// we have been cancelled, the x range corresponds to the actual loaded interval
			histogramInterval.copy(loadedInterval);
		}
		if (keepZoom && plot != null) {
			long min = (long) plot.getDomainAxis().getRange().getLowerBound();
			long max = (long) plot.getDomainAxis().getRange().getUpperBound();
			TimeInterval displayed = new TimeInterval(min, max);
			histogramInterval.copy(displayed);
		}

		/*
		 * Prepare chart
		 */

		final JFreeChart chart = ChartFactory.createHistogram(HISTOGRAM_TITLE, X_LABEL, Y_LABEL,
				hdataset, PlotOrientation.VERTICAL, HAS_LEGEND, HAS_TOOLTIPS, HAS_URLS);

		// customize plot
		preparePlot(first, chart, histogramInterval);

		// display chart in UI
		displayChart(chart, histogramInterval, first);

		logger.debug(dm.endMessage("Finished refreshing"));
	}

	/**
	 * Display the chart in the UI thread, using the loaded interval.
	 * 
	 * @param chart
	 *            jfreechart chart
	 * @param histogramInterval
	 *            displayed interval
	 * @param first
	 *            flag indicating if it is the first refresh for a given load
	 */
	private void displayChart(final JFreeChart chart, final TimeInterval displayed,
			final boolean first) {
		// prepare the new histogram UI
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				// Clean parent
				for (Control c : compositeChart.getChildren()) {
					c.dispose();
				}
				// histogram chart
				chartFrame = new ChartComposite(compositeChart, SWT.NONE, chart, USE_BUFFER) {

					@Override
					public void mouseMove(MouseEvent e) {
						super.mouseMove(e);

						// update cursor
						if (!isInDataArea(e.x, e.y)) {
							getShell().setCursor(ARROW_CURSOR);
						} else {
							if (dragInProgress
									|| (activeSelection && (isNear(e.x, selectedTs0)) || isNear(
											e.x, selectedTs1))) {
								getShell().setCursor(IBEAM_CURSOR);
							} else {
								getShell().setCursor(ARROW_CURSOR);
							}
						}

						// update marker
						long v = getTimestampAt(e.x);
						if (dragInProgress) {
							// when drag is in progress, the moving side is always Ts1
							selectedTs1 = v;
							long min = Math.min(selectedTs0, selectedTs1);
							long max = Math.max(selectedTs0, selectedTs1);
							marker.setStartValue(min);
							marker.setEndValue(max);
							timeBar.setSelection(min, max);
						}

						// update status line
						updateStatusLine(v);
					}

					@Override
					public void mouseUp(MouseEvent e) {
						super.mouseUp(e);

						dragInProgress = false;
						selectedTs1 = getTimestampAt(e.x);
						if (selectedTs0 > selectedTs1) {
							// reorder Ts0 and Ts1
							long tmp = selectedTs1;
							selectedTs1 = selectedTs0;
							selectedTs0 = tmp;
						} else if (selectedTs0 == selectedTs1) {
							marker.setStartValue(selectedTs0);
							marker.setEndValue(selectedTs0);
							activeSelection = false;
							timeBar.setSelection(loadedInterval);
							updateStatusLine(selectedTs0);
						}
					}

					@Override
					public void mouseDown(MouseEvent e) {
						super.mouseDown(e);

						if (activeSelection) {
							if (isNear(e.x, selectedTs0)) {
								// swap in order to have Ts1 as moving side
								long tmp = selectedTs0;
								selectedTs0 = selectedTs1;
								selectedTs1 = tmp;
							} else if (isNear(e.x, selectedTs1)) {
								// nothing to do if the moving side is already Ts1
							} else {
								// near to no one: remove marker and add a new one
								removeMarker();
								selectedTs0 = getTimestampAt(e.x);
								addNewMarker(selectedTs0, selectedTs0);
							}
						} else {
							removeMarker();
							selectedTs0 = getTimestampAt(e.x);
							addNewMarker(selectedTs0, selectedTs0);
						}
						activeSelection = true;
						dragInProgress = true;
					}

					private boolean isNear(int pos, long value) {
						final int RANGE = 3;
						int vPos = getPosAt(value);
						if (Math.abs(vPos - pos) <= RANGE) {
							return true;
						}
						return false;
					}

					boolean isInDataArea(int x, int y) {
						if (chartFrame != null) {
							org.eclipse.swt.graphics.Rectangle swtRect = chartFrame
									.getScreenDataArea();
							Rectangle2D screenDataArea = new Rectangle();
							screenDataArea.setRect(swtRect.x, swtRect.y, swtRect.width,
									swtRect.height);
							return swtRect.contains(x, y);
						}
						return false;
					}
				};

				chartFrame.addMouseWheelListener(new MouseWheelListener() {

					@Override
					public void mouseScrolled(MouseEvent e) {
						if ((e.stateMask & SWT.CTRL) == SWT.CTRL) {
							if (e.count > 0) {
								// zoom in
								zoomChartAxis(true, e.x, e.y);
							} else {
								// zoom out
								zoomChartAxis(false, e.x, e.y);
							}
						}
					}

					private void zoomChartAxis(boolean increase, int x, int y) {
						double min = plot.getDomainAxis().getRange().getLowerBound();
						double max = plot.getDomainAxis().getRange().getUpperBound();
						X_FORMAT.setContext((long) min, (long) max);
						Point2D p = chartFrame.translateScreenToJava2D(new Point(x, y));
						PlotRenderingInfo plotInfo = chartFrame.getChartRenderingInfo()
								.getPlotInfo();

						if (increase) {
							double dmin = min;
							double dmax = max;
							if (dmin <= 0) {
								double inc = -2 * dmin + 1;
								dmin += inc;
								dmax += inc;
							}
							double diff = (dmax - dmin) / dmin;
							if (diff >= 0.01) {
								// zoom only if the (max - min) is at least 1% of the min
								plot.zoomDomainAxes(0.5, plotInfo, p, true);
							}
						} else {
							// XXX On Fedora 17 this always dezoom all
							plot.zoomDomainAxes(-0.5, plotInfo, p, true);
						}

						// adjust
						min = plot.getDomainAxis().getRange().getLowerBound();
						max = plot.getDomainAxis().getRange().getUpperBound();
						Range maxRange = new Range(Math.max(loadedInterval.startTimestamp, min),
								Math.min(loadedInterval.endTimestamp, max));
						plot.getDomainAxis().setRange(maxRange);

					}
				});

				// - size
				chartFrame.setSize(compositeChart.getSize());
				// - prevent y zooming
				chartFrame.setRangeZoomable(false);
				// - prevent x zooming (we do it manually with wheel)
				chartFrame.setDomainZoomable(false);
				// - workaround for last xaxis tick not shown (jfreechart bug)
				RectangleInsets insets = plot.getInsets();
				plot.setInsets(new RectangleInsets(insets.getTop(), insets.getLeft(), insets
						.getBottom(), 25));
				// - time bounds
				plot.getDomainAxis().setLowerBound(displayed.startTimestamp);
				plot.getDomainAxis().setUpperBound(displayed.endTimestamp);
				// timebar
				timeBar.setSelection(loadedInterval);
			}
		});

	}

	private void removeMarker() {
		if (marker != null) {
			plot.removeDomainMarker(marker);
			marker = null;
		}
	}

	private void addNewMarker(long start, long end) {
		marker = new IntervalMarker(selectedTs0, selectedTs0);
		marker.setPaint(BACKGROUND_PAINT);
		marker.setOutlinePaint(MARKER_OUTLINE_PAINT);
		marker.setAlpha(0.5f);
		marker.setStartValue(start);
		marker.setEndValue(end);
		plot.addDomainMarker(marker);
		activeSelection = true;
	}

	/**
	 * Prepare the plot
	 * 
	 * @param chart
	 *            jfreechart chart
	 * @param displayed
	 *            displayed time interval
	 */
	private void preparePlot(boolean first, JFreeChart chart, TimeInterval displayed) {
		// Plot customization
		plot = chart.getXYPlot();
		// Grid and background colors
		plot.setBackgroundPaint(BACKGROUND_PAINT);
		plot.setDomainGridlinePaint(DOMAIN_GRIDLINE_PAINT);
		plot.setRangeGridlinePaint(RANGE_GRIDLINE_PAINT);
		// Tooltip
		XYItemRenderer renderer = plot.getRenderer();
		renderer.setBaseToolTipGenerator(TOOLTIP_GENERATOR);
		// Disable bar white stripes
		XYBarRenderer barRenderer = (XYBarRenderer) plot.getRenderer();
		barRenderer.setBarPainter(new StandardXYBarPainter());
		// X axis
		X_FORMAT.setTimeUnit(TimeUnit.getTimeUnit(currentShownTrace.getTimeUnit()));
		X_FORMAT.setContext(displayed.startTimestamp, displayed.endTimestamp);
		NumberAxis xaxis = (NumberAxis) plot.getDomainAxis();
		xaxis.setTickLabelFont(TICK_LABEL_FONT);
		xaxis.setLabelFont(LABEL_FONT);
		xaxis.setNumberFormatOverride(X_FORMAT);
		TickDescriptor des = X_FORMAT.getTickDescriptor(displayed.startTimestamp,
				displayed.endTimestamp, numberOfTicks);
		xaxis.setTickUnit(new NumberTickUnit(des.delta));
		xaxis.addChangeListener(new AxisChangeListener() {
			@Override
			public void axisChanged(AxisChangeEvent arg) {
				long max = ((Double) plot.getDomainAxis().getRange().getUpperBound()).longValue();
				long min = ((Double) plot.getDomainAxis().getRange().getLowerBound()).longValue();
				TickDescriptor des = X_FORMAT.getTickDescriptor(min, max, numberOfTicks);
				NumberTickUnit newUnit = new NumberTickUnit(des.delta);
				NumberTickUnit currentUnit = ((NumberAxis) arg.getAxis()).getTickUnit();
				// ensure we don't loop
				if (!currentUnit.equals(newUnit)) {
					((NumberAxis) arg.getAxis()).setTickUnit(newUnit);
				}
			}
		});
		// Y axis
		NumberAxis yaxis = (NumberAxis) plot.getRangeAxis();
		yaxis.setTickLabelFont(TICK_LABEL_FONT);
		yaxis.setLabelFont(LABEL_FONT);
		// remove the marker, if any
		if (marker != null) {
			plot.removeDomainMarker(marker);
			marker = null;
		}
	}

	private int getPosAt(long timestamp) {
		if (chartFrame != null && plot != null) {
			org.eclipse.swt.graphics.Rectangle swtRect = chartFrame.getScreenDataArea();
			Rectangle2D screenDataArea = new Rectangle();
			screenDataArea.setRect(swtRect.x, swtRect.y, swtRect.width, swtRect.height);
			int pos = (int) plot.getDomainAxis().valueToJava2D(timestamp, screenDataArea,
					plot.getDomainAxisEdge());
			return pos;
		}
		return 0;
	}

	private long getTimestampAt(int pos) {
		if (chartFrame != null && plot != null && loadedInterval != null) {
			org.eclipse.swt.graphics.Rectangle swtRect = chartFrame.getScreenDataArea();
			Rectangle2D screenDataArea = new Rectangle();
			screenDataArea.setRect(swtRect.x, swtRect.y, swtRect.width, swtRect.height);
			long v = (long) plot.getDomainAxis().java2DToValue(pos, screenDataArea,
					plot.getDomainAxisEdge());
			if (v < loadedInterval.startTimestamp) {
				v = loadedInterval.startTimestamp;
			} else if (v > loadedInterval.endTimestamp) {
				v = loadedInterval.endTimestamp;
			}
			return v;
		}
		return 0;
	}

	@Override
	public void partHandle(FramesocBusTopic topic, Object data) {
		if (topic.equals(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED)) {
			if (currentShownTrace == null)
				return;
			ColorsChangeDescriptor des = (ColorsChangeDescriptor) data;
			logger.debug("Colors changed: {}", des);
			for (SquareIconLabelProvider p : labelProviders) {
				p.disposeImages();
			}
			refresh(false, false, true);
		}
	}

	private void updateStatusLine(long timestamp) {
		if (statusLineManager == null || currentShownTrace == null) {
			return;
		}

		if (timestamp == NO_STATUS) {
			statusLineManager.setMessage("");
			return;
		}

		long ts0 = selectedTs0;
		long ts1 = selectedTs1;
		if (ts0 > ts1) {
			long tmp = ts1;
			ts1 = ts0;
			ts0 = tmp;
		}

		ts0 = Math.max(ts0, currentShownTrace.getMinTimestamp());
		ts1 = Math.min(ts1, currentShownTrace.getMaxTimestamp());

		StringBuilder message = new StringBuilder();
		if (!dragInProgress) {
			message.append("T: "); //$NON-NLS-1$
			message.append(X_FORMAT.format(timestamp));
			message.append("     ");
		}
		message.append("T1: "); //$NON-NLS-1$
		message.append(X_FORMAT.format(ts0));
		if (activeSelection) {
			message.append("     T2: "); //$NON-NLS-1$
			message.append(X_FORMAT.format(ts1));
			message.append("     \u0394: "); //$NON-NLS-1$
			message.append(X_FORMAT.format(Math.abs(ts1 - ts0)));
		}
		statusLineManager.setMessage(message.toString());
	}

}