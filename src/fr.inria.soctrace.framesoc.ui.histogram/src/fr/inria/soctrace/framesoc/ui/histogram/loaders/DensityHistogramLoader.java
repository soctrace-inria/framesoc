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
package fr.inria.soctrace.framesoc.ui.histogram.loaders;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Loader for event density histogram.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class DensityHistogramLoader {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(DensityHistogramLoader.class);
	
	/**
	 * Dataset constants
	 */
	private static int NUMBER_OF_BINS = 1000;
	private static final String DATASET_NAME = "Event frequency";
	private static HistogramType HISTOGRAM_TYPE = HistogramType.FREQUENCY;
	
	private long min;
	private long max;
	
	/**
	 * Load a dataset for the Event Density Histogram
	 * 
	 * @param trace trace to work with
	 * @return the histogram dataset
	 * @throws SoCTraceException
	 */
	public HistogramDataset load(Trace trace) throws SoCTraceException {

		DeltaManager dm = new DeltaManager();
		HistogramDataset dataset = new HistogramDataset();

		if (trace == null)
			return dataset;
		
		dm.start();
		TraceDBObject traceDB = null;		
		try {
			traceDB = new TraceDBObject(trace.getDbName(), DBMode.DB_OPEN);
			double timestamps[] = getTimestapsSeries(traceDB);
			dataset.setType(HISTOGRAM_TYPE);
			dataset.addSeries(DATASET_NAME, timestamps, NUMBER_OF_BINS);
			logger.debug(dm.endMessage("Prepared Histogram dataset"));
		} finally {
			DBObject.finalClose(traceDB);
		}
				
		return dataset;
	}

	/**
	 * Load timestamps vector, considering only positive times.
	 * 
	 * Note that for States and Links, a single event is counted
	 * at the start timestamp. This is consistent with the data model
	 * where a State (Link) is a single event of type State (Link).
	 * 
	 * @param traceDB trace DB object
	 * @return timestamps vector
	 * @throws SoCTraceException
	 */
	private double[] getTimestapsSeries(TraceDBObject traceDB) throws SoCTraceException {
		Statement stm;
		ResultSet rs;
		try {
			DeltaManager dm = new DeltaManager();
			stm = traceDB.getConnection().createStatement();			
			List<Long> tsl = new LinkedList<Long>();
			rs = stm.executeQuery("SELECT TIMESTAMP FROM " + FramesocTable.EVENT + " WHERE TIMESTAMP >= 0");
			while (rs.next()) {
				tsl.add(rs.getLong(1));
			}			
			stm.close();
			logger.debug("Real events: {}", tsl.size());
			double timestamps[] = new double[tsl.size()];
			int i = 0;
			min = Long.MAX_VALUE;
			max = Long.MIN_VALUE;
			for (Long l: tsl) {
				Long lv = l;
				timestamps[i++] = lv;
				if (lv < min)
					min = lv;
				if (lv > max)
					max = lv;
			}
			logger.debug(dm.endMessage("get timestamps"));
			return timestamps;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * @return the min timestamp
	 */
	public long getMin() {
		return min;
	}

	/**
	 * @return the max timestamp
	 */
	public long getMax() {
		return max;
	}
	
}
