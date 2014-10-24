/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.piechart.loaders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventTypeQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
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
	 * A pending duration is a portion of an entity with a duration (e.g., state) that has been
	 * already read from DB, but not completely added yet to current statistics. For example
	 * consider the following situation: we have read a state starting at 0 and ending at 5, but
	 * doRequest() has been called only for the interval (1,3). This generates two pending
	 * durations: (0,1) and (3,5).
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
	private List<PendingDuration> pending = new ArrayList<>();

	@Override
	public FramesocColor getColor(String name) {
		if (name.equals(AGGREGATED_LABEL))
			return AGGREGATED_COLOR;
		FramesocColor color = FramesocColorManager.getInstance().getEventTypeColor(name);
		FramesocColorManager.getInstance().saveEventTypeColors();
		return color;
	}

	@Override
	protected int doRequest(long t0, long t1, boolean first, boolean last,
			Map<String, Double> values, TraceDBObject traceDB, IProgressMonitor monitor)
			throws SoCTraceException {

		logger.debug("do request: {}, {}. First: {}, Last: {}.", t0, t1, first, last);
		logger.debug("before managing pending");
		logger.debug(pending.toString());
		
		// lazily load the type map
		loadEventTypeMap(traceDB);

		// manage pending
		for (PendingDuration d : pending) {
			managePendingDuration(d, t0, t1, values);
		}

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
				managePendingDuration(d, t0, t1, values);
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

	@Override
	protected void reset() {
		pending = new ArrayList<>();
	}

	private void managePendingDuration(PendingDuration d, long t0, long t1,
			Map<String, Double> values) {
		String etName = etMap.get(d.typeId);
		if (d.start < t0) {
			PendingDuration before = new PendingDuration(d.typeId);
			before.start = d.start;
			before.end = t0;
			pending.add(before);
			d.start = t0;
		}
		if (d.end > t1) {
			PendingDuration after = new PendingDuration(d.typeId);
			after.start = t1;
			after.end = d.end;
			pending.add(after);
			d.end = t1;
		}
		Double remainingDuration = ((Long) (d.end - d.start)).doubleValue();
		if (remainingDuration > 0) {
			if (!values.containsKey(etName))
				values.put(etName, remainingDuration);
			else
				values.put(etName, remainingDuration + values.get(etName));
		}
	}

	/**
	 * Get the event category to use.
	 * 
	 * It must be a category that implies the concept of duration, i.e., states or links. The
	 * category is returned as one of the integer constant in <code>EventCategory</code>.
	 * 
	 * @return the duration category to use
	 */
	protected abstract int getDurationCategory();

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
