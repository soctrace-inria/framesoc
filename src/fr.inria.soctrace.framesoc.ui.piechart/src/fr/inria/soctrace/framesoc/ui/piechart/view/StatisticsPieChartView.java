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

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.embed.swt.FXCanvas;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.chart.PieChart;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Cell;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.beans.property.ReadOnlyStringWrapper;

// TODO create a fragment plugin for jfreechart
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.Activator;
import fr.inria.soctrace.framesoc.ui.model.ColorsChangeDescriptor;
import fr.inria.soctrace.framesoc.ui.model.EventProducerNode;
import fr.inria.soctrace.framesoc.ui.model.EventTypeNode;
import fr.inria.soctrace.framesoc.ui.model.GanttTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.HistogramTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.ITreeNode;
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
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableFolderRow;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableRow;
import fr.inria.soctrace.framesoc.ui.piechart.model.StatisticsTableRowFilter;
import fr.inria.soctrace.framesoc.ui.piechart.providers.StatisticsTableRowLabelProvider;
import fr.inria.soctrace.framesoc.ui.piechart.providers.ValueLabelProvider;
import fr.inria.soctrace.framesoc.ui.piechart.snapshot.StatisticsPieChartSnapshotDialog;
import fr.inria.soctrace.framesoc.ui.treefilter.FilterDataManager;
import fr.inria.soctrace.framesoc.ui.treefilter.FilterDimension;
import fr.inria.soctrace.framesoc.ui.treefilter.FilterDimensionData;
import fr.inria.soctrace.framesoc.ui.treefilter.ProducerFilterData;
import fr.inria.soctrace.framesoc.ui.treefilter.TreeFilterDialog;
import fr.inria.soctrace.framesoc.ui.treefilter.TypeFilterData;
import fr.inria.soctrace.framesoc.ui.utils.TimeBar;
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

	private class PieFilterData extends FilterDataManager {

		public PieFilterData(FilterDimensionData dimension) {
			super(dimension);
		}

		@Override
		public void reloadAfterChange() {
			loadPieChart();
		}
	}

	/**
	 * Global filters data.
	 */
	private Map<FilterDimension, PieFilterData> globalFilters;

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
			return globalFilters.get(FilterDimension.PRODUCERS).areCheckedEqual(checkedProducers);
		}

		public boolean typesOk() {
			return globalFilters.get(FilterDimension.TYPE).areCheckedEqual(checkedTypes);
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
	private ComboBox<String> combo;

	/**
	 * Description text
	 */
	private TextField txtDescription;

	/**
	 * The chart parent composite
	 */
	private FXCanvas mainFxCanvas;

	/**
	 * The table viewer
	 */
	private TreeTableView<StatisticsTableRow> tableTreeViewer;

	/**
	 * The time management bar
	 */
	private TimeBar timeBar;

	/**
	 * Status text
	 */
	private TextField statusText;

	/**
	 * Column comparator
	 */
	private StatisticsColumnComparator comparator;

	/**
	 * Filter text for table
	 */
	private TextField textFilter;

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

	private Scene scene;

	private Group root;

	private VBox vboxLeft;

	private PieChart pieChart;

	private ObservableList<TreeItem<StatisticsTableRow>> treeItems;
	private List<TreeTableColumn<StatisticsTableRow, String>> tableColumns;

	private TreeItem<StatisticsTableRow> treeRoot;

	private HBox hbox;

	private VBox vboxRight;
	
    private ContextMenu contextMenu;

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
		globalFilters = new HashMap<>();
		globalFilters.put(FilterDimension.PRODUCERS, new PieFilterData(new ProducerFilterData()));
		globalFilters.put(FilterDimension.TYPE, new PieFilterData(new TypeFilterData()));
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

		//SashForm sashForm = new SashForm(parent, SWT.BORDER | SWT.SMOOTH);
		//sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		root = new Group();
		scene = new Scene(root);

		hbox = new HBox();
		vboxLeft = new VBox();
		mainFxCanvas = new FXCanvas(parent, SWT.NONE);
		mainFxCanvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		mainFxCanvas.setLayout(new GridLayout(1, true));	
		mainFxCanvas.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				hbox.setPrefHeight(mainFxCanvas.getClientArea().height);
				vboxLeft.setPrefWidth(mainFxCanvas.getClientArea().width / 2);
				vboxRight.setPrefWidth(mainFxCanvas.getClientArea().width / 2);
				combo.setPrefWidth(mainFxCanvas.getClientArea().width / 2);
			}
		});
				
		mainFxCanvas.setScene(scene);

		// combo
		combo = new ComboBox<String>();
		combo.getSelectionModel().selectedIndexProperty()
				.addListener(new ChangeListener<Number>() {
					@Override
					public void changed(ObservableValue<? extends Number> ov,
							Number value, Number newValue) {
						boolean firstTime = true;
						for (LoaderDescriptor d : loaderDescriptors) {
							firstTime = firstTime && !d.dataLoaded();
						}
						if (firstTime) {
							return;
						}
						currentDescriptor = loaderDescriptors.get(combo
								.getSelectionModel().getSelectedIndex());
						cleanTableFilter();
						refreshTableFilter();
						// use global load interval
						timeBar.setSelection(globalLoadInterval);
						loadPieChart();
					}
				});
		

		for (LoaderDescriptor descriptor : loaderDescriptors) {
			combo.getItems().add(descriptor.loader.getStatName());
		}
		combo.getSelectionModel().selectFirst();
		currentDescriptor = loaderDescriptors.get(0);
		combo.setDisable(true);
		vboxLeft.getChildren().add(combo);


		// Composite Pie
		txtDescription = new TextField("Select one of the above metrics, then press the Load button.");
		txtDescription.setDisable(true);
		txtDescription.setEditable(false);
		txtDescription.setVisible(false);
		vboxLeft.getChildren().add(txtDescription);
		
		pieChart = new PieChart();
		pieChart.setLabelsVisible(false);
		pieChart.setStartAngle(90.0);

		pieChart.setLegendVisible(false);
		VBox.setVgrow(pieChart, Priority.ALWAYS);
		vboxLeft.getChildren().add(pieChart);

		hbox.getChildren().add(vboxLeft);
		
		// Composite Table
		vboxRight = new VBox();
		
		// filter
		textFilter = new TextField();
		textFilter.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0,
					Boolean wasFocused, Boolean isFocus) {
				String filter = textFilter.getText().trim();
				if (isFocus) {
					if (filter.isEmpty()) {
						cleanTableFilter();
					}
				} else {
					if (filter.equals(FILTER_HINT)) {
						textFilter.setText("");
						// textFilter.setData("");
						// textFilter.setForeground(blackColor);
					}
				}
			}
		});
		
		textFilter.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyPressed) {
				if (keyPressed.getCode() == KeyCode.ENTER
						|| (textFilter.getText().trim()).isEmpty()) {
					textFilter.setText(textFilter.getText());
					refreshTableFilter();
				}
			}
		});
		
		vboxRight.getChildren().add(textFilter);
		treeRoot = new TreeItem<StatisticsTableRow>(new StatisticsTableRow("Toto", "0", "12", null));
	
		// table
		tableTreeViewer = new TreeTableView<StatisticsTableRow>(treeRoot);
		tableTreeViewer.setShowRoot(false);
		tableTreeViewer.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tableTreeViewer.getSelectionModel().setCellSelectionEnabled(false);
		comparator = new StatisticsColumnComparator();

		//tableTreeViewer.setComparator(comparator);
		createColumns();
		createContextMenu();
		contextMenu.setAutoHide(true);

		tableTreeViewer.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent e) {
				if (e.getButton() == MouseButton.SECONDARY) {
					contextMenu.hide();

					if (!contextMenu.getItems().isEmpty()) {
						contextMenu.show(tableTreeViewer, e.getScreenX(),
								e.getScreenY());
					} else {
						// The event won't fire if the menu is empty
						contextMenu.fireEvent(new WindowEvent(contextMenu,
								WindowEvent.WINDOW_SHOWN));
					}
				} else {
					contextMenu.hide();
				}
			}
		});

		VBox.setVgrow(tableTreeViewer, Priority.ALWAYS);
		vboxRight.getChildren().add(tableTreeViewer);

		// status bar
		// text
		statusText = new TextField();
		statusText.setText(getStatus(0, 0));
		statusText.setEditable(false);
		
		vboxRight.getChildren().add(statusText);
		hbox.getChildren().add(vboxRight);
		root.getChildren().add(hbox);
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
		combo.setDisable(true);
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

		// load button
		timeBar.getLoadButton().addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (combo.getSelectionModel().getSelectedIndex() == -1)
					return;
				currentDescriptor = loaderDescriptors.get(combo.getSelectionModel().getSelectedIndex());
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

		// clean the filter, after creating the font
		cleanTableFilter();

	}

	private void createActions() {
		IToolBarManager manager = getViewSite().getActionBars().getToolBarManager();

		// Filters actions
		manager.add(globalFilters.get(FilterDimension.PRODUCERS).initFilterAction());
		manager.add(globalFilters.get(FilterDimension.TYPE).initFilterAction());
		
		// Separator
		manager.add(new Separator());
		
		// Snapshot
		manager.add(createSnapshotAction());
		
		// Separator
		manager.add(new Separator());

		// Expand all action
		Action expandAction = new Action() {
			public void run() {
				expandTreeViewNodes(tableTreeViewer.getRoot(), true);
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
				expandTreeViewNodes(tableTreeViewer.getRoot(), false);
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

	protected TraceIntervalDescriptor getIntervalDescriptor() {
		if (currentShownTrace == null || !currentDescriptor.dataLoaded())
			return null;
		TraceIntervalDescriptor des = new TraceIntervalDescriptor();
		des.setTrace(currentShownTrace);
		des.setTimeInterval(currentDescriptor.interval);
		return des;
	}

	private int getTreeLeafs(TreeItem<StatisticsTableRow> root, int v) {
		for (TreeItem<StatisticsTableRow> child : root.getChildren()) {
			if (child.getChildren().size() > 0) {
				v += getTreeLeafs(child, 0); // add only leafs
			} else {
				v += 1;
			}
		}
		return v;
	}
	
	public TreeTableView getTableTreeViewer() {
		return tableTreeViewer;
	}

	public void setTableTreeViewer(TreeTableView tableTreeViewer) {
		this.tableTreeViewer = tableTreeViewer;
	}


	// GUI creation
	private void createContextMenu() {
		contextMenu = new ContextMenu();

		contextMenu.setOnShown(new EventHandler<WindowEvent>() {
			private boolean allLeaves = false;
			private boolean allMerged = false;
		
		    public void handle(WindowEvent e) {
				// clean menu
				contextMenu.getItems().clear();

				// get current selection
				final List<String> rows = new ArrayList<>();
				getSelectedRows(rows);

				// exclude
				if (allLeaves && rows.size() > 0) {
					MenuItem hide = new MenuItem("Exclude " + ((rows.size() > 1) ? "Items" : "Item")
							+ " from Statistics");
					hide.setOnAction(new EventHandler<ActionEvent>() {
					    public void handle(ActionEvent e) {
							currentDescriptor.excluded.addAll(rows);
							refresh();
							refreshTableFilter();
							//tableTreeViewer.collapseAll();
						}
					});
					contextMenu.getItems().add(hide);
				}

				// merge
				if (allLeaves && rows.size() > 1) {
					MenuItem merge = new MenuItem("Merge Items");
					merge.setOnAction(new EventHandler<ActionEvent>() {
					    public void handle(ActionEvent e) {
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
								//tableTreeViewer.collapseAll();
							}
						}
					});
					contextMenu.getItems().add(merge);
				}

				if ((!currentDescriptor.excluded.isEmpty() || !currentDescriptor.merged
						.isEmpty()) && !contextMenu.getItems().isEmpty()) {
					contextMenu.getItems().add(new SeparatorMenuItem());
				}

				// restore merged
				if (!currentDescriptor.merged.isEmpty() && allMerged) {
					MenuItem restore = new MenuItem("Unmerge Items");

					restore.setOnAction(new EventHandler<ActionEvent>() {
					    public void handle(ActionEvent e) {
							currentDescriptor.merged.removeMergedItems(rows);
							refresh();
							refreshTableFilter();
							//tableTreeViewer.collapseAll();
						}
					});
					contextMenu.getItems().add(restore);
				}

				// restore excluded
				if (!currentDescriptor.excluded.isEmpty()) {
					MenuItem restore = new MenuItem("Restore Excluded Items");
					restore.setOnAction(new EventHandler<ActionEvent>() {
					    public void handle(ActionEvent e) {
							currentDescriptor.excluded = new ArrayList<>();
							refresh();
							refreshTableFilter();
							//tableTreeViewer.collapseAll();
						}
					});
					contextMenu.getItems().add(restore);
				}

				// restore all merged
				if (!currentDescriptor.merged.isEmpty()) {
					MenuItem restore = new MenuItem("Unmerge All Merged Items");
					restore.setOnAction(new EventHandler<ActionEvent>() {
					    public void handle(ActionEvent e) {
							currentDescriptor.merged.removeAllMergedItems();
							refresh();
							refreshTableFilter();
							//tableTreeViewer.collapseAll();
						}
					});
					contextMenu.getItems().add(restore);
				}
			}

			private void getSelectedRows(List<String> rows) {
				final ObservableList<TreeItem<StatisticsTableRow>> sel = tableTreeViewer
						.getSelectionModel().getSelectedItems();
				if (sel.isEmpty()) {
					allLeaves = false;
					allMerged = false;
					return;
				}
				//@SuppressWarnings("unchecked")
				//Iterator<TreeItem<StatisticsTableRow>> it = (Iterator<TreeItem<StatisticsTableRow>>) sel.iterator();
	
				logger.debug("Selection size in table is: " + sel.size());
				
				allLeaves = true;
				allMerged = true;
				
				List<StatisticsTableRow> tmprows = sel.stream()
						.filter(treeItem -> treeItem != null)
						.map(TreeItem::getValue).collect(Collectors.toList());

				for (StatisticsTableRow r : tmprows) {
					try {
						rows.add(r.get(StatisticsTableColumn.NAME));
						allLeaves = allLeaves && !r.hasChildren();
						if (currentDescriptor.loader.isAggregationSupported()) {
							if (r.get(StatisticsTableColumn.NAME).equals(
									currentDescriptor.loader
											.getAggregatedLabel())) {
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
		tableColumns = new ArrayList<TreeTableColumn<StatisticsTableRow,String>>();
		
		for (final StatisticsTableColumn col : StatisticsTableColumn.values()) {
			TreeTableColumn<StatisticsTableRow, String> elemsViewerCol = new TreeTableColumn<StatisticsTableRow, String>(
					col.getHeader());

			if (col.equals(StatisticsTableColumn.NAME)) {
				// add a filter for this column
				nameFilter = new StatisticsTableRowFilter(col);
				
				//tableTreeViewer.addFilter(nameFilter);
				// the label provider puts also the image
				StatisticsTableRowLabelProvider p = new StatisticsTableRowLabelProvider(col);
				nameProviders.add(p);
				elemsViewerCol
						.setCellValueFactory(new Callback<CellDataFeatures<StatisticsTableRow, String>, ObservableValue<String>>() {
							public ObservableValue<String> call(
									CellDataFeatures<StatisticsTableRow, String> param) {
								try {
									return new ReadOnlyStringWrapper(param.getValue()
											.getValue().get(col));
								} catch (SoCTraceException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								return null;
							}
						});
			} else if (col.equals(StatisticsTableColumn.VALUE)) {
				elemsViewerCol.setCellValueFactory(new Callback<CellDataFeatures<StatisticsTableRow, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(
							CellDataFeatures<StatisticsTableRow, String> param) {
						try {
							 NumberFormat format = ValueLabelProvider.getActualFormat(currentDescriptor.loader.getFormat(), getTimeUnit());
							return new ReadOnlyStringWrapper(format.format(Double.valueOf(param.getValue().getValue().get(col))));
						} catch (SoCTraceException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					}
				});
				//elemsViewerCol.setCellValueFactory(new ValueLabelProvider(col, this));
			} else {
				elemsViewerCol.setCellValueFactory(new Callback<CellDataFeatures<StatisticsTableRow, String>, ObservableValue<String>>() {
					public ObservableValue<String> call(
							CellDataFeatures<StatisticsTableRow, String> param) {
						try {
							return new ReadOnlyStringWrapper(param.getValue().getValue().get(col));
						} catch (SoCTraceException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return null;
					}
				});
						
						//new TableRowLabelProvider(col));
			}

			//final TreeColumn elemsTableCol = elemsViewerCol.getColumns();
			elemsViewerCol.setPrefWidth(col.getWidth());
			/*elemsViewerCol.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					comparator.setColumn(col);
					tableTreeViewer.getTree().setSortDirection(comparator.getDirection());
					tableTreeViewer.getTree().setSortColumn(elemsTableCol);
					tableTreeViewer.refresh();
				}
			});*/
			tableColumns.add(elemsViewerCol);
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
			IPieChartLoader l = currentDescriptor.loader;
			// prepare producers to use
			l.setEventProducerFilter(globalFilters.get(FilterDimension.PRODUCERS).getCheckedId());
			// prepare types to use
			l.setEventTypeFilter(globalFilters.get(FilterDimension.TYPE).getCheckedId());
			// load pie
			l.load(currentShownTrace, loadInterval, currentDescriptor.map, monitor);
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
					combo.setDisable(!enable);
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
		timeBar.setDisplayInterval(loadInterval);

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
		
		// set the filters
		currentDescriptor.checkedProducers = globalFilters.get(FilterDimension.PRODUCERS).getChecked();
		currentDescriptor.checkedTypes = globalFilters.get(FilterDimension.TYPE).getChecked();

		// create loader and drawer threads
		LoaderThread loaderThread = new LoaderThread(loadInterval);
		DrawerJob drawerJob = new DrawerJob("Pie Chart Drawer Job", loaderThread, loadInterval);
		loaderThread.start();
		drawerJob.schedule();
	}

	private void cleanTableFilter() {
		textFilter.setPromptText(FILTER_HINT);
		//textFilter.setData("");
	}

	private void refreshTableFilter() {
		if (nameFilter == null || tableTreeViewer == null || statusText == null)
			return;
		if (currentDescriptor == null || currentDescriptor.map == null)
			return;
		String data = (String) textFilter.getText();
		if (!data.isEmpty()) {
			nameFilter.setSearchText(data);
			//tableTreeViewer.refresh();
			//tableTreeViewer.expandAll();
			expandTreeViewNodes(tableTreeViewer.getRoot(), true);
			logger.debug("items: " + getTreeLeafs(tableTreeViewer.getRoot(), 0));
			statusText.setText(getStatus(
					currentDescriptor.map.size() - currentDescriptor.excluded.size(),
					getTreeLeafs(tableTreeViewer.getRoot(), 0)));
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
		final ObservableList<PieChart.Data> dataset = loader.getPieDataset(values, currentDescriptor.excluded,
				currentDescriptor.merged.getMergedItems());
		final List<StatisticsTableRow> roots = loader.getTableDataset(values,
				currentDescriptor.excluded, currentDescriptor.merged.getMergedItems());
		createTreeTableItem(roots);
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

				// Update dataset
				pieChart.setData(dataset);
				
				pieChart.getData().stream().forEach(data -> {
					Tooltip tooltip = new Tooltip();
				    NumberFormat format = ValueLabelProvider.getActualFormat(loader.getFormat(), getTimeUnit());
				    tooltip.setText(data.getName()+ ": (" + format.format(data.getPieValue()) + ")");
				    Tooltip.install(data.getNode(), tooltip);
			
				    data.pieValueProperty().addListener((observable, oldValue, newValue) -> 
				        tooltip.setText(data.getName()+ ": " +format.format(newValue) + ")"));
				    
					String key = data.getName();
					String color = "#" + getHexaColor(loader.getColor(key).getAwtColor());
		
					// Set the corresponding color
					data.getNode().setStyle("-fx-pie-color: " + color + ";");
				});
				
				// Remove present elements
				treeRoot.getChildren().clear();
				// Add current
				treeRoot.getChildren().addAll(treeItems);
				
				tableTreeViewer.getColumns().clear();
				tableTreeViewer.getColumns().addAll(tableColumns);
				
				// update other elements
				/*if (roots.length == 0) {
					tableTreeViewer.setInput(null);
				} else {
					tableTreeViewer.setInput(roots);
				}*/
				expandTreeViewNodes(tableTreeViewer.getRoot(), false);
				//tableTreeViewer.collapseAll();
				timeBar.setTimeUnit(TimeUnit.getTimeUnit(currentShownTrace
						.getTimeUnit()));
				timeBar.setSelection(currentDescriptor.interval.startTimestamp,
						currentDescriptor.interval.endTimestamp);
				timeBar.setDisplayInterval(currentDescriptor.interval);
				statusText.setText(getStatus(valuesCount, valuesCount));
				enableActions(currentDescriptor.dataLoaded());
			}
		});
	}

	private void createTreeTableItem(List<StatisticsTableRow> roots) {
		treeItems = FXCollections.observableArrayList();
		roots.stream()
				.forEach(
						row -> {
							treeItems.add(new TreeItem<>(row));

							if (row instanceof StatisticsTableFolderRow) {
								StatisticsTableFolderRow folder = (StatisticsTableFolderRow) row;
								for (ITreeNode childRow : folder.getChildren()) {
									StatisticsTableRow childTableRow = (StatisticsTableRow) childRow;
									treeItems.get(treeItems.size() - 1)
											.getChildren()
											.add(new TreeItem<>(childTableRow));
								}
							}
						});
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

			Cell r1 = (Cell) e1;
			Cell r2 = (Cell) e2;

			int rc = 0;
			try {
				if (this.col.equals(StatisticsTableColumn.VALUE)) {
					// number comparison
					Double v1 = Double.valueOf((Double)r1.getItem());
					Double v2 = Double.valueOf((Double)r2.getItem());
					rc = v1.compareTo(v2);
				} else if (this.col.equals(StatisticsTableColumn.PERCENTAGE)) {
					// percentage comparison 'xx.xx %'
					NumberFormat format = NumberFormat.getInstance();
					Double v1 = format.parse(((String)r1.getItem()).split(" ")[0]).doubleValue();
					Double v2 = format.parse(((String)r2.getItem()).split(" ")[0]).doubleValue();
					rc = v1.compareTo(v2);
				} else {
					// string comparison
					String v1 = (String)(r1.getItem());
					String v2 = (String)(r2.getItem());
					rc = v1.compareTo(v2);
				}
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
		combo.setDisable(false);
		timeBar.setEnabled(true);
		timeBar.setExtrema(trace.getMinTimestamp(), trace.getMaxTimestamp());
		currentShownTrace = trace;
		initTypesAndProducers(trace);
		if (data != null) {
			TraceIntervalDescriptor intDes = (TraceIntervalDescriptor) data;
			// propose operator selection only if there is no data loaded
			if (!currentDescriptor.dataLoaded()) {
				OperatorDialog operatorDialog = new OperatorDialog(getSite().getShell());
				if (operatorDialog.open() == Dialog.OK) {
					if (operatorDialog.getSelectionIndex() != -1) {
						combo.getSelectionModel().select(operatorDialog.getSelectionIndex());
						currentDescriptor = loaderDescriptors.get(operatorDialog
								.getSelectionIndex());
					}
				}
			}
			timeBar.setSelection(intDes.getStartTimestamp(), intDes.getEndTimestamp());
			globalLoadInterval.copy(intDes.getTimeInterval());
			loadPieChart();
		} else {
			combo.getSelectionModel().select(0);
			timeBar.setSelection(trace.getMinTimestamp(), trace.getMaxTimestamp());
			timeBar.setDisplayInterval(timeBar.getSelection());
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
		for (PieFilterData data : globalFilters.values()) {
			data.initFilterDialog(getSite().getShell());
		}
	}

	private void initTypesAndProducers(Trace t) {
		TraceDBObject traceDB = null;
		try {
			traceDB = TraceDBObject.openNewInstance(t.getDbName());
			// types
			EventTypeQuery tq = new EventTypeQuery(traceDB);
			List<EventType> types = tq.getList();
			ITreeNode[] typeHierarchy = TreeFilterDialog.getTypeHierarchy(types);
			globalFilters.get(FilterDimension.TYPE).setFilterRoots(typeHierarchy);
			// producers
			EventProducerQuery pq = new EventProducerQuery(traceDB);
			List<EventProducer> producers = pq.getList();
			ITreeNode[] producerHierarchy = TreeFilterDialog.getProducerHierarchy(producers);
			globalFilters.get(FilterDimension.PRODUCERS).setFilterRoots(producerHierarchy);
			traceDB.close();
		} catch (SoCTraceException e) {
			e.printStackTrace();
		} finally {
			DBObject.finalClose(traceDB);
		}
	}
	
	/**
	 * Initialize the snapshot action
	 * 
	 * @return the action
	 */
	public IAction createSnapshotAction() {
		SnapshotAction snapshotAction = new SnapshotAction("",
				IAction.AS_PUSH_BUTTON);
		snapshotAction.pieView = this;
		snapshotAction.setImageDescriptor(ResourceManager
				.getPluginImageDescriptor(Activator.PLUGIN_ID,
						"icons/snapshot.png"));
		snapshotAction.setToolTipText("Take a snapshot");

		return snapshotAction;
	}
	
	private class SnapshotAction extends Action {
		public StatisticsPieChartView pieView;

		public SnapshotAction(String string, int asPushButton) {
			super(string, asPushButton);
		}

		@Override
		public void run() {
			new StatisticsPieChartSnapshotDialog(getSite().getShell(), pieView).open();
		}
	}
	
	public String getSnapshotInfo() {
		StringBuffer output = new StringBuffer();
		output.append("\nStatisitics operator: ");
		output.append(getCurrentLoader().getStatName());
		output.append("\nLoaded start timestamp: ");
		output.append(globalLoadInterval.startTimestamp);
		output.append("\nLoaded end timestamp: ");
		output.append(globalLoadInterval.endTimestamp);

		// Filtered types
		String filteredTypes = "";
		List<Object> filteredType = globalFilters.get(FilterDimension.TYPE).getChecked();
		List<Object> allTypes = globalFilters.get(FilterDimension.TYPE).getAllElements();
		for (Object typeObject : allTypes) {
			if (!filteredType.contains(typeObject)) {
				if (typeObject instanceof EventTypeNode) {
					filteredTypes = filteredTypes
							+ ((EventTypeNode) typeObject).getEventType().getName() + ", ";
				}
			}
		}
		
		// If there was some filtered event producers
		if (!filteredTypes.isEmpty()) {
			// Remove last separator
			filteredTypes = filteredTypes.substring(0, filteredTypes.length() - 2);
			output.append("\nFiltered event types: ");
			output.append(filteredTypes);
		}
	
		// Filtered event producers
		String filteredEP = "";
		List<Object> filteredProducers = globalFilters.get(FilterDimension.PRODUCERS).getChecked();
		List<Object> allProducers = globalFilters.get(FilterDimension.PRODUCERS).getAllElements();
		for (Object producer : allProducers) {
			if (!filteredProducers.contains(producer)) {
				if (producer instanceof EventProducerNode) {
					filteredEP = filteredEP
							+ ((EventProducerNode) producer).getName() + ", ";
				}
			}
		}

		// If there was some filtered event producers
		if (!filteredEP.isEmpty()) {
			// Remove last separator
			filteredEP = filteredEP.substring(0, filteredEP.length() - 2);
			output.append("\nFiltered event producers: ");
			output.append(filteredEP);
		}
		
		return output.toString();
	}

	
	private void expandTreeViewNodes(TreeItem anItem, boolean expand) {
		if(anItem == null)
			return;
	/*	
		for (Object aTreeitem : anItem.getChildren()){
			for (Object childItem :  ((TreeItem)aTreeitem).getChildren())
				expandTreeViewNodes(((TreeItem)childItem), expand);
			anItem.setExpanded(expand);
		}*/
	}

	private String getHexaColor(java.awt.Color color) {
		String hexValue = "";
		List<String> colorValue = new ArrayList<String>();
		colorValue.add(Integer.toHexString(color.getRed()));
		colorValue.add(Integer.toHexString(color.getGreen()));
		colorValue.add(Integer.toHexString(color.getBlue()));

		for (String colorString : colorValue) {
			if (colorString.length() <= 1){
				colorString = "0" + colorString;
			}
			hexValue = hexValue + colorString;
		}

		return hexValue;
	}

	
}
