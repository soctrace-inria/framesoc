/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.piechart.loaders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.loaders.LoaderUtils;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.piechart.model.PieChartLoaderMap;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.ValueListString;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.lib.utils.IdManager;

/**
 * Base abstract class for event Pie Chart loaders. It performs aggregation and the load method
 * skips empty regions to avoid useless queries.
 * 
 * Concrete classes have to override the {@link #doRequest()} method.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class EventPieChartLoader extends PieChartLoader {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(EventPieChartLoader.class);

	/**
	 * Average number of event to load in each query
	 */
	protected final int EVENTS_PER_QUERY = 100000;

	/**
	 * Event producers to use (all if null)
	 */
	protected List<Integer> producers = null;

	/**
	 * Event type to use (all if null)
	 */
	protected List<Integer> types = null;

	@Override
	public void setEventProducerFilter(List<Integer> producers) {
		this.producers = producers;
	}

	@Override
	public void setEventTypeFilter(List<Integer> types) {
		this.types = types;
	}

	@Override
	public void load(Trace trace, TimeInterval requestedInterval, PieChartLoaderMap map,
			IProgressMonitor monitor) {

		if (trace == null || requestedInterval == null || map == null || monitor == null)
			throw new NullPointerException();

		TraceDBObject traceDB = null;

		try {

			DeltaManager dm = new DeltaManager();
			dm.start();
			traceDB = new TraceDBObject(trace.getDbName(), DBMode.DB_OPEN);

			// compute interval duration
			long intervalDuration = LoaderUtils.getIntervalDuration(trace, EVENTS_PER_QUERY);

			Map<String, Double> values = new HashMap<>();

			long t0 = requestedInterval.startTimestamp;
			TimeInterval loadedInterval = new TimeInterval(t0, 0);

			boolean first = true;

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
					long oldt0 = t0;
					t0 = getNextTimestampStartingFrom(traceDB, t1);
					logger.debug("saved " + ((t0 - oldt0) / intervalDuration) + " queries.");
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

	/**
	 * Perform the request to load the statistic for the given time interval.
	 * 
	 * @param t0
	 *            start timestamp
	 * @param t1
	 *            end timestamp
	 * @param first
	 *            flag indicating if we are requesting the first interval
	 * @param last
	 *            flag indicating if we are requesting the last interval
	 * @param values
	 *            map to update with new results
	 * @param traceDB
	 *            trace DB object
	 * @param monitor
	 *            progress monitor
	 * @return the number of results of the query
	 * @throws SoCTraceException
	 */
	protected abstract int doRequest(long t0, long t1, boolean first, boolean last,
			Map<String, Double> values, TraceDBObject traceDB, IProgressMonitor monitor)
			throws SoCTraceException;

	protected long getNextTimestampStartingFrom(TraceDBObject traceDB, long end) {
		long next = end + 1;
		try {
			Statement stm = traceDB.getConnection().createStatement();
			DeltaManager dm = new DeltaManager();
			dm.start();
			ResultSet rs = stm.executeQuery("SELECT MIN(TIMESTAMP) FROM EVENT WHERE TIMESTAMP >= "
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

	protected boolean checkCancel(PieChartLoaderMap map, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			map.setStop();
			return true;
		}
		return false;
	}

	protected boolean hasEventProducerFilter() {
		return producers != null;
	}

	protected boolean hasEventTypeFilter() {
		return types != null;
	}

	protected void addFiltersToQuery(StringBuilder sb) {
		if (hasEventProducerFilter()) {
			if (producers.size() == 0) {
				sb.append(" AND EVENT_PRODUCER_ID IN ( " + IdManager.RESERVED_NO_ID + " ) ");
			} else {
				sb.append(" AND EVENT_PRODUCER_ID IN ");
				ValueListString vls = new ValueListString();
				for (Integer p : producers) {
					vls.addValue(p.toString());
				}
				sb.append(vls.getValueString());
			}
		}
		if (hasEventTypeFilter()) {
			if (types.size() == 0) {
				sb.append(" AND EVENT_TYPE_ID IN ( " + IdManager.RESERVED_NO_ID + " ) ");
			} else {
				sb.append(" AND EVENT_TYPE_ID IN ");
				ValueListString vls = new ValueListString();
				for (Integer t : types) {
					vls.addValue(t.toString());
				}
				sb.append(vls.getValueString());
			}
		}
	}

}
