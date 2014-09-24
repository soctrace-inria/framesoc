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
package fr.inria.soctrace.framesoc.ui.gantt;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ILinkEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.gantt.model.IEventDrawer;
import fr.inria.soctrace.framesoc.ui.gantt.model.IEventLoader;
import fr.inria.soctrace.framesoc.ui.gantt.model.LoaderQueue;
import fr.inria.soctrace.framesoc.ui.gantt.model.ReducedEvent;
import fr.inria.soctrace.framesoc.ui.gantt.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.gantt.provider.GanttPresentationProvider;
import fr.inria.soctrace.framesoc.ui.model.ColorsChangeDescriptor;
import fr.inria.soctrace.framesoc.ui.model.TraceIntervalDescriptor;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocPartManager;
import fr.inria.soctrace.framesoc.ui.perspective.FramesocViews;
import fr.inria.soctrace.framesoc.ui.utils.AlphanumComparator;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.ModelConstants.ModelEntity;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Gantt View using the Lttng Time Graph Viewer.
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
	private double arrowsPercentage;

	/**
	 * Constructor
	 */
	public GanttView() {
		super(ID, new GanttPresentationProvider());
		setTreeColumns(new String[] { PRODUCER });
		setTreeLabelProvider(new TreeLabelProvider());
		setFilterColumns(new String[] { PRODUCER });
		setFilterLabelProvider(new TreeLabelProvider());
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
	public void partHandle(String topic, Object data) {
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
			showWindow(des.getTrace(), des.getStartTimestamp(), des.getEndTimestamp());
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
				fPresentationProvider.setTypes(loader.getTypes().values());
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

		setContentDescription("Trace: " + trace.getAlias());

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

	@Override
	protected void manageLinks() {
		if (links == null)
			return;
		long zoom0 = getTimeGraphViewer().getTime0();
		long zoom1 = getTimeGraphViewer().getTime1();
		long resolution = Math.max(1, (zoom1 - zoom0) / getDisplayWidth());
		Collections.sort(links, new Comparator<ILinkEvent>() {
			@Override
			public int compare(ILinkEvent e1, ILinkEvent e2) {
				if (e1.getTime() > e2.getTime())
					return 1;
				if (e1.getTime() == e2.getTime())
					return 0;
				return -1;
			}
		});
		long actualStart = zoom0;
		List<ILinkEvent> actualLinks = new ArrayList<>();
		for (ILinkEvent link : links) {
			if (link.getTime() < actualStart)
				continue;
			actualLinks.add(link);
			actualStart += resolution;
		}
		getTimeGraphViewer().setLinks(actualLinks);
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

		super.fillLocalToolBar(manager);

		IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(getClass().getName());
		if (section == null) {
			section = settings.addNewSection(getClass().getName());
		}

		// Links
		hideArrowsAction = createHideArrowsAction(section);
		ActionContributionItem hideArrowCI = new ActionContributionItem(hideArrowsAction);
		hideArrowCI.setMode(ActionContributionItem.MODE_FORCE_TEXT);
		manager.add(hideArrowCI);
		manager.add(new Separator());

		// Framesoc

		// show in table
		if (FramesocPartManager.getInstance().isFramesocPartExisting(
				FramesocViews.EVENT_TABLE_VIEW_ID)) {
			manager.add(createTableAction());
		}

		// TEST ACTION
		// manager.add(new Action("Test Action", IAction.AS_PUSH_BUTTON) {
		// @Override
		// public void run() {
		//
		// }
		// });
	}

	private IAction createTableAction() {
		ImageDescriptor img = ResourceManager.getPluginImageDescriptor(Activator.PLUGIN_ID,
				"icons/table.png");
		Action showTable = new Action("Show in Table", img) {
			@Override
			public void run() {
				TraceIntervalDescriptor des = new TraceIntervalDescriptor();
				des.setTrace(currentShownTrace);
				des.setStartTimestamp(getStartTime());
				des.setEndTimestamp(getEndTime());
				logger.debug(des.toString());
				FramesocBus.getInstance().send(
						FramesocBusTopic.TOPIC_UI_TABLE_DISPLAY_TIME_INTERVAL, des);
			}
		};
		return showTable;
	}

	private IAction createHideArrowsAction(final IDialogSettings section) {
		final IAction defaultAction = getTimeGraphCombo().getTimeGraphViewer().getHideArrowsAction(
				section);
		hideArrowsAction = new Action("", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				boolean hideArrows = hideArrowsAction.isChecked();
				defaultAction.setChecked(hideArrows);
				defaultAction.run();
				refresh();
				if (hideArrows) {
					hideArrowsAction.setText(getPercentageString(0.0));
				} else {
					hideArrowsAction.setText(getPercentageString(arrowsPercentage));
				}
			}
		};
		hideArrowsAction.setImageDescriptor(defaultAction.getImageDescriptor());
		hideArrowsAction.setText(getPercentageString(0.0));
		hideArrowsAction
				.setToolTipText(defaultAction.getToolTipText() + " [% of displayed arrows]");
		return hideArrowsAction;
	}

	private void updateLinksText(final double shown, final double intersecting) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (intersecting == 0) {
					arrowsPercentage = 100;
				} else {
					arrowsPercentage = (shown / intersecting) * 100;
				}
				if (hideArrowsAction != null && !hideArrowsAction.isChecked()) {
					hideArrowsAction.setText(getPercentageString(arrowsPercentage));
				}
			}
		});
	}

	// TODO find a nicer solution to manage width
	private String getPercentageString(double p) {
		DecimalFormat decim = new DecimalFormat("##.#");
		Double percent = Double.parseDouble(decim.format(p));
		StringBuilder sb = new StringBuilder();
		if (percent < 100)
			sb.append(" ");
		if (percent < 10)
			sb.append("   ");
		sb.append(percent);
		sb.append("%");
		return sb.toString();
	}

}
