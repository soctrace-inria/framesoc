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

import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.framesoc.ui.piechart.model.LoaderMap;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventTypeQuery;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EventTypePieChartLoader extends AggregatedPieChartLoader {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(EventTypePieChartLoader.class);

	@Override
	public String getStatName() {
		return "Event Types";
	}
	
	/* 
	 * TODO 
	 * - partition loading 
	 * - use the progress monitor
	 */
	@Override
	public void load(Trace trace, TimeInterval interval, LoaderMap map, IProgressMonitor monitor)
			throws SoCTraceException {

		if (trace == null || interval == null || map == null || monitor == null)
			throw new NullPointerException();

		TraceDBObject traceDB = null;

		try {
			DeltaManager dm = new DeltaManager();
			dm.start();
			traceDB = new TraceDBObject(trace.getDbName(), DBMode.DB_OPEN);

			EventTypeQuery etq = new EventTypeQuery(traceDB);
			List<EventType> etl = etq.getList();
			Map<Integer, String> etmap = new HashMap<>();
			for (EventType et : etl) {
				etmap.put(et.getId(), et.getName());
			}

			String query = "SELECT EVENT_TYPE_ID, COUNT(*) AS NUMBER FROM EVENT "
					+ " GROUP BY EVENT_TYPE_ID";

			Map<String, Double> values = new HashMap<>();

			try {
				Statement stm = traceDB.getConnection().createStatement();
				ResultSet rs = stm.executeQuery(query);
				while (rs.next()) {
					int id = rs.getInt(1);
					double count = rs.getInt(2);
					values.put(etmap.get(id), count);
				}
				stm.close();
			} catch (SQLException e) {
				throw new SoCTraceException(e);
			}
			traceDB.close();

			map.putSnapshot(values, interval);
			map.setComplete(true);

			logger.debug(dm.endMessage("Prepared Pie Chart dataset"));

		} finally {
			DBObject.finalClose(traceDB);
		}

	}

	@Override
	public FramesocColor getColor(String name) {
		FramesocColor color = FramesocColorManager.getInstance().getEventTypeColor(name);
		FramesocColorManager.getInstance().saveEventTypeColors();
		return color;
	}

}
