/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.lib.query.distribution;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.ValueListString;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Implementation of the {@link HEventIterator} interface,
 * reading events block by block.
 * 
 * The block size is predefined.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
class HEventIdBlockIteratorImpl implements HEventIterator {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(HEventIdBlockIteratorImpl.class);
	
	private TraceDBObject traceDB;
	private Iterator<HEvent> eIterator;
	private List<HEvent> eList;

	private String typeIds;
	private int numOfTypes;
	private long startTimestamp = Long.MIN_VALUE;
	private long endTimestamp = Long.MIN_VALUE;
	private boolean hasNext = true;
	private int nextId = Integer.MIN_VALUE;
	
	private final static int MAXSIZE = 1000000;
	
	public boolean hasNext() {
		return hasNext;
	}

	public HEvent getNext() throws SoCTraceException {
		if (eIterator==null || !eIterator.hasNext()) {
			
			if (!hasNext) {
				clear();
				return null;
			}
			
			eIterator = null;
			if (eList!=null) {
				eList.clear();
				eList = null;
			}
		
			eList = getBlock(traceDB);
			eIterator = eList.iterator();	
		}
		if (eIterator.hasNext()) // in the case an empty list has been loaded
			return eIterator.next();
		return null;
	}
	
	private List<HEvent> getBlock(TraceDBObject traceDB) throws SoCTraceException {

		List<HEvent> list = new LinkedList<HEvent>();
		try {
			DeltaManager dm = new DeltaManager();
			dm.start();
			// prepare query
			int numberOfTraceTypes = traceDB.getEventTypeCache().getElementMap(EventType.class).values().size();
			boolean typecond = (numOfTypes < numberOfTraceTypes);	
			boolean timecond = (startTimestamp!=Long.MIN_VALUE || endTimestamp!=Long.MIN_VALUE);
			
			StringBuilder sb = new StringBuilder("SELECT ID, TIMESTAMP FROM EVENT WHERE ID >= " + nextId);	
			if (typecond) {
				sb.append(" AND EVENT_TYPE_ID IN " + typeIds);
			}
			if (timecond) {
				sb.append(" AND TIMESTAMP BETWEEN " + startTimestamp + " AND " + endTimestamp);
			}
			sb.append(" ORDER BY ID " );
			sb.append(" LIMIT " + MAXSIZE);
			logger.debug(sb.toString());
			
			Statement stm = traceDB.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(sb.toString());
			
			int count = 0;
			while (rs.next()) {
				HEvent he = new HEvent();
				nextId = rs.getInt(1);
				he.timestamp = rs.getLong(2); 
				list.add(he);
				count++;
			}
			if (count==0)
				hasNext = false;
			nextId++;
			stm.close();
			logger.debug(dm.endMessage("Next id: " + (nextId)));
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
		return list;
	}

	@Override
	public void setTraceDB(TraceDBObject traceDB) throws SoCTraceException {
		this.traceDB = traceDB;
	}
	
	@Override
	public void setTypes(List<EventType> types) throws SoCTraceException {
		ValueListString vls = new ValueListString();
		Set<Integer> tset = new HashSet<Integer>();
		numOfTypes = 0;
		for (EventType et: types) {
			if (tset.contains(et.getId()))
				continue;
			vls.addValue(String.valueOf(et.getId()));
			tset.add(et.getId());
			numOfTypes++;
			
		}
		this.typeIds = vls.getValueString();		
	}

	@Override
	public void setTimestamps(long startTimestamp, long endTimestamp) {
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
	}
	
	@Override
	public void clear() {
		hasNext = true;
		startTimestamp = Long.MIN_VALUE;
		endTimestamp = Long.MIN_VALUE;
		nextId = Integer.MIN_VALUE;
		typeIds = "";
		numOfTypes = 0;
		
		traceDB = null;
		eIterator = null;
		if (eList!=null) {
			eList.clear();
			eList = null;			
		}
	}
	
}
