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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.model.CategoryNode;
import fr.inria.soctrace.framesoc.ui.model.EventProducerNode;
import fr.inria.soctrace.framesoc.ui.model.EventTypeNode;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.EventTypeQuery;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
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
	 * @param trace
	 *            trace to work with
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

	public EventProducerNode[] loadProducers(Trace trace) throws SoCTraceException {
		List<EventProducerNode> roots = new LinkedList<>();
		TraceDBObject traceDB = null;
		try {
			traceDB = TraceDBObject.openNewIstance(trace.getDbName());
			EventProducerQuery epq = new EventProducerQuery(traceDB);
			List<EventProducer> producers = epq.getList();
			Map<Integer, EventProducer> prodMap = new HashMap<>();
			Map<Integer, EventProducerNode> nodeMap = new HashMap<>();
			for (EventProducer ep : producers) {
				prodMap.put(ep.getId(), ep);
			}
			for (EventProducer ep : producers) {
				EventProducerNode node = getProducerNode(ep, prodMap, nodeMap);
				if (ep.getParentId() == EventProducer.NO_PARENT_ID) {
					roots.add(node);
				}
			}
		} finally {
			DBObject.finalClose(traceDB);
		}
		return roots.toArray(new EventProducerNode[roots.size()]);
	}

	private EventProducerNode getProducerNode(EventProducer ep,
			Map<Integer, EventProducer> prodMap, Map<Integer, EventProducerNode> nodeMap) {
		if (nodeMap.containsKey(ep.getId()))
			return nodeMap.get(ep.getId());
		EventProducerNode current = new EventProducerNode(ep);
		if (ep.getParentId() != EventProducer.NO_PARENT_ID) {
			EventProducerNode parent = getProducerNode(prodMap.get(ep.getParentId()), prodMap,
					nodeMap);
			parent.addChild(current);
		}
		return current;
	}

	public CategoryNode[] loadEventTypes(Trace trace) throws SoCTraceException {
		Map<Integer, CategoryNode> categories = new HashMap<>();
		TraceDBObject traceDB = null;
		try {
			traceDB = TraceDBObject.openNewIstance(trace.getDbName());
			EventTypeQuery etq = new EventTypeQuery(traceDB);
			List<EventType> types = etq.getList();
			for (EventType et : types) {
				EventTypeNode etn = new EventTypeNode(et);
				if (!categories.containsKey(et.getCategory())) {
					categories.put(et.getCategory(), new CategoryNode(et.getCategory()));
				}
				categories.get(et.getCategory()).addChild(etn);
			}
		} finally {
			DBObject.finalClose(traceDB);
		}
		return categories.values().toArray(new CategoryNode[categories.values().size()]);
	}

	/**
	 * Load timestamps vector, considering only positive times.
	 * 
	 * Note that for States and Links, a single event is counted at the start
	 * timestamp. This is consistent with the data model where a State (Link) is
	 * a single event of type State (Link).
	 * 
	 * @param traceDB
	 *            trace DB object
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
			rs = stm.executeQuery("SELECT TIMESTAMP FROM " + FramesocTable.EVENT
					+ " WHERE TIMESTAMP >= 0");
			while (rs.next()) {
				tsl.add(rs.getLong(1));
			}
			stm.close();
			logger.debug("Real events: {}", tsl.size());
			double timestamps[] = new double[tsl.size()];
			int i = 0;
			min = Long.MAX_VALUE;
			max = Long.MIN_VALUE;
			for (Long l : tsl) {
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
