/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.piechart.loaders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.piechart.model.PieChartLoaderMap;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class EventPieChartLoader extends AggregatedPieChartLoader {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(EventPieChartLoader.class);

	/**
	 * Average number of event to load in each query
	 */
	private final int EVENTS_PER_QUERY = 1;

	private void debugSleep() {
//		try {
//			logger.debug("Start sleep");
//			Thread.sleep(2000);
//			logger.debug("End sleep");
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
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

				debugSleep();
				
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

	protected abstract int doRequest(long t0, long t1, boolean last, Map<String, Double> values,
			TraceDBObject traceDB, IProgressMonitor monitor) throws SoCTraceException;

	private boolean checkCancel(PieChartLoaderMap map, IProgressMonitor monitor) {
		if (monitor.isCanceled()) {
			map.setStop();
			return true;
		}
		return false;
	}

}
