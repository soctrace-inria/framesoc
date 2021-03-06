/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.piechart.loaders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.NumberFormat;
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
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * Pie Chart loader for Event Type instances metric (i.e., how many event
 * instances for each event type).
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EventTypePieChartLoader extends EventPieChartLoader {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(EventTypePieChartLoader.class);

	/**
	 * Event type map.
	 */
	private Map<Integer, String> etMap;

	@Override
	public String getStatName() {
		return "Event Type Instances";
	}

	@Override
	protected FramesocColor getBaseColor(String name) {
		FramesocColor color = FramesocColorManager.getInstance().getEventTypeColor(name);
		FramesocColorManager.getInstance().saveEventTypeColors();
		return color;
	}

	@Override
	protected int doRequest(long t0, long t1, boolean first, boolean last,
			Map<String, Double> values, TraceDBObject traceDB, IProgressMonitor monitor)
			throws SoCTraceException {

		// lazily load the type map
		loadEventTypeMap(traceDB);

		// execute query
		ComparisonOperation lastComp = last ? ComparisonOperation.LE : ComparisonOperation.LT;
		StringBuilder sb = new StringBuilder();
		
		sb.append("SELECT EVENT_TYPE_ID, COUNT(*) AS NUMBER FROM EVENT WHERE TIMESTAMP >= ");
		sb.append(t0);
		sb.append(" AND TIMESTAMP ");
		sb.append(lastComp);
		sb.append(" ");
		sb.append(t1);
		addFiltersToQuery(sb);
		sb.append(" GROUP BY EVENT_TYPE_ID ");
		String query = sb.toString();
		logger.debug(query);
		int results = 0;
		try {
			Statement stm = traceDB.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(query);
			while (rs.next()) {
				if (monitor.isCanceled())
					return results;
				int id = rs.getInt(1);
				double count = rs.getInt(2);
				String etName = etMap.get(id);
				if (!values.containsKey(etName))
					values.put(etName, count);
				else
					values.put(etName, count + values.get(etName));
				results++;
			}
			stm.close();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}

		return results;
	}

	private void loadEventTypeMap(TraceDBObject traceDB) throws SoCTraceException {
		// load all types (do it only once)
		if (etMap == null) {
			EventTypeQuery etq = new EventTypeQuery(traceDB);
			List<EventType> etl = etq.getList();
			etMap = new HashMap<>();
			for (EventType et : etl) {
				etMap.put(et.getId(), et.getName());
			}
		}
	}

	@Override
	public NumberFormat getFormat() {
		return new DecimalFormat();
	}

}
