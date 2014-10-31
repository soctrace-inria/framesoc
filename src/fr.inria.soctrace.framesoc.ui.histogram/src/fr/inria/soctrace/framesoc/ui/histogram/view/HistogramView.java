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
package fr.inria.soctrace.framesoc.ui.histogram.view;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.dialogs.PatternFilter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartMouseEvent;
import org.jfree.chart.ChartMouseListener;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.entity.XYItemEntity;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.linuxtools.tmf.ui.widgets.timegraph.dialogs.FilteredCheckboxTree;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.dialogs.TreePatternFilter;
import fr.inria.soctrace.framesoc.ui.histogram.loaders.DensityHistogramLoader;
import fr.inria.soctrace.framesoc.ui.model.GanttTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.PieTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TableTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPart;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.framesoc.ui.providers.TreeContentProvider;
import fr.inria.soctrace.framesoc.ui.providers.TreeLabelProvider;
import fr.inria.soctrace.framesoc.ui.utils.Constants;
import fr.inria.soctrace.lib.model.Trace;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * 
 */
public class HistogramView extends FramesocPart {
	public HistogramView() {
	}

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = FramesocViews.HISTOGRAM_VIEW_ID;

	/**
	 * Logger
	 */
	public static final Logger logger = LoggerFactory.getLogger(HistogramView.class);

	/**
	 * Constants
	 */
	public final static String TOOLTIP_FORMAT = "bin central timestamp: {1}, events: {2}";
	public final static String POPUP_MENU_SHOW_EVENT_TABLE = "Show Event Table page for timestamp ";
	public final static String POPUP_MENU_SHOW_GANTT_CHART = "Show Gantt Chart page for timestamp ";
	public static final String HISTOGRAM_TITLE = "";
	public static final String X_LABEL = "";
	public static final String Y_LABEL = "";

	public static final boolean HAS_LEGEND = false;
	public static final boolean HAS_TOOLTIPS = true;
	public static final boolean HAS_URLS = true;
	public static final boolean USE_BUFFER = true;

	/**
	 * The chart composite
	 */
	private Composite compositeChart;

	/**
	 * Event producer tree
	 */
	private FilteredCheckboxTree producerTree;
	
	/**
	 * Event type tree (grouped by category)
	 */
	private FilteredCheckboxTree typeTree;
	
	/**
	 * The chart frame
	 */
	private ChartComposite chartFrame;

	protected XYPlot plot;

	// Uncomment this to use the window builder
	@Override
	public void createPartControl(Composite parent) {
		createFramesocPartControl(parent);
	}
	
	@Override
	public void createFramesocPartControl(Composite parent) {
		// Empty view at the beginning
		setContentDescription("Trace: <no trace displayed>");

		// Sash
		SashForm sashForm = new SashForm(parent, SWT.NONE);
		
		// Chart
		compositeChart = new Composite(sashForm, SWT.BORDER);
		compositeChart.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		// Configuration
		Composite compositeConf = new Composite(sashForm, SWT.BORDER);
		compositeConf.setLayout(new GridLayout(1, false));

		// tab
		TabFolder tabFolder = new TabFolder(compositeConf, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        PatternFilter filter = new TreePatternFilter();
        TreeContentProvider contentProvider = new TreeContentProvider();
        TreeLabelProvider labelProvider = new TreeLabelProvider();

		TabItem tbtmEventTypes = new TabItem(tabFolder, SWT.NONE);
		tbtmEventTypes.setText("Event Types");
        filter.setIncludeLeadingWildcard(true);
        typeTree = new FilteredCheckboxTree(parent, SWT.BORDER | SWT.MULTI, filter, true);
        typeTree.getViewer().setContentProvider(contentProvider);
        typeTree.getViewer().setLabelProvider(labelProvider);
		
		TabItem tbtmEventProducers = new TabItem(tabFolder, SWT.NONE);
		tbtmEventProducers.setText("Event Producers");
        producerTree = new FilteredCheckboxTree(parent, SWT.BORDER | SWT.MULTI, filter, true);
        producerTree.getViewer().setContentProvider(contentProvider);
        producerTree.getViewer().setLabelProvider(labelProvider);
		
		// Buttons
		Composite compositeBtn = new Composite(compositeConf, SWT.NONE);
		compositeBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		compositeBtn.setLayout(new GridLayout(2, false));
		
		Button btnReset = new Button(compositeBtn, SWT.NONE);
		btnReset.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnReset.setText("Reset");
		
		Button btnLoad = new Button(compositeBtn, SWT.NONE);
		btnLoad.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnLoad.setText("Load");
		sashForm.setWeights(new int[] {80, 20});

		// build toolbar
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		TableTraceIntervalAction.add(toolBar, createTableAction());
		GanttTraceIntervalAction.add(toolBar, createGanttAction());
		PieTraceIntervalAction.add(toolBar, createPieAction());
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

	private TraceIntervalAction createPieAction() {
		return new PieTraceIntervalAction() {
			@Override
			public TraceIntervalDescriptor getTraceIntervalDescriptor() {
				return getIntervalDescriptor();
			}
		};
	}

	private TraceIntervalDescriptor getIntervalDescriptor() {
		if (currentShownTrace == null)
			return null;
		TraceIntervalDescriptor des = new TraceIntervalDescriptor();
		des.setTrace(currentShownTrace);
		des.setStartTimestamp(((Double) plot.getDomainAxis().getRange().getLowerBound())
				.longValue());
		des.setEndTimestamp(((Double) plot.getDomainAxis().getRange().getUpperBound()).longValue());
		return des;
	}

	/**
	 * Histogram mouse listener
	 */
	public class HistogramMouseListener implements ChartMouseListener {
		@Override
		public void chartMouseMoved(ChartMouseEvent event) {
			// Do nothing
		}

		@Override
		public void chartMouseClicked(ChartMouseEvent event) {
			if (!(event.getEntity() instanceof XYItemEntity))
				return;
			// store selected bin timestamp
			XYItemEntity selectedBin = (XYItemEntity) event.getEntity();
			logger.debug("selected " + selectedBin);
		}
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void showTrace(final Trace trace, Object data) {
		// Clean parent
		for (Control c : compositeChart.getChildren()) {
			c.dispose();
		}

		Job job = new Job("Loading Event Density Chart...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask("Loading Event Density Chart", IProgressMonitor.UNKNOWN);
				try {
					// update trace selection
					if (trace == null)
						return Status.CANCEL_STATUS;
					currentShownTrace = trace;

					// Activate the view after setting the current shown trace, otherwise
					// the set focus triggered by the activation send the old shown trace
					// on the FramesocBus
					activateView();

					// prepare dataset
					DensityHistogramLoader loader = new DensityHistogramLoader();
					HistogramDataset dataset = loader.load(currentShownTrace);

					// prepare chart
					final JFreeChart chart = ChartFactory.createHistogram(HISTOGRAM_TITLE, X_LABEL,
							Y_LABEL, dataset, PlotOrientation.VERTICAL, HAS_LEGEND, HAS_TOOLTIPS,
							HAS_URLS);

					// Customization
					plot = chart.getXYPlot();
					// background color
					plot.setBackgroundPaint(new Color(255, 255, 255));
					plot.setDomainGridlinePaint(new Color(230, 230, 230));
					plot.setRangeGridlinePaint(new Color(200, 200, 200));
					// tooltip
					XYItemRenderer renderer = plot.getRenderer();
					renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator(TOOLTIP_FORMAT,
							new DecimalFormat(Constants.TIMESTAMPS_FORMAT), new DecimalFormat("0")));
					// axis
					Font tickLabelFont = new Font("Tahoma", 0, 11);
					Font labelFont = new Font("Tahoma", 0, 12);
					NumberAxis xaxis = (NumberAxis) plot.getDomainAxis();
					xaxis.setTickLabelFont(tickLabelFont);
					xaxis.setLabelFont(labelFont);
					// x tick format
					NumberFormat formatter = new DecimalFormat(Constants.TIMESTAMPS_FORMAT);
					xaxis.setNumberFormatOverride(formatter);
					// x tick units
					double unit = (loader.getMax() - loader.getMin()) / 10.0;
					xaxis.setTickUnit(new NumberTickUnit(unit));
					xaxis.addChangeListener(new AxisChangeListener() {
						@Override
						public void axisChanged(AxisChangeEvent arg) {
							long max = ((Double) plot.getDomainAxis().getRange().getUpperBound())
									.longValue();
							long min = ((Double) plot.getDomainAxis().getRange().getLowerBound())
									.longValue();
							NumberTickUnit newUnit = new NumberTickUnit((max - min) / 10.0);
							NumberTickUnit currentUnit = ((NumberAxis) arg.getAxis()).getTickUnit();
							// ensure we don't loop
							if (!currentUnit.equals(newUnit))
								((NumberAxis) arg.getAxis()).setTickUnit(newUnit);
						}
					});

					NumberAxis yaxis = (NumberAxis) plot.getRangeAxis();
					yaxis.setTickLabelFont(tickLabelFont);
					yaxis.setLabelFont(labelFont);
					// disable bar white stripe
					XYBarRenderer barRenderer = (XYBarRenderer) plot.getRenderer();
					barRenderer.setBarPainter(new StandardXYBarPainter());

					// prepare the new histogram UI
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							setContentDescription("Trace: " + currentShownTrace.getAlias());
							if (chartFrame != null)
								chartFrame.dispose();
							chartFrame = new ChartComposite(compositeChart, SWT.NONE, chart, USE_BUFFER);
							chartFrame.setSize(compositeChart.getSize()); // size
							chartFrame.setRangeZoomable(false); // prevent y zooming
							chartFrame.addChartMouseListener(new HistogramMouseListener());
							enableActions(true);
						}
					});
					monitor.done();
				} catch (Exception e) {
					e.printStackTrace();
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			}
		};
		job.setUser(true);
		job.schedule();
	}
}
