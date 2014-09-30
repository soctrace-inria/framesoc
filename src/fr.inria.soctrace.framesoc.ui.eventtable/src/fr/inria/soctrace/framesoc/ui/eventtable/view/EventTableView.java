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

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ColorUtil;
import org.eclipse.wb.swt.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.linuxtools.tmf.ui.widgets.virtualtable.ColumnData;
import fr.inria.linuxtools.tmf.ui.widgets.virtualtable.TmfVirtualTable;
import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.eventtable.Activator;
import fr.inria.soctrace.framesoc.ui.eventtable.loader.EventLoader;
import fr.inria.soctrace.framesoc.ui.eventtable.loader.IEventLoader;
import fr.inria.soctrace.framesoc.ui.eventtable.loader.LoaderQueue;
import fr.inria.soctrace.framesoc.ui.eventtable.model.EventTableColumn;
import fr.inria.soctrace.framesoc.ui.eventtable.model.EventTableRow;
import fr.inria.soctrace.framesoc.ui.eventtable.model.EventTableRowFilter;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPart;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartManager;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.framesoc.ui.utils.TimeBar;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
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
	 * Hint for filter row
	 */
	private static final String FILTER_HINT = "<filter>";

	/**
	 * Cache of event table rows
	 */
	private EventTableCache cache = new EventTableCache();

	/**
	 * Virtual table
	 */
	private TmfVirtualTable table;

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
	 * Flag for enabling/disabling the filter
	 */
	private boolean filterEnabled = false;

	// SWT resources
	private LocalResourceManager resourceManager = new LocalResourceManager(
			JFaceResources.getResources());
	private Color grayColor;
	private Color blackColor;
	private Font boldFont;

	// Filtering
	private int filterMatchCount;
	private int filterCheckCount;
	private FilterThread filterThread;
	private EventTableRowFilter rowFilter = new EventTableRowFilter();
	private final Object filterSyncObj = new Object();

	// loading
	private Job loaderJob;
	private Thread drawerThread;

	public interface Key {
		/** Column object, set on a column */
		String COLUMN_OBJ = "$field_id"; //$NON-NLS-1$

		/** Filter text, set on a column */
		String FILTER_TXT = "$fltr_txt"; //$NON-NLS-1$

		/** Filter object, set on the table */
		String FILTER_OBJ = "$fltr_obj"; //$NON-NLS-1$
	}

	// // Uncomment this to use the window builder
	// @Override
	// public void createPartControl(Composite parent) {
	// createFramesocPartControl(parent);
	// }

	private void showWindow(Trace trace, long start, long end) {

		if (trace == null) {
			return;
		}

		// create the queue
		LoaderQueue<Event> queue = new LoaderQueue<>();

		// create the event loader
		IEventLoader loader = new EventLoader();
		loader.setTrace(trace);
		loader.setQueue(queue);

		// compute the actual time interval to load (trim with trace min and max)
		TimeInterval interval = new TimeInterval(start, end);
		interval.startTimestamp = Math.max(trace.getMinTimestamp(), interval.startTimestamp);
		interval.endTimestamp = Math.min(trace.getMaxTimestamp(), interval.endTimestamp);

		// check for contained interval
		if (checkReuse(trace, interval)) {
			cache.index(interval);
			table.refresh();
			timeBar.setMinTimestamp(interval.startTimestamp);
			timeBar.setMaxTimestamp(interval.endTimestamp);
			return;
		}

		// launch the job loading the queue
		launchLoaderJob(loader, interval);

		// update the viewer
		launchDrawerThread(interval, trace, queue);
	}

	private boolean checkReuse(Trace trace, TimeInterval interval) {
		if (trace.equals(currentShownTrace) && cache.contains(interval)) {
			return true;
		}
		return false;
	}

	private void launchLoaderJob(final IEventLoader loader, final TimeInterval requestedInterval) {
		loaderJob = new Job("Loading Event Table...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				DeltaManager all = new DeltaManager();
				all.start();
				loader.loadWindow(requestedInterval.startTimestamp, requestedInterval.endTimestamp,
						monitor);
				logger.debug(all.endMessage("Loader Job: loaded everything"));
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				return Status.OK_STATUS;
			}
		};
		loaderJob.setUser(false);
		loaderJob.schedule();
	}

	private void launchDrawerThread(final TimeInterval requestedInterval, final Trace trace,
			final LoaderQueue<Event> queue) {

		setContentDescription("Trace: " + trace.getAlias());
		timeBar.setMinTimestamp(trace.getMinTimestamp());
		timeBar.setMaxTimestamp(trace.getMaxTimestamp());
		table.clearAll();

		drawerThread = new Thread() {

			private boolean refreshBusy = false;
			private boolean refreshPending = false;
			private Object syncObj = new Object();

			@Override
			public void run() {

				DeltaManager all = new DeltaManager();
				all.start();

				currentShownTrace = trace;
				endTimestamp = Long.MIN_VALUE;
				startTimestamp = Long.MAX_VALUE;
				filterEnabled = false;
				cache.clear();

				while (!queue.done()) {
					try {
						List<Event> events = queue.pop();
						//logger.debug("Events: {}", events.size());
						if (events.isEmpty())
							continue;
						// put the events in the cache
						for (Event e : events) {
							cache.put(new EventTableRow(e));
							startTimestamp = Math.max(requestedInterval.startTimestamp,
									Math.min(e.getTimestamp(), startTimestamp));
							endTimestamp = Math.min(requestedInterval.endTimestamp,
									Math.max(e.getTimestamp(), endTimestamp));
							refreshTable();
						}
					} catch (InterruptedException e) {
						logger.debug("Interrupted while taking the queue head");
					}
				}

				if (!queue.isStop()) {
					// we have not been stopped: the requested interval has been displayed
					startTimestamp = requestedInterval.startTimestamp;
					endTimestamp = requestedInterval.endTimestamp;
					// refresh one last time
					refreshTable();
					activate();
				} else {
					// we have been stopped: something has not been displayed in the table
					startTimestamp = Math.max(requestedInterval.startTimestamp, startTimestamp);
					endTimestamp = Math.min(requestedInterval.endTimestamp, endTimestamp);
					if (cache.getActiveRowCount() > 0) {
						// refresh one last time
						refreshTable();
						activate();
					} else {
						closeView();
					}
				}

				logger.debug(all.endMessage("Drawer Thread: visualizing everything"));
				logger.debug("start: {}", startTimestamp);
				logger.debug("end: {}", endTimestamp);
			}

			/**
			 * Refresh the filter.
			 */
			public void refreshTable() {
				synchronized (syncObj) {
					if (refreshBusy) {
						refreshPending = true;
						return;
					}
					refreshBusy = true;
				}
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						if (table.isDisposed()) {
							return;
						}
						int events = cache.getActiveRowCount();
						table.setItemCount(events + 1); // +1 for header row
						table.refresh();
						timeBar.setSelection(startTimestamp, endTimestamp);
						statusText.setText(getStatus(events, events));
						synchronized (syncObj) {
							refreshBusy = false;
							if (refreshPending) {
								refreshPending = false;
								refreshTable();
							}
						}
					}
				});
			}

			public void activate() {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						filterEnabled = true;
						btnSynch.setEnabled(true);
						btnDraw.setEnabled(true);
						timeBar.setEnabled(true);
					}
				});
			}
		};

		drawerThread.start();
	}

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

		// Create the virtual table
		final int style = SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER;
		table = new TmfVirtualTable(tableComposite, style);

		// Set the table layout
		final GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(layoutData);

		// Create resources
		createResources();

		// Some cosmetic enhancements
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// Set the columns
		setColumnHeaders();

		// Set the frozen row for header row
		table.setFrozenRowCount(1);

		// Create the header row cell editor
		createHeaderEditor();

		// Handle the table item requests
		table.addListener(SWT.SetData, new Listener() {

			@Override
			public void handleEvent(final org.eclipse.swt.widgets.Event event) {

				final TableItem item = (TableItem) event.item;
				int index = event.index - 1; // -1 for the header row

				if (event.index == 0) {
					setHeaderRowItemData(item);
					return;
				}

				final EventTableRow row = cache.get(index);
				if (row != null) {
					item.setText(getItemStrings(row));
					return;
				}

				// Else, fill the cache asynchronously (and off the UI thread)
				event.doit = false;
			}
		});

		table.setItemCount(1); // only the header at the beginning

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
		btnSynch.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/load.png"));
		btnSynch.setEnabled(false);

		// draw button
		btnDraw = new Button(timeComposite, SWT.NONE);
		btnDraw.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnDraw.addSelectionListener(new DrawListener());
		btnDraw.setToolTipText("Draw current selection");
		btnDraw.setImage(ResourceManager.getPluginImage(Activator.PLUGIN_ID, "icons/play.png"));
		btnDraw.setEnabled(false);

	}

	private String[] getItemStrings(EventTableRow row) {
		String values[] = new String[EventTableColumn.values().length];
		int i = 0;
		for (EventTableColumn col : EventTableColumn.values()) {
			try {
				values[i] = row.get(col);
			} catch (SoCTraceException e) {
				logger.error("Columnt " + col.getHeader() + " not found in this row.");
				e.printStackTrace();
			} finally {
				i++;
			}
		}
		return values;
	}

	private void setColumnHeaders() {
		ColumnData columnData[] = new ColumnData[EventTableColumn.values().length];
		int i = 0;
		for (EventTableColumn col : EventTableColumn.values()) {
			columnData[i++] = new ColumnData(col.getHeader(), col.getWidth(), SWT.LEFT);
		}
		table.setColumnHeaders(columnData);
		i = 0;
		for (EventTableColumn col : EventTableColumn.values()) {
			table.getColumns()[i++].setData(Key.COLUMN_OBJ, col);
		}
	}

	/**
	 * Create an editor for the header.
	 */
	private void createHeaderEditor() {
		final TableEditor tableEditor = table.createTableEditor();
		tableEditor.horizontalAlignment = SWT.LEFT;
		tableEditor.verticalAlignment = SWT.CENTER;
		tableEditor.grabHorizontal = true;
		tableEditor.minimumWidth = 50;

		// Handle the header row selection
		table.addMouseListener(new MouseAdapter() {
			int columnIndex;
			TableColumn column;
			TableItem item;

			@Override
			public void mouseDown(final MouseEvent event) {
				if (event.button != 1 || !filterEnabled) {
					return;
				}
				// Identify the selected row
				final Point point = new Point(event.x, event.y);
				item = table.getItem(point);

				// Header row selected
				if ((item != null) && (table.indexOf(item) == 0)) {

					// Identify the selected column
					columnIndex = -1;
					for (int i = 0; i < table.getColumns().length; i++) {
						final Rectangle rect = item.getBounds(i);
						if (rect.contains(point)) {
							columnIndex = i;
							break;
						}
					}

					if (columnIndex == -1) {
						return;
					}

					column = table.getColumns()[columnIndex];

					// The control that will be the editor must be a child of the Table
					final Text newEditor = (Text) table.createTableEditorControl(Text.class);
					final String headerString = (String) column.getData(Key.FILTER_TXT);
					if (headerString != null) {
						newEditor.setText(headerString);
					}
					newEditor.addFocusListener(new FocusAdapter() {
						@Override
						public void focusLost(final FocusEvent e) {
							final boolean changed = updateHeader(newEditor.getText());
							if (changed) {
								applyHeader();
							}
						}
					});
					newEditor.addKeyListener(new KeyAdapter() {
						@Override
						public void keyPressed(final KeyEvent e) {
							if (e.character == SWT.CR) {
								updateHeader(newEditor.getText());
								applyHeader();

								// Set focus on the table so that the next
								// carriage return goes to the next result
								EventTableView.this.table.setFocus();
							} else if (e.character == SWT.ESC) {
								tableEditor.getEditor().dispose();
							}
						}
					});
					newEditor.selectAll();
					newEditor.setFocus();
					tableEditor.setEditor(newEditor, item, columnIndex);
				}
			}

			/*
			 * returns true if the value was changed
			 */
			private boolean updateHeader(final String text) {
				if (text.trim().length() > 0) {
					try {
						final String regex = regexFix(text);
						Pattern.compile(regex);
						if (regex.equals(column.getData(Key.FILTER_TXT))) {
							tableEditor.getEditor().dispose();
							return false;
						}
						EventTableColumn col = (EventTableColumn) column.getData(Key.COLUMN_OBJ);
						rowFilter.setSearchText(col, regex);
						column.setData(Key.FILTER_TXT, regex);
					} catch (final PatternSyntaxException ex) {
						tableEditor.getEditor().dispose();
						MessageDialog.openError(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(), ex.getDescription(),
								ex.getMessage());
						return false;
					}
				} else {
					if (column.getData(Key.FILTER_TXT) == null) {
						tableEditor.getEditor().dispose();
						return false;
					}
					column.setData(Key.FILTER_TXT, null);
				}
				return true;
			}

			public String regexFix(String pattern) {
				String ret = pattern;
				// if the pattern does not contain one of the expressions .* !^
				// (at the beginning) $ (at the end), then a .* is added at the
				// beginning and at the end of the pattern
				if (!(ret.indexOf(".*") >= 0 || ret.charAt(0) == '^' || ret.charAt(ret.length() - 1) == '$')) { //$NON-NLS-1$
					ret = ".*" + ret + ".*"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				return ret;
			}

			private void applyHeader() {
				System.out.println("apply header");
				stopFilterThread();
				filterMatchCount = 0;
				filterCheckCount = 0;
				table.clearAll();
				table.setData(Key.FILTER_OBJ, rowFilter);
				table.setItemCount(1); // +1 for header row
				startFilterThread();
				tableEditor.getEditor().dispose();
				table.refresh();
			}
		});

		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				e.doit = false;
				if (e.character == SWT.ESC) {
					stopFilterThread();
					table.refresh();
				} else if (e.character == SWT.DEL) {
					clearFilters();
				}
			}
		});
	}

	/**
	 * Clear all currently active filters.
	 */
	private void clearFilters() {
		if (table.getData(Key.FILTER_OBJ) == null) {
			return;
		}
		stopFilterThread();
		table.clearAll();
		for (final TableColumn column : table.getColumns()) {
			column.setData(Key.FILTER_TXT, null);
		}
		table.setData(Key.FILTER_OBJ, null);
		if (currentShownTrace != null) {
			cache.index();
			table.setItemCount(cache.getActiveRowCount() + 1); // +1 for header row
		} else {
			table.setItemCount(1); // +1 for header row
		}
		filterMatchCount = 0;
		filterCheckCount = 0;
		table.setSelection(0);
		statusText.setText(getStatus(cache.getActiveRowCount(), cache.getActiveRowCount()));
	}

	private void setHeaderRowItemData(final TableItem item) {
		item.setForeground(grayColor);
		for (int i = 0; i < table.getColumns().length; i++) {
			final TableColumn column = table.getColumns()[i];
			final String filter = (String) column.getData(Key.FILTER_TXT);
			if (filter == null) {
				item.setText(i, FILTER_HINT);
				item.setForeground(i, grayColor);
				item.setFont(i, table.getFont());
			} else {
				item.setText(i, filter);
				item.setForeground(i, blackColor);
				item.setFont(i, boldFont);
			}
		}
	}

	/**
	 * Create the SWT resources.
	 */
	private void createResources() {
		grayColor = resourceManager.createColor(ColorUtil.blend(table.getBackground().getRGB(),
				table.getForeground().getRGB()));
		blackColor = table.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		boldFont = resourceManager.createFont(FontDescriptor.createFrom(table.getFont()).setStyle(
				SWT.BOLD));
	}

	class DrawListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent e) {
			showWindow(currentShownTrace, timeBar.getStartTimestamp(), timeBar.getEndTimestamp());
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
				for (TableColumn column : table.getColumns()) {
					EventTableColumn col = (EventTableColumn) column.getData(Key.COLUMN_OBJ);
					column.pack();
					if (column.getWidth() < col.getWidth())
						column.setWidth(col.getWidth());
				}
			}
		};
		colAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID,
				"icons/adjust_h.png"));
		colAction.setToolTipText("Adjust column size to content");
		return colAction;
	}

	@Override
	public void setFocus() {
		if (table != null)
			table.setFocus();
		super.setFocus();
	}

	@Override
	public void dispose() {
		stopFilterThread();
		// TODO stop other threads
		if (table != null)
			table.dispose();
		if (timeBar != null)
			timeBar.dispose();
		timeBar = null;
		super.dispose();
	}

	@Override
	public String getId() {
		return ID;
	}

	/**
	 * Show the trace.
	 * 
	 * @param data
	 *            if null, all the trace must be loaded. Otherwise data is a
	 *            TraceIntervalDescriptor.
	 */
	@Override
	public void showTrace(final Trace trace, final Object data) {
		if (data == null) {
			showWindow(trace, trace.getMinTimestamp(), trace.getMaxTimestamp());
		} else {
			TraceIntervalDescriptor des = (TraceIntervalDescriptor) data;
			showWindow(des.getTrace(), des.getStartTimestamp(), des.getEndTimestamp());
		}
	}

	@Override
	public void partHandle(String topic, Object data) {
		logger.debug("nothing to do here");
	}

	/*
	 * Filtering
	 */

	/**
	 * Wrapper Thread object for the filtering thread.
	 */
	private class FilterThread extends Thread {

		private volatile boolean stop = false;
		private EventTableRowFilter filter;
		private boolean refreshBusy = false;
		private boolean refreshPending = false;
		private final Object syncObj = new Object();

		/**
		 * Constructor.
		 */
		public FilterThread(EventTableRowFilter filter) {
			super("Filter Thread"); //$NON-NLS-1$
			this.filter = filter;
		}

		@Override
		public void run() {
			if (currentShownTrace == null) {
				return;
			}
			cache.clearIndex();
			Iterator<EventTableRow> it = cache.activeRowIterator();
			while (it.hasNext()) {
				if (stop) {
					break;
				}
				EventTableRow r = it.next();
				if (filter.matches(r)) {
					filterMatchCount++;
					cache.put(r);
					refreshTable();
				} else if ((filterCheckCount % 100) == 0) {
					refreshTable();
				}
				filterCheckCount++;
			}
			refreshTable();
			synchronized (filterSyncObj) {
				filterThread = null;
			}
		}

		/**
		 * Refresh the filter.
		 */
		public void refreshTable() {
			synchronized (syncObj) {
				if (refreshBusy) {
					refreshPending = true;
					return;
				}
				refreshBusy = true;
			}
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					if (stop) {
						return;
					}
					if (table.isDisposed()) {
						return;
					}
					table.setItemCount(filterMatchCount + 1); // +1 for header row
					table.refresh();
					statusText.setText(getStatus(filterCheckCount, filterMatchCount));
					synchronized (syncObj) {
						refreshBusy = false;
						if (refreshPending) {
							refreshPending = false;
							refreshTable();
						}
					}
				}
			});
		}

		/**
		 * Cancel this filtering thread.
		 */
		public void cancel() {
			stop = true;
		}
	}

	/**
	 * Start the filtering thread.
	 */
	protected void startFilterThread() {
		synchronized (filterSyncObj) {
			if (filterThread != null) {
				filterThread.cancel();
				filterThread = null;
			}
			filterThread = new FilterThread(rowFilter);
			filterThread.start();
		}
	}

	/**
	 * Stop the filtering thread.
	 */
	protected void stopFilterThread() {
		synchronized (filterSyncObj) {
			if (filterThread != null) {
				filterThread.cancel();
				filterThread = null;
			}
		}
	}

	// Utilities

	private void closeView() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				FramesocPartManager.getInstance().disposeFramesocPart(EventTableView.this);
				EventTableView.this.hideView();
			}
		});
	}

}
