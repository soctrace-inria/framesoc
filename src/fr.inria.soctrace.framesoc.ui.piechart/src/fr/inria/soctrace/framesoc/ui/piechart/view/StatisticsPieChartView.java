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
package fr.inria.soctrace.framesoc.ui.piechart.view;

import java.awt.Color;
import java.awt.Font;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.themes.ColorUtil;
import org.eclipse.wb.swt.ResourceManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.general.PieDataset;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.jfree.ui.RectangleEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO create a fragment plugin for jfreechart
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.Activator;
import fr.inria.soctrace.framesoc.ui.model.CategoryNode;
import fr.inria.soctrace.framesoc.ui.model.ColorsChangeDescriptor;
import fr.inria.soctrace.framesoc.ui.model.EventProducerNode;
import fr.inria.soctrace.framesoc.ui.model.EventTypeNode;
import fr.inria.soctrace.framesoc.ui.model.GanttTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.HistogramTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TableTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPart;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.framesoc.ui.piechart.PieContributionManager;
import fr.inria.soctrace.framesoc.ui.piechart.model.IPieChartLoader;
import fr.inria.soctrace.framesoc.ui.piechart.model.MergedItem;
import fr.inria.soctrace.framesoc.ui.piechart.model.MergedItems;
import fr.inria.soctrace.framesoc.ui.piechart.model.PieChartLoaderMap;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableColumn;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableRow;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableRowFilter;
import fr.inria.soctrace.framesoc.ui.piechart.providers.StatisticsTableRowLabelProvider;
import fr.inria.soctrace.framesoc.ui.piechart.providers.ValueLabelProvider;
import fr.inria.soctrace.framesoc.ui.providers.EventProducerTreeLabelProvider;
import fr.inria.soctrace.framesoc.ui.providers.EventTypeTreeLabelProvider;
import fr.inria.soctrace.framesoc.ui.providers.TableRowLabelProvider;
import fr.inria.soctrace.framesoc.ui.providers.TreeContentProvider;
import fr.inria.soctrace.framesoc.ui.utils.TimeBar;
import fr.inria.soctrace.framesoc.ui.utils.TreeFilterDialog;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.EventTypeQuery;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Statistics pie chart view.
 * 
 * All the operators share the time selection, the event producer filtering and the event type
 * filtering. When changing operator, if previously loaded data are not up to date with current
 * selection/filtering, data are automatically reloaded.
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
	 * Total work for build job
	 */
	private static final int TOTAL_WORK = 1000;

	/**
	 * Constants
	 */
	public static final boolean HAS_LEGEND = false;
	public static final boolean HAS_TOOLTIPS = true;
	public static final boolean HAS_URLS = true;
	public static final boolean USE_BUFFER = true;

	/**
	 * Hint for filter row
	 */
	private static final String FILTER_HINT = "<Name filter>";

	/**
	 * Loader data
	 */
	private class LoaderDescriptor {
		public IPieChartLoader loader;
		public PieChartLoaderMap map;
		public TimeInterval interval;
		public List<Object> checkedProducers = null;
		public List<Object> checkedTypes = null;
		public List<String> excluded;
		public MergedItems merged;

		public LoaderDescriptor(IPieChartLoader loader) {
			this.loader = loader;
			this.interval = new TimeInterval(0, 0);
			this.map = new PieChartLoaderMap();
			this.excluded = new ArrayList<>();
			this.merged = new MergedItems();
		}

		/**
		 * Check if some data has been loaded (either complete or cancel status).
		 */
		public boolean dataLoaded() {
			return (map != null && (map.isStop() || map.isComplete()));
		}

		/**
		 * Check if the current loaded data is in synch with the current global time selection,
		 * producer selection and type selection.
		 */
		public boolean isAllOk() {
			return intervalOk() && producersOk() && typesOk();
		}

		public boolean intervalOk() {
			return interval.equals(globalLoadInterval);
		}

		public boolean producersOk() {
			return areListsEqual(globalCheckedProducers, checkedProducers);
		}

		public boolean typesOk() {
			return areListsEqual(globalCheckedTypes, checkedTypes);
		}

		public void dispose() {
			loader = null;
			if (map != null) {
				map = null;
			}
		}

		@Override
		public String toString() {
			return "LoaderDescriptor [loader=" + loader + ", dataset=" + map + ", interval="
					+ interval + "]";
		}

	}

	/**
	 * Global loaded interval, shared among all operators.
	 */
	private TimeInterval globalLoadInterval = new TimeInterval(0, 0);

	/**
	 * Global checked producers, shared among all operators.
	 */
	private List<Object> globalCheckedProducers = null;

	/**
	 * Global checked types, shared among all operators.
	 */
	private List<Object> globalCheckedTypes = null;

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

	/**
	 * Label providers for name column.
	 */
	private List<StatisticsTableRowLabelProvider> nameProviders = new ArrayList<>();

	// SWT resources
	private LocalResourceManager resourceManager = new LocalResourceManager(
			JFaceResources.getResources());
	private org.eclipse.swt.graphics.Color grayColor;
	private org.eclipse.swt.graphics.Color blackColor;

	// Filters and actions
	private EventProducerNode[] producerHierarchy;
	private CategoryNode[] typeHierarchy;
	private TreeFilterDialog producerFilterDialog;
	private TreeFilterDialog typeFilterDialog;
	private IAction producerFilterAction;
	private IAction typeFilterAction;
	private List<Object> allTypesElements;
	private List<Object> allProducersElements;

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

	/**
	 * 
	 * @return the current loader, or null if not set
	 */
	public IPieChartLoader getCurrentLoader() {
		if (currentDescriptor != null) {
			return currentDescriptor.loader;
		}
		return null;
	}

	/**
	 * 
	 * @return the current shown trace time unit
	 */
	public TimeUnit getTimeUnit() {
		if (currentShownTrace != null) {
			return TimeUnit.getTimeUnit(currentShownTrace.getTimeUnit());
		}
		return TimeUnit.UNKNOWN;
	}

	// Uncomment this to use the window builder
	// public void createPartControl(Composite parent) {
	// createFramesocPartControl(parent);
	// }

	@Override
	public void createFramesocPartControl(Composite parent) {

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
				boolean firstTime = true;
				for (LoaderDescriptor d : loaderDescriptors) {
					firstTime = firstTime && !d.dataLoaded();
				}
				if (firstTime) {
					return;
				}
				currentDescriptor = loaderDescriptors.get(combo.getSelectionIndex());
				cleanTableFilter();
				refreshTableFilter();
				// use global load interval
				timeBar.setSelection(globalLoadInterval);
				loadPieChart();
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
		textFilter.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				String filter = textFilter.getText().trim();
				if (filter.isEmpty()) {
					cleanTableFilter();
				}
			}

			@Override
			public void focusGained(FocusEvent e) {
				String filter = textFilter.getText().trim();
				if (filter.equals(FILTER_HINT)) {
					textFilter.setText("");
					textFilter.setData("");
					textFilter.setForeground(blackColor);
				}
			}
		});
		textFilter.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.CR || textFilter.getText().trim().isEmpty()) {
					textFilter.setData(textFilter.getText());
					refreshTableFilter();
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
		createContextMenu();

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
		GridLayout gl_timeComposite = new GridLayout(1, false);
		gl_timeComposite.horizontalSpacing = 0;
		timeComposite.setLayout(gl_timeComposite);
		// time manager
		timeBar = new TimeBar(timeComposite, SWT.NONE, true, true);
		timeBar.setEnabled(false);
		combo.setEnabled(false);
		IStatusLineManager statusLineManager = getViewSite().getActionBars().getStatusLineManager();
		timeBar.setStatusLineManager(statusLineManager);

		// button to synch the timebar with the gantt
		timeBar.getSynchButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (combo != null && timeBar != null && currentDescriptor != null) {
					if (currentDescriptor.dataLoaded()) {
						timeBar.setSelection(currentDescriptor.interval.startTimestamp,
								currentDescriptor.interval.endTimestamp);
					} else {
						timeBar.setSelection(currentShownTrace.getMinTimestamp(),
								currentShownTrace.getMaxTimestamp());
					}
				}
			}
		});
		timeBar.getSynchButton().setToolTipText("Synch Selection With Pie Chart");

		// load button
		timeBar.getLoadButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (combo.getSelectionIndex() == -1)
					return;
				currentDescriptor = loaderDescriptors.get(combo.getSelectionIndex());
				cleanTableFilter();
				refreshTableFilter();
				loadPieChart();
			}
		});

		// ----------
		// TOOL BAR
		// ----------

		// filters and actions
		createFilterDialogs();
		createActions();

		// create SWT resources
		createResources();
		// clean the filter, after creating the font
		cleanTableFilter();

	}

	private void createActions() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();

		// Filters actions
		manager.add(createShowProducerFilterAction());
		manager.add(createShowTypeFilterAction());

		// Separator
		manager.add(new Separator());

		// Expand all action
		Action expandAction = new Action() {
			public void run() {
				tableTreeViewer.expandAll();
			}
		};
		expandAction.setText("Expand all");
		expandAction.setToolTipText("Expand all");
		expandAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(
				Activator.PLUGIN_ID, "icons/expandall.gif"));
		manager.add(expandAction);

		// Collapse all action
		Action collapseAction = new Action() {
			public void run() {
				tableTreeViewer.collapseAll();
			}
		};
		collapseAction.setText("Collapse all");
		collapseAction.setToolTipText("Collapse all");
		collapseAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(
				Activator.PLUGIN_ID, "icons/collapseall.gif"));
		manager.add(collapseAction);

		// Separator
		manager.add(new Separator());

		// Framesoc Actions
		TableTraceIntervalAction.add(manager, createTableAction());
		GanttTraceIntervalAction.add(manager, createGanttAction());
		HistogramTraceIntervalAction.add(manager, createHistogramAction());

		// disable all actions
		enableActions(false);
	}

	private IAction createShowProducerFilterAction() {
		producerFilterAction = new Action("", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				showProducerFilterAction();
			}
		};
		producerFilterAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(
				Activator.PLUGIN_ID, "icons/producer_filter.png"));
		producerFilterAction.setToolTipText("Show Event Producer Filter");
		return producerFilterAction;
	}

	private IAction createShowTypeFilterAction() {
		typeFilterAction = new Action("", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				showTypeFilterAction();
			}
		};
		typeFilterAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(
				Activator.PLUGIN_ID, "icons/type_filter.png"));
		typeFilterAction.setToolTipText("Show Event Type Filter");
		return typeFilterAction;
	}

	protected TraceIntervalDescriptor getIntervalDescriptor() {
		if (currentShownTrace == null || !currentDescriptor.dataLoaded())
			return null;
		TraceIntervalDescriptor des = new TraceIntervalDescriptor();
		des.setTrace(currentShownTrace);
		des.setTimeInterval(currentDescriptor.interval);
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

	private void createContextMenu() {
		final Tree tree = tableTreeViewer.getTree();
		final Menu menu = new Menu(tree);
		tree.setMenu(menu);
		menu.addMenuListener(new MenuAdapter() {

			private boolean allLeaves = false;
			private boolean allMerged = false;

			@Override
			public void menuShown(MenuEvent e) {

				// clean menu
				MenuItem[] items = menu.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}

				// get current selection
				final List<String> rows = new ArrayList<>();
				getSelectedRows(rows);

				// exclude
				if (allLeaves && rows.size() > 0) {
					MenuItem hide = new MenuItem(menu, SWT.NONE);
					hide.setText("Exclude " + ((rows.size() > 1) ? "Items" : "Item")
							+ " from Statistics");
					hide.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							currentDescriptor.excluded.addAll(rows);
							refresh();
							refreshTableFilter();
							tableTreeViewer.collapseAll();
						}
					});
				}

				// merge
				if (allLeaves && rows.size() > 1) {
					MenuItem merge = new MenuItem(menu, SWT.NONE);
					merge.setText("Merge Items");
					merge.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							MergeItemsDialog dlg = new MergeItemsDialog(getSite().getShell());
							if (dlg.open() == Dialog.OK) {
								MergedItem mergedItem = new MergedItem();
								mergedItem.setBaseItems(rows);
								if (!currentDescriptor.loader.checkLabel(dlg.getLabel())) {
									MessageDialog.openError(getSite().getShell(), "Error",
											"Illegal label: '" + dlg.getLabel()
													+ "'. Labels must be unique.");
									return;
								}
								mergedItem.setColor(dlg.getColor());
								mergedItem.setLabel(dlg.getLabel());
								currentDescriptor.merged.addMergedItem(mergedItem);
								refresh();
								tableTreeViewer.collapseAll();
							}
						}
					});
				}

				if (!currentDescriptor.excluded.isEmpty() || !currentDescriptor.merged.isEmpty()) {
					new MenuItem(menu, SWT.SEPARATOR);
				}

				// restore merged
				if (!currentDescriptor.merged.isEmpty() && allMerged) {
					MenuItem restore = new MenuItem(menu, SWT.NONE);
					restore.setText("Unmerge Items");
					restore.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							currentDescriptor.merged.removeMergedItems(rows);
							refresh();
							refreshTableFilter();
							tableTreeViewer.collapseAll();
						}
					});
				}

				// restore excluded
				if (!currentDescriptor.excluded.isEmpty()) {
					MenuItem restore = new MenuItem(menu, SWT.NONE);
					restore.setText("Restore Excluded Items");
					restore.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							currentDescriptor.excluded = new ArrayList<>();
							refresh();
							refreshTableFilter();
							tableTreeViewer.collapseAll();
						}
					});
				}

				// restore all merged
				if (!currentDescriptor.merged.isEmpty()) {
					MenuItem restore = new MenuItem(menu, SWT.NONE);
					restore.setText("Unmerge All Merged Items");
					restore.addSelectionListener(new SelectionAdapter() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							currentDescriptor.merged.removeAllMergedItems();
							refresh();
							refreshTableFilter();
							tableTreeViewer.collapseAll();
						}
					});
				}

			}

			private void getSelectedRows(List<String> rows) {
				final IStructuredSelection sel = (IStructuredSelection) tableTreeViewer
						.getSelection();
				if (sel.isEmpty()) {
					allLeaves = false;
					allMerged = false;
					return;
				}
				@SuppressWarnings("unchecked")
				Iterator<StatisticsTableRow> it = (Iterator<StatisticsTableRow>) sel.iterator();
				allLeaves = true;
				allMerged = true;
				while (it.hasNext()) {
					StatisticsTableRow r = it.next();
					try {
						rows.add(r.get(StatisticsTableColumn.NAME));
						allLeaves = allLeaves && !r.hasChildren();
						if (currentDescriptor.loader.isAggregationSupported()) {
							if (r.get(StatisticsTableColumn.NAME).equals(
									currentDescriptor.loader.getAggregatedLabel())) {
								allMerged = false;
							} else {
								allMerged = allMerged && r.hasChildren();
							}
						} else {
							allMerged = allMerged && r.hasChildren();
						}
					} catch (SoCTraceException e) {
						e.printStackTrace();
					}
				}
				return;
			}

		});
	}

	private void createResources() {
		grayColor = resourceManager.createColor(ColorUtil.blend(tableTreeViewer.getTree()
				.getBackground().getRGB(), tableTreeViewer.getTree().getForeground().getRGB()));
		blackColor = tableTreeViewer.getTree().getDisplay().getSystemColor(SWT.COLOR_BLACK);
	}

	private String getStatus(int events, int matched) {
		StringBuilder sb = new StringBuilder();
		sb.append("Name Filter matched ");
		sb.append(matched);
		sb.append(" of ");
		sb.append(events);
		sb.append(" items.");
		if (!currentDescriptor.excluded.isEmpty()) {
			int s = currentDescriptor.excluded.size();
			sb.append("There " + ((s > 1) ? "are " : "is "));
			sb.append(s);
			sb.append(" item" + ((s > 1) ? "s " : " "));
			sb.append("excluded from statistics computation.");
		}
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
				StatisticsTableRowLabelProvider p = new StatisticsTableRowLabelProvider(col);
				nameProviders.add(p);
				elemsViewerCol.setLabelProvider(p);
			} else if (col.equals(StatisticsTableColumn.VALUE)) {
				elemsViewerCol.setLabelProvider(new ValueLabelProvider(col, this));
			} else {
				elemsViewerCol.setLabelProvider(new TableRowLabelProvider(col));
			}

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
		resourceManager.dispose();
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
			// prepare producers to use
			if (globalCheckedProducers != null) {
				List<EventProducer> prods = new ArrayList<>();
				for (Object o : globalCheckedProducers) {
					EventProducerNode epn = (EventProducerNode) o;
					prods.add(epn.getEventProducer());
				}
				currentDescriptor.checkedProducers = new ArrayList<>(globalCheckedProducers);
				currentDescriptor.loader.setEventProducerFilter(prods);
			}
			// prepare types to use
			if (globalCheckedTypes != null) {
				List<EventType> types = new ArrayList<>();
				for (Object o : globalCheckedTypes) {
					if (o instanceof CategoryNode)
						continue;
					EventTypeNode etn = (EventTypeNode) o;
					types.add(etn.getEventType());
				}
				currentDescriptor.checkedTypes = new ArrayList<>(globalCheckedTypes);
				currentDescriptor.loader.setEventTypeFilter(types);
			}
			// load pie
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
		private final TimeInterval loadInterval;

		public DrawerJob(String name, LoaderThread loaderThread, TimeInterval loadInterval) {
			super(name);
			this.loaderThread = loaderThread;
			this.loadInterval = loadInterval;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			DeltaManager dm = new DeltaManager();
			dm.start();
			monitor.beginTask("Loading trace " + currentShownTrace.getAlias(), TOTAL_WORK);
			try {
				enableTimeBar(false);
				PieChartLoaderMap map = currentDescriptor.map;
				boolean done = false;
				boolean refreshed = false;
				long oldLoadedEnd = 0;
				final long intervalDuration = loadInterval.getDuration();
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
					oldLoadedEnd = currentDescriptor.interval.endTimestamp;
					refresh();
					refreshed = true;

					double delta = currentDescriptor.interval.endTimestamp - oldLoadedEnd;
					if (delta > 0) {
						monitor.worked((int) ((delta / intervalDuration) * TOTAL_WORK));
					}
				}
				if (!refreshed) {
					// refresh at least once when there is no data.
					refresh();
				}
				if (currentDescriptor.checkedProducers == null
						|| areListsEqual(currentDescriptor.checkedProducers, allProducersElements)) {
					updateProducerFilter(FilterStatus.UNSET);
				} else {
					updateProducerFilter(FilterStatus.APPLIED);
				}
				if (currentDescriptor.checkedTypes == null
						|| areListsEqual(currentDescriptor.checkedTypes, allTypesElements)) {
					updateTypeFilter(FilterStatus.UNSET);
				} else {
					updateTypeFilter(FilterStatus.APPLIED);
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
					combo.setEnabled(enable);
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

		// reset the global interval
		globalLoadInterval.copy(loadInterval);

		if (currentDescriptor.dataLoaded() && currentDescriptor.isAllOk()) {
			logger.debug("Data is ready. Nothing to do. Refresh only.");
			refresh();
			return;
		}

		// create a new loader map
		currentDescriptor.map = new PieChartLoaderMap();

		// reset the loaded interval in the descriptor
		currentDescriptor.interval.startTimestamp = loadInterval.startTimestamp;
		currentDescriptor.interval.endTimestamp = loadInterval.startTimestamp;

		// create loader and drawer threads
		LoaderThread loaderThread = new LoaderThread(loadInterval);
		DrawerJob drawerJob = new DrawerJob("Pie Chart Drawer Job", loaderThread, loadInterval);
		loaderThread.start();
		drawerJob.schedule();

	}

	private void cleanTableFilter() {
		textFilter.setText(FILTER_HINT);
		textFilter.setData("");
		textFilter.setForeground(grayColor);
	}

	private void refreshTableFilter() {
		if (nameFilter == null || tableTreeViewer == null || statusText == null)
			return;
		if (currentDescriptor == null || currentDescriptor.map == null)
			return;
		String data = (String) textFilter.getData();
		if (data != null) {
			nameFilter.setSearchText(data);
			tableTreeViewer.refresh();
			tableTreeViewer.expandAll();
			logger.debug("items: " + getTreeLeafs(tableTreeViewer.getTree().getItems(), 0));
			statusText.setText(getStatus(
					currentDescriptor.map.size() - currentDescriptor.excluded.size(),
					getTreeLeafs(tableTreeViewer.getTree().getItems(), 0)));
		}
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
		globalLoadInterval.copy(currentDescriptor.interval);
		final IPieChartLoader loader = currentDescriptor.loader;
		loader.updateLabels(values, currentDescriptor.merged.getMergedItems());
		final PieDataset dataset = loader.getPieDataset(values, currentDescriptor.excluded,
				currentDescriptor.merged.getMergedItems());
		final StatisticsTableRow[] roots = loader.getTableDataset(values,
				currentDescriptor.excluded, currentDescriptor.merged.getMergedItems());
		final String title = loader.getStatName();
		final int valuesCount = values.size();

		// update the new UI
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {

				if (currentDescriptor.dataLoaded() && values.isEmpty()) {
					// store the loaded interval in case of no data
					currentDescriptor.interval.startTimestamp = timeBar.getStartTimestamp();
					currentDescriptor.interval.endTimestamp = timeBar.getEndTimestamp();
				}

				// clean UI: composite pie + images
				for (Control c : compositePie.getChildren()) {
					c.dispose();
				}

				// create new chart
				JFreeChart chart = createChart(dataset, "", loader, currentDescriptor.dataLoaded());
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
				tableTreeViewer.collapseAll();
				timeBar.setTimeUnit(TimeUnit.getTimeUnit(currentShownTrace.getTimeUnit()));
				if (currentDescriptor.dataLoaded()) {
					timeBar.setSelection(currentDescriptor.interval.startTimestamp,
							currentDescriptor.interval.endTimestamp);
				} else {
					timeBar.setSelection(currentShownTrace.getMinTimestamp(),
							currentShownTrace.getMaxTimestamp());
				}
				statusText.setText(getStatus(valuesCount, valuesCount));
				enableActions(currentDescriptor.dataLoaded());

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
	private JFreeChart createChart(PieDataset dataset, String title, IPieChartLoader loader,
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
				+ (dataRequested ? "in this time interval" : "yet. Press the Load button."));
		plot.setCircular(true);
		plot.setLabelGenerator(null); // hide labels
		plot.setBackgroundPaint(Color.WHITE);
		plot.setOutlineVisible(false);
		plot.setShadowPaint(Color.WHITE);
		plot.setBaseSectionPaint(Color.WHITE);
		StandardPieToolTipGenerator g = (StandardPieToolTipGenerator) plot.getToolTipGenerator();
		NumberFormat format = ValueLabelProvider.getActualFormat(loader.getFormat(), getTimeUnit());
		StandardPieToolTipGenerator sg = new StandardPieToolTipGenerator(g.getLabelFormat(),
				format, g.getPercentFormat());
		plot.setToolTipGenerator(sg);

		for (Object o : dataset.getKeys()) {
			String key = (String) o;
			plot.setSectionPaint(key, loader.getColor(key).getAwtColor());
		}
		return chart;
	}

	public class StatisticsColumnComparator extends ViewerComparator {
		private StatisticsTableColumn col = StatisticsTableColumn.VALUE;
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

			int rc = 0;
			try {
				if (this.col.equals(StatisticsTableColumn.VALUE)) {
					// number comparison
					Double v1 = Double.valueOf(r1.get(this.col));
					Double v2 = Double.valueOf(r2.get(this.col));
					rc = v1.compareTo(v2);
				} else if (this.col.equals(StatisticsTableColumn.PERCENTAGE)) {
					// percentage comparison 'xx.xx %'
					NumberFormat format = NumberFormat.getInstance();
					Double v1 = format.parse(r1.get(this.col).split(" ")[0]).doubleValue();
					Double v2 = format.parse(r2.get(this.col).split(" ")[0]).doubleValue();
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
			for (StatisticsTableRowLabelProvider p : nameProviders) {
				p.disposeImages();
			}
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
		initTypesAndProducers(trace);
		producerFilterAction.setEnabled(true);
		typeFilterAction.setEnabled(true);
		if (data != null) {
			TraceIntervalDescriptor intDes = (TraceIntervalDescriptor) data;
			OperatorDialog operatorDialog = new OperatorDialog(getSite().getShell());
			if (operatorDialog.open() == Dialog.OK) {
				if (operatorDialog.getSelectionIndex() != -1) {
					combo.select(operatorDialog.getSelectionIndex());
					currentDescriptor = loaderDescriptors.get(operatorDialog.getSelectionIndex());
					timeBar.setSelection(intDes.getStartTimestamp(), intDes.getEndTimestamp());
					globalLoadInterval.copy(intDes.getTimeInterval());
					loadPieChart();
				}
			}
		} else {
			combo.select(0);
			timeBar.setSelection(trace.getMinTimestamp(), trace.getMaxTimestamp());
			txtDescription.setVisible(true);
			globalLoadInterval.startTimestamp = trace.getMinTimestamp();
			globalLoadInterval.endTimestamp = trace.getMaxTimestamp();
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

	private void createFilterDialogs() {
		typeFilterDialog = new TreeFilterDialog(getSite().getShell());
		typeFilterDialog.setColumnNames(new String[] { "Event Type" });
		typeFilterDialog.setContentProvider(new TreeContentProvider());
		typeFilterDialog.setLabelProvider(new EventTypeTreeLabelProvider());
		producerFilterDialog = new TreeFilterDialog(getSite().getShell());
		producerFilterDialog.setColumnNames(new String[] { "Event Producer" });
		producerFilterDialog.setContentProvider(new TreeContentProvider());
		producerFilterDialog.setLabelProvider(new EventProducerTreeLabelProvider());
	}

	private void initTypesAndProducers(Trace t) {
		TraceDBObject traceDB = null;
		try {
			traceDB = TraceDBObject.openNewIstance(t.getDbName());
			EventTypeQuery tq = new EventTypeQuery(traceDB);
			List<EventType> types = tq.getList();
			typeHierarchy = TreeFilterDialog.getTypeHierarchy(types);
			allTypesElements = TreeFilterDialog.listAllInputs(Arrays.asList(typeHierarchy));
			EventProducerQuery pq = new EventProducerQuery(traceDB);
			List<EventProducer> producers = pq.getList();
			producerHierarchy = TreeFilterDialog.getProducerHierarchy(producers);
			allProducersElements = TreeFilterDialog.listAllInputs(Arrays.asList(producerHierarchy));
			traceDB.close();
		} catch (SoCTraceException e) {
			e.printStackTrace();
		} finally {
			DBObject.finalClose(traceDB);
		}
	}

	/**
	 * Callback for the show type filter action
	 */
	private void showTypeFilterAction() {

		if (typeHierarchy.length > 0) {
			typeFilterDialog.setInput(typeHierarchy);
			typeFilterDialog.setTitle("Event Type Filter");
			typeFilterDialog.setMessage("Check the event types to consider");

			if (globalCheckedTypes == null) {
				globalCheckedTypes = allTypesElements;
			}
			typeFilterDialog.setExpandedElements(allTypesElements.toArray());
			typeFilterDialog.setInitialElementSelections(globalCheckedTypes);
			typeFilterDialog.create();

			// reset checked status, managed manually
			typeFilterAction.setChecked(!typeFilterAction.isChecked());
			
			// open the dialog
			if (typeFilterDialog.open() != Window.OK) {
				return;
			}

			// Process selected elements
			if (typeFilterDialog.getResult() != null) {
				globalCheckedTypes = Arrays.asList(typeFilterDialog.getResult());
				if (areListsEqual(allTypesElements, globalCheckedTypes)) {
					// all checked
					if (currentDescriptor.checkedTypes == null
							|| areListsEqual(allTypesElements, currentDescriptor.checkedTypes)) {
						updateTypeFilter(FilterStatus.UNSET);
					} else {
						// the loaded data is with unchecked elements
						updateTypeFilter(FilterStatus.SET);
					}
				} else if (areListsEqual(currentDescriptor.checkedTypes, globalCheckedTypes)) {
					updateTypeFilter(FilterStatus.APPLIED);
				} else {
					updateTypeFilter(FilterStatus.SET);
				}
			}
			if (currentDescriptor.dataLoaded()) {
				loadPieChart();
			}
		}

	}

	/**
	 * Callback for the show type filter action
	 */
	private void showProducerFilterAction() {

		if (producerHierarchy.length > 0) {
			producerFilterDialog.setInput(producerHierarchy);
			producerFilterDialog.setTitle("Event Producer Filter");
			producerFilterDialog.setMessage("Check the event producers to consider");

			if (globalCheckedProducers == null) {
				globalCheckedProducers = allProducersElements;
			}
			producerFilterDialog.setExpandedElements(allProducersElements.toArray());
			producerFilterDialog.setInitialElementSelections(globalCheckedProducers);
			producerFilterDialog.create();

			// reset checked status, managed manually
			producerFilterAction.setChecked(!producerFilterAction.isChecked());
			
			// open the dialog
			if (producerFilterDialog.open() != Window.OK) {
				return;
			}

			// Process selected elements
			if (producerFilterDialog.getResult() != null) {
				globalCheckedProducers = Arrays.asList(producerFilterDialog.getResult());
				if (areListsEqual(allProducersElements, globalCheckedProducers)) {
					// all checked
					if (currentDescriptor.checkedProducers == null
							|| areListsEqual(allProducersElements,
									currentDescriptor.checkedProducers)) {
						updateProducerFilter(FilterStatus.UNSET);
					} else {
						// the loaded data is with unchecked elements
						updateProducerFilter(FilterStatus.SET);
					}
				} else if (areListsEqual(currentDescriptor.checkedProducers, globalCheckedProducers)) {
					updateProducerFilter(FilterStatus.APPLIED);
				} else {
					updateProducerFilter(FilterStatus.SET);
				}
			}
			if (currentDescriptor.dataLoaded()) {
				loadPieChart();
			}
		}
	}

	private boolean areListsEqual(List<Object> l1, List<Object> l2) {
		if (l1 == null) {
			return l2 == null;
		}
		if (l2 == null) {
			return l1 == null;
		}
		Set<Object> s1 = new HashSet<>(l1);
		Set<Object> s2 = new HashSet<>(l2);
		return s1.equals(s2);
	}

	private void updateTypeFilter(FilterStatus status) {
		StringBuilder icon = new StringBuilder("icons/");
		StringBuilder tooltip = new StringBuilder("Show Event Type Filter");

		switch (status) {
		case APPLIED:
			typeFilterAction.setChecked(true);
			icon.append("type_filter.png");
			tooltip.append(" (filter applied)");
			break;
		case SET:
			typeFilterAction.setChecked(false);
			icon.append("type_filter_set.png");
			tooltip.append(" (filter set but not applied)");
			break;
		case UNSET:
			typeFilterAction.setChecked(false);
			icon.append("type_filter.png");
			break;
		}

		typeFilterAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(
				Activator.PLUGIN_ID, icon.toString()));
		typeFilterAction.setToolTipText(tooltip.toString());
	}

	private void updateProducerFilter(FilterStatus status) {
		StringBuilder icon = new StringBuilder("icons/");
		StringBuilder tooltip = new StringBuilder("Show Event Producer Filter");

		switch (status) {
		case APPLIED:
			producerFilterAction.setChecked(true);
			icon.append("producer_filter.png");
			tooltip.append(" (filter applied)");
			break;
		case SET:
			producerFilterAction.setChecked(false);
			icon.append("producer_filter_set.png");
			tooltip.append(" (filter set but not applied)");
			break;
		case UNSET:
			producerFilterAction.setChecked(false);
			icon.append("producer_filter.png");
			break;
		}

		producerFilterAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(
				Activator.PLUGIN_ID, icon.toString()));
		producerFilterAction.setToolTipText(tooltip.toString());

	}

	private static enum FilterStatus {
		UNSET, SET, APPLIED;
	}
}
