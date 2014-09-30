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

package fr.inria.soctrace.framesoc.ui.gantt.defaults;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.gantt.model.IEventLoader;
import fr.inria.soctrace.framesoc.ui.gantt.model.LoaderQueue;
import fr.inria.soctrace.framesoc.ui.gantt.model.ReducedEvent;
import fr.inria.soctrace.framesoc.ui.gantt.model.TimeInterval;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.EventTypeQuery;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
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
	private LoaderQueue<ReducedEvent> fQueue = null;

	// current visualized trace data
	private TraceDBObject fTraceDB = null;
	private Map<Integer, EventProducer> fProducers = null;
	private boolean fProducersLoaded = false;
	private Map<Integer, EventType> fTypes = null;
	private boolean fTypesLoaded = false;
	private TimeInterval fTimeInterval;
	private long fLatestStart;

	@Override
	public Map<Integer, EventProducer> getProducers() {
		if (fProducersLoaded)
			return fProducers;
		fProducers = new HashMap<Integer, EventProducer>();
		try {
			EventProducerQuery epq = new EventProducerQuery(getTraceDB());
			List<EventProducer> epl = epq.getList();
			for (EventProducer ep : epl) {
				fProducers.put(ep.getId(), ep);
			}
			fProducersLoaded = true;
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
		return fProducers;
	}

	@Override
	public Map<Integer, EventType> getTypes() {
		if (fTypesLoaded)
			return fTypes;
		fTypes = new HashMap<Integer, EventType>();
		try {
			EventTypeQuery etq = new EventTypeQuery(getTraceDB());
			List<EventType> etl = etq.getList();
			for (EventType et : etl) {
				fTypes.put(et.getId(), et);
			}
			fTypesLoaded = true;
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
		return fTypes;
	}

	@Override
	public void setTrace(Trace trace) {
		if (fTrace != trace) {
			clean();
			fTrace = trace;
		}
	}

	@Override
	public void setQueue(LoaderQueue<ReducedEvent> queue) {
		fQueue = queue;
	}

	@Override
	public void release() {
		fTrace = null;
		fQueue = null;
		clean();
	}

	public boolean checkCancel(IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			fQueue.setStop();
			return true;
		}
		return false;
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
			monitor.beginTask("Loading Gantt Chart", totalWork);
			int oldWorked = 0;

			boolean first = true;
			long t0 = start;
			long tstart=start;
			while (t0 <= end) {
				// check if cancelled
				if (checkCancel(monitor)) {
					return;
				}
				// load interval
				long t1 = Math.min(end, t0 + intervalDuration);
				List<ReducedEvent> events = loadInterval(false, t0, t1, monitor);
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

				// update progress monitor
				int worked = (int) ((double) (fLatestStart - start) / (intervalDuration + 1));
				monitor.worked(Math.max(0, worked - oldWorked));
				oldWorked = worked;
				t0 = t1 + 1;
				first = false;

				fQueue.push(events, new TimeInterval(fTimeInterval));
			}
			
			fQueue.setComplete();
			
		} finally {
			if (!fQueue.isStop() && !fQueue.isComplete()) {
				// something went wrong, respect the queue contract anyway
				fQueue.setStop();
			}
			monitor.done();
		}
	}

	private List<ReducedEvent> loadInterval(boolean first, long t0, long t1,
			IProgressMonitor monitor) {
		List<ReducedEvent> events = new LinkedList<>();
		try {
			Statement stm = getTraceDB().getConnection().createStatement();
			DeltaManager dm = new DeltaManager();
			dm.start();
			ResultSet rs;
			if (first){
				rs = stm.executeQuery(getQuery(t0, t1));
			}
			else{
				rs = stm.executeQuery(getFirstQuery(t0,t1));
			}
			logger.debug(dm.endMessage("exec query"));
			dm.start();
			while (rs.next()) {
				ReducedEvent ev = new ReducedEvent(rs);
				events.add(ev);
				if (ev.timestamp > fLatestStart)
					fLatestStart = ev.timestamp;
				if (fTimeInterval.startTimestamp > ev.timestamp)
					fTimeInterval.startTimestamp = ev.timestamp;
				long end = ((ev.category == 0) ? ev.timestamp : ev.endTimestamp);
				if (fTimeInterval.endTimestamp < end)
					fTimeInterval.endTimestamp = end;
				if (monitor.isCanceled()) {
					fQueue.setStop();
					break;
				}
			}
			logger.debug(dm.endMessage("reduced event creation"));
			rs.close();
			stm.close();
		} catch (SQLException e) {
			e.printStackTrace();
			fQueue.setStop();
		} catch (SoCTraceException e) {
			e.printStackTrace();
			fQueue.setStop();
		}
		return events;
	}

	private long getNextTimestampAfter(long end) {
		long next = end;
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
		return next;
	}

	private String getQuery(long start, long end) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT " + ReducedEvent.SELECT_COLUMNS + " FROM " + FramesocTable.EVENT
				+ " WHERE ");
			sb.append(" (TIMESTAMP >= " + start + " AND TIMESTAMP < " + end + ") ");
		logger.debug("Query: " + sb.toString());
		return sb.toString();
	}
	
	private String getFirstQuery(long start, long end) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT " + ReducedEvent.SELECT_COLUMNS + " FROM " + FramesocTable.EVENT
				+ " WHERE ");
			sb.append(" (CATEGORY IN (1,2) AND (TIMESTAMP<" + start + " AND LPAR>" + start + ")) ");
		logger.debug("Query: " + sb.toString());
		return sb.toString();
	}

	private void clean() {
		fProducersLoaded = false;
		fTypesLoaded = false;
		fProducers = new HashMap<Integer, EventProducer>();
		fTypes = new HashMap<Integer, EventType>();
		fLatestStart = Long.MIN_VALUE;
		DBObject.finalClose(fTraceDB);
	}

	private TraceDBObject getTraceDB() throws SoCTraceException {
		if (fTraceDB == null) {
			Assert.isNotNull(fTrace, "Null trace in event loader");
			fTraceDB = TraceDBObject.openNewIstance(fTrace.getDbName());
		}
		return fTraceDB;
	}

	private void debug(List<ReducedEvent> events) {
		for (ReducedEvent event : events) {
			logger.trace(event.toString());
		}
	}

}
