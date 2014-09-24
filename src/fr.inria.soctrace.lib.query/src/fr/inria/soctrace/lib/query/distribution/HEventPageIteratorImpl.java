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

/**
 * Implementation of the {@link HEventIterator} interface,
 * reading events page by page.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
class HEventPageIteratorImpl implements HEventIterator {

	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(HEventPageIteratorImpl.class);
	
	private TraceDBObject traceDB;
	protected Iterator<HEvent> eIterator;
	protected List<HEvent> eList;

	private String typeIds;
	private int numOfTypes;
	private long startTimestamp = Long.MIN_VALUE;
	private long endTimestamp = Long.MIN_VALUE;

	private long MIN_PAGE = Long.MAX_VALUE;
	private long MAX_PAGE = Long.MIN_VALUE;
	private long nextPage = Long.MAX_VALUE;
	
	public boolean hasNext() {
		if (eIterator==null || !eIterator.hasNext())
			if (nextPage>MAX_PAGE) 
				return false;
		return true;
	}

	public HEvent getNext() throws SoCTraceException {
		if (eIterator==null || !eIterator.hasNext()) {
			
			if (nextPage>MAX_PAGE) {
				clear();
				return null;
			}
			
			eIterator = null;
			if (eList!=null) {
				eList.clear();
				eList = null;
			}
		
			eList = getNextPage(traceDB);
			eIterator = eList.iterator();	
		}
		if (eIterator.hasNext()) // in the case an empty list has been loaded
			return eIterator.next();
		return null;
	}
	
	private List<HEvent> getNextPage(TraceDBObject traceDB) throws SoCTraceException {
		
		List<HEvent> list = new LinkedList<HEvent>();
		try {
			// prepare query
			int numberOfTraceTypes = traceDB.getEventTypeCache().getElementMap(EventType.class).values().size();
			boolean typecond = (numOfTypes < numberOfTraceTypes);	
			boolean timecond = (startTimestamp!=Long.MIN_VALUE || endTimestamp!=Long.MIN_VALUE);
			StringBuilder sb = new StringBuilder("SELECT TIMESTAMP FROM EVENT WHERE PAGE="+nextPage+" ");	
			if (typecond) 
				sb.append(" AND EVENT_TYPE_ID IN " + typeIds);
			if (timecond)
				sb.append(" AND TIMESTAMP BETWEEN " + startTimestamp + " AND " + endTimestamp);
			logger.debug(sb.toString());
			
			Statement stm = traceDB.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(sb.toString());
			while (rs.next()) {
				HEvent he = new HEvent();
				he.timestamp = rs.getLong(1); 
				list.add(he);
			}
			stm.close();
			nextPage++;	
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
		return list;
	}

	@Override
	public void setTraceDB(TraceDBObject traceDB) throws SoCTraceException {
		this.traceDB = traceDB;
		this.MIN_PAGE = traceDB.getMinPage();
		this.MAX_PAGE = traceDB.getMaxPage();
		this.nextPage = MIN_PAGE;
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
		MIN_PAGE = Long.MAX_VALUE;
		MAX_PAGE = Long.MIN_VALUE;
		nextPage = Long.MAX_VALUE;
		startTimestamp = Long.MIN_VALUE;
		endTimestamp = Long.MIN_VALUE;
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
