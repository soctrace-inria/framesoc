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
package fr.inria.soctrace.framesoc.ui.eventtable.loader;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.eventtable.model.EventTableRow;
import fr.inria.soctrace.framesoc.ui.model.LoadDescriptor;
import fr.inria.soctrace.framesoc.ui.model.LoadDescriptor.LoadStatus;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;


/**
 * Load the data of the model for the Event Table view.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class EventTableLoader {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(EventTableLoader.class);

	/**
	 * Constant for no limit in queries
	 */
	public final static int NO_LIMIT = -1;

	// current visualized trace data
	private TraceDBObject traceDB = null;
	private Trace oldTrace = null;

	// Current visualized window data
	private List<EventTableRow> events;
	private long startTs;
	private long endTs;

	public EventTableLoader() {
		super();
		this.events = new ArrayList<EventTableRow>();
	}


	/**
	 * Build the JFace table model for the given trace
	 * and the given time window.
	 * 
	 * @param trace trace object containing trace metadata
	 * @param startTimestamp start timestamp
	 * @param endTimestamp end timestamp
	 * @param limit max number of events to load in the window (use the NO_LIMIT constant for no limit)
	 * @param monitor progress monitor
	 * @return a load descriptor, containing relevant information about loading operation
	 * @throws SoCTraceException 
	 */
	public LoadDescriptor loadTimeWindow(Trace trace, long startTimestamp, long endTimestamp, int limit, IProgressMonitor monitor) throws SoCTraceException {

		logger.debug("loadTimeWindow()");

		LoadDescriptor des = new LoadDescriptor();
		des.setStatus(LoadStatus.LOAD_COMPLETE);
		des.setActualStartTimestamp(startTimestamp);
		des.setActualEndTimestamp(endTimestamp);
		des.setMessage("Time window loaded");

		/* Preliminary checks */
		if (!trace.equals(oldTrace)) {
			oldTrace = trace;
		} else {
			if (startTimestamp == startTs && endTimestamp == endTs) {
				des.setStatus(LoadStatus.LOAD_UNCHANGED);
				return des; // nothing to do
			}		
		}
		if (end(monitor, des)) return des;

		try {
			
			traceDB = TraceDBObject.openNewIstance(trace.getDbName());
			startTs = startTimestamp;
			endTs = endTimestamp;
			events.clear();

			/* Prepare the Event table rows */

			DeltaManager dm = new DeltaManager();
			dm.start();
			logger.debug("----------------------------------------");
			logger.debug("Prepare Table Model");

			List<Event> elist = null;
			elist = getWindow(startTimestamp, endTimestamp, limit);
			if (end(monitor, des)) return des;
			logger.debug("Events to draw: " + elist.size());
			dm.start();
			for (Event e: elist) {
				events.add(new EventTableRow(e));
				if (end(monitor, des)) return des;
			}
			logger.debug(dm.endMessage("End preparing Table model"));
			logger.debug("----------------------------------------");		
			return des;
			
		} finally {
			DBObject.finalClose(traceDB);	
		}

	}

	private List<Event> getWindow(long startTimestamp, long endTimestamp, int limit) throws SoCTraceException {		
		EventQuery eq = new EventQuery(traceDB);	
		eq.setElementWhere(new SimpleCondition("TIMESTAMP", ComparisonOperation.BETWEEN, 
				startTimestamp + " AND " + endTimestamp));
		eq.setOrderBy("TIMESTAMP", OrderBy.ASC);
		if ( limit != NO_LIMIT )
			eq.setLimit(limit);		
		List<Event> elist = eq.getList(); 
		return elist;
	}

	private boolean end(IProgressMonitor monitor, LoadDescriptor des) {
		if (monitor.isCanceled()) {
			// reset status
			startTs = -1;
			endTs = -1;
			des.setMessage("Loading cancelled");
			des.setStatus(LoadStatus.LOAD_CANCELLED);
			return true;
		}
		return false;
	}

	public void dispose() {
		events.clear();
	}

	public List<EventTableRow> getEvents() {
		return events;
	}

	/**
	 * Get the current trace (may be null).
	 * @return the current trace
	 */
	public Trace getCurrentTrace() {
		return oldTrace;
	}

}
