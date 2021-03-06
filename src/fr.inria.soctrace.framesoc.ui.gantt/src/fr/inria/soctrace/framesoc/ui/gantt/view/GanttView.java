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
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.wb.swt.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.linuxtools.tmf.ui.widgets.timegraph.dialogs.TimeGraphFilterDialog;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ILinkEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils;
import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.framesoc.ui.gantt.Activator;
import fr.inria.soctrace.framesoc.ui.gantt.GanttContributionManager;
import fr.inria.soctrace.framesoc.ui.gantt.loaders.CpuEventDrawer;
import fr.inria.soctrace.framesoc.ui.gantt.loaders.GanttEntry;
import fr.inria.soctrace.framesoc.ui.gantt.model.IEventDrawer;
import fr.inria.soctrace.framesoc.ui.gantt.model.IEventLoader;
import fr.inria.soctrace.framesoc.ui.gantt.model.ReducedEvent;
import fr.inria.soctrace.framesoc.ui.gantt.provider.GanttPresentationProvider;
import fr.inria.soctrace.framesoc.ui.gantt.snapshot.GanttSnapshotDialog;
import fr.inria.soctrace.framesoc.ui.model.CategoryNode;
import fr.inria.soctrace.framesoc.ui.model.ColorsChangeDescriptor;
import fr.inria.soctrace.framesoc.ui.model.EventProducerNode;
import fr.inria.soctrace.framesoc.ui.model.EventTableDescriptor;
import fr.inria.soctrace.framesoc.ui.model.EventTypeNode;
import fr.inria.soctrace.framesoc.ui.model.HistogramTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.ITreeNode;
import fr.inria.soctrace.framesoc.ui.model.LoaderQueue;
import fr.inria.soctrace.framesoc.ui.model.PieTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.SynchronizeTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TableTraceIntervalAction;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.model.TraceConfigurationDescriptor;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartManager;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.framesoc.ui.providers.EventProducerTreeLabelProvider;
import fr.inria.soctrace.framesoc.ui.utils.AlphanumComparator;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.ModelConstants.ModelEntity;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;

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
	private static final Logger logger = LoggerFactory
			.getLogger(GanttView.class);

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
	private TimeInterval loadedInterval = new TimeInterval(Long.MAX_VALUE,
			Long.MIN_VALUE);

	/**
	 * Links
	 */
	private List<ILinkEvent> links;

	/**
	 * Event Producer filter dialog action
	 */
	private IAction producerFilterAction;

	/**
	 * Event Type filter dialog action
	 */
	private IAction typeFilterAction;

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
	private List<Object> visibleTypeNodes;

	/**
	 * Number of event type nodes
	 */
	private int allTypeNodes;

	/**
	 * Should the Gantt focus on a particular event ?
	 */
	private boolean focusOnEvent = false;

	/**
	 * Information about the event the Gantt should be focused on
	 */
	private TraceConfigurationDescriptor focusEventDescriptor = null;

	/**
	 * Flag specifying if we use the CPU drawer?
	 */
	private boolean forceCpuDrawer = false;

	/**
	 * Flag indicating saying to check if we need to set up the filters (event
	 * producers and event types)
	 */
	private boolean setFilters = false;

	private ArrayList<Integer> mergedIDs;

	public boolean isForceCpuDrawer() {
		return forceCpuDrawer;
	}

	public void setForceCpuDrawer(boolean forceCpuDrawer) {
		this.forceCpuDrawer = forceCpuDrawer;
	}

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

	private static class GanttViewEntryComparator implements
			Comparator<ITimeGraphEntry> {
		@Override
		public int compare(ITimeGraphEntry o1, ITimeGraphEntry o2) {
			if ((o1 instanceof TimeGraphEntry)
					&& (o2 instanceof TimeGraphEntry)) {
				int ret = AlphanumComparator
						.compare(o1.getName(), o2.getName());
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
				showWindow(des.getTrace(), des.getTrace().getMinTimestamp(),
						des.getTrace().getMaxTimestamp());
			} else {
				long start = des.getStartTimestamp();
				long end = des.getEndTimestamp();

				// Are we in the case where we should focus on an event
				if (data instanceof TraceConfigurationDescriptor) {
					focusEventDescriptor = (TraceConfigurationDescriptor) data;

					// Check later if filters apply since we need loading to
					// have taken place before performing the checks (check
					// performed in DrawerThread)
					setFilters = true;

					if (focusEventDescriptor.isFocusOnEvent()) {
						focusOnEvent = true;

						// Change duration to load one percent of the trace
						// around the event
						long duration = Math
								.max((des.getTrace().getMaxTimestamp() - des
										.getTrace().getMinTimestamp()) / 100,
										TraceConfigurationDescriptor.MIN_TIME_UNIT_SHOWING);
						start = Math.max(des.getTrace().getMinTimestamp(),
								des.getStartTimestamp() - duration / 2);
						end = Math.min(des.getTrace().getMaxTimestamp(),
								des.getEndTimestamp() + duration / 2);

						// If it is within the already loaded interval
						if ((start >= loadedInterval.startTimestamp)
								&& (end <= loadedInterval.endTimestamp)) {
							// Don't reload
							start = loadedInterval.startTimestamp;
							end = loadedInterval.endTimestamp;
						}
					}
				}

				showWindow(des.getTrace(), start, end);
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
		IEventLoader loader = GanttContributionManager.getEventLoader(trace
				.getType().getId());
		loader.setTrace(trace);
		loader.setQueue(queue);

		// compute the actual time interval to load
		TimeInterval interval = new TimeInterval(start, end);

		// check for unchanged interval
		if (checkReuse(trace, interval)) {
			loader.release();

			if (setFilters) {
				setFilters = false;
				checkFilters();
			}

			refresh(interval);

			// Do we need to focus on a particular event ?
			if (focusOnEvent) {
				focusOnEvent = false;
				focusViewOn(focusEventDescriptor);
			}

			return;
		}

		// create the event drawer
		IEventDrawer drawer;
		if (forceCpuDrawer) {
			drawer = new CpuEventDrawer();
		} else {
			drawer = GanttContributionManager.getEventDrawer(trace.getType()
					.getId());
		}
		drawer.setProducers(loader.getProducers());

		// launch the job loading the queue
		launchLoaderJob(loader, interval);

		// update the viewer
		launchDrawerThread(drawer, interval, trace, queue);
	}

	private void reloadWindow(Trace trace, long start, long end) {

		if (trace == null) {
			return;
		}

		// create the queue
		LoaderQueue<ReducedEvent> queue = new LoaderQueue<>();

		// create the event loader
		IEventLoader loader = GanttContributionManager.getEventLoader(trace
				.getType().getId());
		loader.setTrace(trace);
		loader.setQueue(queue);

		// compute the actual time interval to load
		TimeInterval interval = new TimeInterval(start, end);

		// create the event drawer
		IEventDrawer drawer = null;
		if (forceCpuDrawer) {
			drawer = new CpuEventDrawer();
		} else {
			drawer = GanttContributionManager.getEventDrawer(trace.getType()
					.getId());
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

	private void launchLoaderJob(final IEventLoader loader,
			final TimeInterval interval) {
		loaderJob = new Job("Loading Gantt Chart...") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				DeltaManager all = new DeltaManager();
				all.start();
				Collection<EventType> types = loader.getTypes().values();
				fPresentationProvider.setTypes(types);
				typeHierarchy = getTypeHierarchy(types);
				visibleTypeNodes = listAllInputs(new ArrayList<CategoryNode>(Arrays.asList(typeHierarchy)));
				allTypeNodes = visibleTypeNodes.size();

				loader.loadWindow(interval.startTimestamp,
						interval.endTimestamp, monitor);
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
						long newStart = Math
								.max(requestedInterval.startTimestamp, Math
										.min(partial.startTimestamp,
												getStartTime()));
						long newEnd = Math.min(requestedInterval.endTimestamp,
								Math.max(partial.endTimestamp, getEndTime()));

						// update start time
						setStartTime(newStart);
						// update end time
						setEndTime(newEnd);
						// update entry list
						addToEntryList(drawer.getNewRootEntries());
						// copy the list for concurrent access
						links = sortLinks(new ArrayList<>(drawer.getLinks()));
						
						boolean needRefresh = drawer.needRefresh()
								|| (newEnd > getEndTime())
								|| newStart < getStartTime();

						if (needRefresh) {
							refresh();
							// Do not resize the Gantt after the first display
							setfUserChangedTimeRange(true);
						} else {
							redraw();
						}
						closeIfCancelled = false;
					} catch (InterruptedException e) {
						logger.debug("Interrupted while taking the queue head");
					}
				}

				// Merge entries only at the end to avoid a bug where some
				// producers are not shown
				mergeEntries();
				refresh();
				
				TimeInterval queueInterval = queue.getTimeInterval();
				if (queue.isComplete()) {
					// the whole requested interval has been loaded
					setStartTime(Math.max(requestedInterval.startTimestamp,
							queueInterval.startTimestamp));
					setEndTime(Math.min(requestedInterval.endTimestamp,
							queueInterval.endTimestamp));

					// Update loadedInterval values
					loadedInterval.startTimestamp = getStartTime();
					loadedInterval.endTimestamp = getEndTime();
				} else {
					// something has not been loaded
					if (partial != null && queueInterval != null) {
						// something has been loaded
						setStartTime(Math.max(requestedInterval.startTimestamp,
								queueInterval.startTimestamp));
						setEndTime(Math.min(partial.endTimestamp, Math.min(
								requestedInterval.endTimestamp,
								queueInterval.endTimestamp)));
						loadedInterval.startTimestamp = getStartTime();
						loadedInterval.endTimestamp = getEndTime();
					}
					handleCancel(closeIfCancelled);
				}

				// Apply filter from synchronization with other views
				if (setFilters) {
					setFilters = false;
					checkFilters();
				}

				// Should we focus on an event ?
				if (focusOnEvent) {
					Display.getDefault().syncExec(new Runnable() {

						@Override
						public void run() {
							focusOnEvent = false;
							focusViewOn(focusEventDescriptor);
						}
					});
				}

				// refresh one last time
				refresh();

				// release drawer
				drawer.release();

				logger.debug(all
						.endMessage("Drawer Thread: visualizing everything"));
				logger.debug("Waited {} ms", waited);
				logger.debug("LoadedInterval {}", loadedInterval.toString());
				logger.debug("start: {}", getStartTime());
				logger.debug("end: {}", getEndTime());

			}
		};
		drawerThread.start();
	}

	protected void mergeEntries() {
		GanttEntry root = null;
		for (TimeGraphEntry entry : fEntryList) {
			if (entry.getParent() == null) {
				root = (GanttEntry) entry;
				break;
			}
		}

		List<TimeGraphEntry> mergedEntries = new ArrayList<TimeGraphEntry>();
		mergedIDs = new ArrayList<Integer>();
		
		if (root != null && root.getChildren().size() == 1) {
			TimeGraphEntry child = root.getChildren().get(0);
			while (child.getChildren().size() == 1) {
				if (!((GanttEntry) child).isProducingEvent()) {
					root.setName(root.getName() + " - " + child.getName());
					root.getChildren().clear();
					root.addChild(child.getChildren().get(0));
					mergedEntries.add(child);
					mergedIDs.add(((GanttEntry) child).getEventProducerID());

					child = child.getChildren().get(0);
				} else {
					break;
				}
			}

			root.setName(root.getName() + " - " + child.getName());
			root.getChildren().clear();
			for (TimeGraphEntry children : child.getChildren()) {
				root.addChild(children);
			}
			mergedEntries.add(child);
			mergedIDs.add(((GanttEntry) child).getEventProducerID());

			for (TimeGraphEntry children : mergedEntries.get(
					mergedEntries.size() - 1).getChildren()) {
				children.setParent(root);
			}
		}

		fEntryList.removeAll(mergedEntries);
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
	protected List<ILinkEvent> getLinkList(long startTime, long endTime,
			long resolution, IProgressMonitor monitor) {
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

			if (link.getTime() > endTime
					|| (link.getTime() + link.getDuration()) < startTime) {
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
					FramesocPartManager.getInstance().disposeFramesocPart(
							GanttView.this);
					GanttView.this.hideView();
				}
			}
		});
	}

	@Override
	protected void fillLocalToolBar(IToolBarManager manager) {

		// Filters
		producerFilterAction = getTimeGraphCombo().getShowFilterAction();
		getTimeGraphCombo().getFilterDialog().setLabelProvider(
				new EventProducerTreeLabelProvider());
		getTimeGraphCombo().getFilterDialog().setComparator(
				new ViewerComparator(new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						return AlphanumComparator.compare(o1, o2);
					}
				}));
		manager.add(producerFilterAction);
		typeFilterAction = createShowTypeFilterAction();
		manager.add(typeFilterAction);
		hideArrowsAction = createHideArrowsAction();
		manager.add(hideArrowsAction);
		manager.add(new Separator());

		// snapshot
		manager.add(createSnapshotAction());
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
		SynchronizeTraceIntervalAction.add(manager, createSynchronizeAction()); 

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
		TraceConfigurationDescriptor des = new TraceConfigurationDescriptor();
		des.setTrace(currentShownTrace);
		des.setStartTimestamp(getStartTime());
		des.setEndTimestamp(getEndTime());

		// Set event producers and event types filter
		Set<ITimeGraphEntry> entries = getTimeGraphViewer()
				.getTimeGraphControl().getEntries();

		des.setEventProducers(convertToEventProducerNode(entries));

		des.setEventTypes(visibleTypeNodes);

		return des;
	}

	private IAction createShowTypeFilterAction() {
		IAction action = new Action("", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				showTypeFilterAction();
			}
		};
		action.setImageDescriptor(ResourceManager.getPluginImageDescriptor(
				Activator.PLUGIN_ID, "icons/filter_types.gif"));
		action.setToolTipText("Show Event Type Filter");
		return action;
	}

	private IAction createHideArrowsAction() {
		DialogSettings settings = (DialogSettings) Activator.getDefault()
				.getDialogSettings();
		final IAction defaultAction = getTimeGraphCombo().getTimeGraphViewer()
				.getHideArrowsAction(settings);
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
		action.setChecked(defaultAction.isChecked());
		return action;
	}

	private IAction createCpuDrawerAction() {
		IAction action = new Action("", IAction.AS_CHECK_BOX) {

			@Override
			public void run() {
				setForceCpuDrawer(isChecked());
				reloadWindow(currentShownTrace, getStartTime(), getEndTime());
			}
		};
		action.setImageDescriptor(ResourceManager.getPluginImageDescriptor(
				Activator.PLUGIN_ID, "icons/cpu_node.png"));
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
				categories.put(et.getCategory(),
						new CategoryNode(et.getCategory()));
			}
			categories.get(et.getCategory()).addChild(etn);
		}
		return categories.values().toArray(
				new CategoryNode[categories.values().size()]);
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

			List<Object> allElements = listAllInputs(Arrays
					.asList(typeHierarchy));
			typeFilterDialog.setExpandedElements(allElements.toArray());
			typeFilterDialog.setInitialElementSelections(visibleTypeNodes);
			// Sort in alphabetical order
			typeFilterDialog.setComparator(new ViewerComparator(
					new Comparator<String>() {
						@Override
						public int compare(String o1, String o2) {
							return AlphanumComparator.compare(o1, o2);
						}
					}));
			typeFilterDialog.create();

			// reset checked status, managed manually
			typeFilterAction.setChecked(!typeFilterAction.isChecked());

			if (typeFilterDialog.open() != Window.OK) {
				return;
			}

			// Process selected elements
			if (typeFilterDialog.getResult() != null) {
				visibleTypeNodes = new ArrayList<Object>(Arrays.asList(typeFilterDialog.getResult()));
				checkTypeFilter(visibleTypeNodes.size() != allTypeNodes);
				ArrayList<Object> filteredElements = new ArrayList<Object>(
						allElements);
				filteredElements.removeAll(visibleTypeNodes);
				List<Integer> filteredTypes = new ArrayList<>(
						filteredElements.size());
				for (Object o : filteredElements) {
					if (o instanceof EventTypeNode) {
						EventTypeNode type = (EventTypeNode) o;
						filteredTypes.add(type.getId());
					}
				}
				fPresentationProvider.setFilteredTypes(filteredTypes);
			} else {
				fPresentationProvider.setFilteredTypes(Collections
						.<Integer> emptyList());
			}
			refresh();
		}
	}

	private void checkTypeFilter(boolean check) {
		if (check) {
			typeFilterAction.setChecked(true);
			typeFilterAction
					.setToolTipText("Show Event Type Filter (filter applied)");

		} else {
			typeFilterAction.setChecked(false);
			typeFilterAction.setToolTipText("Show Event Type Filter");
		}
	}

	/**
	 * Focus the Gantt chart on a specific event (time region), by selecting the
	 * corresponding row and centering the view on the event.
	 * 
	 * @param des
	 *            the parameters needed to focus on the event
	 */
	private void focusViewOn(TraceConfigurationDescriptor des) {
		if (des.getEventProducer() == null) {
			logger.debug("No event producer was provided in the descriptor");
			return;
		}

		// Look for the entry corresponding to the event producer
		ITimeGraphEntry entry = null;
		for (ITimeGraphEntry tge : getTimeGraphViewer().getExpandedElements()) {
			if (tge.getName().equals(des.getEventProducer().getName())) {
				entry = tge;
				break;
			}
		}

		if (entry == null) {
			logger.debug("The event producer ("
					+ des.getEventProducer().toString()
					+ ") was not found in the Gantt Chart");
			return;
		}

		// Select the corresponding entry
		getTimeGraphCombo().setSelection(entry);

		// Zoom in on the desired part
		getTimeGraphViewer().setStartFinishTimeNotify(des.getStartTimestamp(),
				des.getEndTimestamp());
	}

	/**
	 * Initialize the snapshot action
	 * 
	 * @return the action
	 */
	public IAction createSnapshotAction() {
		SnapshotAction snapshotAction = new SnapshotAction("",
				IAction.AS_PUSH_BUTTON);
		snapshotAction.ganttView = this;
		snapshotAction.setImageDescriptor(ResourceManager
				.getPluginImageDescriptor(Activator.PLUGIN_ID,
						"icons/snapshot.png"));
		snapshotAction.setToolTipText("Take a snapshot");

		return snapshotAction;
	}

	private class SnapshotAction extends Action {
		public GanttView ganttView;

		public SnapshotAction(String string, int asPushButton) {
			super(string, asPushButton);
		}

		@Override
		public void run() {
			Rectangle bound = getTimeGraphCombo().getClientArea();
			new GanttSnapshotDialog(getSite().getShell(), ganttView,
					bound.width, bound.height).open();
		}
	}

	/**
	 * Take a snaphsot of the Gantt Chart
	 * 
	 * @param width
	 *            width of the snapshot
	 * @param height
	 *            height of the snapshot. Is ignored if full height is selected.
	 * @param fullHeight
	 *            should we take the whole hierarchy
	 * @param includeTimeScale
	 *            Should we take the time scale
	 * @param fileName
	 *            name of the file where to save the snapshot
	 */
	public void takeSnapshot(int width, int height, boolean fullHeight,
			boolean includeTimeScale, String fileName) {
		int totalHeight = 0;
		int hierarchyWidth = getTimeGraphCombo().getTreeViewer().getTree()
				.getBounds().width;
		int headerHeight = 0;

		if (includeTimeScale)
			headerHeight = getTimeGraphViewer().getHeaderHeight();

		totalHeight = computeHeight(fullHeight, headerHeight, height);

		Image image = new Image(Display.getCurrent(), width, totalHeight);
		GC gc = new GC(image);

		// Draw hierarchy and adjust the height
		drawHierarchy(gc, fullHeight);

		// Create a paint event to paint on the image graphic context
		Event newEvent = new Event();
		newEvent.display = Display.getCurrent();
		newEvent.widget = getTimeGraphViewer().getTimeGraphControl();

		PaintEvent paintEvent = new PaintEvent(newEvent);
		paintEvent.gc = gc;
		paintEvent.width = width;
		paintEvent.height = totalHeight;
		paintEvent.x = hierarchyWidth;

		// Paint the graph
		getTimeGraphViewer().getTimeGraphControl().takeSnapshot(paintEvent,
				fullHeight);

		// Paint the time scale
		if (includeTimeScale) {
			// Mask the extra hierarchy in the corner
			gc.setBackground(Display.getCurrent().getSystemColor(
					SWT.COLOR_WIDGET_BACKGROUND));
			gc.fillRectangle(0, totalHeight - headerHeight, hierarchyWidth,
					headerHeight);

			// Make the drawing starts at the bottom of the graph
			paintEvent.y = totalHeight - headerHeight;
			getTimeGraphViewer().getTimeGraphScale().takeSnapshot(paintEvent,
					false);
		}

		// Save the file
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { image.getImageData() };
		loader.save(fileName, SWT.IMAGE_PNG);

		// Clean stuff
		image.dispose();
		gc.dispose();
	}

	/**
	 * Draw the hierarchy
	 * 
	 * @param gc
	 *            the graphics context on which to draw
	 * @return the total height of the drawing
	 */
	private void drawHierarchy(GC gc, boolean fullHeight) {
		ITimeGraphEntry[] graphEntries = getTimeGraphViewer()
				.getExpandedElements();
		int entryHeight = 0;
		int currentHeight = 0;
		int entryLevel = 0;
		// Shift to apply when getting down one hierarchy level
		int entryShifting = 20;
		// Shift to center the name of the producer
		int verticalShift = 5;
		int width = getTimeGraphCombo().getTreeViewer().getTree().getBounds().width;

		// Set colors
		Color rectangleBgColor1 = Display.getCurrent().getSystemColor(
				SWT.COLOR_WIDGET_BACKGROUND);
		Color rectangleBgColor2 = Display.getCurrent().getSystemColor(
				SWT.COLOR_LIST_BACKGROUND);
		Color fgColor = Display.getCurrent().getSystemColor(
				SWT.COLOR_LIST_FOREGROUND);
		gc.setBackground(rectangleBgColor2);
		gc.setForeground(fgColor);

		int topIndex = getTimeGraphViewer().getTimeGraphControl().getTopIndex();
		if (fullHeight)
			// Draw all
			topIndex = 0;

		for (int i = topIndex; i < graphEntries.length; i++) {
			entryHeight = getTimeGraphViewer().getTimeGraphControl()
					.getItemHeight(graphEntries[i]);
			entryLevel = getTimeGraphViewer().getTimeGraphControl()
					.getItemLevel(graphEntries[i]) + 1;

			// Alternate background colors for better visibility
			if (i % 2 == 0) {
				gc.setBackground(rectangleBgColor1);
			} else {
				gc.setBackground(rectangleBgColor2);
			}

			// Draw the entry
			gc.fillRectangle(0, currentHeight, width, entryHeight);
			gc.drawText(graphEntries[i].getName(), entryLevel * entryShifting,
					currentHeight + verticalShift);

			// Update total height
			currentHeight += entryHeight;
		}
	}

	/**
	 * Compute the height of the final image according to the given constraints
	 * 
	 * @param fullHeight
	 *            do we capture the whole height of the gantt
	 * @param timeScaleHeight
	 *            do we capture the time scale
	 * @param askedHeight
	 *            the height required by the user
	 * @return the computed height
	 */
	private int computeHeight(boolean fullHeight, int timeScaleHeight,
			int askedHeight) {
		ITimeGraphEntry[] graphEntries = getTimeGraphViewer()
				.getExpandedElements();
		int computedHeight = timeScaleHeight;
		// Index of the top element currently displayed on the gantt
		int topIndex = 0;

		if (!fullHeight)
			topIndex = getTimeGraphViewer().getTimeGraphControl().getTopIndex();

		for (int i = topIndex; i < graphEntries.length; i++) {
			// Update total height
			computedHeight += getTimeGraphViewer().getTimeGraphControl()
					.getItemHeight(graphEntries[i]);
		}

		// If the computed is larger than what was asked
		if (!fullHeight && computedHeight > askedHeight)
			computedHeight = askedHeight;

		return computedHeight;
	}

	public String getSnapshotInfo() {
		StringBuffer output = new StringBuffer();
		output.append("\nDisplayed start timestamp: ");
		output.append(getTimeGraphViewer().getTime0());
		output.append("\nDisplayed end timestamp: ");
		output.append(getTimeGraphViewer().getTime1());
		output.append("\nSelection start timestamp: ");
		output.append(getTimeGraphViewer().getSelectionBegin());
		output.append("\nSelection end timestamp: ");
		output.append(getTimeGraphViewer().getSelectionEnd());
		output.append("\nLoaded start timestamp: ");
		output.append(getTimeGraphViewer().getBeginTime());
		output.append("\nLoaded end timestamp: ");
		output.append(getTimeGraphViewer().getEndTime());

		// Filtered types
		String filteredTypes = "";
		List<Integer> filteredType = fPresentationProvider.getFilteredTypes();
		for (Object typeObject : listAllInputs(Arrays.asList(typeHierarchy))) {
			if (typeObject instanceof EventTypeNode) {
				if (filteredType.contains(((EventTypeNode) typeObject)
						.getEventType().getId()))

					filteredTypes = filteredTypes
							+ ((EventTypeNode) typeObject).getEventType()
									.getName() + ", ";
			}
		}

		// If there was some filtered event type
		if (!filteredTypes.isEmpty()) {
			// Remove last separator
			filteredTypes = filteredTypes.substring(0,
					filteredTypes.length() - 2);
			output.append("\nFiltered event types: ");
			output.append(filteredTypes);
		}

		// Filtered event producers
		String filteredEP = "";
		for (Object objectEntry : getTimeGraphCombo().getFilteredEntries()) {
			filteredEP = filteredEP + ((GanttEntry) objectEntry).getName()
					+ ", ";
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

	@Override
	public void createContextMenu() {
		final Menu contextMenu = new Menu(getTimeGraphViewer()
				.getTimeGraphControl());
		getTimeGraphViewer().getTimeGraphControl().setContextMenu(contextMenu);
		getTimeGraphViewer().getTimeGraphControl().setMenu(
				getTimeGraphViewer().getTimeGraphControl().getContextMenu());
		contextMenu.addMenuListener(new MenuAdapter() {
			@Override
			public void menuShown(MenuEvent menuEvent) {
				// clean menu
				MenuItem[] items = getTimeGraphViewer().getTimeGraphControl()
						.getContextMenu().getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}

				final MouseEvent rightClickEvent = getTimeGraphViewer()
						.getTimeGraphControl().getRightClickEvent();

				if (rightClickEvent == null) {
					return;
				}

				// Get the corresponding event
				final ITimeEvent selectedEvent = Utils.findEvent(
						getTimeGraphViewer().getTimeGraphControl()
								.getExpandedElements()[getTimeGraphViewer()
								.getTimeGraphControl().getItemIndexAtY(
										rightClickEvent.y)],
						getTimeGraphViewer().getTimeGraphControl().getTimeAtX(
								rightClickEvent.x), 0);

				// If there is no event at the given position
				if (selectedEvent == null
						|| fPresentationProvider
								.getStateTableIndex(selectedEvent) < 0) {
					return;
				}

				// Hide type
				MenuItem hideType = new MenuItem(getTimeGraphViewer()
						.getTimeGraphControl().getContextMenu(), SWT.NONE);
				hideType.setText("Hide Type");
				hideType.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						// Find the type ID
						int typeIndex = fPresentationProvider
								.getStateTableIndex(selectedEvent);

						// Add it to the filtered types
						List<Integer> filteredTypes = fPresentationProvider
								.getFilteredTypes();
						filteredTypes.add(typeIndex);
						fPresentationProvider.setFilteredTypes(filteredTypes);

						// Synchronize with filter action
						for (Object o : visibleTypeNodes) {
							if (o instanceof EventTypeNode) {
								EventTypeNode type = (EventTypeNode) o;
								if (type.getId() == typeIndex) {
									visibleTypeNodes.remove(o);
									typeFilterAction.setChecked(true);
									break;
								}
							}
						}
						refresh();
					}
				});

				// Change color
				MenuItem changeColor = new MenuItem(contextMenu, SWT.NONE);
				changeColor.setText("Change Color Type");
				changeColor.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {

						// Get the current color
						Color c = getTimeGraphViewer().getTimeGraphControl()
								.getEventColorMap()[fPresentationProvider
								.getStateTableIndex(selectedEvent)];
						ColorDialog colorDialog = new ColorDialog(Display
								.getDefault().getActiveShell());
						// Set the default color of the color dialog to the
						// current color
						colorDialog.setRGB(new RGB(c.getRed(), c.getGreen(), c
								.getBlue()));
						RGB rgb = colorDialog.open();
						// If a color was selected
						if (rgb != null) {
							Color newColor = new Color(Display.getDefault(),
									rgb.red, rgb.green, rgb.blue);
							FramesocColor fColor = new FramesocColor(rgb.red,
									rgb.green, rgb.blue);

							// Change the color
							fPresentationProvider.getStateTable()[fPresentationProvider
									.getStateTableIndex(selectedEvent)]
									.setStateColor(rgb);

							getTimeGraphViewer().getTimeGraphControl()
									.getEventColorMap()[fPresentationProvider
									.getStateTableIndex(selectedEvent)] = newColor;

							// Save it in the general settings
							FramesocColorManager
									.getInstance()
									.setEventTypeColor(
											fPresentationProvider
													.getEventName(selectedEvent),
											fColor);
							FramesocColorManager.getInstance()
									.saveEventTypeColors();
							refresh();
						}
					}
				});

				// Show in table (and highlight event)
				MenuItem showInfo = new MenuItem(contextMenu, SWT.NONE);
				showInfo.setText("Show in Event Table");
				showInfo.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						switchToEventTable(
								selectedEvent,
								fPresentationProvider
										.getEventName(selectedEvent),
								getTimeGraphViewer().getTimeGraphControl()
										.getExpandedElements()[getTimeGraphViewer()
										.getTimeGraphControl().getItemIndexAtY(
												rightClickEvent.y)].getName());
					}
				});
			}
		});
	}

	/**
	 * Method that launch the event table view and focus on a specific event
	 * 
	 * @param event
	 *            the (Gantt) event to set the focus on
	 * @param typeName
	 *            the type of the focused event
	 * @param eventProducerName
	 *            the event producer name of the focused event
	 */
	private void switchToEventTable(ITimeEvent event, String typeName,
			String eventProducerName) {
		long startTimestamp = event.getTime();
		long endTimestamp = event.getTime() + event.getDuration();
		long duration = event.getDuration();

		// Punctual event
		if (duration == 0)
			// Show only the minimum between MIN_TIME_UNIT_SHOWING time units
			// around or one
			// percent of the trace
			duration = Math.min(
					TraceConfigurationDescriptor.MIN_TIME_UNIT_SHOWING,
					(currentShownTrace.getMaxTimestamp() - currentShownTrace
							.getMinTimestamp()) / 100);

		EventTableDescriptor des = new EventTableDescriptor();
		des.setTrace(currentShownTrace);
		des.setStartTimestamp(startTimestamp - duration / 2);
		des.setEndTimestamp(endTimestamp + duration / 2);
		des.setEventProducerName(eventProducerName);
		des.setTypeName(typeName);
		des.setEventStartTimeStamp(startTimestamp);

		FramesocBus.getInstance().send(
				FramesocBusTopic.TOPIC_UI_TABLE_DISPLAY_TIME_INTERVAL, des);
	}

	/**
	 * Apply the event producers and event types filters
	 */
	private void checkFilters() {
		// Event producers
		if (!focusEventDescriptor.getEventProducers().isEmpty()
				&& Boolean.valueOf(Configuration.getInstance().get(
						SoCTraceProperty.producer_filter_synhronization))) {
			// Get the IDs of unfiltered EventProducers
			List<Integer> ids = new ArrayList<Integer>();
			for (Object o : focusEventDescriptor.getEventProducers()) {
				if (o instanceof EventProducerNode) {
					ids.add(((EventProducerNode) o).getEventProducer().getId());
				}
			}
			List<Object> filteredEntries = new ArrayList<Object>();
			for (ITimeGraphEntry entry : getTimeGraphViewer()
					.getTimeGraphControl().getEntries()) {
				if (entry instanceof GanttEntry) {
					GanttEntry ganttEntry = (GanttEntry) entry;
					if (!ids.contains(ganttEntry.getEventProducerID())) {
						filteredEntries.add(ganttEntry);
					}
				}
			}
			getTimeGraphCombo().setFilteredEntries(filteredEntries);
		}

		// Event types
		if (!focusEventDescriptor.getEventTypes().isEmpty()
				&& Boolean.valueOf(Configuration.getInstance().get(
						SoCTraceProperty.type_filter_synchronization))) {
			if (typeHierarchy.length > 0) {
				List<Object> allElements = listAllInputs(Arrays
						.asList(typeHierarchy));

				visibleTypeNodes = focusEventDescriptor.getEventTypes();
				checkTypeFilter(!(focusEventDescriptor.getEventTypes().size() == allTypeNodes || focusEventDescriptor
						.getEventTypes().isEmpty()));
				ArrayList<Object> filteredElements = new ArrayList<Object>(
						allElements);
				filteredElements
						.removeAll(focusEventDescriptor.getEventTypes());
				List<Integer> filteredTypes = new ArrayList<>(
						filteredElements.size());
				for (Object o : filteredElements) {
					if (o instanceof EventTypeNode) {
						EventTypeNode type = (EventTypeNode) o;
						filteredTypes.add(type.getId());
					}
				}
				fPresentationProvider.setFilteredTypes(filteredTypes);

			}
		}
	}

	/**
	 * Convert the GanntEntry to EventProducerNode
	 * 
	 * @param entries
	 *            the entries of the Gantt
	 * @return the list of event producer nodes produced from the converted
	 *         entries
	 */
	private List<Object> convertToEventProducerNode(Set<ITimeGraphEntry> entries) {
		IEventLoader loader = GanttContributionManager
				.getEventLoader(currentShownTrace.getType().getId());
		loader.setTrace(currentShownTrace);
		Map<Integer, EventProducer> eventProducers = loader.getProducers();
		Map<Integer, EventProducerNode> eventProducersNode = new HashMap<Integer, EventProducerNode>();
		List<Object> epNodes = new ArrayList<Object>();
		EventProducerNode root = null;

		// Create EP Nodes
		for (ITimeGraphEntry entry : entries) {
			if (entry instanceof GanttEntry) {
				if (!getTimeGraphCombo().getFilteredEntries().contains(entry)) {
					EventProducerNode epn = new EventProducerNode(
							eventProducers.get(((GanttEntry) entry)
									.getEventProducerID()));
					eventProducersNode.put(epn.getId(), epn);
					epNodes.add(epn);
				}
			}
		}

		// Handle merged entries
		for (int epId : mergedIDs) {
			EventProducerNode epn = new EventProducerNode(
					eventProducers.get(epId));
			eventProducersNode.put(epn.getId(), epn);

			epNodes.add(epn);
		}

		// Rebuild hierarchy
		for (ITimeGraphEntry entry : entries) {
			if (entry instanceof GanttEntry) {
				if (!getTimeGraphCombo().getFilteredEntries().contains(entry)) {
					GanttEntry gEntry = (GanttEntry) entry;
					EventProducerNode epn = eventProducersNode.get(gEntry
							.getEventProducerID());

					// Set parent
					if (gEntry.getParent() != null) {
						epn.setParent(eventProducersNode
								.get(((GanttEntry) gEntry.getParent())
										.getEventProducerID()));
					} else {
						root = epn;
					}

					// Set children
					for (ITimeGraphEntry childEntry : gEntry.getChildren()) {
						if (!getTimeGraphCombo().getFilteredEntries().contains(
								childEntry)) {
							epn.addChild(eventProducersNode
									.get(((GanttEntry) childEntry)
											.getEventProducerID()));
						}
					}
				}
			}
		}

		// Handle merged entries
		if (!mergedIDs.isEmpty()) {
			EventProducerNode rootChild = null;
			EventProducerNode lastMerged = null;

			for (int epId : mergedIDs) {
				EventProducerNode epn = eventProducersNode.get(epId);
				if (epn.getEventProducer().getParentId() >= 0) {
					// Set parent
					epn.setParent(eventProducersNode.get(epn.getEventProducer()
							.getParentId()));
					// Set children
					eventProducersNode
							.get(epn.getEventProducer().getParentId())
							.addChild(epn);

					// If parent is root
					if (((EventProducerNode) epn.getParent())
							.getEventProducer().getId() == root.getId()) {
						rootChild = epn;
					}
				}
			}

			for (int epId : mergedIDs) {
				EventProducerNode epn = eventProducersNode.get(epId);
				if (!epn.hasChildren()) {
					lastMerged = epn;
				}
			}

			for (ITreeNode treenode : root.getChildren()) {
				EventProducerNode epn = (EventProducerNode) treenode;
				lastMerged.addChild(epn);
				epn.setParent(lastMerged);
			}
			root.getChildren().clear();
			root.addChild(rootChild);
		}

		return epNodes;
	}

}
