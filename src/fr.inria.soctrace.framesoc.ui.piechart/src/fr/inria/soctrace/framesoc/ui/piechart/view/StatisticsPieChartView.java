/*******************************************************************************
 * Copyright (c) 2012-2014 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.piechart.view;

import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.wb.swt.ResourceManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.PieDataset;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.jfree.ui.RectangleEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO create a fragment plugin for jfreechart
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.model.ColorsChangeDescriptor;
import fr.inria.soctrace.framesoc.ui.model.GanttTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TableTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPart;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.framesoc.ui.piechart.PieContributionManager;
import fr.inria.soctrace.framesoc.ui.piechart.model.IPieChartLoader;
import fr.inria.soctrace.framesoc.ui.piechart.model.PieChartLoaderMap;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableColumn;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableFolderRow;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableRow;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableRowFilter;
import fr.inria.soctrace.framesoc.ui.piechart.providers.StatisticsTableRowLabelProvider;
import fr.inria.soctrace.framesoc.ui.providers.TableRowLabelProvider;
import fr.inria.soctrace.framesoc.ui.providers.TreeContentProvider;
import fr.inria.soctrace.framesoc.ui.utils.TimeBar;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Statistics pie chart view
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class StatisticsPieChartView extends FramesocPart {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(StatisticsPieChartView.class);

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = FramesocViews.STATISTICS_PIE_CHART_VIEW_ID;

	/**
	 * Build update timeout
	 */
	private static final long BUILD_UPDATE_TIMEOUT = 300;

	/**
	 * Constants
	 */
	public static final boolean HAS_LEGEND = false;
	public static final boolean HAS_TOOLTIPS = true;
	public static final boolean HAS_URLS = true;
	public static final boolean USE_BUFFER = true;

	/**
	 * Loader data
	 */
	private class LoaderDescriptor {
		public IPieChartLoader loader;
		public PieChartLoaderMap map;
		public TimeInterval interval;
		public boolean dirty = false;

		public LoaderDescriptor(IPieChartLoader loader) {
			this.loader = loader;
			this.interval = new TimeInterval(0, 0);
			this.map = new PieChartLoaderMap();
		}

		public boolean dataReady() {
			return (map != null && map.isComplete());
		}

		public void dispose() {
			loader = null;
			if (map != null)
				map = null;
		}

		@Override
		public String toString() {
			return "LoaderDescriptor [loader=" + loader + ", dataset=" + map + ", interval="
					+ interval + "]";
		}

	}

	/**
	 * Available loaders, read from the extension point. The i-th element of this array corresponds
	 * to the i-th element of the combo-box.
	 */
	private ArrayList<LoaderDescriptor> loaderDescriptors;

	/**
	 * Descriptor related to the current active loader.
	 */
	private LoaderDescriptor currentDescriptor;

	/**
	 * Statistics loader combo
	 */
	private Combo combo;

	/**
	 * Pie load button
	 */
	private Button btnLoad;

	/**
	 * Description text
	 */
	private Text txtDescription;

	/**
	 * The chart parent composite
	 */
	private Group compositePie;

	/**
	 * The table viewer
	 */
	private TreeViewer tableTreeViewer;

	/**
	 * The time management bar
	 */
	private TimeBar timeBar;

	/**
	 * Status text
	 */
	private Text statusText;

	/**
	 * Images
	 */
	private Map<String, Image> images = new HashMap<String, Image>();

	/**
	 * Column comparator
	 */
	private StatisticsColumnComparator comparator;

	/**
	 * Filter text for table
	 */
	private Text textFilter;

	/**
	 * Filter for table
	 */
	private StatisticsTableRowFilter nameFilter;

	private Button btnSynch;

	/**
	 * Constructor
	 */
	public StatisticsPieChartView() {
		super();
		topics.addTopic(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED);
		topics.registerAll();
		List<IPieChartLoader> loaders = PieContributionManager.getLoaders();
		loaderDescriptors = new ArrayList<>(loaders.size());
		for (IPieChartLoader loader : loaders) {
			loaderDescriptors.add(new LoaderDescriptor(loader));
		}
	}

	// Uncomment this to use the window builder
	// @Override
	// public void createPartControl(Composite parent) {
	// createFramesocPartControl(parent);
	// }

	@Override
	public void createFramesocPartControl(Composite parent) {

		setContentDescription("Trace: <no trace displayed>");

		// parent layout
		GridLayout gl_parent = new GridLayout(1, false);
		gl_parent.verticalSpacing = 2;
		gl_parent.marginWidth = 0;
		gl_parent.horizontalSpacing = 0;
		gl_parent.marginHeight = 0;
		parent.setLayout(gl_parent);

		// -------------------------------
		// Base GUI: pie + table
		// -------------------------------

		SashForm sashForm = new SashForm(parent, SWT.BORDER | SWT.SMOOTH);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		// Composite Left: composite combo + composite pie
		Composite compositeLeft = new Composite(sashForm, SWT.NONE);
		GridLayout gl_compositeLeft = new GridLayout(1, false);
		gl_compositeLeft.marginBottom = 3;
		gl_compositeLeft.verticalSpacing = 0;
		gl_compositeLeft.marginHeight = 0;
		compositeLeft.setLayout(gl_compositeLeft);

		// Composite Combo
		Composite compositeCombo = new Composite(compositeLeft, SWT.NONE);
		compositeCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		GridLayout gl_compositeCombo = new GridLayout(1, false);
		gl_compositeCombo.marginWidth = 0;
		compositeCombo.setLayout(gl_compositeCombo);

		// combo
		combo = new Combo(compositeCombo, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentDescriptor = loaderDescriptors.get(combo.getSelectionIndex());
				refresh();
			}
		});

		int position = 0;
		for (LoaderDescriptor descriptor : loaderDescriptors) {
			combo.add(descriptor.loader.getStatName(), position++);
		}
		combo.select(0);
		currentDescriptor = loaderDescriptors.get(0);
		combo.setEnabled(false);

		// Composite Pie
		compositePie = new Group(compositeLeft, SWT.NONE);
		// Fill layout with Grid Data (FILL) to allow correct resize
		compositePie.setLayout(new FillLayout());
		compositePie.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		txtDescription = new Text(compositePie, SWT.READ_ONLY | SWT.WRAP | SWT.CENTER | SWT.MULTI);
		txtDescription.setEnabled(false);
		txtDescription.setEditable(false);
		txtDescription.setText("Select one of the above metrics, then press the Load button.");
		txtDescription.setVisible(false);

		// Composite Table
		Composite compositeTable = new Composite(sashForm, SWT.NONE);
		GridLayout gl_compositeTable = new GridLayout(1, false);
		compositeTable.setLayout(gl_compositeTable);

		// filter
		textFilter = new Text(compositeTable, SWT.BORDER);
		textFilter.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		textFilter.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.CR) {
					if (nameFilter == null || tableTreeViewer == null || statusText == null)
						return;
					if (currentDescriptor == null || currentDescriptor.map == null)
						return;
					nameFilter.setSearchText(textFilter.getText());
					tableTreeViewer.refresh();
					tableTreeViewer.expandAll();
					logger.debug("items: " + getTreeLeafs(tableTreeViewer.getTree().getItems(), 0));
					statusText.setText(getStatus(currentDescriptor.map.size(),
							getTreeLeafs(tableTreeViewer.getTree().getItems(), 0)));
				}
			}
		});

		// table
		tableTreeViewer = new TreeViewer(compositeTable, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		tableTreeViewer.setContentProvider(new TreeContentProvider());
		comparator = new StatisticsColumnComparator();
		tableTreeViewer.setComparator(comparator);
		Tree table = tableTreeViewer.getTree();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		createColumns();

		// status bar
		Composite statusBar = new Composite(compositeTable, SWT.BORDER);
		GridLayout statusBarLayout = new GridLayout();
		GridData statusBarGridData = new GridData();
		statusBarGridData.horizontalAlignment = SWT.FILL;
		statusBarGridData.grabExcessHorizontalSpace = true;
		statusBar.setLayoutData(statusBarGridData);
		statusBarLayout.numColumns = 1;
		statusBar.setLayout(statusBarLayout);
		// text
		statusText = new Text(statusBar, SWT.NONE);
		statusText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		statusText.setText(getStatus(0, 0));

		// -------------------------------
		// TIME MANAGEMENT BAR
		// -------------------------------

		Composite timeComposite = new Composite(parent, SWT.BORDER);
		timeComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		timeComposite.setLayout(new GridLayout(4, false));
		// time manager
		timeBar = new TimeBar(timeComposite, SWT.NONE);
		timeBar.setEnabled(false);
		IStatusLineManager statusLineManager = getViewSite().getActionBars().getStatusLineManager();
		timeBar.setStatusLineManager(statusLineManager);
		timeBar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnLoad != null && timeBar != null) {
					TimeInterval barInterval = new TimeInterval(timeBar.getStartTimestamp(),
							timeBar.getEndTimestamp());
					if (!barInterval.equals(currentDescriptor.interval)) {
						btnLoad.setEnabled(true);
						btnSynch.setEnabled(true);
					} else {
						btnLoad.setEnabled(false);
						btnSynch.setEnabled(false);
					}
				}
			}
		});

		// button to synch the timebar with the gantt
		btnSynch = new Button(timeComposite, SWT.NONE);
		btnSynch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnSynch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (combo != null && timeBar != null && currentDescriptor != null) {
					if (currentDescriptor.dirty) {
						timeBar.setSelection(currentDescriptor.interval.startTimestamp,
								currentDescriptor.interval.endTimestamp);
					} else {
						timeBar.setSelection(currentShownTrace.getMinTimestamp(),
								currentShownTrace.getMaxTimestamp());
					}
					btnSynch.setEnabled(false);
					btnLoad.setEnabled(!currentDescriptor.dirty);
				}
			}
		});
		btnSynch.setToolTipText("Synch selection with Pie Chart");
		btnSynch.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui",
				"icons/load.png"));
		btnSynch.setEnabled(false);

		// load button
		btnLoad = new Button(timeComposite, SWT.NONE);
		btnLoad.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (combo.getSelectionIndex() == -1)
					return;
				currentDescriptor = loaderDescriptors.get(combo.getSelectionIndex());
				loadPieChart();
			}
		});
		btnLoad.setToolTipText("Load metric");
		btnLoad.setImage(ResourceManager.getPluginImage("fr.inria.soctrace.framesoc.ui",
				"icons/play.png"));
		btnLoad.setEnabled(false);

		// ----------
		// TOOL BAR
		// ----------

		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();
		TableTraceIntervalAction.add(manager, createTableAction());
		GanttTraceIntervalAction.add(manager, createGanttAction());
		enableActions(false);

	}

	private TraceIntervalAction createTableAction() {
		return new TableTraceIntervalAction() {
			@Override
			public TraceIntervalDescriptor getTraceIntervalDescriptor() {
				return getIntervalDescriptor();
			}
		};
	}

	private TraceIntervalAction createGanttAction() {
		return new GanttTraceIntervalAction() {
			@Override
			public TraceIntervalDescriptor getTraceIntervalDescriptor() {
				return getIntervalDescriptor();
			}
		};
	}

	private TraceIntervalDescriptor getIntervalDescriptor() {
		if (currentShownTrace == null || !currentDescriptor.dirty)
			return null;
		TraceIntervalDescriptor des = new TraceIntervalDescriptor();
		des.setTrace(currentShownTrace);
		des.setStartTimestamp(currentDescriptor.interval.startTimestamp);
		des.setEndTimestamp(currentDescriptor.interval.endTimestamp);
		return des;
	}

	private int getTreeLeafs(TreeItem[] items, int v) {
		for (TreeItem ti : items) {
			if (ti.getItems().length > 0) {
				v += getTreeLeafs(ti.getItems(), 0); // add only leafs
			} else {
				v += 1;
			}
		}
		return v;
	}

	// GUI creation

	private String getStatus(int events, int matched) {
		StringBuilder sb = new StringBuilder();
		sb.append("Filter matched ");
		sb.append(matched);
		sb.append(" of ");
		sb.append(events);
		sb.append(" items");
		return sb.toString();
	}

	private void createColumns() {
		for (final StatisticsTableColumn col : StatisticsTableColumn.values()) {
			TreeViewerColumn elemsViewerCol = new TreeViewerColumn(tableTreeViewer, SWT.NONE);

			if (col.equals(StatisticsTableColumn.NAME)) {
				// add a filter for this column
				nameFilter = new StatisticsTableRowFilter(col);
				tableTreeViewer.addFilter(nameFilter);

				// the label provider puts also the image
				elemsViewerCol.setLabelProvider(new StatisticsTableRowLabelProvider(col, images));
			} else
				elemsViewerCol.setLabelProvider(new TableRowLabelProvider(col));

			final TreeColumn elemsTableCol = elemsViewerCol.getColumn();
			elemsTableCol.setWidth(col.getWidth());
			elemsTableCol.setText(col.getHeader());
			elemsTableCol.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					comparator.setColumn(col);
					tableTreeViewer.getTree().setSortDirection(comparator.getDirection());
					tableTreeViewer.getTree().setSortColumn(elemsTableCol);
					tableTreeViewer.refresh();
				}
			});
		}
	}

	@Override
	public void setFocus() {
		super.setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
		for (LoaderDescriptor descriptor : loaderDescriptors) {
			descriptor.dispose();
		}
		loaderDescriptors = null;
		disposeImages();
		images = null;
	}

	private void disposeImages() {
		Iterator<Image> it = images.values().iterator();
		while (it.hasNext()) {
			it.next().dispose();
		}
		images.clear();
	}

	/**
	 * Loader thread.
	 */
	private class LoaderThread extends Thread {

		private final TimeInterval loadInterval;
		private final IProgressMonitor monitor;

		public LoaderThread(TimeInterval loadInterval) {
			this.loadInterval = loadInterval;
			this.monitor = new NullProgressMonitor();
		}

		@Override
		public void run() {
			currentDescriptor.loader.load(currentShownTrace, loadInterval, currentDescriptor.map,
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
			try {
				enableTimeBar(false);
				PieChartLoaderMap map = currentDescriptor.map;
				boolean done = false;
				boolean refreshed = false;
				while (!done) {
					done = map.waitUntilDone(BUILD_UPDATE_TIMEOUT);
					if (!map.isDirty()) {
						continue;
					}
					if (monitor.isCanceled()) {
						loaderThread.cancel();
						logger.debug("Drawer thread cancelled");
						return Status.CANCEL_STATUS;
					}
					refresh();
					refreshed = true;
				}
				if (!refreshed) {
					// refresh at least once when there is no data.
					refresh();
				}
				return Status.OK_STATUS;
			} finally {
				enableTimeBar(true);
				logger.debug(dm.endMessage("finished drawing"));
			}
		}

	}

	/**
	 * Enable/disable the time bar in the UI thread (sync execution).
	 * 
	 * @param enable
	 *            enable flag
	 */
	private void enableTimeBar(final boolean enable) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if (!timeBar.isDisposed()) {
					timeBar.setEnabled(enable);
				}
			}
		});
	}

	/**
	 * Load a pie chart using the current trace, the current loader and the time interval in the
	 * time bar.
	 */
	private void loadPieChart() {

		final TimeInterval loadInterval = new TimeInterval(timeBar.getStartTimestamp(),
				timeBar.getEndTimestamp());

		currentDescriptor.dirty = true;

		if (currentDescriptor.dataReady() && currentDescriptor.interval.equals(loadInterval)) {
			logger.debug("Data is ready. Nothing to do. Refresh only.");
			refresh();
			return;
		}

		// create a new loader map
		currentDescriptor.map = new PieChartLoaderMap();

		// create loader and drawer threads
		LoaderThread loaderThread = new LoaderThread(loadInterval);
		DrawerJob drawerJob = new DrawerJob("Pie Chart Drawer Job", loaderThread);
		loaderThread.start();
		drawerJob.schedule();

	}

	/**
	 * Refresh the UI using the current trace and the current descriptor.
	 * 
	 * @param dataReady
	 */
	private void refresh() {

		// compute graphical elements
		PieChartLoaderMap map = currentDescriptor.map;
		final Map<String, Double> values = map.getSnapshot(currentDescriptor.interval);
		final IPieChartLoader loader = currentDescriptor.loader;
		final PieDataset dataset = loader.getPieDataset(values);
		final StatisticsTableRow[] roots = loader.getTableDataset(values);
		final String title = loader.getStatName();
		final int valuesCount = values.size();

		// update the new UI
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {

				if (currentDescriptor.dataReady() && values.isEmpty()) {
					// store the loaded interval in case of no data
					currentDescriptor.interval.startTimestamp = timeBar.getStartTimestamp();
					currentDescriptor.interval.endTimestamp = timeBar.getEndTimestamp();
				}

				// clean UI: composite pie + images
				for (Control c : compositePie.getChildren()) {
					c.dispose();
				}
				disposeImages();

				// create new chart
				JFreeChart chart = createChart(dataset, "", loader, currentDescriptor.dirty);
				setContentDescription("Trace: " + currentShownTrace.getAlias());
				compositePie.setText(title);
				ChartComposite chartFrame = new ChartComposite(compositePie, SWT.NONE, chart,
						USE_BUFFER);

				Point size = compositePie.getSize();
				size.x -= 5; // consider the group border
				size.y -= 25; // consider the group border and text
				chartFrame.setSize(size);

				Point location = chartFrame.getLocation();
				location.x += 1; // consider the group border
				location.y += 20; // consider the group border and text
				chartFrame.setLocation(location);

				// update other elements
				if (roots.length == 0) {
					tableTreeViewer.setInput(null);
				} else {
					tableTreeViewer.setInput(roots);
				}
				tableTreeViewer.expandAll();
				btnLoad.setEnabled(!currentDescriptor.dirty);
				btnSynch.setEnabled(false);
				if (currentDescriptor.dirty) {
					timeBar.setSelection(currentDescriptor.interval.startTimestamp,
							currentDescriptor.interval.endTimestamp);
				} else {
					timeBar.setSelection(currentShownTrace.getMinTimestamp(),
							currentShownTrace.getMaxTimestamp());
				}
				statusText.setText(getStatus(valuesCount, valuesCount));
				enableActions(currentDescriptor.dirty);

			}
		});
	}

	/**
	 * Creates the chart.
	 * 
	 * @param dataset
	 *            the dataset.
	 * @param loader
	 *            the pie chart loader
	 * @param dataRequested
	 *            flag indicating if the data have been requested for the current loader and the
	 *            current interval
	 * @return the pie chart
	 */
	private static JFreeChart createChart(PieDataset dataset, String title, IPieChartLoader loader,
			boolean dataRequested) {

		JFreeChart chart = ChartFactory.createPieChart(title, dataset, HAS_LEGEND, HAS_TOOLTIPS,
				HAS_URLS);

		// legend
		if (HAS_LEGEND) {
			LegendTitle legend = chart.getLegend();
			legend.setPosition(RectangleEdge.LEFT);
		}

		// plot
		PiePlot plot = (PiePlot) chart.getPlot();
		plot.setSectionOutlinesVisible(false);
		plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
		plot.setNoDataMessage("No data available "
				+ (dataRequested ? "in this time interval" : "yet"));
		plot.setCircular(true);
		plot.setLabelGenerator(null); // hide labels
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlineVisible(false);
		plot.setShadowPaint(Color.WHITE);
		plot.setBaseSectionPaint(Color.WHITE);

		for (Object o : dataset.getKeys()) {
			String key = (String) o;
			plot.setSectionPaint(key, loader.getColor(key).getAwtColor());
		}
		return chart;
	}

	public class StatisticsColumnComparator extends ViewerComparator {
		private StatisticsTableColumn col = StatisticsTableColumn.OCCURRENCES;
		private int direction = SWT.DOWN;

		public int getDirection() {
			return direction;
		}

		public void setColumn(StatisticsTableColumn col) {
			if (this.col.equals(col)) {
				// Same column as last sort: toggle the direction
				direction = (direction == SWT.UP) ? SWT.DOWN : SWT.UP;
			} else {
				// New column: do an ascending sort
				this.col = col;
				direction = SWT.UP;
			}
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {

			StatisticsTableRow r1 = (StatisticsTableRow) e1;
			StatisticsTableRow r2 = (StatisticsTableRow) e2;

			// Aggregated rows at the end
			if (r1 instanceof StatisticsTableFolderRow)
				return +1;
			if (r2 instanceof StatisticsTableFolderRow)
				return -1;

			int rc = 0;
			try {
				if (this.col.equals(StatisticsTableColumn.OCCURRENCES)) {
					// number comparison
					Double v1 = Double.valueOf(r1.get(this.col));
					Double v2 = Double.valueOf(r2.get(this.col));
					rc = v1.compareTo(v2);
				} else if (this.col.equals(StatisticsTableColumn.PERCENTAGE)) {
					// percentage comparison 'xx.xx %'
					NumberFormat format = NumberFormat.getInstance();
					Double v1 = format.parse(r1.get(this.col).split(" ")[0])
							.doubleValue();
					Double v2 = format.parse(r2.get(this.col).split(" ")[0])
							.doubleValue();
					rc = v1.compareTo(v2);
				} else {
					// string comparison
					String v1 = r1.get(this.col);
					String v2 = r2.get(this.col);
					rc = v1.compareTo(v2);
				}
			} catch (SoCTraceException e) {
				e.printStackTrace();
				rc = 0;
			} catch (ParseException e) {
				e.printStackTrace();
				rc = 0;
			}
			// If descending order, flip the direction
			if (direction == SWT.DOWN) {
				rc = -rc;
			}
			return rc;
		}
	}

	@Override
	public void partHandle(FramesocBusTopic topic, Object data) {
		if (topic.equals(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED)) {
			if (currentShownTrace == null || currentDescriptor == null)
				return;
			ColorsChangeDescriptor des = (ColorsChangeDescriptor) data;
			logger.debug("Colors changed: {}", des);
			loadPieChart();
		}
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void showTrace(Trace trace, Object data) {
		combo.setEnabled(true);
		timeBar.setEnabled(true);
		timeBar.setExtrema(trace.getMinTimestamp(), trace.getMaxTimestamp());
		currentShownTrace = trace;
		setContentDescription("Trace: " + trace.getAlias());
		if (data != null) {
			TraceIntervalDescriptor intDes = (TraceIntervalDescriptor) data;
			OperatorDialog operatorDialog = new OperatorDialog(getSite().getShell());
			if (operatorDialog.open() == Dialog.OK) {
				if (operatorDialog.getSelectionIndex() != -1) {
					combo.select(operatorDialog.getSelectionIndex());
					currentDescriptor = loaderDescriptors.get(operatorDialog.getSelectionIndex());
					timeBar.setSelection(intDes.getStartTimestamp(), intDes.getEndTimestamp());
					loadPieChart();
				}
			}
		} else {
			combo.select(0);
			btnLoad.setEnabled(true);
			timeBar.setSelection(trace.getMinTimestamp(), trace.getMaxTimestamp());
			txtDescription.setVisible(true);
		}
	}

	private class OperatorDialog extends Dialog {

		private int selectionIndex;

		protected OperatorDialog(Shell parentShell) {
			super(parentShell);
		}

		@Override
		protected Control createDialogArea(Composite parent) {

			Composite composite = (Composite) super.createDialogArea(parent);

			final Combo operators = new Combo(composite, SWT.READ_ONLY);
			operators.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
			for (LoaderDescriptor s : loaderDescriptors) {
				operators.add(s.loader.getStatName());
			}
			operators.select(0);
			operators.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					selectionIndex = operators.getSelectionIndex();
				}
			});

			return composite;
		}

		public int getSelectionIndex() {
			return selectionIndex;
		}

	}
}
