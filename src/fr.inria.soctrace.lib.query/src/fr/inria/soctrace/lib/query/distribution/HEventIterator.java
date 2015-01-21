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

import java.util.List;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * Event iterator to be used by histogram loaders ({@link HistogramLoader}).
 * It deals with {@link HEvent} objects.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface HEventIterator {

	/**
	 * Check if there is another event in the trace.
	 * @return true if there is another event, false otherwise.
	 */
	boolean hasNext();

	/**
	 * Get the next event, or null if not present.
	 * @return the next event, or null if not present.
	 * @throws SoCTraceException 
	 */
	HEvent getNext() throws SoCTraceException;

	/**
	 * Set the trace DB to work with
	 * @param traceDB valid trace DB object
	 * @throws SoCTraceException 
	 */
	void setTraceDB(TraceDBObject traceDB) throws SoCTraceException;
	
	/**
	 * Set the types of interest.
	 * @param types valid list of event types.
	 * @throws SoCTraceException 
	 */
	void setTypes(List<EventType> types) throws SoCTraceException;
	
	/**
	 * Set the start and end timestamp
	 * 
	 * @param startTimestamp start timestamp
	 * @param endTimestamp end timestamp
	 */
	void setTimestamps(long startTimestamp, long endTimestamp);
	
	/**
	 * Clear any data structure.
	 */
	void clear();

}
