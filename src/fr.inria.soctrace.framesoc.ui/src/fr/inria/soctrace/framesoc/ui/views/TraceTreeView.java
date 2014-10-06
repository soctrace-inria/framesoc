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
package fr.inria.soctrace.framesoc.ui.views;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopicList;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusVariable;
import fr.inria.soctrace.framesoc.core.bus.IFramesocBusListener;
import fr.inria.soctrace.framesoc.ui.Activator;
import fr.inria.soctrace.framesoc.ui.handlers.HandlerUtils;
import fr.inria.soctrace.framesoc.ui.loaders.TraceLoader;
import fr.inria.soctrace.framesoc.ui.model.FolderNode;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.model.TraceNode;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartManager;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.framesoc.ui.providers.TreeContentProvider;
import fr.inria.soctrace.framesoc.ui.providers.TreeLabelProvider;
import fr.inria.soctrace.framesoc.ui.utils.TraceSelection;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * View displaying all the traces.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceTreeView extends ViewPart implements IFramesocBusListener {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = FramesocViews.TRACE_TREE_VIEW_ID;

	/**
	 * TODO get the command from the extension point ID of the commands we have to remove for
	 * multiple selection
	 */
	private static final String COMMAND_PREFIX = Activator.PLUGIN_ID + ".commands"; //$NON-NLS-1$
	private static final String COMMAND_DENSITY = COMMAND_PREFIX + ".showEventDensityHistogram"; //$NON-NLS-1$
	private static final String COMMAND_PIE = COMMAND_PREFIX + ".ShowStatisticsPieChart"; //$NON-NLS-1$
	private static final String COMMAND_TABLE = COMMAND_PREFIX + ".showEventTable"; //$NON-NLS-1$
	private static final String COMMAND_GANTT = COMMAND_PREFIX + ".ShowGantt"; //$NON-NLS-1$
	private static final String COMMAND_COPY_DB_NAME = COMMAND_PREFIX + ".CopyToClipboard"; //$NON-NLS-1$

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(TraceTreeView.class);

	private TreeViewer viewer;
	private TraceLoader tracesLoader;
	private FramesocBusTopicList topics;

	/**
	 * The listener we register with the selection service
	 */
	private ISelectionListener listener = new ISelectionListener() {
		public void selectionChanged(IWorkbenchPart sourcepart, ISelection selection) {
			if (sourcepart.equals(TraceTreeView.this) && selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (!TraceSelection.isSelectionValid(ss))
					return;
				FramesocBus.getInstance().setVariable(
						FramesocBusVariable.TRACE_VIEW_SELECTED_TRACE,
						TraceSelection.getTraceFromSelection(ss));
				FramesocBus.getInstance().setVariable(
						FramesocBusVariable.TRACE_VIEW_CURRENT_TRACE_SELECTION, ss);
				logger.debug(
						"Selected trace: {}",
						FramesocBus.getInstance()
								.getVariable(FramesocBusVariable.TRACE_VIEW_SELECTED_TRACE)
								.toString());
				logger.debug(
						"Current trace selection: {}",
						FramesocBus
								.getInstance()
								.getVariable(FramesocBusVariable.TRACE_VIEW_CURRENT_TRACE_SELECTION)
								.toString());
				logger.debug("# of selected: {}", ss.size());
			}
		}
	};

	/**
	 * Constructor. Register to Framesoc Notification Bus.
	 */
	public TraceTreeView() {
		topics = new FramesocBusTopicList(this);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_FOCUSED_TRACE);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_SYNCH_TRACES_NEEDED);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_SYSTEM_INITIALIZED);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_REFRESH_TRACES_NEEDED);
		topics.registerAll();
	}

	@Override
	public void createPartControl(Composite parent) {

		tracesLoader = new TraceLoader();
		viewer = new TreeViewer(parent, SWT.MULTI);
		viewer.setContentProvider(new TreeContentProvider());
		viewer.setLabelProvider(new TreeLabelProvider());
		viewer.setInput(tracesLoader.loadFromDB());

		// default comparator
		viewer.setComparator(new ViewerComparator());

		// double click
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection();
				if (TraceSelection.isFolderNode(thisSelection)) {
					FolderNode folder = (FolderNode) thisSelection.getFirstElement();
					boolean expanded = viewer.getExpandedState(folder);
					viewer.setExpandedState(folder, !expanded);
					return;
				}
				if (!TraceSelection.isSelectionValid(thisSelection)
						|| !FramesocPartManager.getInstance().isFramesocPartExisting(
								FramesocViews.HISTOGRAM_VIEW_ID)) {
					return;
				}
				TraceNode selectedNode = (TraceNode) thisSelection.getFirstElement();
				TraceIntervalDescriptor des = new TraceIntervalDescriptor();
				des.setTrace(selectedNode.getTrace());
				des.setStartTimestamp(selectedNode.getTrace().getMinTimestamp());
				des.setEndTimestamp(selectedNode.getTrace().getMaxTimestamp());
				logger.debug(des.toString());
				FramesocBus.getInstance().send(FramesocBusTopic.TOPIC_UI_HISTOGRAM_DISPLAY, des);
			}
		});

		// key listener
		viewer.getControl().addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(final KeyEvent event) {
				if (event.keyCode == SWT.DEL) {
					HandlerUtils.launchCommand(getSite(),
							"fr.inria.soctrace.framesoc.ui.commands.deleteDB");
				}
			}
		});

		// Context menu
		MenuManager menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(viewer.getTree());
		viewer.getTree().setMenu(menu);
		getSite().registerContextMenu(menuManager, viewer);

		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager) {
				// show the menu only on leaf nodes
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (!TraceSelection.isSelectionValid(selection)) {
					manager.removeAll();
					return;
				}
				if (selection.size() > 1) {
					manager.remove(COMMAND_DENSITY);
					manager.remove(COMMAND_PIE);
					manager.remove(COMMAND_TABLE);
					manager.remove(COMMAND_GANTT);
					manager.remove(COMMAND_COPY_DB_NAME);
				}
			}

		});

		// build the toolbar
		buildToolbar(parent);

		// register the selection listener
		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(listener);

		// provide our selection for other viewers
		getSite().setSelectionProvider(viewer);

		// expand all
		viewer.expandAll();
		viewer.refresh();

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

	/**
	 * Load the traces from the System DB.
	 */
	private void loadTracesFromDB() {
		viewer.setInput(tracesLoader.loadFromDB());
		viewer.refresh();
	}

	/**
	 * Synchronize Traces view with the System DB.
	 */
	private void synchTracesWithDB() {
		FolderNode root = tracesLoader.getRoot();
		Object[] path = viewer.getExpandedElements();
		try {
			root = tracesLoader.synchWithDB();
		} catch (SoCTraceException e) {
			MessageDialog.openError(getSite().getShell(), "Exception", e.getMessage());
		}
		viewer.setInput(root);
		viewer.setExpandedElements(path);
	}

	/**
	 * Synchronize viewer input with the data model.
	 */
	private void synchTracesWithModel() {
		Object[] path = viewer.getExpandedElements();
		viewer.setInput(tracesLoader.synchWithModel());
		viewer.setExpandedElements(path);
	}

	// utilities

	private void buildToolbar(final Composite parent) {
		// create refresh the action
		Action refreshAction = new Action() {
			public void run() {
				synchTracesWithDB();
			}
		};
		refreshAction.setText("Refresh traces");
		refreshAction.setToolTipText("Refresh traces");
		refreshAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));

		// create the expand all action
		Action expandAction = new Action() {
			public void run() {
				viewer.expandAll();
			}
		};
		expandAction.setText("Expand all");
		expandAction.setToolTipText("Expand all");
		expandAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(
				Activator.PLUGIN_ID, "icons/expandall.gif"));

		// create the collapse all action
		Action collapseAction = new Action() {
			public void run() {
				viewer.collapseAll();
			}
		};
		collapseAction.setText("Collapse all");
		collapseAction.setToolTipText("Collapse all");
		collapseAction.setImageDescriptor(ResourceManager.getPluginImageDescriptor(
				Activator.PLUGIN_ID, "icons/collapseall.gif"));

		// add to toolbar
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		toolBar.add(expandAction);
		toolBar.add(collapseAction);
		toolBar.add(refreshAction);
	}

	@Override
	public void handle(String topic, Object data) {
		if (topic.equals(FramesocBusTopic.TOPIC_UI_FOCUSED_TRACE) && data != null) {
			TraceNode node = tracesLoader.getTraceNode((Trace) data);
			if (node == null)
				return;
			IStructuredSelection sel = new StructuredSelection(node);
			viewer.setSelection(sel, true);
			FramesocBus.getInstance().setVariable(FramesocBusVariable.TRACE_VIEW_SELECTED_TRACE,
					(Trace) data);
			FramesocBus.getInstance().setVariable(
					FramesocBusVariable.TRACE_VIEW_CURRENT_TRACE_SELECTION, sel);
			logger.debug(
					"Selected trace: {}",
					FramesocBus.getInstance()
							.getVariable(FramesocBusVariable.TRACE_VIEW_SELECTED_TRACE).toString());
			logger.debug(
					"Current trace selection: {}",
					FramesocBus.getInstance()
							.getVariable(FramesocBusVariable.TRACE_VIEW_CURRENT_TRACE_SELECTION)
							.toString());
			logger.debug("# of selected: {}", sel.size());
		} else if (topic.equals(FramesocBusTopic.TOPIC_UI_SYNCH_TRACES_NEEDED) && data != null) {
			if ((Boolean) data)
				synchTracesWithDB();
		} else if (topic.equals(FramesocBusTopic.TOPIC_UI_SYSTEM_INITIALIZED)) {
			loadTracesFromDB();
		} else if (topic.equals(FramesocBusTopic.TOPIC_UI_REFRESH_TRACES_NEEDED)) {
			synchTracesWithModel();
		}
	}

}
