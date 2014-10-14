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
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Loader for event type statistics
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EventProducerStatisticsLoader extends PieChartStatisticsLoader {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(EventProducerStatisticsLoader.class);

	@Override
	public String getStatName() {
		return "Event Producers";
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

			EventProducerQuery epq = new EventProducerQuery(traceDB);
			List<EventProducer> epl = epq.getList();
			Map<Integer, String> epmap = new HashMap<>();
			for (EventProducer ep: epl) {
				epmap.put(ep.getId(), ep.getWholeName());
			}

			String query = "SELECT EVENT_PRODUCER_ID, COUNT(*) AS NUMBER " +
					" FROM EVENT " +
					" GROUP BY EVENT_PRODUCER_ID ";

			values = new HashMap<>();
			try {
				Statement stm = traceDB.getConnection().createStatement();
				ResultSet rs = stm.executeQuery(query);
				while (rs.next()) {
					int id = rs.getInt(1);
					double count = rs.getInt(2);
					values.put(epmap.get(id), count);
				}
				stm.close();
			} catch (SQLException e) {
				throw new SoCTraceException(e);
			}
			logger.debug(dm.endMessage("Prepared Pie Chart dataset"));

		} finally {
			DBObject.finalClose(traceDB);			
		}
	}	

	@Override
	protected Map<String, FramesocColor> getRawColors() throws SoCTraceException {
		HashMap<String, FramesocColor> colorMap = new HashMap<String, FramesocColor>();
		for (String name: values.keySet()) {
			colorMap.put(name, FramesocColorManager.getInstance().getEventProducerColor(name));
		}
		FramesocColorManager.getInstance().saveEventProducerColors();
		return colorMap;
	}

}

