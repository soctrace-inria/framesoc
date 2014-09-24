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
package fr.inria.soctrace.lib.query.iterators;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * Iterator for trace events using time intervals.
 * 
 * <p>
 * The iterator reads the events by time intervals of a given size, 
 * configurable by the user.
 * Events are ordered by timestamp in each time interval.
 * 
 * <p>
 * The user does not have interval visibility, but simply
 * calls getNext() to get the next event of the current
 * interval or the first event of the next interval.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class IntervalEventIterator extends AbstractEventIterator {

	protected final long MIN_TIMESTAMP;
	protected final long MAX_TIMESTAMP;
	
	/**
	 * Time interval size
	 */
	protected long delta;
	
	/**
	 * Next start timestamp
	 */
	protected long nextStartTimestamp;
	
	/**
	 * Iterator to read events by time intervals.
	 * 
	 * @param traceDB database object
	 * @param delta time interval size
	 * @throws SoCTraceException
	 */
	public IntervalEventIterator(TraceDBObject traceDB, long delta) throws SoCTraceException {
		super(traceDB);
		this.delta = delta;
		this.MIN_TIMESTAMP = this.traceDB.getMinTimestamp();
		this.MAX_TIMESTAMP = this.traceDB.getMaxTimestamp();
		this.nextStartTimestamp = MIN_TIMESTAMP;
	}
	
	@Override
	public Event getNext() throws SoCTraceException {
		checkValid();
		while (eIterator==null || !eIterator.hasNext()) {
			
			long nextEndTimestamp = nextStartTimestamp + delta;
			debug("load from " + nextStartTimestamp + " to " + nextEndTimestamp);
			
			if (nextStartTimestamp>MAX_TIMESTAMP) {
				clear();
				return null;
			}
			
			eIterator = null;
			if (eList!=null) {
				eList.clear();
				eList = null;
			}
			query.clear();
			
			query.setElementWhere(new SimpleCondition("TIMESTAMP", ComparisonOperation.BETWEEN, 
					nextStartTimestamp + " AND " + (nextEndTimestamp - 1)));
			query.setOrderBy("TIMESTAMP", OrderBy.ASC);
			eList = query.getList();
			debug("loaded events: " + eList.size());
			eIterator = eList.iterator();
			nextStartTimestamp = nextEndTimestamp;
		}
		if (eIterator.hasNext()) // in the case an empty list has been loaded
			return eIterator.next();
		return eIterator.next();
	}

	@Override
	public boolean hasNext() throws SoCTraceException {
		checkValid();
		if (eIterator==null || !eIterator.hasNext())
			if (nextStartTimestamp>MAX_TIMESTAMP) 
				return false;
		return true;
	}

}
