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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.jfree.data.statistics.HistogramType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.ui.histogram.model.HistogramLoaderDataset;
import fr.inria.soctrace.framesoc.ui.model.CategoryNode;
import fr.inria.soctrace.framesoc.ui.model.EventProducerNode;
import fr.inria.soctrace.framesoc.ui.model.EventTypeNode;
import fr.inria.soctrace.framesoc.ui.model.ITreeNode;
import fr.inria.soctrace.framesoc.ui.model.TimeInterval;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.EventTypeQuery;
import fr.inria.soctrace.lib.query.ValueListString;
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

	public enum ConfigurationDimension {
		PRODUCERS("Event Producers"), TYPE("Event Type");

		private String name;

		ConfigurationDimension(String str) {
			name = str;
		}

		public String getName() {
			return name;
		}
	}
	
	/**
	 * Average number of event to load in each query
	 */
	protected final int EVENTS_PER_QUERY = 1000000;

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(DensityHistogramLoader.class);

	/**
	 * Dataset constants
	 */
	public final static int NUMBER_OF_BINS = 1000;
	public final static String DATASET_NAME = "Event frequency";
	public final static HistogramType HISTOGRAM_TYPE = HistogramType.FREQUENCY;

	private long min;
	private long max;

	/**
	 * Load a dataset for the Event Density Histogram
	 * 
	 * @param trace
	 *            trace to work with
	 * @param types
	 *            event type ids to load
	 * @param producers
	 *            event producer ids to load
	 * @param monitor
	 *            progress monitor
	 * @param dataset
	 *            loader dataset
	 * @throws SoCTraceException
	 */
	public void load(Trace trace, List<Integer> types, List<Integer> producers,
			HistogramLoaderDataset dataset, IProgressMonitor monitor) {

		DeltaManager dm = new DeltaManager();
		dm.start();

		if ((trace == null) || (types != null && types.size() == 0)
				|| (producers != null && producers.size() == 0)) {
			dataset.setStop();
			return;
		}
		TraceDBObject traceDB = null;
		try {
			traceDB = new TraceDBObject(trace.getDbName(), DBMode.DB_OPEN);
			
			
			
			double timestamps[] = getTimestapsSeries(traceDB, types, producers);
			if (timestamps.length != 0) {
				dataset.setSnapshot(timestamps, new TimeInterval(min, max));
				dataset.setComplete();
			}
			
			
			
			
		} catch (SoCTraceException e) {
			e.printStackTrace();
		} finally {
			if (!dataset.isStop() && !dataset.isComplete()) {
				// something went wrong, respect the map contract anyway
				dataset.setStop();
			}
			logger.debug(dm.endMessage("Prepared Histogram dataset"));
			DBObject.finalClose(traceDB);
		}
	}

	/**
	 * Load the hierarchy of items for the given configuration dimension.
	 * 
	 * @param dimension
	 *            configuration dimension
	 * @param trace
	 *            trace
	 * @return the hierarchy of items for the passed dimension
	 * @throws SoCTraceException
	 */
	public ITreeNode[] loadDimension(ConfigurationDimension dimension, Trace trace)
			throws SoCTraceException {
		switch (dimension) {
		case TYPE:
			return loadEventTypes(trace);
		case PRODUCERS:
			return loadProducers(trace);
		}
		throw new SoCTraceException("Unknown dimension: " + dimension.getName());
	}

	/**
	 * Get the trace event producer hierarchy
	 * 
	 * @param trace
	 *            trace
	 * @return the event producer roots
	 * @throws SoCTraceException
	 */
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

	/**
	 * Get the event type hierarchy. Types are grouped by category.
	 * 
	 * @param trace
	 *            trace
	 * @return the root nodes, corresponding to the event category
	 * @throws SoCTraceException
	 */
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

	private EventProducerNode getProducerNode(EventProducer ep,
			Map<Integer, EventProducer> prodMap, Map<Integer, EventProducerNode> nodeMap) {
		if (nodeMap.containsKey(ep.getId()))
			return nodeMap.get(ep.getId());
		EventProducerNode current = new EventProducerNode(ep);
		nodeMap.put(ep.getId(), current);
		if (ep.getParentId() != EventProducer.NO_PARENT_ID) {
			EventProducerNode parent = getProducerNode(prodMap.get(ep.getParentId()), prodMap,
					nodeMap);
			parent.addChild(current);
			nodeMap.put(parent.getEventProducer().getId(), parent);
		}
		return current;
	}

	/**
	 * Load timestamps vector, considering only positive times.
	 * 
	 * Note that for States and Links, a single event is counted at the start timestamp. This is
	 * consistent with the data model where a State (Link) is a single event of type State (Link).
	 * 
	 * @param traceDB
	 *            trace DB object
	 * @param producers
	 *            event producer ids to consider
	 * @param types
	 *            event type ids to consider
	 * @return timestamps vector
	 * @throws SoCTraceException
	 */
	private double[] getTimestapsSeries(TraceDBObject traceDB, List<Integer> types,
			List<Integer> producers) throws SoCTraceException {
		Statement stm;
		ResultSet rs;
		String query = "";
		try {
			DeltaManager dm = new DeltaManager();
			stm = traceDB.getConnection().createStatement();
			List<Long> tsl = new LinkedList<Long>();

			query = prepareQuery(traceDB, types, producers);
			logger.debug(query);
			rs = stm.executeQuery(query);
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
			throw new SoCTraceException("Query: " + query, e);
		}
	}

	private String prepareQuery(TraceDBObject traceDB, List<Integer> types, List<Integer> producers)
			throws SoCTraceException {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT TIMESTAMP FROM ");
		sb.append(FramesocTable.EVENT.toString());
		sb.append(" WHERE TIMESTAMP >= 0");

		if (producers != null && getNumberOfProducers(traceDB) != producers.size()) {
			ValueListString vls = new ValueListString();
			for (Integer epId : producers) {
				vls.addValue(String.valueOf(epId));
			}
			sb.append(" AND EVENT_PRODUCER_ID IN ");
			sb.append(vls.getValueString());
		}

		if (types != null) {
			Map<Integer, EventType> typesMap = new HashMap<>();
			Map<Integer, Integer> tpc = getTypesPerCategory(traceDB, typesMap);
			Map<Integer, List<Integer>> requested = new HashMap<>();
			for (Integer etId : types) {
				int category = typesMap.get(etId).getCategory();
				if (!requested.containsKey(category)) {
					requested.put(category, new LinkedList<Integer>());
				}
				requested.get(category).add(etId);
			}
			ValueListString categories = new ValueListString();
			ValueListString typeIds = new ValueListString();
			Iterator<Entry<Integer, List<Integer>>> it = requested.entrySet().iterator();
			int totTypes = 0;
			for (Integer numberPerCategory : tpc.values()) {
				totTypes += numberPerCategory;
			}
			while (it.hasNext()) {
				Entry<Integer, List<Integer>> e = it.next();
				int category = e.getKey();
				List<Integer> tl = e.getValue();
				if (tl.size() == tpc.get(category)) {
					categories.addValue(String.valueOf(category));
				} else {
					for (Integer etId : tl) {
						typeIds.addValue(String.valueOf(etId));
					}
				}
			}
			if (totTypes != types.size()) {
				sb.append(" AND ");
				boolean both = false;
				if (categories.size() > 0 && typeIds.size() > 0) {
					both = true;
				}
				if (both) {
					sb.append("( ( ");
				}
				if (categories.size() > 0) {
					sb.append("CATEGORY IN ");
					sb.append(categories.getValueString());
				}
				if (both) {
					sb.append(" ) OR ( ");
				}
				if (typeIds.size() > 0) {
					sb.append("EVENT_TYPE_ID IN ");
					sb.append(typeIds.getValueString());
				}
				if (both) {
					sb.append(" ) ) ");
				}
			}
		}

		return sb.toString();
	}

	private Map<Integer, Integer> getTypesPerCategory(TraceDBObject traceDB,
			Map<Integer, EventType> typesMap) throws SoCTraceException {
		Map<Integer, Integer> typesPerCategory = new HashMap<>();
		EventTypeQuery etq = new EventTypeQuery(traceDB);
		List<EventType> etl = etq.getList();
		for (EventType et : etl) {
			typesMap.put(et.getId(), et);
			if (!typesPerCategory.containsKey(et.getCategory())) {
				typesPerCategory.put(et.getCategory(), 0);
			}
			typesPerCategory.put(et.getCategory(), typesPerCategory.get(et.getCategory()) + 1);
		}
		return typesPerCategory;
	}

	private int getNumberOfProducers(TraceDBObject traceDB) throws SoCTraceException {
		int count = 0;
		try {
			Statement stm = traceDB.getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SELECT COUNT(*) FROM EVENT_PRODUCER");
			if (rs.next()) {
				count = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SoCTraceException(e);
		}
		return count;
	}

}
