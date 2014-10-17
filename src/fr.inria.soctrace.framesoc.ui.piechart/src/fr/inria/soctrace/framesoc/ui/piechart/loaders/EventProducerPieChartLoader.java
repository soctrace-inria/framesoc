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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.piechart.model.PieChartLoaderMap;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class EventProducerPieChartLoader extends AggregatedPieChartLoader {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(EventProducerPieChartLoader.class);

	/**
	 * Average number of event to load in each query
	 */
	private final int EVENTS_PER_QUERY = 1;

	/**
	 * Event producer map
	 */
	private Map<Integer, String> epMap;

	@Override
	public String getStatName() {
		return "Event Producers";
	}

	/*
	 * TODO - check for empty regions
	 */
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

			// load event producer map
			loadEventProducerMap(traceDB);

			// compute interval duration
			long duration = trace.getMaxTimestamp() - trace.getMinTimestamp();
			Assert.isTrue(duration != 0, "The trace duration cannot be 0");
			double density = ((double) trace.getNumberOfEvents()) / duration;
			Assert.isTrue(density != 0, "The density cannot be 0");
			long intervalDuration = (long) (EVENTS_PER_QUERY / density);

			Map<String, Double> values = new HashMap<>();

			long t0 = requestedInterval.startTimestamp;
			TimeInterval loadedInterval = new TimeInterval(t0, 0);
			while (t0 <= requestedInterval.endTimestamp) {

				if (checkCancel(map, monitor)) {
					return;
				}

				// load interval
				long t1 = Math.min(requestedInterval.endTimestamp, t0 + intervalDuration);
				int results = doRequest(t0, t1, (t1 >= requestedInterval.endTimestamp), values,
						traceDB, monitor);
				logger.debug("Loaded: " + results);

				if (checkCancel(map, monitor)) {
					return;
				}

				// check for empty regions
				if (results == 0) {
					long oldt0 = t0;
					t0 = getNextTimestampAfter(traceDB, t1);
					logger.debug("saved " + ((t0 - oldt0) / intervalDuration) + " queries.");
					continue;
				}
				
				loadedInterval.endTimestamp = t1;
				map.setSnapshot(values, loadedInterval);

				t0 = t1 + 1;

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
	
	private long getNextTimestampAfter(TraceDBObject traceDB, long end) {
		long next = end + 1;
		try {
			Statement stm = traceDB.getConnection().createStatement();
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

	protected abstract String getQuery(long t0, long t1, boolean last);
	
	private int doRequest(long t0, long t1, boolean last, Map<String, Double> values,
			TraceDBObject traceDB, IProgressMonitor monitor) throws SoCTraceException {

		ComparisonOperation lastComp = last ? ComparisonOperation.LE : ComparisonOperation.LT;
		String query = "SELECT EVENT_PRODUCER_ID, COUNT(*) AS NUMBER FROM EVENT "
				+ " WHERE TIMESTAMP >= " + t0 + " AND TIMESTAMP " + lastComp + " " + t1
				+ " GROUP BY EVENT_PRODUCER_ID ";

		int results = 0;
		try {
			Statement stm = traceDB.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(query);
			while (rs.next()) {
				if (monitor.isCanceled())
					return results;
				int id = rs.getInt(1);
				double count = rs.getInt(2);
				String epName = epMap.get(id);
				if (!values.containsKey(epName))
					values.put(epName, count);
				else
					values.put(epName, count + values.get(epName));
				results++;
			}
			stm.close();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}

		return results;
	}

	private boolean checkCancel(PieChartLoaderMap map, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			map.setStop();
			return true;
		}
		return false;
	}

	private void loadEventProducerMap(TraceDBObject traceDB) throws SoCTraceException {
		// load all producers (do it only once)
		if (epMap == null) {
			EventProducerQuery epq = new EventProducerQuery(traceDB);
			List<EventProducer> epl = epq.getList();
			epMap = new HashMap<>();
			for (EventProducer ep : epl) {
				epMap.put(ep.getId(), ep.getWholeName());
			}
		}
	}

	@Override
	public FramesocColor getColor(String name) {
		if (name.equals(AGGREGATED_LABEL))
			return AGGREGATED_COLOR;
		FramesocColor color = FramesocColorManager.getInstance().getEventProducerColor(name);
		FramesocColorManager.getInstance().saveEventProducerColors();
		return color;
	}
}
