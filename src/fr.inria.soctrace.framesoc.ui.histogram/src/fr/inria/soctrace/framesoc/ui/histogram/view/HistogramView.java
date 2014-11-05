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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.wb.swt.ResourceManager;
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
import fr.inria.soctrace.framesoc.ui.histogram.Activator;
import fr.inria.soctrace.framesoc.ui.histogram.loaders.DensityHistogramLoader;
import fr.inria.soctrace.framesoc.ui.histogram.loaders.DensityHistogramLoader.ConfigurationDimension;
import fr.inria.soctrace.framesoc.ui.model.GanttTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.IModelElementNode;
import fr.inria.soctrace.framesoc.ui.model.ITreeNode;
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
 * Framesoc Bar Chart view.
 * 
 * <pre>
 * TODO
 * - multi selection
 * - min size for button sash
 * - dezoom
 * </pre>
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class HistogramView extends FramesocPart {
	public HistogramView() {
	}

	private class ConfigurationData {
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
	 * The configuration composite
	 */
	private Composite compositeConf;

	/**
	 * The chart frame
	 */
	private ChartComposite chartFrame;

	/**
	 * The chart plot
	 */
	protected XYPlot plot;

	/**
	 * Buttons
	 */
	private Button btnCheckall;
	private Button btnUncheckall;
	private Button btnCheckSubtree;
	private Button btnUncheckSubtree;
	private Button btnReset;
	private Button btnLoad;

	private ConfigurationDimension currentDimension = ConfigurationDimension.TYPE;
	private Map<ConfigurationDimension, ConfigurationData> configurationMap;

	/**
	 * Tree node comparator
	 */
	private final static Comparator<ITreeNode> TREE_NODE_COMPARATOR = new Comparator<ITreeNode>() {
		@Override
		public int compare(ITreeNode o1, ITreeNode o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	private final static Object[] EMPTY_ARRAY = new Object[0];

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
		compositeConf = new Composite(sashForm, SWT.NONE);
		GridLayout gl_compositeConf = new GridLayout(1, false);
		gl_compositeConf.marginHeight = 1;
		gl_compositeConf.verticalSpacing = 0;
		gl_compositeConf.marginWidth = 0;
		compositeConf.setLayout(gl_compositeConf);

		// tab
		TabFolder tabFolder = new TabFolder(compositeConf, SWT.NONE);
		tabFolder.setLayout(new GridLayout(1, false));
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		configurationMap = new HashMap<>();
		for (ConfigurationDimension tab : ConfigurationDimension.values()) {
			configurationMap.put(tab, new ConfigurationData(tab));
		}

		PatternFilter filter = new TreePatternFilter();
		TreeContentProvider contentProvider = new TreeContentProvider();
		TreeLabelProvider labelProvider = new TreeLabelProvider();
		ViewerComparator treeComparator = new ViewerComparator();
		SelectionChangedListener selectionChangeListener = new SelectionChangedListener();
		CheckStateListener checkStateListener = new CheckStateListener();

		TabItem tbtmEventTypes = new TabItem(tabFolder, SWT.NONE);
		tbtmEventTypes.setData(ConfigurationDimension.TYPE);
		tbtmEventTypes.setText(ConfigurationDimension.TYPE.getName());
		filter.setIncludeLeadingWildcard(true);
		Composite typeComposite = new Composite(tabFolder, SWT.NONE);
		GridLayout gl_typeComposite = new GridLayout(1, false);
		gl_typeComposite.marginBottom = 2;
		gl_typeComposite.marginHeight = 0;
		gl_typeComposite.marginWidth = 0;
		gl_typeComposite.verticalSpacing = 0;
		typeComposite.setLayout(gl_typeComposite);
		typeComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tbtmEventTypes.setControl(typeComposite);

		FilteredCheckboxTree typeTree = new FilteredCheckboxTree(typeComposite, SWT.BORDER, filter,
				true);
		configurationMap.get(ConfigurationDimension.TYPE).tree = typeTree;
		typeTree.getViewer().setContentProvider(contentProvider);
		typeTree.getViewer().setLabelProvider(labelProvider);
		typeTree.getViewer().setComparator(treeComparator);
		typeTree.addCheckStateListener(checkStateListener);
		typeTree.getViewer().addSelectionChangedListener(selectionChangeListener);

		TabItem tbtmEventProducers = new TabItem(tabFolder, SWT.NONE);
		tbtmEventProducers.setData(ConfigurationDimension.PRODUCERS);
		tbtmEventProducers.setText(ConfigurationDimension.PRODUCERS.getName());
		Composite prodComposite = new Composite(tabFolder, SWT.NONE);
		GridLayout gl_producersComposite = new GridLayout(1, false);
		gl_producersComposite.marginHeight = 0;
		gl_producersComposite.marginWidth = 0;
		gl_producersComposite.verticalSpacing = 0;
		prodComposite.setLayout(gl_producersComposite);
		prodComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tbtmEventProducers.setControl(prodComposite);
		FilteredCheckboxTree producerTree = new FilteredCheckboxTree(prodComposite, SWT.BORDER,
				filter, true);
		configurationMap.get(ConfigurationDimension.PRODUCERS).tree = producerTree;
		producerTree.getViewer().setContentProvider(contentProvider);
		producerTree.getViewer().setLabelProvider(labelProvider);
		producerTree.getViewer().setComparator(treeComparator);
		producerTree.addCheckStateListener(checkStateListener);
		producerTree.getViewer().addSelectionChangedListener(selectionChangeListener);

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
		compositeBtn.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 1, 1));
		compositeBtn.setLayout(new GridLayout(8, false));

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
		btnUncheckall.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
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

		btnCheckSubtree = new Button(compositeBtn, SWT.NONE);
		btnCheckSubtree.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
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
		btnUncheckSubtree.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
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

		btnReset = new Button(compositeBtn, SWT.NONE);
		btnReset.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnReset.setToolTipText("Reset");
		btnReset.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/reset.png"));
		btnReset.setEnabled(false);
		btnReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (ConfigurationData data : configurationMap.values()) {
					data.tree.setCheckedElements(data.checked);
				}
				enableResetLoadButtons(false);
				enableTreeButtons();
				enableSubTreeButtons();
			}
		});

		btnLoad = new Button(compositeBtn, SWT.NONE);
		btnLoad.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnLoad.setToolTipText("Load");
		btnLoad.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/play.png"));
		btnLoad.setEnabled(false);
		btnLoad.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showTrace(currentShownTrace, null);
			}
		});

		// sash weights
		sashForm.setWeights(new int[] { 80, 20 });

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

		// prepare the configuration for the loader
		final Map<ConfigurationDimension, List<Integer>> confMap = new HashMap<>();
		for (ConfigurationData confData : configurationMap.values()) {
			if (confData.roots == null) {
				continue;
			}
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

		Job job = new Job("Loading Event Density Chart...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				monitor.beginTask("Loading Event Density Chart", IProgressMonitor.UNKNOWN);
				try {
					// update trace selection
					if (trace == null)
						return Status.CANCEL_STATUS;
					currentShownTrace = trace;

					/*
					 * Activate the view after setting the current shown trace, otherwise the set
					 * focus triggered by the activation send the old shown trace on the FramesocBus
					 */
					activateView();

					// prepare dataset
					DensityHistogramLoader loader = new DensityHistogramLoader();
					List<Integer> producers = confMap.get(ConfigurationDimension.PRODUCERS);
					List<Integer> types = confMap.get(ConfigurationDimension.TYPE);
					HistogramDataset dataset = loader.load(currentShownTrace, types, producers);

					// load producers and types if necessary
					for (ConfigurationData data : configurationMap.values()) {
						if (data.roots == null) {
							data.roots = loader.loadDimension(data.dimension, currentShownTrace);
							data.checked = linearizeAndSort(data.roots);
						}
					}

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
							chartFrame = new ChartComposite(compositeChart, SWT.NONE, chart,
									USE_BUFFER) {
								@Override
								public void restoreAutoBounds() {
									// restore domain axis to trace range when dezooming
									plot.getDomainAxis().setRange(
											currentShownTrace.getMinTimestamp(),
											currentShownTrace.getMaxTimestamp());
								}
							};
							// size
							chartFrame.setSize(compositeChart.getSize());
							// prevent y zooming
							chartFrame.setRangeZoomable(false);
							chartFrame.addChartMouseListener(new HistogramMouseListener());

							// time bounds
							plot.getDomainAxis().setLowerBound(currentShownTrace.getMinTimestamp());
							plot.getDomainAxis().setUpperBound(currentShownTrace.getMaxTimestamp());

							// producers and types
							for (ConfigurationData data : configurationMap.values()) {
								data.tree.getViewer().setInput(data.roots);
								data.tree.setCheckedElements(data.checked);
								data.tree.getViewer().refresh();
								data.tree.getViewer().expandAll();
							}

							// buttons
							enableResetLoadButtons(false);
							enableTreeButtons();
							enableSubTreeButtons();

							// actions
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
	 * Enable/disable the reset and load buttons.
	 * 
	 * @param enable
	 *            flag stating if we must enable or not the load and reset buttons
	 */
	private void enableResetLoadButtons(boolean enable) {
		btnReset.setEnabled(enable);
		btnLoad.setEnabled(enable);
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
		enableResetLoadButtons(enable);
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

}
