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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Default event loader for the Gantt Chart.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EventLoader implements IEventLoader {

	// logger
	private static final Logger logger = LoggerFactory.getLogger(EventLoader.class);

	// constants
	private final int EVENTS_PER_QUERY = 100000;

	// set by the user
	private Trace fTrace = null;
	private LoaderQueue<Event> fQueue = null;

	// current visualized trace data
	private TraceDBObject fTraceDB = null;
	private EventQuery fQuery = null;
	private TimeInterval fTimeInterval;

	@Override
	public void setTrace(Trace trace) {
		if (fTrace != trace) {
			clean();
			fTrace = trace;
		}
	}

	@Override
	public void setQueue(LoaderQueue<Event> queue) {
		fQueue = queue;
	}

	@Override
	public void loadWindow(long start, long end, IProgressMonitor monitor) {

		try {
			Assert.isNotNull(fTrace, "Null trace in event loader");
			Assert.isNotNull(fQueue, "Null queue in event loader");
			start = Math.max(fTrace.getMinTimestamp(), start);
			end = Math.min(fTrace.getMaxTimestamp(), end);

			fTimeInterval = new TimeInterval(Long.MAX_VALUE, Long.MIN_VALUE);

			// compute interval duration
			long duration = fTrace.getMaxTimestamp() - fTrace.getMinTimestamp();
			Assert.isTrue(duration != 0, "The trace duration cannot be 0");
			double density = ((double) fTrace.getNumberOfEvents()) / duration;
			Assert.isTrue(density != 0, "The density cannot be 0");
			long intervalDuration = (long) (EVENTS_PER_QUERY / density);
			int totalWork = (int) ((double) duration / intervalDuration);

			// read the time window, interval by interval
			monitor.beginTask("Loading Event Table", totalWork);
			int oldWorked = 0;

			/*
			 * XXX Current solution (may change).
			 * The table only loads the events with timestamp contained in the interval.
			 * So there is no difference between the first interval and the other.
			 */
			//boolean first = true;
			long t0 = start;
			while (t0 <= end) {
				// check if cancelled
				if (checkCancel(monitor)) {
					return;
				}

				// load interval
				long t1 = Math.min(end, t0 + intervalDuration);
				//List<Event> events = loadInterval(first, (t1 >= end), t0, t1, monitor);
				List<Event> events = loadInterval(false, (t1 >= end), t0, t1, monitor);
				debug(events);
				if (checkCancel(monitor)) {
					return;
				}

				// check for empty regions
				if (events.size() == 0) {
					long oldt0 = t0;
					t0 = getNextTimestampAfter(t1);
					logger.debug("saved " + ((t0 - oldt0) / intervalDuration) + " queries.");
					continue;
				}

				fTimeInterval.startTimestamp = Math.min(fTimeInterval.startTimestamp, t0);
				fTimeInterval.endTimestamp = Math.max(fTimeInterval.endTimestamp, t1);
				fQueue.push(events, new TimeInterval(fTimeInterval));

				// update progress monitor
				int worked = (int) ((double) (t0 - start) / intervalDuration);
				monitor.worked(Math.max(0, worked - oldWorked));
				oldWorked = worked;
				t0 = t1 + 1;
				//first = false;
			}

			fQueue.setComplete();

		} finally {
			if (!fQueue.isStop() && !fQueue.isComplete()) {
				// something went wrong, respect the queue contract anyway
				fQueue.setStop();
			}
			monitor.done();
			clean();
		}
	}

	private List<Event> loadInterval(boolean first, boolean last, long t0, long t1,
			IProgressMonitor monitor) {
		
		ComparisonOperation endComp = (last) ? ComparisonOperation.LE : ComparisonOperation.LT;
		
		try {
			EventQuery query = getQueryObject();
			query.clear();
			if (first && t0 != fTrace.getMinTimestamp()) {
				// first interval
				LogicalCondition or = new LogicalCondition(LogicalOperation.OR);
				// punctual events and variables: t0 <= t < t1 (last interval: t0 <= t <= t1)
				LogicalCondition andPunct = new LogicalCondition(LogicalOperation.AND);
				andPunct.addCondition(new SimpleCondition("CATEGORY", ComparisonOperation.IN,
						"(0, 3)"));
				andPunct.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.GE,
						String.valueOf(t0)));
				andPunct.addCondition(new SimpleCondition("TIMESTAMP", endComp,
						String.valueOf(t1)));
				// states and links: start < t1 and end >= t0 (last interval: start <= t1 and end >= t0)
				LogicalCondition andDuration = new LogicalCondition(LogicalOperation.AND);
				andDuration.addCondition(new SimpleCondition("CATEGORY", ComparisonOperation.IN,
						"(1, 2)"));
				andDuration.addCondition(new SimpleCondition("TIMESTAMP", endComp,
						String.valueOf(t1)));
				andDuration.addCondition(new SimpleCondition("LPAR", ComparisonOperation.GE, String
						.valueOf(t0)));
				or.addCondition(andPunct);
				or.addCondition(andDuration);
				query.setElementWhere(or);
			} else {
				// other intervals
				// all events: t0 <= t < t1 (last interval: t0 <= t <= t1)
				LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
				and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.GE, String
						.valueOf(t0)));
				ComparisonOperation end = (last) ? ComparisonOperation.LE : ComparisonOperation.LT;
				and.addCondition(new SimpleCondition("TIMESTAMP", end, String.valueOf(t1)));
				query.setElementWhere(and);
			}
			query.setOrderBy("TIMESTAMP", OrderBy.ASC);
			return fQuery.getList();
		} catch (SoCTraceException e) {
			e.printStackTrace();
			fQueue.setStop();
		}
		return new ArrayList<>();
	}

	private boolean checkCancel(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			fQueue.setStop();
			return true;
		}
		return false;
	}

	private long getNextTimestampAfter(long end) {
		long next = end + 1;
		try {
			Statement stm = getTraceDB().getConnection().createStatement();
			DeltaManager dm = new DeltaManager();
			dm.start();
			ResultSet rs = stm.executeQuery("SELECT MIN(TIMESTAMP) FROM EVENT WHERE TIMESTAMP > "
					+ end);
			logger.debug(dm.endMessage("exec query"));
			while (rs.next()) {
				next = rs.getLong(1);
			}
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
		return Math.max(next, end + 1);
	}

	private void clean() {
		fTrace = null;
		fQueue = null;
		fQuery = null;
		DBObject.finalClose(fTraceDB);
	}

	private EventQuery getQueryObject() throws SoCTraceException {
		if (fQuery == null) {
			fQuery = new EventQuery(getTraceDB());
		}
		return fQuery;
	}

	private TraceDBObject getTraceDB() throws SoCTraceException {
		if (fTraceDB == null) {
			Assert.isNotNull(fTrace, "Null trace in event loader");
			fTraceDB = TraceDBObject.openNewIstance(fTrace.getDbName());
		}
		return fTraceDB;
	}

	private void debug(List<Event> events) {
		for (Event event : events) {
			logger.trace(event.toString());
		}
	}

}
