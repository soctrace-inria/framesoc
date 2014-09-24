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
package fr.inria.soctrace.framesoc.ui.piechart.loaders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.colors.FramesocColor;
import fr.inria.soctrace.framesoc.ui.colors.FramesocColorManager;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventTypeQuery;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Loader for event type statistics
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EventTypeStatisticsLoader extends PieChartStatisticsLoader {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(EventTypeStatisticsLoader.class);

	@Override
	public String getStatName() {
		return "Event Types";
	}

	@Override
	public void load(Trace trace) throws SoCTraceException {

		TraceDBObject traceDB = null;

		try {
			if (trace == null)
				throw new SoCTraceException("Null trace passed");

			DeltaManager dm = new DeltaManager();		
			dm.start();
			traceDB = new TraceDBObject(trace.getDbName(), DBMode.DB_OPEN);

			EventTypeQuery etq = new EventTypeQuery(traceDB);
			List<EventType> etl = etq.getList();
			Map<Integer, String> etmap = new HashMap<Integer, String>();
			for (EventType et: etl) {
				etmap.put(et.getId(), et.getName());
			}

			String query = "SELECT EVENT_TYPE_ID, COUNT(*) AS NUMBER " +
					" FROM EVENT " +
					" GROUP BY EVENT_TYPE_ID" +
					" ORDER BY NUMBER DESC";

			values = new HashMap<String, Double>();
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
			logger.debug(dm.endMessage("Prepared Pie Chart dataset"));

		} finally {
			DBObject.finalClose(traceDB);
		}

	}	

	@Override
	public Map<String, FramesocColor> getRawColors() throws SoCTraceException {
		HashMap<String, FramesocColor> colorMap = new HashMap<String, FramesocColor>();
		for (String name: values.keySet()) {
			colorMap.put(name, FramesocColorManager.getInstance().getEventTypeColor(name));
		}
		FramesocColorManager.getInstance().saveEventTypeColors();
		return colorMap;
	}

}

