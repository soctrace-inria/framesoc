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
package fr.inria.soctrace.framesoc.ui.gantt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Event loader supporting the Paje algorithm for gantt data structure management.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TimeSliceEntityLoader {

	/**
	 * Logger
	 */
	private static Logger logger = LoggerFactory.getLogger(TimeSliceEntityLoader.class);

	protected Trace trace;		
	private Map<Long, EventProducer> eps = new HashMap<>();
	private PunctualEvents events = new PunctualEvents();
	private TimeSlices states = new TimeSlices();
	private TimeSlices links = new TimeSlices();

	// TODO Variables
	// private Variables variables = new Variables();

	public void setTrace(Trace trace) {
		this.trace = trace;
	}

	public void loadTrace() throws SoCTraceException {

		logger.debug("loading trace...");
		eps.clear();
		events.clear();
		links.clear();
		states.clear();

		DeltaManager dm = new DeltaManager();
		dm.start();

		TraceDBObject traceDB = null;
		try {
			traceDB = TraceDBObject.openNewInstance(trace.getDbName());

			EventProducerQuery epq = new EventProducerQuery(traceDB);
			List<EventProducer> epl = epq.getList();
			for (EventProducer ep: epl) {
				eps.put(ep.getId(), ep);
			}

			EventQuery query = new EventQuery(traceDB);
			query.setLoadParameters(false);
			List<Event> elist = query.getList();

			long min = traceDB.getMinTimestamp();
			long max = traceDB.getMaxTimestamp();
			long eNum = trace.getNumberOfEvents();
			long slices = Math.max(eNum/30, 1) + 1; // 30 has been taken from Benhur's thesis
			long sliceSize = Math.max((max-min)/slices, 1);

			logger.debug("Min timestamp {}", min);
			logger.debug("Max timestamp {}", max);
			logger.debug("Event number {}", eNum);
			logger.debug("Number of slices {}", slices);
			logger.debug("Slice size {}", sliceSize);

			for (int i=0; i<slices; i++) {
				states.addTimeSlice(new TimeSlice(min + i*sliceSize));
				links.addTimeSlice(new TimeSlice(min + i*sliceSize));
			}

			for (Event e: elist) {
				logger.info(e.toString());
				switch(e.getCategory()) {
				case EventCategory.PUNCTUAL_EVENT:
					events.addEvent(e);
					break;
				case EventCategory.LINK:
					links.addEvent(e);
					break;
				case EventCategory.STATE:
					states.addEvent(e);
					break;
				case EventCategory.VARIABLE:
					break; // ignored now
				default:
					break; // ignored now
				}
			}
			
		} finally {
			DBObject.finalClose(traceDB);
		}

		logger.debug("States: {}", states.size());
		logger.debug("Links: {}", links.size());
		logger.debug("Events: {}", events.size());
		logger.debug(dm.endMessage("loading all trace in structures"));

	}

	public List<Event> loadPage(int minPage) throws SoCTraceException {
		throw new SoCTraceException("NOT IMPLEMENTED");
	}

	public PunctualEvents getPunctualEvents() {
		return events;
	}

	public TimeSlices getStates() {
		return states;
	}

	public TimeSlices getLinks() {
		return links;
	}

	// encapsulate in class Producers
	public Map<Long, EventProducer> getProducers() {
		return eps;
	}

}
