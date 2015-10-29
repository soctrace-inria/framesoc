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

import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;

import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import fr.inria.linuxtools.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

/**
 * Default implementation of time graph row.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class GanttEntry extends TimeGraphEntry {
	
	// Id of the event producer corresponding to the one
	private long eventProducerID = -1l;
	
	private boolean producingEvent = false;
	
	public GanttEntry(String name, long eventProducerID) {
		super(name, Long.MAX_VALUE, Long.MIN_VALUE);
		this.eventProducerID = eventProducerID;
	}

	public void debug(Logger logger) {
		debugEntry(logger, this, "");
	}
	
	public static void debug(Logger logger, List<TimeGraphEntry> entries) {
		for(TimeGraphEntry entry: entries) {
			debugEntry(logger, entry, "");
		}
	}
	
	private static void debugEntry(Logger logger, TimeGraphEntry entry, String space){
		logger.debug(space + entry.toString() + ": " + entry.getStartTime() + ", " + entry.getEndTime());
		Iterator<ITimeEvent> it = entry.getTimeEventsIterator();
		while (it.hasNext()) {
			logger.debug(space + "<ev> " + it.next());
		}
		for (TimeGraphEntry e: entry.getChildren()) {
			debugEntry(logger, e, space + " ");
		}
	}

	public long getEventProducerID() {
		return eventProducerID;
	}

	public void setEventProducerID(long eventProducerID) {
		this.eventProducerID = eventProducerID;
	}

	public boolean isProducingEvent() {
		return producingEvent;
	}

	public void setProducingEvent(boolean producingEvent) {
		this.producingEvent = producingEvent;
	}

}
