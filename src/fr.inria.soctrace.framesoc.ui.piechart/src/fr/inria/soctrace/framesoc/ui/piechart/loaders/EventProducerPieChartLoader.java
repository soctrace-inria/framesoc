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
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * Pie Chart loader for Event Producer instances metric (i.e., how many event
 * instances for each event producer).
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EventProducerPieChartLoader extends EventPieChartLoader {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(EventProducerPieChartLoader.class);

	/**
	 * Event producer map
	 */
	private Map<Integer, String> epMap;

	@Override
	public String getStatName() {
		return "Event Producer Instances";
	}

	@Override
	protected FramesocColor getBaseColor(String name) {
		FramesocColor color = FramesocColorManager.getInstance().getEventProducerColor(name);
		FramesocColorManager.getInstance().saveEventProducerColors();
		return color;
	}

	@Override
	protected int doRequest(long t0, long t1, boolean first, boolean last,
			Map<String, Double> values, TraceDBObject traceDB, IProgressMonitor monitor)
			throws SoCTraceException {

		// lazily load the producer map
		loadEventProducerMap(traceDB);

		// execute query
		ComparisonOperation lastComp = last ? ComparisonOperation.LE : ComparisonOperation.LT;
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT EVENT_PRODUCER_ID, COUNT(*) AS NUMBER FROM EVENT ");
		sb.append(" WHERE TIMESTAMP >= ");
		sb.append(t0);
		sb.append(" AND TIMESTAMP ");
		sb.append(lastComp.toString());
		sb.append(" ");
		sb.append(t1);
		addFiltersToQuery(sb);
		sb.append(" GROUP BY EVENT_PRODUCER_ID ");
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
	public NumberFormat getFormat() {
		return new DecimalFormat();
	}

}
