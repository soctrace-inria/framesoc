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
package fr.inria.soctrace.framesoc.ui.perspective;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopicList;
import fr.inria.soctrace.framesoc.core.bus.IFramesocBusListener;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartContributionManager.PartContributionDescriptor;
import fr.inria.soctrace.framesoc.ui.utils.TraceSelection;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;

/**
 * Singleton to access Framesoc parts management functionalities. It is created at UI plugin startup
 * (@see FramesocUiStartup).
 * 
 * <p>
 * This manager provides the following functionalities:
 * <ul>
 * <li>create of Framesoc analysis views, ensuring correct secondary ID management
 * <li>clean the Framesoc perspective
 * <li>handle inter-view communication topics
 * <li>enable disposal of Framesoc analysis views
 * <li>enable checking of Framesoc analysis views existence
 * </ul>
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * 
 */
public final class FramesocPartManager implements IFramesocBusListener {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(FramesocPartManager.class);

	/**
	 * Followed topics
	 */
	protected FramesocBusTopicList topics = null;

	/**
	 * The listener we register with the selection service.
	 * 
	 * I have to set highlighting directly here and avoid sending the focused trace event on the
	 * Framesoc bus, in order to avoid recursion: in fact, the trace tree view listens to such event
	 * too, so it would change its selection, and trigger this listener again....
	 */
	private ISelectionListener listener = new ISelectionListener() {
		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			logger.debug("Updating titles after selectionChanged in Trace view");
			updateTitlesHighlight(TraceSelection.getTraceFromSelection(selection));
		}
	};

	/**
	 * View Descriptor
	 */
	private class ViewDesc {

		public final int maxInstances;
		public int instances;
		public List<FramesocPart> openParts;

		public ViewDesc(int maxInstances) {
			this.maxInstances = maxInstances;
			this.instances = 0;
			this.openParts = new LinkedList<>();
		}

		@Override
		public String toString() {
			return "ViewDesc [maxInstances=" + maxInstances + ", instances=" + instances
					+ ", openParts=" + openParts + "]";
		}
	}

	/**
	 * Single instance of the manager
	 */
	private static FramesocPartManager instance = null;

	/**
	 * Map of view descriptors for Framesoc view types
	 */
	private Map<String, ViewDesc> viewDescMap;

	/**
	 * Instance getter
	 * 
	 * @return the manager instance
	 */
	public static FramesocPartManager getInstance() {
		if (instance == null)
			instance = new FramesocPartManager();
		return instance;
	}

	/*
	 * Public methods
	 */

	/**
	 * Get an instance for the given Framesoc analysis view ID.
	 * 
	 * <p>
	 * If an empty view corresponding to this ID is present, it is used. Otherwise a new one is
	 * created and activated, if the maximum number of instance has not been reached yet.
	 * 
	 * @param viewID
	 *            view ID corresponding to an existing Framesoc analysis view
	 * @param trace
	 *            the trace we want to load, or null if we need an empty view
	 * 
	 * @return a view, or null if the passed ID does not correspond to a Framesoc view, if the
	 *         maximum number of instances for the view has been reached, if PartInitException is
	 *         launched.
	 */
	public OpenFramesocPartStatus getPartInstance(String viewID, Trace trace) {

		OpenFramesocPartStatus status = new OpenFramesocPartStatus();
		status.message = "View loaded.";

		// see if the trace is already loaded in a view
		if (trace != null) {
			logger.debug("see if the trace is already loaded");
			FramesocPart part = searchAlreadyLoaded(viewID, trace);
			if (part != null) {
				part.activateView();
				status.part = part;
				return status;
			}
		}

		// try to reuse an empty view, if any
		logger.debug("try to reuse an empty view");
		FramesocPart part = searchEmpty(viewID);
		if (part != null) {
			part.activateView();
			status.part = part;
			return status;
		}

		// create a new view, if possible
		logger.debug("create a new view if possible");
		ViewDesc desc = viewDescMap.get(viewID);

		if (desc == null) {
			status.part = null;
			status.message = "View '" + viewID + "' is not a Framesoc view.";
			logger.error(status.message);
			return status;
		}

		if (desc.instances >= desc.maxInstances) {
			status.part = null;
			status.message = "Maximum number of instances reached for view '" + viewID + "'.";
			logger.error(status.message);
			return status;
		}

		FramesocPart v = createNewView(viewID);
		if (v == null) {
			status.part = null;
			status.message = "Unable to create view '" + viewID + "'.";
			logger.error(status.message);
			return status;
		}

		desc.instances++;
		desc.openParts.add(v);
		status.part = v;
		v.activateView();

		return status;
	}

	/**
	 * Close all the instances (except one) for the open Framesoc views.
	 */
	public void cleanFramesocParts() {

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {

				logger.debug("Before clean");
				printDescriptors();

				final List<IViewReference> framesocRefs = new LinkedList<IViewReference>();
				final IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();

				// clean desc
				for (ViewDesc desc : viewDescMap.values()) {
					desc.instances = 0;
					desc.openParts.clear();
				}

				logger.debug("After clean");
				printDescriptors();

				// for all workbench windows
				for (int w = 0; w < windows.length; w++) {
					final IWorkbenchPage[] pages = windows[w].getPages();

					// for all workbench pages
					for (int p = 0; p < pages.length; p++) {
						final IWorkbenchPage page = pages[p];
						final IViewReference[] viewRefs = page.getViewReferences();

						// for all view references
						for (int v = 0; v < viewRefs.length; v++) {
							final IViewReference viewRef = viewRefs[v];
							logger.debug("name: {}, sec id: {}", viewRef.getPartName(),
									viewRef.getSecondaryId());
							// count the FramesocPart instances
							ViewDesc desc = viewDescMap.get(viewRef.getId());
							if (desc != null) {
								logger.debug("found desc name: {}, sec id: {}",
										viewRef.getPartName(), viewRef.getSecondaryId());
								framesocRefs.add(viewRef);
								desc.instances++;
								desc.openParts.add((FramesocPart) viewRef.getPart(true));
							} else {
								logger.debug("not found desc name: {}, sec id: {}",
										viewRef.getPartName(), viewRef.getSecondaryId());
							}
						}
					}
				}

				logger.debug("before leave one instance");
				printDescriptors();

				logger.debug("leave only one instance");
				Iterator<Entry<String, ViewDesc>> it = viewDescMap.entrySet().iterator();
				while (it.hasNext()) {
					Entry<String, ViewDesc> e = it.next();
					logger.debug("View ID: {}", e.getKey());
					ViewDesc desc = e.getValue();
					Iterator<FramesocPart> pit = desc.openParts.iterator();
					while (pit.hasNext()) {
						FramesocPart part = pit.next();
						if (desc.instances > 1) {
							logger.debug("Hide view ID: {}", e.getKey());
							part.hideView();
							desc.instances--;
							pit.remove();
						}
					}
				}

				logger.debug("After leave one instance, reload");
				printDescriptors();
			}
		});
	}

	@Override
	public void handle(FramesocBusTopic topic, Object data) {
		if (topic.equals(FramesocBusTopic.TOPIC_UI_HISTOGRAM_DISPLAY_TIME_INTERVAL)) {
			logger.debug("Topic histogram interval");
			displayFramesocView(FramesocViews.HISTOGRAM_VIEW_ID, data);
		} else if (topic.equals(FramesocBusTopic.TOPIC_UI_TABLE_DISPLAY_TIME_INTERVAL)
				&& data != null) {
			logger.debug("Topic table interval");
			displayFramesocView(FramesocViews.EVENT_TABLE_VIEW_ID, data);
		} else if (topic.equals(FramesocBusTopic.TOPIC_UI_GANTT_DISPLAY_TIME_INTERVAL)
				&& data != null) {
			logger.debug("Topic gantt interval");
			displayFramesocView(FramesocViews.GANTT_CHART_VIEW_ID, data);
		} else if (topic.equals(FramesocBusTopic.TOPIC_UI_PIE_DISPLAY_TIME_INTERVAL)
				&& data != null) {
			logger.debug("Topic pie interval");
			displayFramesocView(FramesocViews.STATISTICS_PIE_CHART_VIEW_ID, data);
		}
	}

	/**
	 * Display a Framesoc view using the trace interval descriptor contained in the data.
	 * 
	 * @param viewId
	 *            view id
	 * @param data
	 *            data containing a trace interval descriptor
	 */
	private void displayFramesocView(String viewId, Object data) {
		TraceIntervalDescriptor des = (TraceIntervalDescriptor) data;
		OpenFramesocPartStatus status = getPartInstance(viewId, des.getTrace());
		if (status.part == null) {
			MessageDialog.openError(Display.getDefault().getActiveShell(),
					"Error", status.message);
			return;
		}
		status.part.showTrace(des.getTrace(), des);
		updateTitlesHighlight((Trace) des.getTrace());
	}

	/**
	 * Update the Framesoc parts view names, highlighting them if the selected trace is the one
	 * shown in the view, unhighlighting otherwise.
	 * 
	 * We use the asyncExec to avoid a non-deterministic buggy behavior: for the pie-chart view, the
	 * show view event does not highlight the name otherwise...
	 * 
	 * @param trace
	 *            selected trace
	 */
	public void updateTitlesHighlight(final Trace trace) {
		if (trace == null)
			return;
		Display.getCurrent().asyncExec(new Runnable() {
			@Override
			public void run() {
				logger.debug("------------------------------------------");
				for (ViewDesc desc : viewDescMap.values()) {
					for (FramesocPart fp : desc.openParts) {
						if (trace.equals(fp.getCurrentShownTrace())) {
							logger.debug("Highlight " + fp.getPartName());
							fp.higlightTitle(true);
						} else {
							logger.debug("Unhighlight " + fp.getPartName());
							fp.higlightTitle(false);
						}
					}
				}
				logger.debug("------------------------------------------");
			}
		});
	}

	/**
	 * Dispose the passed FramesocPart
	 * 
	 * @param framesocPart
	 *            the part to dispose
	 */
	public void disposeFramesocPart(FramesocPart framesocPart) {
		ViewDesc desc = viewDescMap.get(framesocPart.getId());
		if (desc != null) {
			desc.instances--;
			Iterator<FramesocPart> it = desc.openParts.iterator();
			while (it.hasNext()) {
				FramesocPart part = it.next();
				if (framesocPart.equals(part))
					it.remove();
			}
		}
	}

	/**
	 * Tell if a FramesocPart exists or not in the current runtime.
	 * 
	 * @param id
	 *            FramesocPart id
	 * @return true if the FramesocPart exists in the runtime, false otherwise
	 */
	public boolean isFramesocPartExisting(String id) {
		return viewDescMap.containsKey(id);
	}

	/*
	 * Private methods
	 */

	/**
	 * Private constructor. Prevents instantiation.
	 */
	private FramesocPartManager() {
		loadDescMap();
		topics = new FramesocBusTopicList(this);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_HISTOGRAM_DISPLAY_TIME_INTERVAL);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_TABLE_DISPLAY_TIME_INTERVAL);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_GANTT_DISPLAY_TIME_INTERVAL);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_PIE_DISPLAY_TIME_INTERVAL);
		topics.registerAll();

		// register the selection listener
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
						.addSelectionListener(listener);
			}
		});

	};

	/**
	 * Create and activate a new FramesocPart, assigning a random secondary ID.
	 * 
	 * @param id
	 *            primary ID of a FramesocPart
	 * @return a view corresponding to the given ID, or null if a PartInitException occurs
	 */
	private FramesocPart createNewView(String id) {
		// get the active page
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		// pick a random secondary ID
		String secondaryId = UUID.randomUUID().toString();
		// these two lines open (create) and focus on the view
		try {
			// Check if there is a plugin providing a view with this ID
			if (!isFramesocPartExisting(id))
				return null;
			page.showView(id, secondaryId, IWorkbenchPage.VIEW_CREATE);
			FramesocPart view = (FramesocPart) page.showView(id, secondaryId,
					IWorkbenchPage.VIEW_ACTIVATE);
			return view;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Load constants in view descriptor map.
	 */
	private void loadDescMap() {
		int max = 5;
		try {
			max = Integer.valueOf(Configuration.getInstance().get(
					SoCTraceProperty.max_view_instances));
		} catch (NumberFormatException e) {
			logger.error(SoCTraceProperty.max_view_instances.toString()
					+ " is not an integer, using " + max + " instead.");
		}
		viewDescMap = new HashMap<String, ViewDesc>();

		if (FramesocPerspective.DEBUG) {
			viewDescMap.put(FramesocViews.DEBUG_VIEW_ID, new ViewDesc(max));
			return;
		}

		// Add the FramesocPart advertised in the extension point to the descriptor map
		List<PartContributionDescriptor> parts = FramesocPartContributionManager.getInstance()
				.getPartContributionDescriptors();
		for (PartContributionDescriptor des : parts) {
			viewDescMap.put(des.id, new ViewDesc(max));
		}

	}

	/**
	 * Look for a FramesocPart for the given id with the given trace loaded inside.
	 * 
	 * @param viewId
	 *            view ID
	 * @return the part, or null if not found
	 */
	private FramesocPart searchAlreadyLoaded(String viewId, Trace trace) {
		ViewDesc desc = viewDescMap.get(viewId);
		if (desc != null) {
			for (FramesocPart part : desc.openParts) {
				Trace t = part.getCurrentShownTrace();
				if (t != null) {
					if (trace.equals(t))
						return part;
				}
			}
		}
		return null;
	}

	/**
	 * Look for a FramesocPart for the given id without a loaded trace inside.
	 * 
	 * @param viewId
	 *            view ID
	 * @return the part, or null if not found
	 */
	private FramesocPart searchEmpty(String viewId) {
		ViewDesc desc = viewDescMap.get(viewId);
		if (desc != null) {
			for (FramesocPart part : desc.openParts) {
				if (part.getCurrentShownTrace() == null)
					return part;
			}
		}
		return null;
	}

	// debug

	private void printDescriptors() {
		Iterator<Entry<String, ViewDesc>> it = viewDescMap.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, ViewDesc> e = it.next();
			logger.debug("View ID: {}", e.getKey());
			logger.debug("Descriptor: {}", e.getValue());
		}
	}
}
