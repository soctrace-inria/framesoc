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

import java.util.Iterator;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * Abstract implementation of the {@link EventIterator} interface.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class AbstractEventIterator implements EventIterator {

	/**
	 * Debug flag
	 */
	private final boolean DEBUG = false;
	
	/**
	 * Trace DB object (ownership is outside)
	 */
	protected TraceDBObject traceDB;
	
	/**
	 * Event query object, managed by the iterator.
	 */
	protected EventQuery query;

	/**
	 * Inner iterator for events
	 */
	protected Iterator<Event> eIterator;
	
	/**
	 * Event list
	 */
	protected List<Event> eList;
	
	/**
	 * If true, the iterator cannot be used anymore
	 */
	protected boolean valid;
	
	/**
	 * Base constructor.
	 * 
	 * @param traceDB database object
	 * @throws SoCTraceException
	 */
	public AbstractEventIterator(TraceDBObject traceDB) throws SoCTraceException {
		this.traceDB = traceDB;
		this.query = new EventQuery(traceDB);
		this.eIterator = null;
		this.eList = null;
		this.valid = true;
	}
	
	/* (non-Javadoc)
	 * @see internal.fr.inria.soctrace.tests.EventIterator#getNext()
	 */
	@Override
	public Event getNext() throws SoCTraceException {
		// do nothing
		return null;
	}

	/* (non-Javadoc)
	 * @see internal.fr.inria.soctrace.tests.EventIterator#hasNext()
	 */
	@Override
	public boolean hasNext() throws SoCTraceException {
		// do nothing
		return false;
	}

	/* (non-Javadoc)
	 * @see internal.fr.inria.soctrace.tests.EventIterator#clear()
	 */
	@Override
	public void clear() throws SoCTraceException {
		eIterator = null;
		if (eList!=null) {
			eList.clear();
			eList = null;			
		}
		if (query!=null) {
			query.clear();
			query = null;			
		}
		valid = false;
	}

	/**
	 * Print a debug message
	 * @param message
	 */
	protected void debug(String message) {
		if (DEBUG)
			System.out.println(message);
	}
	
	/**
	 * Throws an exception if the iterator is not valid anymore.
	 * 
	 * @throws SoCTraceException
	 */
	protected void checkValid() throws SoCTraceException {
		if (!valid)
			throw new SoCTraceException("Trying to use a cleared iterator!");
	}
}
