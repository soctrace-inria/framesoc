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
package fr.inria.soctrace.framesoc.ui.gantt.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.linuxtools.tmf.ui.widgets.timegraph.dialogs.TimeGraphFilterDialog;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ILinkEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.gantt.Activator;
import fr.inria.soctrace.framesoc.ui.gantt.GanttContributionManager;
import fr.inria.soctrace.framesoc.ui.gantt.loaders.CpuEventDrawer;
import fr.inria.soctrace.framesoc.ui.gantt.model.IEventDrawer;
import fr.inria.soctrace.framesoc.ui.gantt.model.IEventLoader;
import fr.inria.soctrace.framesoc.ui.gantt.model.LoaderQueue;
import fr.inria.soctrace.framesoc.ui.gantt.model.ReducedEvent;
import fr.inria.soctrace.framesoc.ui.gantt.provider.GanttPresentationProvider;
import fr.inria.soctrace.framesoc.ui.model.CategoryNode;
import fr.inria.soctrace.framesoc.ui.model.ColorsChangeDescriptor;
import fr.inria.soctrace.framesoc.ui.model.EventTypeNode;
import fr.inria.soctrace.framesoc.ui.model.HistogramTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.ITreeNode;
import fr.inria.soctrace.framesoc.ui.model.PieTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TableTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartManager;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.framesoc.ui.utils.AlphanumComparator;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.ModelConstants.ModelEntity;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Gantt View using the Lttng Time Graph Viewer.
 * 
 * <pre>
 * TODO: add support for filtering (making invisible) specific links, 
 * as done for states and punctual events.
 * Constraints:
 * - invisible links must not be considered when filtering because of resolution
 * - if at least one link type is visible, the hide arrows action must be unchecked
 * - if all links are invisible the hide arrows action must be checked
 * - if the hide arrows action is checked, all link types must be invisible
 * - if the hide arrows action is unchecked, all the link types must be visible
 * </pre>
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class GanttView extends AbstractGanttView {

	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(GanttView.class);

	/**
	 * Event producer column
	 */
	public static final String PRODUCER = "Event Producer";

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = FramesocViews.GANTT_CHART_VIEW_ID;

	/**
	 * Time graph presentation provider
	 */
	private GanttPresentationProvider fPresentationProvider;

	/**
	 * The event loader job
	 */
	private Job loaderJob;

	/**
	 * The event drawer thread
	 */
	private Thread drawerThread;

	/**
	 * Largest requested and loaded interval for the current shown trace
	 */
	private TimeInterval loadedInterval = new TimeInterval(Long.MAX_VALUE, Long.MIN_VALUE);

	/**
	 * Links
	 */
	private List<ILinkEvent> links;

	/**
	 * Hide arrows action
	 */
	private IAction hideArrowsAction;

	/**
	 * Percentage of displayed arrows
	 */
	private double arrowPercentage;

	/**
	 * Roots of type hierarchy
	 */
	private CategoryNode[] typeHierarchy;

	/**
	 * Tree nodes corresponding to checked nodes.
	 */
	private List<Object> visibleNodes;

	/**
	 * Constructor
	 */
	public GanttView() {
		super(ID, new GanttPresentationProvider());
		setTreeColumns(new String[] { PRODUCER });
		setTreeLabelProvider(new TimeGraphTreeLabelProvider());
		setFilterColumns(new String[] { PRODUCER });
		setFilterLabelProvider(new TimeGraphTreeLabelProvider());
		setEntryComparator(new GanttViewEntryComparator());
		fPresentationProvider = (GanttPresentationProvider) getPresentationProvider();

		topics.addTopic(FramesocBusTopic.TOPIC_UI_TRACES_SYNCHRONIZED);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_REFRESH_TRACES_NEEDED);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED);
		topics.addTopic(FramesocBusTopic.TOPIC_UI_SYSTEM_INITIALIZED);
		topics.registerAll();
	}

	private static class GanttViewEntryComparator implements Comparator<ITimeGraphEntry> {
		@Override
		public int compare(ITimeGraphEntry o1, ITimeGraphEntry o2) {
			if ((o1 instanceof TimeGraphEntry) && (o2 instanceof TimeGraphEntry)) {
				int ret = AlphanumComparator.compare(o1.getName(), o2.getName());
				return ret;
			}
			return 0;
		}
	}

	@Override
	public void setFocus() {
		super.setFocus();
	}

	@Override
	public void dispose() {
		if (loaderJob != null)
			loaderJob.cancel();
		if (drawerThread != null)
			drawerThread.interrupt();
		super.dispose();
	}

	@Override
	public void partHandle(FramesocBusTopic topic, Object data) {
		if (topic.equals(FramesocBusTopic.TOPIC_UI_COLORS_CHANGED)) {
			if (currentShownTrace == null)
				return;
			ColorsChangeDescriptor des = (ColorsChangeDescriptor) data;
			logger.debug("Colors changed: {}", des);
			if (des.getEntity().equals(ModelEntity.EVENT_TYPE)) {
				fPresentationProvider.updateColors();
				refresh();
			}
		}
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
	public void showTrace(Trace trace, Object data) {
		if (data == null) {
			showWindow(trace, trace.getMinTimestamp(), trace.getMaxTimestamp());
		} else {
			TraceIntervalDescriptor des = (TraceIntervalDescriptor) data;
			if (des.getTimeInterval().equals(TimeInterval.NOT_SPECIFIED)) {
				// double click
				showWindow(des.getTrace(), des.getTrace().getMinTimestamp(), des.getTrace()
						.getMaxTimestamp());
			} else {
				showWindow(des.getTrace(), des.getStartTimestamp(), des.getEndTimestamp());
			}
		}
	}

	private void showWindow(Trace trace, long start, long end) {

		if (trace == null) {
			return;
		}

		// create the queue
		LoaderQueue<ReducedEvent> queue = new LoaderQueue<>();

		// create the event loader
		IEventLoader loader = GanttContributionManager.getEventLoader(trace.getType().getId());
		loader.setTrace(trace);
		loader.setQueue(queue);

		// compute the actual time interval to load
		TimeInterval interval = new TimeInterval(start, end);

		// check for unchanged interval
		if (checkReuse(trace, interval)) {
			loader.release();
			refresh(interval);
			return;
		}

		// create the event drawer
		IEventDrawer drawer = GanttContributionManager.getEventDrawer(trace.getType().getId());
		drawer.setProducers(loader.getProducers());

		// launch the job loading the queue
		launchLoaderJob(loader, interval);

		// update the viewer
		launchDrawerThread(drawer, interval, trace, queue);
	}

	private void reloadWindow(Trace trace, long start, long end, boolean forceCpuDrawer) {

		if (trace == null) {
			return;
		}

		// create the queue
		LoaderQueue<ReducedEvent> queue = new LoaderQueue<>();

		// create the event loader
		IEventLoader loader = GanttContributionManager.getEventLoader(trace.getType().getId());
		loader.setTrace(trace);
		loader.setQueue(queue);

		// compute the actual time interval to load
		TimeInterval interval = new TimeInterval(start, end);

		// create the event drawer
		IEventDrawer drawer = null;
		if (forceCpuDrawer) {
			drawer = new CpuEventDrawer();
		} else {
			drawer = GanttContributionManager.getEventDrawer(trace.getType().getId());
		}
		drawer.setProducers(loader.getProducers());

		// launch the job loading the queue
		launchLoaderJob(loader, interval);

		// update the viewer
		launchDrawerThread(drawer, interval, trace, queue);
	}

	private boolean checkReuse(Trace trace, TimeInterval interval) {
		if (trace.equals(currentShownTrace)
				&& (interval.startTimestamp >= loadedInterval.startTimestamp)
				&& (interval.endTimestamp <= loadedInterval.endTimestamp)) {
			return true;
		}
		loadedInterval = interval;
		setStartTime(Long.MAX_VALUE);
		setEndTime(Long.MIN_VALUE);
		refresh();
		return false;
	}

	private void launchLoaderJob(final IEventLoader loader, final TimeInterval interval) {
		loaderJob = new Job("Loading Gantt Chart...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				DeltaManager all = new DeltaManager();
				all.start();
				Collection<EventType> types = loader.getTypes().values();
				fPresentationProvider.setTypes(types);
				typeHierarchy = getTypeHierarchy(types);
				visibleNodes = listAllInputs(Arrays.asList(typeHierarchy));
				loader.loadWindow(interval.startTimestamp, interval.endTimestamp, monitor);
				loader.release();
				logger.debug(all.endMessage("Loader Job: loaded everything"));
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				return Status.OK_STATUS;
			}
		};
		loaderJob.setUser(false);
		loaderJob.schedule();
	}

	private void launchDrawerThread(final IEventDrawer drawer,
			final TimeInterval requestedInterval, final Trace trace,
			final LoaderQueue<ReducedEvent> queue) {

		enableActions(true);

		drawerThread = new Thread() {
			@Override
			public void run() {

				DeltaManager all = new DeltaManager();
				all.start();

				DeltaManager dm = new DeltaManager();
				setEntryComparator(new GanttViewEntryComparator());
				if (getEntryList() != null)
					getEntryList().clear();
				boolean closeIfCancelled = true;
				currentShownTrace = trace;
				long min = trace.getMinTimestamp();
				long max = trace.getMaxTimestamp();

				setMinTime(min);
				setMaxTime(max);
				setStartTime(Long.MAX_VALUE);
				setEndTime(Long.MIN_VALUE);

				long waited = 0;
				TimeInterval partial = null;
				while (!queue.done()) {
					try {
						dm.start();
						List<ReducedEvent> events = queue.pop();
						dm.end();
						waited += dm.getDelta();
						if (events.isEmpty())
							continue;

						// prepare the viewer model
						partial = drawer.draw(events);

						// update start / end timestamp
						long newStart = Math.max(requestedInterval.startTimestamp,
								Math.min(partial.startTimestamp, getStartTime()));
						long newEnd = Math.min(requestedInterval.endTimestamp,
								Math.max(partial.endTimestamp, getEndTime()));
						boolean needRefresh = drawer.needRefresh() || (newEnd > getEndTime())
								|| newStart < getStartTime();

						// update start time
						setStartTime(newStart);
						// update end time
						setEndTime(newEnd);
						// update entry list
						addToEntryList(drawer.getNewRootEntries());
						// copy the list for concurrent access
						links = sortLinks(new ArrayList<>(drawer.getLinks()));

						if (needRefresh) {
							refresh();
						} else {
							redraw();
						}
						closeIfCancelled = false;
					} catch (InterruptedException e) {
						logger.debug("Interrupted while taking the queue head");
					}
				}

				TimeInterval queueInterval = queue.getTimeInterval();
				if (queue.isComplete()) {
					// the whole requested interval has been loaded
					setStartTime(Math.max(requestedInterval.startTimestamp,
							queueInterval.startTimestamp));
					setEndTime(Math.min(requestedInterval.endTimestamp, queueInterval.endTimestamp));
				} else {
					// something has not been loaded
					if (partial != null && queueInterval != null) {
						// something has been loaded
						setStartTime(Math.max(requestedInterval.startTimestamp,
								queueInterval.startTimestamp));
						setEndTime(Math.min(partial.endTimestamp, Math.min(
								requestedInterval.endTimestamp, queueInterval.endTimestamp)));
						loadedInterval.startTimestamp = getStartTime();
						loadedInterval.endTimestamp = getEndTime();
					}
					handleCancel(closeIfCancelled);
				}

				// refresh one last time
				refresh();

				// release drawer
				drawer.release();

				logger.debug(all.endMessage("Drawer Thread: visualizing everything"));
				logger.debug("Waited {} ms", waited);
				logger.debug("LoadedInterval {}", loadedInterval.toString());
				logger.debug("start: {}", getStartTime());
				logger.debug("end: {}", getEndTime());

			}
		};
		drawerThread.start();
	}

	private List<ILinkEvent> sortLinks(List<ILinkEvent> arrayList) {
		Collections.sort(arrayList, new Comparator<ILinkEvent>() {
			@Override
			public int compare(ILinkEvent e1, ILinkEvent e2) {
				if (e1.getTime() > e2.getTime())
					return 1;
				if (e1.getTime() == e2.getTime())
					return 0;
				return -1;
			}
		});
		return arrayList;
	}

	@Override
	protected List<ILinkEvent> getLinkList(long startTime, long endTime, long resolution,
			IProgressMonitor monitor) {
		if (links == null)
			return null;
		List<ILinkEvent> actualLinks = new ArrayList<>();
		long actualStart = startTime;
		int intersecting = 0; // links intersecting the time interval
		// iterate over the *sorted* list of events
		for (ILinkEvent link : links) {

			if (monitor.isCanceled()) {
				return null;
			}

			// move the time cursor
			actualStart = Math.max(link.getTime(), actualStart);

			if (link.getTime() > endTime || (link.getTime() + link.getDuration()) < startTime) {
				// link not intersecting the interval
				continue;
			}
			intersecting++;
			if (link.getDuration() > resolution) {
				// link whose duration is visible
				actualLinks.add(link);
			} else {
				// for links whose duration is smaller than or equal to the
				// resolution pick only one
				if (link.getTime() >= actualStart) {
					actualLinks.add(link);
					actualStart += resolution;
				}
			}
		}
		updateLinksText(actualLinks.size(), intersecting);
		return actualLinks;
	}

	private void handleCancel(final boolean closeIfCancelled) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (closeIfCancelled) {
					FramesocPartManager.getInstance().disposeFramesocPart(GanttView.this);
					GanttView.this.hideView();
				}
			}
		});
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {

		// Filters
		manager.add(getTimeGraphCombo().getShowFilterAction());
		manager.add(createShowTypeFilterAction());
		hideArrowsAction = createHideArrowsAction();
		manager.add(hideArrowsAction);
		manager.add(new Separator());
		
		// zoom
		manager.add(getTimeGraphViewer().getResetScaleAction());
		manager.add(getTimeGraphViewer().getZoomInAction());
		manager.add(getTimeGraphViewer().getZoomOutAction());
		manager.add(new Separator());
		
		// navigation
		manager.add(getTimeGraphViewer().getPreviousEventAction());
		manager.add(getTimeGraphViewer().getNextEventAction());
		manager.add(getPreviousResourceAction());
		manager.add(getNextResourceAction());
		manager.add(new Separator());

		// others
		manager.add(getTimeGraphViewer().getShowLegendAction());
		manager.add(createCpuDrawerAction());
		manager.add(new Separator());

		// Framesoc
		TableTraceIntervalAction.add(manager, createTableAction());
		PieTraceIntervalAction.add(manager, createPieAction());
		HistogramTraceIntervalAction.add(manager, createHistogramAction());

		// TEST ACTION
		// manager.add(new Action("Test Action", IAction.AS_PUSH_BUTTON) {
		// @Override
		// public void run() {
		//
		// }
		// });
	}

	protected TraceIntervalDescriptor getIntervalDescriptor() {
		if (currentShownTrace == null)
			return null;
		TraceIntervalDescriptor des = new TraceIntervalDescriptor();
		des.setTrace(currentShownTrace);
		des.setStartTimestamp(getStartTime());
		des.setEndTimestamp(getEndTime());
		return des;
	}

	private IAction createShowTypeFilterAction() {
		IAction action = new Action("", IAction.AS_PUSH_BUTTON) {
			@Override
			public void run() {
				showTypeFilterAction();
			}
		};
		action.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID,
				"icons/filter_types.gif"));
		action.setToolTipText("Show Event Type Filter");
		return action;
	}

	private IAction createHideArrowsAction() {
		// ignore dialog settings (null is passed)
		final IAction defaultAction = getTimeGraphCombo().getTimeGraphViewer().getHideArrowsAction(
				null);
		IAction action = new Action("", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				boolean hideArrows = hideArrowsAction.isChecked();
				defaultAction.setChecked(hideArrows);
				defaultAction.run();
				refresh();
				if (hideArrows) {
					setArrowPercentage(0.0);
				} else {
					setArrowPercentage(arrowPercentage);
				}
			}
		};
		action.setImageDescriptor(defaultAction.getImageDescriptor());
		action.setToolTipText(defaultAction.getToolTipText());
		return action;
	}

	private IAction createCpuDrawerAction() {
		IAction action = new Action("", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				reloadWindow(currentShownTrace, getStartTime(), getEndTime(), isChecked());
			}
		};
		action.setImageDescriptor(ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID,
				"icons/cpu_node.png"));
		action.setToolTipText("Use CPU Drawer");
		return action;
	}

	private void updateLinksText(final double shown, final double intersecting) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (intersecting == 0) {
					arrowPercentage = 100;
				} else {
					arrowPercentage = (shown / intersecting) * 100;
				}
				if (hideArrowsAction != null && !hideArrowsAction.isChecked()) {
					setArrowPercentage(arrowPercentage);
				}
			}
		});
	}

	/**
	 * Get the event type hierarchy
	 * 
	 * @param types
	 *            collection of event types
	 * @return an array containing the roots of the type hierarchy
	 */
	public CategoryNode[] getTypeHierarchy(Collection<EventType> types) {
		Map<Integer, CategoryNode> categories = new HashMap<>();
		for (EventType et : types) {
			EventTypeNode etn = new EventTypeNode(et);
			if (et.getCategory() == EventCategory.LINK
					|| et.getCategory() == EventCategory.VARIABLE) {
				// skip links and variables
				continue;
			}
			if (!categories.containsKey(et.getCategory())) {
				categories.put(et.getCategory(), new CategoryNode(et.getCategory()));
			}
			categories.get(et.getCategory()).addChild(etn);
		}
		return categories.values().toArray(new CategoryNode[categories.values().size()]);
	}

	/**
	 * Explores the list of top-level inputs and returns all the inputs
	 * 
	 * @param inputs
	 *            The top-level inputs
	 * @return All the inputs
	 */
	private List<Object> listAllInputs(List<? extends ITreeNode> inputs) {
		ArrayList<Object> items = new ArrayList<>();
		for (ITreeNode entry : inputs) {
			items.add(entry);
			if (entry.hasChildren()) {
				items.addAll(listAllInputs(entry.getChildren()));
			}
		}
		return items;
	}

	/**
	 * Callback for the show type filter action
	 */
	private void showTypeFilterAction() {

		TimeGraphFilterDialog typeFilterDialog = getTypeFilterDialog();

		if (typeHierarchy.length > 0) {
			typeFilterDialog.setInput(typeHierarchy);
			typeFilterDialog.setTitle("Event Type Filter");
			typeFilterDialog.setMessage("Check the event types to show");

			List<Object> allElements = listAllInputs(Arrays.asList(typeHierarchy));
			typeFilterDialog.setExpandedElements(allElements.toArray());
			typeFilterDialog.setInitialElementSelections(visibleNodes);
			typeFilterDialog.create();
			typeFilterDialog.open();

			// Process selected elements
			if (typeFilterDialog.getResult() != null) {
				visibleNodes = Arrays.asList(typeFilterDialog.getResult());
				ArrayList<Object> filteredElements = new ArrayList<Object>(allElements);
				filteredElements.removeAll(visibleNodes);
				List<Integer> filteredTypes = new ArrayList<>(filteredElements.size());
				for (Object o : filteredElements) {
					if (o instanceof EventTypeNode) {
						EventTypeNode type = (EventTypeNode) o;
						filteredTypes.add(type.getId());
					}
				}
				fPresentationProvider.setFilteredTypes(filteredTypes);
			} else {
				fPresentationProvider.setFilteredTypes(Collections.<Integer> emptyList());
			}
			refresh();
		}

	}

}
