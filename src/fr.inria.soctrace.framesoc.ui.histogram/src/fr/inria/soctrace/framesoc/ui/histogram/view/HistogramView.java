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
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.wb.swt.ResourceManager;
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

import fr.inria.linuxtools.tmf.ui.widgets.timegraph.dialogs.FilteredCheckboxTree;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.dialogs.TreePatternFilter;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.histogram.Activator;
import fr.inria.soctrace.framesoc.ui.histogram.loaders.DensityHistogramLoader;
import fr.inria.soctrace.framesoc.ui.histogram.loaders.DensityHistogramLoader.ConfigurationDimension;
import fr.inria.soctrace.framesoc.ui.histogram.model.HistogramLoaderDataset;
import fr.inria.soctrace.framesoc.ui.model.ColorsChangeDescriptor;
import fr.inria.soctrace.framesoc.ui.model.GanttTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.IModelElementNode;
import fr.inria.soctrace.framesoc.ui.model.ITreeNode;
import fr.inria.soctrace.framesoc.ui.model.PieTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TableTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPart;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.framesoc.ui.providers.EventProducerTreeLabelProvider;
import fr.inria.soctrace.framesoc.ui.providers.EventTypeTreeLabelProvider;
import fr.inria.soctrace.framesoc.ui.providers.SquareIconLabelProvider;
import fr.inria.soctrace.framesoc.ui.providers.TreeContentProvider;
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
 * <pre>
 * TODO
 * - remove the sash and use a filter for types and producers, as in gantt
 * </pre>
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class HistogramView extends FramesocPart {

	private static class ConfigurationData {
		ConfigurationDimension dimension;
		FilteredCheckboxTree tree;
		ITreeNode[] roots;
		ITreeNode[] checked;

		ConfigurationData(ConfigurationDimension dimension) {
			this.dimension = dimension;
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
	private static final Object[] EMPTY_ARRAY = new Object[0];
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
	private Composite compositeConf;
	private ChartComposite chartFrame;
	protected XYPlot plot;
	private TimeBar timeBar;
	private Button btnCheckall;
	private Button btnUncheckall;
	private Button btnCheckSubtree;
	private Button btnUncheckSubtree;
	private IntervalMarker marker;

	private List<SquareIconLabelProvider> labelProviders = new ArrayList<>();

	/*
	 * Loading data and configuration
	 */
	private TimeInterval requestedInterval;
	private TimeInterval loadedInterval;
	private ConfigurationDimension currentDimension = ConfigurationDimension.TYPE;
	private Map<ConfigurationDimension, ConfigurationData> configurationMap;
	private HistogramLoaderDataset dataset;
	private boolean timeChanged = false;
	private boolean configurationChanged = false;

	/*
	 * Timestamp management
	 */
	private int numberOfTicks = 10;
	private IStatusLineManager statusLineManager;
	private TimestampFormat formatter = new TimestampFormat();

	/*
	 * Selection
	 */
	private boolean activeSelection = false;
	private boolean dragInProgress = false;
	private long selectedTs0 = Long.MAX_VALUE;
	private long selectedTs1 = Long.MIN_VALUE;

	/**
	 * Tree node comparator
	 */
	private final static Comparator<ITreeNode> TREE_NODE_COMPARATOR = new Comparator<ITreeNode>() {
		@Override
		public int compare(ITreeNode o1, ITreeNode o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	public HistogramView() {
		super();
		topics.addTopic(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED);
		topics.registerAll();
		configurationMap = new HashMap<>();
		for (ConfigurationDimension dimension : ConfigurationDimension.values()) {
			configurationMap.put(dimension, new ConfigurationData(dimension));
		}
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

		// Sash
		SashForm sashForm = new SashForm(parent, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// Chart Composite
		compositeChart = new Composite(sashForm, SWT.BORDER);
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

		// Configuration Composite
		compositeConf = new Composite(sashForm, SWT.NONE);
		GridLayout gl_compositeConf = new GridLayout(1, false);
		gl_compositeConf.marginHeight = 1;
		gl_compositeConf.verticalSpacing = 0;
		gl_compositeConf.marginWidth = 0;
		compositeConf.setLayout(gl_compositeConf);
		compositeConf.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// Tab folder
		TabFolder tabFolder = new TabFolder(compositeConf, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		PatternFilter filter = new TreePatternFilter();
		TreeContentProvider contentProvider = new TreeContentProvider();
		ViewerComparator treeComparator = new ViewerComparator();
		SelectionChangedListener selectionChangeListener = new SelectionChangedListener();
		CheckStateListener checkStateListener = new CheckStateListener();

		SquareIconLabelProvider p = null;

		// Tab item types
		TabItem tbtmEventTypes = new TabItem(tabFolder, SWT.NONE);
		tbtmEventTypes.setData(ConfigurationDimension.TYPE);
		tbtmEventTypes.setText(ConfigurationDimension.TYPE.getName());
		filter.setIncludeLeadingWildcard(true);
		FilteredCheckboxTree typeTree = new FilteredCheckboxTree(tabFolder, SWT.BORDER, filter,
				true);
		configurationMap.get(ConfigurationDimension.TYPE).tree = typeTree;
		typeTree.getViewer().setContentProvider(contentProvider);
		p = new EventTypeTreeLabelProvider();
		labelProviders.add(p);
		typeTree.getViewer().setLabelProvider(p);
		typeTree.getViewer().setComparator(treeComparator);
		typeTree.addCheckStateListener(checkStateListener);
		typeTree.getViewer().addSelectionChangedListener(selectionChangeListener);
		tbtmEventTypes.setControl(typeTree);

		// Tab item producers
		TabItem tbtmEventProducers = new TabItem(tabFolder, SWT.NONE);
		tbtmEventProducers.setData(ConfigurationDimension.PRODUCERS);
		tbtmEventProducers.setText(ConfigurationDimension.PRODUCERS.getName());
		FilteredCheckboxTree prodTree = new FilteredCheckboxTree(tabFolder, SWT.BORDER, filter,
				true);
		configurationMap.get(ConfigurationDimension.PRODUCERS).tree = prodTree;
		prodTree.getViewer().setContentProvider(contentProvider);
		p = new EventProducerTreeLabelProvider();
		labelProviders.add(p);
		prodTree.getViewer().setLabelProvider(p);
		prodTree.getViewer().setComparator(treeComparator);
		prodTree.addCheckStateListener(checkStateListener);
		prodTree.getViewer().addSelectionChangedListener(selectionChangeListener);
		tbtmEventProducers.setControl(prodTree);

		// sash weights
		sashForm.setWeights(new int[] { 80, 20 });
		// tab switch
		tabFolder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				currentDimension = (ConfigurationDimension) event.item.getData();
				enableTreeButtons();
				enableSubTreeButtons();
			}
		});

		// Buttons
		Composite compositeBtn = new Composite(compositeConf, SWT.BORDER);
		GridLayout gl_compositeBtn = new GridLayout(10, false);
		gl_compositeBtn.marginWidth = 1;
		gl_compositeBtn.horizontalSpacing = 1;
		compositeBtn.setLayout(gl_compositeBtn);
		compositeBtn.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));

		btnCheckall = new Button(compositeBtn, SWT.NONE);
		btnCheckall.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		btnCheckall.setToolTipText("Check all");
		btnCheckall.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID,
				"icons/check_all.png"));
		btnCheckall.setEnabled(false);
		btnCheckall.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (configurationMap.get(currentDimension).roots != null) {
					FilteredCheckboxTree tree = configurationMap.get(currentDimension).tree;
					TreeContentProvider provider = (TreeContentProvider) tree.getViewer()
							.getContentProvider();
					Object[] roots = provider.getElements(tree.getViewer().getInput());
					for (Object root : roots) {
						checkElementAndSubtree(root);
					}
					selectionChanged();
				}
			}
		});

		btnUncheckall = new Button(compositeBtn, SWT.NONE);
		btnUncheckall.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnUncheckall.setToolTipText("Uncheck all");
		btnUncheckall.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID,
				"icons/uncheck_all.png"));
		btnUncheckall.setEnabled(false);
		btnUncheckall.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				configurationMap.get(currentDimension).tree.setCheckedElements(EMPTY_ARRAY);
				selectionChanged();
			}
		});

		Label separator = new Label(compositeBtn, SWT.SEPARATOR | SWT.VERTICAL);
		GridData gd_separator = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_separator.horizontalIndent = 2;
		gd_separator.widthHint = 5;
		gd_separator.heightHint = 20;
		separator.setLayoutData(gd_separator);

		btnCheckSubtree = new Button(compositeBtn, SWT.NONE);
		btnCheckSubtree.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnCheckSubtree.setToolTipText("Check subtree");
		btnCheckSubtree.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID,
				"icons/check_subtree.png"));
		btnCheckSubtree.setEnabled(false);
		btnCheckSubtree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (configurationMap.get(currentDimension).roots != null) {
					ITreeNode node = getCurrentSelection(configurationMap.get(currentDimension).tree);
					if (node == null)
						return;
					checkElementAndSubtree(node);
					selectionChanged();
				}
			}
		});

		btnUncheckSubtree = new Button(compositeBtn, SWT.NONE);
		btnUncheckSubtree.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnUncheckSubtree.setToolTipText("Uncheck subtree");
		btnUncheckSubtree.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID,
				"icons/uncheck_subtree.png"));
		btnUncheckSubtree.setEnabled(false);
		btnUncheckSubtree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (configurationMap.get(currentDimension).roots != null) {
					ITreeNode node = getCurrentSelection(configurationMap.get(currentDimension).tree);
					if (node == null)
						return;
					uncheckElement(node);
					uncheckAncestors(node);
					selectionChanged();
				}
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
					System.out.println("A: " + activeSelection);
				} else {
					marker.setStartValue(barInterval.startTimestamp);
					marker.setEndValue(barInterval.endTimestamp);
				}
				timeChanged = !barInterval.equals(loadedInterval);
			}
		});
		// button to synch the timebar, producers and type with the current loaded data
		timeBar.getSynchButton().setToolTipText("Reset timebar, types and producers");
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
				for (ConfigurationData data : configurationMap.values()) {
					data.tree.setCheckedElements(data.checked);
				}
				timeChanged = false;
				enableTreeButtons();
				enableSubTreeButtons();
			}
		});
		// draw button
		timeBar.getLoadButton().setToolTipText("Load");
		timeBar.getLoadButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (timeChanged || configurationChanged) {
					loadHistogram(currentShownTrace, timeBar.getSelection());
				}
			}
		});

		// build toolbar
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		TableTraceIntervalAction.add(toolBar, createTableAction());
		GanttTraceIntervalAction.add(toolBar, createGanttAction());
		PieTraceIntervalAction.add(toolBar, createPieAction());
		enableActions(false);
	}

	protected ITreeNode getCurrentSelection(FilteredCheckboxTree tree) {
		IStructuredSelection sel = (IStructuredSelection) tree.getViewer().getSelection();
		if (sel.isEmpty())
			return null;
		return (ITreeNode) sel.getFirstElement();
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

		if (data == null) {
			// called after right click on trace tree menu
			loadHistogram(trace, new TimeInterval(trace.getMinTimestamp(), trace.getMaxTimestamp()));
		} else {
			// called after double click on trace tree or a Framesoc bus message
			// coming from a "show in" action

			boolean sameConf = true;

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
			} else {
				// message on the bus from "show in" action
				// - check all producers and types
				boolean loaded = true;
				for (ConfigurationData confData : configurationMap.values()) {
					if (confData.roots == null) {
						// first time, tree information not loaded yet
						loaded = false;
						break;
					}
					Object[] objs = linearizeAndSort(confData.roots);
					for (Object o : objs) {
						if (!confData.tree.getChecked(o)) {
							// node not selected
							sameConf = false;
							break;
						}
					}
				}
				if (loaded && !sameConf) {
					checkAll();
				}
			}

			if (loadedInterval == null || (!loadedInterval.equals(desInterval) || !sameConf)) {
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
		// nothing is loaded so far, so the interval is [start, start] (duration
		// 0)
		loadedInterval = new TimeInterval(interval.startTimestamp, interval.startTimestamp);
		requestedInterval = new TimeInterval(interval);

		Thread showThread = new Thread() {
			@Override
			public void run() {
				try {
					// prepare the configuration for the loader
					Map<ConfigurationDimension, List<Integer>> confMap = new HashMap<>();
					for (ConfigurationData confData : configurationMap.values()) {
						if (confData.roots == null) {
							// first time, tree information not loaded yet
							continue;
						}
						// store in confData.checked a sorted array containing
						// a snapshot of checked elements at load time
						Object[] currentChecked = confData.tree.getCheckedElements();
						confData.checked = new ITreeNode[currentChecked.length];
						List<Integer> toLoad = new ArrayList<Integer>(currentChecked.length);
						confMap.put(confData.dimension, toLoad);
						for (int i = 0; i < currentChecked.length; i++) {
							confData.checked[i] = (ITreeNode) currentChecked[i];
							if (!(currentChecked[i] instanceof IModelElementNode))
								continue;
							toLoad.add(((IModelElementNode) currentChecked[i]).getId());
						}
						Arrays.sort(confData.checked, TREE_NODE_COMPARATOR);
					}

					// create loader
					DensityHistogramLoader loader = new DensityHistogramLoader();

					// load producers and types if necessary
					for (ConfigurationData data : configurationMap.values()) {
						if (data.roots == null) {
							data.roots = loader.loadDimension(data.dimension, currentShownTrace);
							data.checked = linearizeAndSort(data.roots);
						}
					}

					// create a new loader dataset
					dataset = new HistogramLoaderDataset();

					// create loader and builder threads
					LoaderThread loaderThread = new LoaderThread(interval, loader,
							confMap.get(ConfigurationDimension.TYPE),
							confMap.get(ConfigurationDimension.PRODUCERS));
					BuilderJob builderJob = new BuilderJob("Event Density Chart Job", loaderThread);
					loaderThread.start();
					builderJob.schedule();
				} catch (SoCTraceException e) {
					e.printStackTrace();
				}
			}
		};
		showThread.start();
	}

	/**
	 * Loader thread.
	 */
	private class LoaderThread extends Thread {

		private final TimeInterval interval;
		private final DensityHistogramLoader loader;
		private final IProgressMonitor monitor;
		private List<Integer> types;
		private List<Integer> producer;

		public LoaderThread(TimeInterval interval, DensityHistogramLoader loader,
				List<Integer> types, List<Integer> producers) {
			this.interval = interval;
			this.loader = loader;
			this.types = types;
			this.producer = producers;
			this.monitor = new NullProgressMonitor();
		}

		@Override
		public void run() {
			loader.load(currentShownTrace, interval, types, producer, dataset, monitor);
		}

		public void cancel() {
			monitor.setCanceled(true);
		}
	}

	/**
	 * Builder job.
	 */
	private class BuilderJob extends Job {

		private final LoaderThread loaderThread;

		public BuilderJob(String name, LoaderThread loaderThread) {
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
						logger.debug("Drawer thread cancelled");
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
				timeChanged = false;
				configurationChanged = false;
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
					btnCheckall.setEnabled(false);
					btnCheckSubtree.setEnabled(false);
					btnUncheckall.setEnabled(false);
					btnUncheckSubtree.setEnabled(false);
				}
			});
		}

		private void enableButtons() {
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					enableTreeButtons();
					enableSubTreeButtons();
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
						long v = getTimestampAt(e.x);
						if (dragInProgress) {
							selectedTs1 = v;
							long min = Math.min(selectedTs0, selectedTs1);
							long max = Math.max(selectedTs0, selectedTs1);
							marker.setStartValue(min);
							marker.setEndValue(max);
							timeChanged = true;
							timeBar.setSelection(min, max);
						}
						updateStatusLine(v);
					}

					@Override
					public void mouseUp(MouseEvent e) {
						dragInProgress = false;
						selectedTs1 = getTimestampAt(e.x);
						if (selectedTs0 > selectedTs1) {
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
						removeMarker();
						selectedTs0 = getTimestampAt(e.x);
						addNewMarker(selectedTs0, selectedTs0);
						activeSelection = true;
						dragInProgress = true;
					}

					private void changeCursor(Cursor cursor) {
						getShell().setCursor(cursor);
					}

					private boolean isNearValue(int pos, long value) {
						final int RANGE = 4;
						int vPos = getPosAt(value);
						System.out.println(vPos);
						System.out.println(pos);
						if (Math.abs(vPos - pos) <= RANGE) {
							return true;
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
						X_FORMAT.setContext((long) min, (long) max, true);
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
				// producers and types
				if (first) {
					for (ConfigurationData data : configurationMap.values()) {
						data.tree.getViewer().setInput(data.roots);
						data.tree.setCheckedElements(data.checked);
						data.tree.getViewer().refresh();
						data.tree.getViewer().expandAll();
					}
				}
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
		X_FORMAT.setContext(displayed.startTimestamp, displayed.endTimestamp, true);
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

	int getPosAt(long timestamp) {
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

	long getTimestampAt(int pos) {
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

	/**
	 * Linearize the tree whose roots are passed
	 * 
	 * @param roots
	 *            root nodes
	 * @return the linearized tree in an array
	 */
	private ITreeNode[] linearizeAndSort(ITreeNode[] roots) {
		List<ITreeNode> nodes = new LinkedList<>();
		for (ITreeNode root : roots) {
			linearize(root, nodes);
		}
		ITreeNode[] array = nodes.toArray(new ITreeNode[nodes.size()]);
		Arrays.sort(array, TREE_NODE_COMPARATOR);
		return array;
	}

	private void linearize(ITreeNode node, List<ITreeNode> nodes) {
		nodes.add(node);
		for (ITreeNode n : node.getChildren()) {
			linearize(n, nodes);
		}
	}

	/*
	 * Tree selection and button enable/disable status management
	 */

	/**
	 * Listener for tree selection
	 */
	private class SelectionChangedListener implements ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			enableSubTreeButtons();
		}
	}

	/**
	 * Listener for check state change
	 */
	private class CheckStateListener implements ICheckStateListener {
		@Override
		public void checkStateChanged(CheckStateChangedEvent event) {
			try {
				ITreeNode entry = (ITreeNode) event.getElement();
				boolean checked = event.getChecked();
				if (checked) {
					checkElement(entry);
				} else {
					uncheckElement(entry);
					uncheckAncestors(entry);
				}
			} catch (ClassCastException e) {
				return;
			} finally {
				selectionChanged();
			}
		}
	}

	/**
	 * Enable/disable the checkAll/uncheckAll buttons, according to the context.
	 */
	private void enableTreeButtons() {
		int checked = configurationMap.get(currentDimension).tree.getCheckedElements().length;
		int treeItems = getTreeItemCount(configurationMap.get(currentDimension).tree);
		btnCheckall.setEnabled(checked < treeItems);
		btnUncheckall.setEnabled(checked != 0);
	}

	/**
	 * Enable/disable the checkSubTree/uncheckSubTree buttons, according to the context.
	 */
	private void enableSubTreeButtons() {
		FilteredCheckboxTree tree = configurationMap.get(currentDimension).tree;
		TreeContentProvider provider = (TreeContentProvider) tree.getViewer().getContentProvider();
		IStructuredSelection sel = (IStructuredSelection) tree.getViewer().getSelection();
		if (sel.isEmpty()) {
			btnCheckSubtree.setEnabled(false);
			btnUncheckSubtree.setEnabled(false);
		} else {
			ITreeNode node = (ITreeNode) sel.getFirstElement();
			if (!provider.hasChildren(node)) {
				btnCheckSubtree.setEnabled(false);
				btnUncheckSubtree.setEnabled(false);
			} else {
				btnUncheckSubtree.setEnabled(hasCheckedSon(node, provider, tree));
				btnCheckSubtree.setEnabled(hasUncheckedSon(node, provider, tree));
			}
		}
	}

	/**
	 * Enable / disable all the buttons when there is a change in check status.
	 */
	private void selectionChanged() {
		boolean enable = false;
		for (ConfigurationData data : configurationMap.values()) {
			enable = enable || selectionChanged(data.checked, data.tree);
			if (enable)
				break;
		}
		configurationChanged = enable;
		enableTreeButtons();
		enableSubTreeButtons();
	}

	/**
	 * Check if the checked elements changed for the given tree.
	 * 
	 * @param checked
	 *            old checked elements
	 * @param tree
	 *            tree
	 * @return if the checked elements have changed
	 */
	private boolean selectionChanged(ITreeNode[] checked, FilteredCheckboxTree tree) {
		// if checked elements changed, enable buttons, disable them otherwise
		Object[] objs = tree.getCheckedElements();
		ITreeNode[] currentChecked = new ITreeNode[objs.length];
		for (int i = 0; i < currentChecked.length; i++) {
			currentChecked[i] = (ITreeNode) objs[i];
		}
		Arrays.sort(currentChecked, TREE_NODE_COMPARATOR);
		return !Arrays.equals(checked, currentChecked);
	}

	/**
	 * Check an element and all its parents in the current tree.
	 * 
	 * @param element
	 *            The element to check.
	 */
	private void checkElement(Object element) {
		FilteredCheckboxTree tree = configurationMap.get(currentDimension).tree;
		tree.setChecked(element, true);
		Object parent = ((TreeContentProvider) tree.getViewer().getContentProvider())
				.getParent(element);
		if (parent != null && !tree.getChecked(parent)) {
			checkElement(parent);
		}
	}

	/**
	 * Uncheck an element and all its children in the current tree.
	 * 
	 * @param element
	 *            The element to uncheck.
	 */
	private void uncheckElement(Object element) {
		FilteredCheckboxTree tree = configurationMap.get(currentDimension).tree;
		tree.setChecked(element, false);
		TreeContentProvider provider = (TreeContentProvider) tree.getViewer().getContentProvider();
		for (Object child : provider.getChildren(element)) {
			uncheckElement(child);
		}
	}

	/**
	 * Uncheck all the ancestors of an element if all the elements siblings are unchecked, in the
	 * current tree. This is done recursively.
	 * 
	 * @param element
	 *            the element to start with
	 */
	private void uncheckAncestors(Object element) {
		FilteredCheckboxTree tree = configurationMap.get(currentDimension).tree;
		TreeContentProvider provider = (TreeContentProvider) tree.getViewer().getContentProvider();
		Object parent = provider.getParent(element);
		if (parent == null)
			return;
		Object[] siblings = provider.getChildren(parent);
		boolean uncheckParent = true;
		for (Object sibling : siblings) {
			if (tree.getChecked(sibling)) {
				uncheckParent = false;
				break;
			}
		}
		if (uncheckParent) {
			tree.setChecked(parent, false);
			uncheckAncestors(parent);
		}
	}

	/**
	 * Check an element, all its parents and all its children, in the current tree.
	 * 
	 * @param element
	 *            The element to check.
	 */
	private void checkElementAndSubtree(Object element) {
		FilteredCheckboxTree tree = configurationMap.get(currentDimension).tree;
		checkElement(element);
		TreeContentProvider provider = (TreeContentProvider) tree.getViewer().getContentProvider();
		for (Object child : provider.getChildren(element)) {
			checkElementAndSubtree(child);
		}
	}

	/**
	 * Check all the elements of all configuration dimensions
	 */
	private void checkAll() {
		for (ConfigurationData data : configurationMap.values()) {
			TreeViewer treeViewer = data.tree.getViewer();
			TreeContentProvider provider = (TreeContentProvider) treeViewer.getContentProvider();
			Object[] roots = provider.getElements(treeViewer.getInput());
			for (Object root : roots) {
				checkElementAndSubtree(root);
			}
		}
	}

	private boolean hasUncheckedSon(Object node, TreeContentProvider provider,
			FilteredCheckboxTree tree) {
		if (!provider.hasChildren(node))
			return false;
		Object[] children = provider.getChildren(node);
		for (Object son : children) {
			if (!tree.getChecked(son))
				return true;
			if (hasUncheckedSon(son, provider, tree))
				return true;
		}
		return false;
	}

	private boolean hasCheckedSon(Object node, TreeContentProvider provider,
			FilteredCheckboxTree tree) {
		if (!provider.hasChildren(node))
			return false;
		Object[] children = provider.getChildren(node);
		for (Object son : children) {
			if (tree.getChecked(son))
				return true;
			if (hasCheckedSon(son, provider, tree))
				return true;
		}
		return false;
	}

	private int getTreeItemCount(FilteredCheckboxTree tree) {
		TreeItem[] roots = tree.getViewer().getTree().getItems();
		int count = 0;
		for (TreeItem r : roots) {
			count += countItems(r);
		}
		return count;
	}

	private int countItems(TreeItem item) {
		int count = 1;
		for (TreeItem i : item.getItems()) {
			count += countItems(i);
		}
		return count;
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
			message.append(formatter.format(timestamp));
			message.append("     ");
		}
		message.append("T1: "); //$NON-NLS-1$
		message.append(formatter.format(ts0));
		if (activeSelection) {
			message.append("     T2: "); //$NON-NLS-1$
			message.append(formatter.format(ts1));
			message.append("     \u0394: "); //$NON-NLS-1$
			message.append(formatter.format(Math.abs(ts1 - ts0)));
		}
		statusLineManager.setMessage(message.toString());
	}

}
