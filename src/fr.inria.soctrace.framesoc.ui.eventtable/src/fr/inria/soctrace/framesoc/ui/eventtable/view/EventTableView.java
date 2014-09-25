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
/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.eventtable.view;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.deferred.DeferredContentProvider;
import org.eclipse.jface.viewers.deferred.SetModel;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.wb.swt.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.eventtable.Activator;
import fr.inria.soctrace.framesoc.ui.eventtable.loader.EventTableLoader;
import fr.inria.soctrace.framesoc.ui.eventtable.model.EventTableColumn;
import fr.inria.soctrace.framesoc.ui.eventtable.model.EventTableRow;
import fr.inria.soctrace.framesoc.ui.model.ITableColumn;
import fr.inria.soctrace.framesoc.ui.model.ITableRow;
import fr.inria.soctrace.framesoc.ui.model.LoadDescriptor;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPart;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartManager;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.framesoc.ui.providers.FilterTableRowLabelProvider;
import fr.inria.soctrace.framesoc.ui.providers.TableRowLabelProvider;
import fr.inria.soctrace.framesoc.ui.utils.RowFilter;
import fr.inria.soctrace.framesoc.ui.utils.TimeBar;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Class for the Event table view
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public final class EventTableView extends FramesocPart {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(EventTableView.class);

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = FramesocViews.EVENT_TABLE_VIEW_ID;

	/**
	 * Events Viewer
	 */
	private TableViewer eventsTableViewer;

	/**
	 * Filters viewer
	 */
	private TableViewer filtersTableViewer;

	/**
	 * Data loader
	 */
	private EventTableLoader eventsLoader;

	/**
	 * Column comparator
	 */
	private EventTableColumnComparator comparator;
	
	/**
	 * Row filter
	 */
	private EventTableRowFilter filter;

	/**
	 * Events Columns
	 */
	private Map<EventTableColumn, TableColumn> eventColumns = new HashMap<EventTableColumn, TableColumn>();

	/**
	 * Filters Columns
	 */
	private Map<EventTableColumn, TableColumn> filterColumns = new HashMap<EventTableColumn, TableColumn>();

	/**
	 * Status text
	 */
	private Text statusText;

	/**
	 * Time bar
	 */
	private TimeBar timeBar;

	/**
	 * Synch time bar with table
	 */
	private Button btnSynch;

	/**
	 * Draw current selected time interval
	 */
	private Button btnDraw;

	/**
	 * Start timestamp currently loaded
	 */
	private long startTimestamp;

	/**
	 * End timestamp currently loaded
	 */
	private long endTimestamp;

	/**
	 * Concurrent input
	 */
	private SetModel input;

	// // Uncomment this to use the window builder
	// @Override
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

		// table layout
		Composite tableComposite = new Composite(parent, SWT.FILL);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		GridLayout gl_table = new GridLayout(1, false);
		gl_table.verticalSpacing = 0;
		gl_table.marginWidth = 0;
		gl_table.horizontalSpacing = 0;
		gl_table.marginHeight = 0;
		tableComposite.setLayout(gl_table);

		// viewers and tables
		createViewers(tableComposite);

		// columns
		createColumns();

		// content providers
		createProviders(tableComposite);

		// ----------
		// TOOLBAR
		// ----------

		getViewSite().getActionBars().getToolBarManager().add(createColumnAction());
		if (FramesocPartManager.getInstance().isFramesocPartExisting(
				FramesocViews.GANTT_CHART_VIEW_ID))
			getViewSite().getActionBars().getToolBarManager().add(createGanttAction());
		enableActions(false);

		// -------------
		// STATUS BAR
		// -------------

		Composite statusBar = new Composite(parent, SWT.BORDER);
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
		timeComposite.setLayout(new GridLayout(3, false));
		// time manager
		timeBar = new TimeBar(timeComposite, SWT.NONE);
		timeBar.setEnabled(false);

		// button to synch the timeline with the table
		btnSynch = new Button(timeComposite, SWT.NONE);
		btnSynch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnSynch.addSelectionListener(new SynchListener());
		btnSynch.setToolTipText("Synch with table");
		btnSynch.setEnabled(false);
		btnSynch.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/load.png"));

		// draw button
		btnDraw = new Button(timeComposite, SWT.NONE);
		btnDraw.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnDraw.addSelectionListener(new DrawListener());
		btnDraw.setToolTipText("Draw current selection");
		btnDraw.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/play.png"));
		btnDraw.setEnabled(false);

		// provide our selection for other viewers
		getSite().setSelectionProvider(eventsTableViewer);

		dummyFill();
	}

	class DrawListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			showSelectedWindow(currentShownTrace, timeBar.getStartTimestamp(),
					timeBar.getEndTimestamp(), EventTableLoader.NO_LIMIT);
		}
	}

	class SynchListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			timeBar.setSelection(startTimestamp, endTimestamp);
		}
	}

	// GUI creation

	private String getStatus(int events, int matched) {
		StringBuilder sb = new StringBuilder();
		sb.append("Filter matched ");
		sb.append(matched);
		sb.append(" of ");
		sb.append(events);
		sb.append(" loaded events");
		return sb.toString();
	}

	private IAction createGanttAction() {
		ImageDescriptor img = ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID,
				"icons/gantt.png");
		Action showGantt = new Action("Show in Gantt Chart", img) {
			@Override
			public void run() {
				TraceIntervalDescriptor des = new TraceIntervalDescriptor();
				des.setTrace(currentShownTrace);
				des.setStartTimestamp(startTimestamp);
				des.setEndTimestamp(endTimestamp);
				logger.debug(des.toString());
				FramesocBus.getInstance().send(
						FramesocBusTopic.TOPIC_UI_GANTT_DISPLAY_TIME_INTERVAL, des);
			}
		};
		return showGantt;
	}

	private void enableActions(boolean enabled) {
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		for (IContributionItem item : toolBar.getItems()) {
			if (item instanceof ActionContributionItem) {
				((ActionContributionItem) item).getAction().setEnabled(enabled);
			}
		}
	}

	private IAction createColumnAction() {
		IAction colAction = new Action("Adjust Columns", Action.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				refreshColumnSize();
			}
		};
		colAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID,
				"icons/adjust_h.png"));
		colAction.setToolTipText("Adjust column size to content");
		return colAction;
	}

	private void refreshColumnSize() {
		Iterator<Entry<EventTableColumn, TableColumn>> it = eventColumns.entrySet().iterator();
		while (it.hasNext()) {
			Entry<EventTableColumn, TableColumn> entry = it.next();
			EventTableColumn col = entry.getKey();
			TableColumn tc = entry.getValue();
			tc.pack();
			if (tc.getWidth() < col.getWidth())
				tc.setWidth(col.getWidth());
			filterColumns.get(col).setWidth(tc.getWidth());
		}
	}

	private void createColumns() {

		for (EventTableColumn col : EventTableColumn.values()) {

			// elements table column
			TableViewerColumn elemsViewerCol = new TableViewerColumn(eventsTableViewer, SWT.NONE);
			elemsViewerCol.setLabelProvider(new TableRowLabelProvider(col));
			TableColumn elemsTableCol = elemsViewerCol.getColumn();
			elemsTableCol.setWidth(col.getWidth());
			eventColumns.put(col, elemsTableCol);

			// add a filter for this column in the elements viewer
			RowFilter rowFilter = new RowFilter(col);
			eventsTableViewer.addFilter(rowFilter);

			// filter table column
			TableViewerColumn filterViewerCol = new TableViewerColumn(filtersTableViewer, SWT.NONE);
			filterViewerCol.setLabelProvider(new FilterTableRowLabelProvider(col));
			filterViewerCol.setEditingSupport(new FilterEditingSupport(filtersTableViewer,
					eventsTableViewer, col, rowFilter));
			TableColumn filterTableCol = filterViewerCol.getColumn();
			filterTableCol.addControlListener(new TableColumnResizeListener(elemsTableCol));
			filterTableCol.setWidth(col.getWidth());
			filterTableCol.setText(col.getHeader());
			filterTableCol.addSelectionListener(getSelectionAdapter(elemsTableCol, filterTableCol,
					col));
			filterColumns.put(col, filterTableCol);
		}
	}

	private SelectionAdapter getSelectionAdapter(final TableColumn eventsCol,
			final TableColumn filtersCol, final EventTableColumn col) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(col);
				// update on events table
				eventsTableViewer.getTable().setSortDirection(comparator.getDirection());
				eventsTableViewer.getTable().setSortColumn(eventsCol);
				eventsTableViewer.refresh();
				// update also on filter table to update header icon
				filtersTableViewer.getTable().setSortDirection(comparator.getDirection());
				filtersTableViewer.getTable().setSortColumn(filtersCol);
			}
		};
		return selectionAdapter;
	}

	private void createProviders(Composite parent) {
		// filters: a simple model with a single line.
		filtersTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		filtersTableViewer.setInput(getEmptyInput());

		// events: event rows
		comparator = new EventTableColumnComparator();
		filter = new EventTableRowFilter();
		DeferredContentProvider contentProvider = new DeferredContentProvider(comparator);
		contentProvider.setFilter(filter);
		eventsTableViewer.setContentProvider(contentProvider);
		input = new SetModel();
		eventsTableViewer.setInput(input);
		eventsLoader = new EventTableLoader();
	}

	private void dummyFill() {
		Job fillJob = new Job("fill") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				DeltaManager dm = new DeltaManager();
				dm.start();
				ModelFactory factory = new ModelFactory();
				Event e = factory.createEvent();
				int elem = 0;
				int delta = 2;
				while (!monitor.isCanceled()) {
					input.addAll(getElements(e, delta, elem));
					elem += delta;
					System.out.println("Elem: " + elem);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
				dm.end("stopped");
				return Status.OK_STATUS;
			}
		};
		fillJob.setPriority(Job.DECORATE);
		fillJob.schedule();
	}

	private List<EventTableRow> getElements(Event e, int n, long start) {
		long timestamp = start;
		List<EventTableRow> l = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			e.setTimestamp(timestamp++);
			e.getType().setName("type_" + (long) (Math.random() * 9));
			l.add(new EventTableRow(e));
		}
		return l;
	}

	private List<EventTableRow> getEmptyInput() {
		List<EventTableRow> input = new LinkedList<EventTableRow>();
		input.add(new EventTableRow());
		return input;
	}

	private void createViewers(Composite parent) {
		setContentDescription("Trace: <no trace displayed>");

		filtersTableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL
				| SWT.NO_SCROLL);
		Table filterTable = filtersTableViewer.getTable();
		filterTable.setLinesVisible(true);
		filterTable.setHeaderVisible(true);
		GridData gdFiltersData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		filterTable.setLayoutData(gdFiltersData);
		ColumnViewerToolTipSupport.enableFor(filtersTableViewer, ToolTip.NO_RECREATE);

		eventsTableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		Table elemsTable = eventsTableViewer.getTable();
		elemsTable.setHeaderVisible(false);
		elemsTable.setLinesVisible(true);
		GridData gdEvensData = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gdEvensData.widthHint = 300;
		elemsTable.setLayoutData(gdEvensData);
	}

	@Override
	public void setFocus() {
		if (eventsTableViewer != null)
			eventsTableViewer.getControl().setFocus();
		super.setFocus();
	}

	@Override
	public void dispose() {
		eventsTableViewer = null;
		filtersTableViewer = null;
		comparator = null;
		if (eventsLoader != null)
			eventsLoader.dispose();
		eventsLoader = null;
		if (eventColumns != null)
			eventColumns.clear();
		eventColumns = null;
		if (filterColumns != null)
			filterColumns.clear();
		filterColumns = null;
		if (timeBar != null)
			timeBar.dispose();
		timeBar = null;
		super.dispose();
	}

	/**
	 * Main method: show the table representation of the given time window.
	 * 
	 * @param trace
	 *            trace to show
	 * @param start
	 *            start timestamp to show
	 * @param end
	 *            end timestamp to show
	 * @param limit
	 *            max number of events to load in the window
	 */
	private void showSelectedWindow(final Trace trace, final long start, final long end,
			final int limit) {

		Job job = new Job("Loading Events table...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				boolean closeIfCancelled = (currentShownTrace == null);
				try {

					monitor.beginTask("Loading Events table", IProgressMonitor.UNKNOWN);
					// update trace selection
					if (trace == null) {
						handleCancel(closeIfCancelled);
						return Status.CANCEL_STATUS;
					}

					currentShownTrace = trace;

					// activate the view after setting the current shown trace
					activateView(); // TODO check if needed

					if (monitor.isCanceled()) {
						handleCancel(closeIfCancelled);
						return Status.CANCEL_STATUS;
					}

					// Manage start/end timestamp
					IntervalDesc intervalToLoad = new IntervalDesc(start, end);

					// load events from DB
					final LoadDescriptor des = eventsLoader.loadTimeWindow(trace,
							intervalToLoad.t1, intervalToLoad.t2, limit, monitor);
					logger.debug(des.getMessage());
					if (monitor.isCanceled()) {
						handleCancel(closeIfCancelled);
						return Status.CANCEL_STATUS;
					}

					// refresh table and page selector UI
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							setContentDescription("Trace: " + currentShownTrace.getAlias());
							clearFilters();
							btnSynch.setEnabled(true);
							btnDraw.setEnabled(true);
							timeBar.setEnabled(true);
							timeBar.setMinTimestamp(currentShownTrace.getMinTimestamp());
							timeBar.setMaxTimestamp(currentShownTrace.getMaxTimestamp());
							startTimestamp = Math.max(currentShownTrace.getMinTimestamp(),
									des.getActualStartTimestamp());
							endTimestamp = Math.min(currentShownTrace.getMaxTimestamp(),
									des.getActualEndTimestamp());
							timeBar.setSelection(startTimestamp, endTimestamp);
							logger.debug(timeBar.toString());
							setInput();
							enableActions(true);
						}
					});
					monitor.done();
					return Status.OK_STATUS;
				} catch (Exception e) {
					e.printStackTrace();
					handleCancel(closeIfCancelled);
					return Status.CANCEL_STATUS;
				}
			}
		};
		job.setUser(true);
		job.schedule();

	}

	// Utilities

	private void handleCancel(final boolean closeIfCancelled) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				timeBar.setSelection(startTimestamp, endTimestamp);
				if (closeIfCancelled) {
					FramesocPartManager.getInstance().disposeFramesocPart(EventTableView.this);
					EventTableView.this.hideView();
				}
			}
		});
	}

	private void setInput() {
		DeltaManager dm = new DeltaManager();
		dm.start();
		eventsTableViewer.setInput(eventsLoader.getEvents());
		statusText.setText(getStatus(eventsLoader.getEvents().size(), eventsLoader.getEvents()
				.size()));
		Trace t = eventsLoader.getCurrentTrace();
		if (t != null) {
			logger.debug(dm.endMessage("## refresh input: trace={}" + t.getAlias()));
		} else {
			logger.debug(dm.endMessage("## refresh input"));
		}
	}

	private void clearFilters() {
		filtersTableViewer.setInput(getEmptyInput());
		for (ViewerFilter filter : eventsTableViewer.getFilters())
			((RowFilter) filter).setSearchText("");
	}

	/**
	 * Editing support class for ITableRow objects.
	 */
	public class FilterEditingSupport extends EditingSupport {

		private TableViewer filterViewer;
		private TableViewer elementsViewer;
		private ITableColumn col;
		private RowFilter filter;

		/**
		 * Constructor.
		 * 
		 * @param filterViewer
		 *            filter table viewer
		 * @param elementsViewer
		 *            elements table viewer
		 * @param col
		 *            column on the elements table
		 * @param filter
		 *            filter for a given column of the elements viewer
		 */
		public FilterEditingSupport(TableViewer filterViewer, TableViewer elementsViewer,
				ITableColumn col, RowFilter filter) {
			super(filterViewer);
			this.filterViewer = filterViewer;
			this.elementsViewer = elementsViewer;
			this.col = col;
			this.filter = filter;
		}

		@Override
		protected void setValue(Object element, Object value) {
			((ITableRow) element).set(this.col, value.toString());
			this.filterViewer.update(element, null);
			this.filter.setSearchText(value.toString());
			this.elementsViewer.refresh();
			if (eventsLoader.getEvents() != null) {
				statusText.setText(getStatus(eventsLoader.getEvents().size(), elementsViewer
						.getTable().getItemCount()));
			}
		}

		@Override
		protected Object getValue(Object element) {
			try {
				return ((ITableRow) element).get(this.col);
			} catch (SoCTraceException e) {
				e.printStackTrace();
			}
			return "";
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor(this.filterViewer.getTable());
		}

		@Override
		protected boolean canEdit(Object element) {
			return true;
		}
	}

	/**
	 * Resize listener used to synchronize column size in filters and elements
	 * table. It must be added to the filter viewer.
	 */
	public class TableColumnResizeListener extends ControlAdapter {

		/**
		 * Column to resize when the column this listener is connected to is
		 * resized.
		 */
		private TableColumn destColumn;

		/**
		 * Constructor
		 * 
		 * @param dest
		 *            column to resize
		 */
		public TableColumnResizeListener(TableColumn dest) {
			this.destColumn = dest;
		}

		@Override
		public void controlResized(ControlEvent e) {
			TableColumn column = (TableColumn) e.getSource();
			this.destColumn.setWidth(column.getWidth());
		}
	}

	// move in another file
	public class EventTableColumnComparator implements Comparator<EventTableRow> {
		private EventTableColumn col = EventTableColumn.TIMESTAMP;
		private int direction = SWT.UP;

		public int getDirection() {
			return direction;
		}

		public void setColumn(EventTableColumn col) {
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
		public int compare(EventTableRow r1, EventTableRow r2) {
			int rc = 0;
			try {
				if (this.col.equals(EventTableColumn.TIMESTAMP)
						|| this.col.equals(EventTableColumn.CPU)) {
					// long comparison
					Long v1 = Long.valueOf(r1.get(this.col));
					Long v2 = Long.valueOf(r2.get(this.col));
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
			}
			// If descending order, flip the direction
			if (direction == SWT.DOWN) {
				rc = -rc;
			}
			return rc;
		}
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void showTrace(final Trace trace, final Object data) {
		if (data == null)
			showSelectedWindow(trace, trace.getMinTimestamp(), trace.getMaxTimestamp(),
					EventTableLoader.NO_LIMIT);
		else {
			TraceIntervalDescriptor des = (TraceIntervalDescriptor) data;
			showSelectedWindow(des.getTrace(), des.getStartTimestamp(), des.getEndTimestamp(),
					EventTableLoader.NO_LIMIT);
		}
	}

	@Override
	public void partHandle(String topic, Object data) {
		logger.debug("nothing to do here");
	}

}
