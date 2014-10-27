/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.piechart.loaders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.piechart.model.PieChartLoaderMap;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventTypeQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Base abstract class for Pie Chart loaders dealing with duration (e.g., state
 * duration).
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class DurationPieChartLoader extends EventPieChartLoader {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(DurationPieChartLoader.class);

	/**
	 * Event type map for the duration category managed: id -> name
	 */
	private Map<Integer, String> etMap;

	/**
	 * Utility class for a pending duration.
	 * 
	 * A pending duration is a portion of an entity with a duration (e.g.,
	 * state) that has been already read from DB, but not completely added yet
	 * to current statistics. For example consider the following situation: we
	 * have read a state starting at 0 and ending at 5, but doRequest() has been
	 * called only for the interval (1,3). This generates two pending durations:
	 * (0,1) and (3,5).
	 */
	private static class PendingDuration {

		public int typeId;
		public long start;
		public long end;

		public PendingDuration(int type) {
			typeId = type;
		}

		@Override
		public String toString() {
			return "PendingDuration [typeId=" + typeId + ", start=" + start + ", end=" + end + "]";
		}
	}

	/**
	 * List of pending durations for the current load operation.
	 */
	private List<PendingDuration> pending;

	/**
	 * Get the event category to use.
	 * 
	 * It must be a category that implies the concept of duration, i.e., states
	 * or links. The category is returned as one of the integer constant in
	 * <code>EventCategory</code>.
	 * 
	 * @return the duration category to use
	 */
	protected abstract int getDurationCategory();

	@Override
	public FramesocColor getColor(String name) {
		if (name.equals(AGGREGATED_LABEL))
			return AGGREGATED_COLOR;
		FramesocColor color = FramesocColorManager.getInstance().getEventTypeColor(name);
		FramesocColorManager.getInstance().saveEventTypeColors();
		return color;
	}

	@Override
	public void load(Trace trace, TimeInterval requestedInterval, PieChartLoaderMap map,
			IProgressMonitor monitor) {

		if (trace == null || requestedInterval == null || map == null || monitor == null)
			throw new NullPointerException();

		TraceDBObject traceDB = null;

		try {

			// reset pending
			pending = new LinkedList<>();

			DeltaManager dm = new DeltaManager();
			dm.start();
			traceDB = new TraceDBObject(trace.getDbName(), DBMode.DB_OPEN);

			// lazily load the type map
			loadEventTypeMap(traceDB);

			// compute interval duration
			long duration = trace.getMaxTimestamp() - trace.getMinTimestamp();
			Assert.isTrue(duration != 0, "The trace duration cannot be 0");
			double density = ((double) trace.getNumberOfEvents()) / duration;
			Assert.isTrue(density != 0, "The density cannot be 0");
			long intervalDuration = (long) (EVENTS_PER_QUERY / density);
			Assert.isTrue(intervalDuration > 0, "The interval duration must be positive");

			Map<String, Double> values = new HashMap<>();

			long t0 = requestedInterval.startTimestamp;
			TimeInterval loadedInterval = new TimeInterval(t0, 0);

			// if we are loading the metric from the beginning of the trace, the
			// first interval is
			// not different from the others
			boolean first = (t0 != trace.getMinTimestamp());

			while (t0 < requestedInterval.endTimestamp) {

				if (checkCancel(map, monitor)) {
					return;
				}

				// load interval
				long t1 = Math.min(requestedInterval.endTimestamp, t0 + intervalDuration);
				boolean last = (t1 >= requestedInterval.endTimestamp);
				int results = doRequest(t0, t1, first, last, values, traceDB, monitor);
				first = false;
				logger.debug("Loaded: " + results);

				if (checkCancel(map, monitor)) {
					return;
				}

				// check for empty regions
				if (results == 0 && !last) {
					t0 = getNextTimestampStartingFrom(traceDB, t1);
					// if we skip and interval, manage the pending durations for
					// the skipped parts
					managePendingDurations(t1, t0, values);
					logger.debug("saved " + ((t0 - t1) / intervalDuration) + " queries.");
					continue;
				}

				loadedInterval.endTimestamp = t1;
				map.setSnapshot(values, loadedInterval);

				t0 = t1;
			}

			map.setComplete();
			logger.debug(dm.endMessage("Prepared Pie Chart dataset"));

		} catch (SoCTraceException e) {
			e.printStackTrace();
			map.setStop();
		} finally {
			if (!map.isStop() && !map.isComplete()) {
				// something went wrong, respect the map contract anyway
				map.setStop();
			}
			DBObject.finalClose(traceDB);
		}

	}

	@Override
	protected int doRequest(long t0, long t1, boolean first, boolean last,
			Map<String, Double> values, TraceDBObject traceDB, IProgressMonitor monitor)
			throws SoCTraceException {

		logger.debug("do request: {}, {}. First: {}, Last: {}.", t0, t1, first, last);
		logger.debug("before managing pending");
		logger.debug(pending.toString());

		// manage pending
		managePendingDurations(t0, t1, values);

		logger.debug("after managing pending");
		logger.debug(pending.toString());
		// execute query
		ComparisonOperation lastComp = last ? ComparisonOperation.LE : ComparisonOperation.LT;
		StringBuilder query = new StringBuilder("SELECT EVENT_TYPE_ID, TIMESTAMP, LPAR FROM EVENT");
		query.append(" WHERE ");
		query.append("CATEGORY = ").append(getDurationCategory());
		query.append(" AND ");
		if (first) {
			// start <(=) t1 && end >= t0
			query.append("TIMESTAMP " + lastComp + t1 + " AND LPAR >= " + t0);
		} else {
			query.append("TIMESTAMP >= " + t0 + " AND TIMESTAMP " + lastComp + " " + t1);
		}
		String queryString = query.toString();
		logger.debug(queryString);
		int results = 0;
		try {
			Statement stm = traceDB.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(queryString);
			while (rs.next()) {
				if (monitor.isCanceled())
					return results;
				PendingDuration d = new PendingDuration(rs.getInt(1));
				d.start = rs.getLong(2);
				d.end = rs.getLong(3);
				managePendingDuration(d, t0, t1, values, pending);
				results++;
			}
			stm.close();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}

		logger.debug("end");
		logger.debug(pending.toString());
		logger.debug(values.toString());

		return results;
	}

	/**
	 * Manage the pending duration for the given time interval, as explained in
	 * {@link #managePendingDuration(PendingDuration, long, long, Map, List)}.
	 * 
	 * @param t0
	 *            start timestamp
	 * @param t1
	 *            end timestamp
	 * @param values
	 *            pie chart values
	 */
	private void managePendingDurations(long t0, long t1, Map<String, Double> values) {
		List<PendingDuration> newPending = new ArrayList<>();
		for (Iterator<PendingDuration> it = pending.iterator(); it.hasNext();) {
			if (managePendingDuration(it.next(), t0, t1, values, newPending)) {
				it.remove();
			}
		}
		if (!newPending.isEmpty()) {
			pending.addAll(newPending);
		}
	}

	/**
	 * Process a pending duration in a given time interval.
	 * 
	 * This means that the part of the passed pending duration intersecting the
	 * interval will be added to the pie chart values, while the remaining parts
	 * of the passed pending duration (if any) will be added to the list of new
	 * pending durations.
	 * 
	 * @param d
	 *            pending duration to process
	 * @param t0
	 *            start timestamp
	 * @param t1
	 *            end timestamp
	 * @param values
	 *            pie chart values
	 * @param newPending
	 *            list containing the new pending durations
	 * @return <true> if the passed pending duration intersects the passed
	 *         interval and one or two new pending durations have been added to
	 *         the list of new pending durations
	 */
	private boolean managePendingDuration(PendingDuration d, long t0, long t1,
			Map<String, Double> values, List<PendingDuration> newPending) {
		String etName = etMap.get(d.typeId);
		boolean modified = false;
		if (d.start < t0 && d.end > t0) {
			// d intersects t0: cut the part before
			PendingDuration before = new PendingDuration(d.typeId);
			before.start = d.start;
			before.end = t0;
			newPending.add(before);
			d.start = t0;
			modified = true;
		}
		if (d.start < t1 && d.end > t1) {
			// d intersects t1: cut the part after
			PendingDuration after = new PendingDuration(d.typeId);
			after.start = t1;
			after.end = d.end;
			newPending.add(after);
			d.end = t1;
			modified = true;
		}
		if (d.start >= t0 && d.end <= t1) {
			// the remaining part is contained in [t0, t1]
			Double remainingDuration = ((Long) (d.end - d.start)).doubleValue();
			if (remainingDuration > 0) {
				modified = true;
				if (!values.containsKey(etName))
					values.put(etName, remainingDuration);
				else
					values.put(etName, remainingDuration + values.get(etName));
			}
		}
		return modified;
	}

	/**
	 * Lazily load the event type map for the duration category.
	 * 
	 * @param traceDB
	 *            trace DB object
	 * @throws SoCTraceException
	 */
	private void loadEventTypeMap(TraceDBObject traceDB) throws SoCTraceException {
		// load all types (do it only once)
		if (etMap == null) {
			EventTypeQuery etq = new EventTypeQuery(traceDB);
			etq.setElementWhere(new SimpleCondition("CATEGORY", ComparisonOperation.EQ, String
					.valueOf(getDurationCategory())));
			List<EventType> etl = etq.getList();
			etMap = new HashMap<>();
			for (EventType et : etl) {
				etMap.put(et.getId(), et.getName());
			}
		}
	}

}
