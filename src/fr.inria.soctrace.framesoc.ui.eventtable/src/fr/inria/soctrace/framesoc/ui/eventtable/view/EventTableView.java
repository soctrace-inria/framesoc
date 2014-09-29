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

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
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
import org.eclipse.swt.widgets.Event;
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

import fr.inria.linuxtools.tmf.core.filter.model.TmfFilterMatchesNode;
import fr.inria.linuxtools.tmf.ui.viewers.events.TmfEventsTable.Key;
import fr.inria.linuxtools.tmf.ui.widgets.virtualtable.ColumnData;
import fr.inria.linuxtools.tmf.ui.widgets.virtualtable.TmfVirtualTable;
import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.eventtable.Activator;
import fr.inria.soctrace.framesoc.ui.eventtable.loader.EventTableLoader;
import fr.inria.soctrace.framesoc.ui.eventtable.model.EventTableColumn;
import fr.inria.soctrace.framesoc.ui.eventtable.model.EventTableRow;
import fr.inria.soctrace.framesoc.ui.model.ITableColumn;
import fr.inria.soctrace.framesoc.ui.model.ITableRow;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPart;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartManager;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.framesoc.ui.utils.RowFilter;
import fr.inria.soctrace.framesoc.ui.utils.TimeBar;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

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
	 * Data loader
	 */
	private EventTableLoader eventsLoader;

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

	// SWT resources
	private LocalResourceManager fResourceManager = new LocalResourceManager(
			JFaceResources.getResources());
	private Color fGrayColor;
	private Color fBlackColor;
	private Font fBoldFont;

	// // Uncomment this to use the window builder
	// @Override
	// public void createPartControl(Composite parent) {
	// createFramesocPartControl(parent);
	// }

	// ************************************************************
	// Dummy stuff
	private int n = 1;

	private void dummyFillCache() {
		n *= 2;
		int N = n;
		System.out.println("Loading");
		for (int i = 0; i < N; i++) {
			EventTableRow r = new EventTableRow();
			r.setTimestamp(i);
			cache.put(r);
		}
		cache.setRequestedInterval(new TimeInterval(0, N - 1));
		table.setItemCount(N + 1); // +1 for the header
	}

	private IAction fillCacheAction() {
		Action showGantt = new Action("Fill cache") {
			@Override
			public void run() {
				dummyFillCache();
			}
		};
		return showGantt;
	}

	// ************************************************************

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

		// Handle the table item selection
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				if (e.item == null) {
					return;
				}
				System.out.println("line select");
			}
		});

		// Handle the table item requests
		table.addListener(SWT.SetData, new Listener() {

			@Override
			public void handleEvent(final Event event) {

				final TableItem item = (TableItem) event.item;
				System.out.println(event.index);
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

		table.setItemCount(1); // the header

		// ----------
		// TOOLBAR
		// ----------

		getViewSite().getActionBars().getToolBarManager().add(fillCacheAction());
		if (FramesocPartManager.getInstance().isFramesocPartExisting(
				FramesocViews.GANTT_CHART_VIEW_ID))
			getViewSite().getActionBars().getToolBarManager().add(createGanttAction());
		// XXX
		// enableActions(false);

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

	// TODO maybe optimize
	public String[] getItemStrings(EventTableRow row) {
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

	protected void setColumnHeaders() {
		// Set the columns
		ColumnData columnData[] = new ColumnData[EventTableColumn.values().length];
		int i = 0;
		for (EventTableColumn col : EventTableColumn.values()) {
			columnData[i++] = new ColumnData(col.getHeader(), col.getWidth(), SWT.LEFT);
		}
		table.setColumnHeaders(columnData);
	}

	/**
	 * Create an editor for the header.
	 */
	protected void createHeaderEditor() {
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
				if (event.button != 1) {
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
			 * returns true is value was changed
			 */
			private boolean updateHeader(final String text) {
				String objKey = Key.FILTER_OBJ;
				String txtKey = Key.FILTER_TXT;

				if (text.trim().length() > 0) {
					try {
						final String regex = TmfFilterMatchesNode.regexFix(text);
						Pattern.compile(regex);
						if (regex.equals(column.getData(txtKey))) {
							tableEditor.getEditor().dispose();
							return false;
						}
						final TmfFilterMatchesNode filter = new TmfFilterMatchesNode(null);
						String fieldId = (String) column.getData(Key.FIELD_ID);
						if (fieldId == null) {
							fieldId = column.getText();
						}
						filter.setField(fieldId);
						filter.setRegex(regex);
						column.setData(objKey, filter);
						column.setData(txtKey, regex);
					} catch (final PatternSyntaxException ex) {
						tableEditor.getEditor().dispose();
						MessageDialog.openError(PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getShell(), ex.getDescription(),
								ex.getMessage());
						return false;
					}
				} else {
					if (column.getData(txtKey) == null) {
						tableEditor.getEditor().dispose();
						return false;
					}
					column.setData(objKey, null);
					column.setData(txtKey, null);
				}
				return true;
			}

			private void applyHeader() {
				System.out.println("apply");
				tableEditor.getEditor().dispose();
				table.refresh();
			}
		});

		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				e.doit = false;
				if (e.character == SWT.ESC) {
					System.out.println("esc");
					// stopFilterThread();
					table.refresh();
				} else if (e.character == SWT.DEL) {
					// clearFilters();
					System.out.println("del");
				}
			}
		});
	}

	protected void setHeaderRowItemData(final TableItem item) {
		item.setForeground(fGrayColor);
		for (int i = 0; i < table.getColumns().length; i++) {
			final TableColumn column = table.getColumns()[i];
			final String filter = (String) column.getData(Key.FILTER_TXT);
			if (filter == null) {
				item.setText(i, FILTER_HINT);
				item.setForeground(i, fGrayColor);
				item.setFont(i, table.getFont());
			} else {
				item.setText(i, filter);
				item.setForeground(i, fBlackColor);
				item.setFont(i, fBoldFont);
			}
		}
	}

	/**
	 * Create the SWT resources.
	 */
	protected void createResources() {
		fGrayColor = fResourceManager.createColor(ColorUtil.blend(table.getBackground().getRGB(),
				table.getForeground().getRGB()));
		fBlackColor = table.getDisplay().getSystemColor(SWT.COLOR_BLACK);
		fBoldFont = fResourceManager.createFont(FontDescriptor.createFrom(table.getFont())
				.setStyle(SWT.BOLD));
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

	// private IAction createColumnAction() {
	// IAction colAction = new Action("Adjust Columns", Action.AS_PUSH_BUTTON) {
	// @Override
	// public void run() {
	// // refreshColumnSize();
	// }
	// };
	// colAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID,
	// "icons/adjust_h.png"));
	// colAction.setToolTipText("Adjust column size to content");
	// return colAction;
	// }

	// private void refreshColumnSize() {
	// Iterator<Entry<EventTableColumn, TableColumn>> it = eventColumns.entrySet().iterator();
	// while (it.hasNext()) {
	// Entry<EventTableColumn, TableColumn> entry = it.next();
	// EventTableColumn col = entry.getKey();
	// TableColumn tc = entry.getValue();
	// tc.pack();
	// if (tc.getWidth() < col.getWidth())
	// tc.setWidth(col.getWidth());
	// filterColumns.get(col).setWidth(tc.getWidth());
	// }
	// }

	@Override
	public void setFocus() {
		if (table != null)
			table.setFocus();
		super.setFocus();
	}

	@Override
	public void dispose() {
		// TODO kill jobs
		if (table != null)
			table.dispose();
		if (eventsLoader != null)
			eventsLoader.dispose();
		eventsLoader = null;
		// if (eventColumns != null)
		// eventColumns.clear();
		// eventColumns = null;
		// if (filterColumns != null)
		// filterColumns.clear();
		// filterColumns = null;
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

		// Job job = new Job("Loading Events table...") {
		// @Override
		// protected IStatus run(IProgressMonitor monitor) {
		// boolean closeIfCancelled = (currentShownTrace == null);
		// try {
		//
		// monitor.beginTask("Loading Events table", IProgressMonitor.UNKNOWN);
		// // update trace selection
		// if (trace == null) {
		// handleCancel(closeIfCancelled);
		// return Status.CANCEL_STATUS;
		// }
		//
		// currentShownTrace = trace;
		//
		// // activate the view after setting the current shown trace
		// activateView(); // TODO check if needed
		//
		// if (monitor.isCanceled()) {
		// handleCancel(closeIfCancelled);
		// return Status.CANCEL_STATUS;
		// }
		//
		// // Manage start/end timestamp
		// IntervalDesc intervalToLoad = new IntervalDesc(start, end);
		//
		// // load events from DB
		// final LoadDescriptor des = eventsLoader.loadTimeWindow(trace,
		// intervalToLoad.t1, intervalToLoad.t2, limit, monitor);
		// logger.debug(des.getMessage());
		// if (monitor.isCanceled()) {
		// handleCancel(closeIfCancelled);
		// return Status.CANCEL_STATUS;
		// }
		//
		// // refresh table and page selector UI
		// Display.getDefault().syncExec(new Runnable() {
		// @Override
		// public void run() {
		// setContentDescription("Trace: " + currentShownTrace.getAlias());
		// clearFilters();
		// btnSynch.setEnabled(true);
		// btnDraw.setEnabled(true);
		// timeBar.setEnabled(true);
		// timeBar.setMinTimestamp(currentShownTrace.getMinTimestamp());
		// timeBar.setMaxTimestamp(currentShownTrace.getMaxTimestamp());
		// startTimestamp = Math.max(currentShownTrace.getMinTimestamp(),
		// des.getActualStartTimestamp());
		// endTimestamp = Math.min(currentShownTrace.getMaxTimestamp(),
		// des.getActualEndTimestamp());
		// timeBar.setSelection(startTimestamp, endTimestamp);
		// logger.debug(timeBar.toString());
		// setInput();
		// enableActions(true);
		// }
		// });
		// monitor.done();
		// return Status.OK_STATUS;
		// } catch (Exception e) {
		// e.printStackTrace();
		// handleCancel(closeIfCancelled);
		// return Status.CANCEL_STATUS;
		// }
		// }
		// };
		// job.setUser(true);
		// job.schedule();

	}

	// Utilities

	private void handleCancel(final boolean closeIfCancelled) {
		// Display.getDefault().syncExec(new Runnable() {
		// @Override
		// public void run() {
		// timeBar.setSelection(startTimestamp, endTimestamp);
		// if (closeIfCancelled) {
		// FramesocPartManager.getInstance().disposeFramesocPart(EventTableView.this);
		// EventTableView.this.hideView();
		// }
		// }
		// });
	}

	@Override
	public String getId() {
		return ID;
	}

	/**
	 * @param data
	 *            if null, the first page must be loaded. Otherwise it is a HistogramBinDisplay.
	 *            This may change in future, with a more generic data structure, to be used when the
	 *            page will be no longer used in the table.
	 */
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
