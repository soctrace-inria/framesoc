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
package fr.inria.soctrace.framesoc.ui.gantt.loaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ILinkEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.TimeLinkEvent;
import fr.inria.soctrace.framesoc.ui.gantt.model.IEventDrawer;
import fr.inria.soctrace.framesoc.ui.gantt.model.ReducedEvent;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Default implementation of the Gantt Chart event drawer.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class CpuEventDrawer implements IEventDrawer {

	/**
	 * Interface for reduced event drawers
	 * 
	 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
	 */
	private interface IReducedEventDrawer {

		/**
		 * Draw a reduced event
		 * 
		 * @param e
		 *            the reduced event to draw
		 */
		void draw(ReducedEvent rs);
	}

	// logger
	private final static Logger logger = LoggerFactory.getLogger(CpuEventDrawer.class);

	// drawers
	private Map<Integer, IReducedEventDrawer> drawers = new HashMap<Integer, IReducedEventDrawer>();

	// current visualized trace data
	private Map<Integer, EventProducer> producers; // epid, ep
	private Map<Integer, Map<Integer, GanttEntry>> rows; // cpu, (epid, row)
	private Map<Integer, GanttEntry> main; // cpu, row
	private ArrayList<TimeGraphEntry> mainItems;
	private ArrayList<ILinkEvent> linkList;

	// current time interval
	private ArrayList<TimeGraphEntry> newRoots;
	private boolean needRefresh;

	// stats
	private int punctualEvents;
	private int states;
	private int links;

	/**
	 * Constructor.
	 */
	public CpuEventDrawer() {
		drawers.put(EventCategory.PUNCTUAL_EVENT, new PunctualEventDrawer());
		drawers.put(EventCategory.STATE, new StateDrawer());
		drawers.put(EventCategory.LINK, new LinkDrawer());
		drawers.put(EventCategory.VARIABLE, new VariableDrawer());

		rows = new HashMap<>();
		main = new HashMap<>();
		mainItems = new ArrayList<>();
		linkList = new ArrayList<>();
		newRoots = new ArrayList<>();
	}

	@Override
	public void setProducers(Map<Integer, EventProducer> producers) {
		this.producers = producers;
	}
	
	@Override
	public ArrayList<TimeGraphEntry> getNewRootEntries() {
		return newRoots;
	}

	@Override
	public ArrayList<ILinkEvent> getLinks() {
		return linkList;
	}

	@Override
	public void release() {
		mainItems.clear();
		linkList.clear();
		rows.clear();
		main.clear();
		punctualEvents = 0;
		states = 0;
		links = 0;
	}

	@Override
	public TimeInterval draw(List<ReducedEvent> events) {
		Assert.isNotNull(producers, "Null producers in event drawer");
		newRoots = new ArrayList<>();
		needRefresh = false;

		// draw states, links and events
		DeltaManager dm = new DeltaManager();
		dm.start();
		logger.debug("----------------------------------------");
		logger.debug("Prepare Gantt Model");
		TimeInterval interval = new TimeInterval(Long.MAX_VALUE, Long.MIN_VALUE);
		for (ReducedEvent ev : events) {
			drawers.get(ev.category).draw(ev);
			if (ev.timestamp < interval.startTimestamp)
				interval.startTimestamp = ev.timestamp;
			if (ev.timestamp > interval.endTimestamp) 
				interval.endTimestamp = ev.timestamp;
		}
		logger.debug(dm.endMessage("End preparing Gantt model"));
		logger.debug("----------------------------------------");

		// update entries start/end times
		for (TimeGraphEntry entry: mainItems) {
			getStartTime(entry);
			getEndTime(entry);
		}

		debug();
		
		return interval;
	}

	@Override
	public boolean needRefresh() {
		return needRefresh;
	}
	
	private void debug() {
		logger.debug("Punctual Events: " + punctualEvents);
		logger.debug("States: " + states);
		logger.debug("Link: " + links);
//		logger.debug("Entries");
//		GanttEntry.debug(logger, mainItems);
//		logger.debug("Links");
//		for (ILinkEvent e : linkList) {
//			logger.debug(e.toString());
//		}
	}

	private GanttEntry getProducerRow(int cpu, EventProducer ep) {
		// get the map containing all the rows for this CPU
		if (!rows.containsKey(cpu)) {
			rows.put(cpu, new HashMap<Integer, GanttEntry>());
			main.put(cpu, new GanttEntry("CPU " + cpu, -1));
			mainItems.add(main.get(cpu));
			newRoots.add(main.get(cpu));
			needRefresh = true;
		}
		Map<Integer, GanttEntry> cpuMap = rows.get(cpu);

		// get the row for the given producer
		if (!cpuMap.containsKey(ep.getId()))
			cpuMap.put(ep.getId(),
					getNewEventProducerRow(ep, producers, cpuMap, main.get(cpu)));
		return cpuMap.get(ep.getId());
	}

	private GanttEntry getNewEventProducerRow(EventProducer ep, Map<Integer, EventProducer> eps,
			Map<Integer, GanttEntry> cpuRows, GanttEntry cpuRow) {

		logger.trace("Creating event producer row " + ep.getId() + ", parent " + ep.getParentId());

		GanttEntry parentRow = cpuRow;
		if (ep.getParentId() != EventProducer.NO_PARENT_ID) {
			// there's a parent
			if (cpuRows.containsKey(ep.getParentId()))
				// there is already its row
				parentRow = cpuRows.get(ep.getParentId());
			else {
				parentRow = getNewEventProducerRow(eps.get(ep.getParentId()), eps, cpuRows, cpuRow);
				cpuRows.put(ep.getParentId(), parentRow);
			}
		}
		GanttEntry entry = new GanttEntry(ep.getName(), ep.getId());
		parentRow.addChild(entry);
		needRefresh = true;
		return entry;
	}

	private long getStartTime(TimeGraphEntry entry) {
		// leaf
		if (!entry.hasChildren())
			return entry.getStartTime();
		
		// return the min among its start and its sons start
		long st = entry.getStartTime();
		for (TimeGraphEntry e: entry.getChildren()) {
			st = Math.min(st, getStartTime(e));
		}
		entry.updateStartTime(st);
		return entry.getStartTime();
	}
	
	private long getEndTime(TimeGraphEntry entry) {
		// leaf
		if (!entry.hasChildren())
			return entry.getEndTime();
		
		// return the max among its end and its sons end
		long st = entry.getEndTime();
		for (TimeGraphEntry e: entry.getChildren()) {
			st = Math.max(st, getEndTime(e));
		}
		entry.updateEndTime(st);
		return entry.getEndTime();
	}

	/*
	 * D R A W E R S
	 */

	private class PunctualEventDrawer implements IReducedEventDrawer {
		@Override
		public void draw(ReducedEvent e) {
			punctualEvents++;
			GanttEntry producerRow = getProducerRow(e.cpu, producers.get(e.producerId));
			producerRow.addEvent(new GanttEvent(producerRow, e.timestamp, 0, e.typeId));
			logger.trace("punctual: {}", e.timestamp);
		}
	}

	private class StateDrawer implements IReducedEventDrawer {
		@Override
		public void draw(ReducedEvent e) {
			states++;
			GanttEntry producerRow = getProducerRow(e.cpu, producers.get(e.producerId));
			long duration = e.endTimestamp - e.timestamp;
			producerRow.addEvent(new GanttEvent(producerRow, e.timestamp, duration, e.typeId));
			logger.trace("state: {} {}", e.timestamp, e.endTimestamp);
		}
	}

	private class LinkDrawer implements IReducedEventDrawer {
		@Override
		public void draw(ReducedEvent e) {
			links++;
			GanttEntry start = getProducerRow(e.cpu, producers.get(e.producerId));
			GanttEntry end = getProducerRow(e.cpu, producers.get(e.endProducerId));
			linkList.add(new TimeLinkEvent(start, end, e.timestamp, e.endTimestamp - e.timestamp,
					e.typeId));
			logger.trace("link: {} {}", e.timestamp, e.endTimestamp);
		}
	}

	private class VariableDrawer implements IReducedEventDrawer {
		@Override
		public void draw(ReducedEvent e) {
			// NOP
		}
	}
}
