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
package fr.inria.soctrace.framesoc.ui.views;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopicList;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusVariable;
import fr.inria.soctrace.framesoc.core.bus.IFramesocBusListener;
import fr.inria.soctrace.framesoc.ui.Activator;
import fr.inria.soctrace.framesoc.ui.dialogs.NewParamDialog;
import fr.inria.soctrace.framesoc.ui.loaders.TraceDetailsLoader;
import fr.inria.soctrace.framesoc.ui.loaders.TraceLoader.TraceChange;
import fr.inria.soctrace.framesoc.ui.model.DetailsTableRow;
import fr.inria.soctrace.framesoc.ui.model.TraceNode;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.framesoc.ui.utils.TraceSelection;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TraceTableModel;

/**
 * View displaying trace metadata.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceDetailsView extends ViewPart implements IFramesocBusListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = FramesocViews.TRACE_DETAILS_VIEW_ID;

	/**
	 * Table Columns
	 */
	private final static String COL_PROPERTY = "Property";
	private final static String COL_VALUE = "Value";

	/**
	 * View header for multi trace selection
	 */
	private final static String MULTI_TRACE_SELECTION = "Multi-trace selection";

	/**
	 * Followed topics
	 */
	private FramesocBusTopicList topics;

	/**
	 * Viewer
	 */
	private TableViewer viewer;

	/**
	 * Data loader
	 */
	private TraceDetailsLoader traceDetailsLoader = new TraceDetailsLoader();

	/**
	 * Current traces
	 */
	private List<Trace> currentTraces = null;

	/**
	 * Editing support
	 */
	private ValueEditingSupport editingSupport;

	/**
	 * Delete param action
	 */
	private Action delParamsAction;

	/**
	 * The listener we register with the selection service
	 */
	private ISelectionListener listener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			// we ignore our own selections
			if (sourcepart != TraceDetailsView.this) {
				storeSelection(selection);
				showSelection();
			}
		}
	};

	private Action addParamAction;

	/**
	 * Constructor. Register to Framesoc Notification Bus.
	 */
	public TraceDetailsView() {
		topics = new FramesocBusTopicList(this);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_FOCUSED_TRACE);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_SYSTEM_INITIALIZED);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_TRACES_SYNCHRONIZED);
		topics.registerAll();
	}

	@Override
	public void createPartControl(Composite parent) {

		// create viewer and editing support
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.FULL_SELECTION | SWT.BORDER);
		editingSupport = new ValueEditingSupport(viewer);
		createColumns();
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setInput(traceDetailsLoader.getProperties());

		// viewer selection listener
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				Object[] rows = selection.toArray();
				boolean enable = (rows.length > 0);
				for (Object o : rows) {
					DetailsTableRow row = (DetailsTableRow) o;
					if (!row.isCustomParam()) {
						enable = false;
						break;
					}
				}
				delParamsAction.setEnabled(enable);
			}
		});

		// build toolbar
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(editingSupport.createResetAction());
		toolBar.add(editingSupport.createSaveAction());
		toolBar.add(new Separator());
		toolBar.add(createAddParamAction());
		toolBar.add(createDelParamsAction());

		// register the selection listener
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(listener);

		// provide our selection for other viewers
		getSite().setSelectionProvider(viewer);

	}

	private boolean editingClean() {
		// clean editing before
		if (currentTraces.size() > 0 && editingSupport.isModified()) {
			MessageDialog
					.openWarning(
							getSite().getShell(),
							"Warning",
							"There are unsaved changes in trace details. Save or rollback them before adding or removing properties.");
			return false;
		}
		return true;
	}

	private IAction createDelParamsAction() {
		delParamsAction = new Action("Delete property") {
			@Override
			public void run() {

				if (!editingClean())
					return;

				// get the selection
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				Object[] rows = selection.toArray();
				List<String> params = new ArrayList<>(rows.length);

				StringBuilder sb = new StringBuilder();
				sb.append("Remove the following properties from the selected traces?\n\n");
				sb.append("Properties to remove:\n");
				for (Object row : rows) {
					String name = (String) ((DetailsTableRow) row).getName();
					params.add(name);
					sb.append("* ");
					sb.append(name);
					sb.append("\n");
				}

				if (!MessageDialog.openQuestion(getSite().getShell(), "Remove properties",
						sb.toString())) {
					return;
				}

				// delete corresponding params in all selected traces
				List<Trace> traces = getSelectedTraces();
				try {
					FramesocManager.getInstance().deleteParams(traces, params);
					showSelection();
				} catch (SoCTraceException e) {
					MessageDialog.openError(getSite().getShell(), "Error",
							"An error occurred removing the properties.\n" + e.getMessage());
					e.printStackTrace();
				}
			}
		};
		delParamsAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(
				Activator.PLUGIN_ID, "icons/minus.png"));
		delParamsAction.setEnabled(false);
		return delParamsAction;

	}

	private IAction createAddParamAction() {
		addParamAction = new Action("Add property") {
			@Override
			public void run() {

				if (!editingClean())
					return;

				NewParamDialog dlg = new NewParamDialog(getSite().getShell());
				if (dlg.open() == Dialog.OK) {
					// add the new param in all selected traces
					List<Trace> traces = getSelectedTraces();
					try {
						FramesocManager.getInstance().saveParam(traces, dlg.getName(),
								dlg.getType(), dlg.getValue());
						showSelection();
					} catch (SoCTraceException e) {
						MessageDialog.openError(getSite().getShell(), "Error",
								"An error occurred saving the properties.\n" + e.getMessage());
						e.printStackTrace();
					}
				}
			}
		};
		addParamAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(
				Activator.PLUGIN_ID, "icons/plus.png"));
		addParamAction.setEnabled(false);
		return addParamAction;
	}

	private List<Trace> getSelectedTraces() {
		IStructuredSelection selection = (IStructuredSelection) FramesocBus.getInstance()
				.getVariable(FramesocBusVariable.TRACE_VIEW_CURRENT_TRACE_SELECTION);
		Object[] traceNodes = selection.toArray();
		List<Trace> traces = new ArrayList<>(traceNodes.length);
		for (Object node : traceNodes) {
			traces.add(((TraceNode) node).getTrace());
		}
		return traces;
	}

	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	@Override
	public void dispose() {
		// unregister listeners
		topics.unregisterAll();
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(listener);
		super.dispose();
	}

	private void showSelection(Trace selection) {
		currentTraces = new LinkedList<Trace>();
		currentTraces.add(selection);
		showSelection();
	}

	private void storeSelection(ISelection selection) {
		if (!TraceSelection.isSelectionValid(selection))
			return;
		currentTraces = TraceSelection.getTracesFromSelection(selection);
	}

	private void showSelection() {

		if (currentTraces == null)
			return;

		// manage old trace and clean editing support
		if (currentTraces.size() > 0 && editingSupport.isModified()) {
			if (MessageDialog.openQuestion(getSite().getShell(), "Save trace details",
					"There are unsaved changes in trace details, do you want to save them?"))
				editingSupport.save();
			editingSupport.clean();
		}

		// display current selection
		if (currentTraces.size() == 1) {
			setContentDescription("Trace: " + currentTraces.iterator().next().getAlias());
			traceDetailsLoader.load(currentTraces.iterator().next());
		} else {
			setContentDescription("Multi-trace selection");
			traceDetailsLoader.load(currentTraces);
		}
		List<DetailsTableRow> prop = traceDetailsLoader.getProperties();
		if (prop != null && prop.size() > 0) {
			viewer.setInput(prop);
			addParamAction.setEnabled(true);
		}

	}

	// Utilities

	private void createColumns() {
		TableViewerColumn col = createTableViewerColumn(COL_PROPERTY, 200, 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((DetailsTableRow) element).getName();
			}

			@Override
			public Image getImage(Object element) {
				if (((DetailsTableRow) element).isCustomParam())
					return PlatformUI.getWorkbench().getSharedImages()
							.getImage(ISharedImages.IMG_DEF_VIEW);
				else
					return PlatformUI.getWorkbench().getSharedImages()
							.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
			}
		});
		col = createTableViewerColumn(COL_VALUE, 100, 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((DetailsTableRow) element).getValue();
			}

			@Override
			public org.eclipse.swt.graphics.Color getForeground(Object element) {
				if (((DetailsTableRow) element).isReadOnly())
					return ColorConstants.gray;
				return ColorConstants.black;
			}
		});
		col.setEditingSupport(editingSupport);
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	@Override
	public void handle(FramesocBusTopic topic, Object data) {
		if (topic.equals(FramesocBusTopic.TOPIC_UI_FOCUSED_TRACE) && data != null) {
			showSelection((Trace) data);
		} else if (topic.equals(FramesocBusTopic.TOPIC_UI_SYSTEM_INITIALIZED)) {
			// clean the viewer content
			currentTraces = null;
			viewer.setInput(null);
			viewer.refresh();
		} else if (topic.equals(FramesocBusTopic.TOPIC_UI_TRACES_SYNCHRONIZED)) {
			if (currentTraces != null) {
				@SuppressWarnings("unchecked")
				Map<TraceChange, List<Trace>> traceChangeMap = ((Map<TraceChange, List<Trace>>) data);
				boolean reload = false;
				Iterator<Trace> it = currentTraces.iterator();
				while (it.hasNext()) {
					Trace trace = it.next();
					if (traceChangeMap.get(TraceChange.REMOVE).contains(trace)) {
						it.remove();
						reload = true;
					} else if (traceChangeMap.get(TraceChange.UPDATE).contains(trace)) {
						reload = true;
					}
				}
				// all selected traces have been deleted
				if (currentTraces.size() == 0) {
					viewer.setInput(null);
					addParamAction.setEnabled(false);
				}
				// some trace has been removed or updated, and there are still selected traces
				else if (reload) {
					traceDetailsLoader.load(currentTraces);
					viewer.setInput(traceDetailsLoader.getProperties());
				}
			}
		}
	}

	/**
	 * Editing support class
	 */
	private class ValueEditingSupport extends EditingSupport {

		private final ImageDescriptor imgSave;
		private final ImageDescriptor imgReset;
		private Action saveAction;
		private Action resetAction;
		private boolean modified;
		private boolean editable;
		private final TableViewer viewer;
		private final TextCellEditor textEditor;
		private final ComboBoxCellEditor timeUnitEditor;
		private final String[] UNITS;

		public ValueEditingSupport(TableViewer viewer) {
			super(viewer);
			this.editable = true;
			this.modified = false;
			this.viewer = viewer;
			this.textEditor = new TextCellEditor(viewer.getTable());
		
			// time unit
			TimeUnit[] units = TimeUnit.values();
			UNITS = new String[units.length];
			for (int i = 0; i < units.length; i++) {
				UNITS[i] = units[i].getLabel();
			}
			this.timeUnitEditor = new ComboBoxCellEditor(viewer.getTable(), UNITS, SWT.READ_ONLY);
			// images
			imgSave = ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID,
					"icons/save.png");
			imgReset = ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID,
					"icons/load.png");
		}

		@Override
		protected CellEditor getCellEditor(Object element) {
			if (isTimeUnit(element)) {
				return timeUnitEditor;
			} else {
				return textEditor;
			}
		}

		@Override
		protected boolean canEdit(Object element) {
			return editable && !((DetailsTableRow) element).isReadOnly();
		}

		@Override
		protected Object getValue(Object element) {
			String value = ((DetailsTableRow) element).getValue();
			if (isTimeUnit(element)) {
				return getIndex(value);
			} else {
				return value;
			}
		}

		@Override
		protected void setValue(Object element, Object value) {

			DetailsTableRow row = ((DetailsTableRow) element);
			if (String.valueOf(value).equals(row.getValue()))
				return;

			if (isTimeUnit(element)) {
				row.setValue(UNITS[(Integer) value]);
			} else {
				row.setValue(String.valueOf(value));
			}
			viewer.update(element, null);
			modified = true;
			saveAction.setEnabled(true);
			resetAction.setEnabled(true);
			if (currentTraces.size() == 1) {
				setContentDescription("*Trace: " + currentTraces.iterator().next().getAlias());
			} else {
				setContentDescription("*" + MULTI_TRACE_SELECTION);
			}
		}

		public boolean isModified() {
			return modified;
		}

		public void save() {
			try {
				printCurrentTraces();
				traceDetailsLoader.store(currentTraces);
			} catch (SoCTraceException e) {
				MessageDialog.openError(getSite().getShell(), "Exception", e.getMessage());
			}
			if (currentTraces.size() == 1) {
				traceDetailsLoader.load(currentTraces.iterator().next());
				setContentDescription("Trace: " + currentTraces.iterator().next().getAlias());
			} else {
				traceDetailsLoader.load(currentTraces);
				setContentDescription(MULTI_TRACE_SELECTION);
			}
			viewer.setInput(traceDetailsLoader.getProperties());
		}

		public void clean() {
			if (currentTraces.size() == 1) {
				setContentDescription("Trace: " + currentTraces.iterator().next().getAlias());
			} else {
				setContentDescription(MULTI_TRACE_SELECTION);
			}
			modified = false;
			editable = true;
			saveAction.setEnabled(false);
			resetAction.setEnabled(false);
		}

		// utilities

		private int getIndex(String s) {
			for (int i = 0; i < UNITS.length; i++) {
				if (s.equals(UNITS[i]))
					return i;
			}
			return -1;
		}

		private boolean isTimeUnit(Object element) {
			DetailsTableRow row = (DetailsTableRow) element;
			return row.getName().equals(TraceTableModel.TIMEUNIT.getDescription());
		}

		public Action createSaveAction() {
			saveAction = new Action("Save changes") {
				@Override
				public void run() {
					if (!modified)
						return; // just to be robust
					viewer.getTable().forceFocus(); // store the last edited field too
					save();
					clean();

				}
			};
			saveAction.setImageDescriptor(imgSave);
			saveAction.setEnabled(false);
			return saveAction;
		}

		public Action createResetAction() {
			resetAction = new Action("Reset changes") {
				@Override
				public void run() {
					if (!modified)
						return; // just to be robust
					clean();
					traceDetailsLoader.load(currentTraces);
					viewer.setInput(traceDetailsLoader.getProperties());
					viewer.getTable().forceFocus(); // avoid editing during saving
				}
			};
			resetAction.setImageDescriptor(imgReset);
			//resetAction.setEnabled(false);
			return resetAction;
		}

	}

	private void printCurrentTraces() {
		for (Trace t : currentTraces) {
			t.print(true);
		}
	}
}
