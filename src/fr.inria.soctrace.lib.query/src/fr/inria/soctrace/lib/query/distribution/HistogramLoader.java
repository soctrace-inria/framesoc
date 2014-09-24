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

import java.util.List;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Interface for distribution histogram loader.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface HistogramLoader {

	/**
	 * Constant for min timestamp.
	 * Note: it is *not* the value of the min timestamp
	 * but only an enumeration for this.
	 */
	static final long MIN_TIMESTAMP = Long.MIN_VALUE;
	
	/**
	 * Constant for max timestamp.
	 * Note: it is *not* the value of the max timestamp
	 * but only an enumeration for this. 
	 */
	static final long MAX_TIMESTAMP = Long.MIN_VALUE+1;
	
	/**
	 * Minimum number of buckets
	 */
	static final int MIN_BUCKETS = 2;
	
	/**
	 * Maximum number of buckets
	 */
	static final int MAX_BUCKETS = Integer.MAX_VALUE;
	
	/**
	 * Load an histogram containing the distribution of one or more event types
	 * over a trace time interval, using a given number of histogram buckets.
	 * Start and end timestamp must be either one of the predefined values
	 * (MIN_TIMESTAMP and MAX_TIMSTAMP) or positive values.
	 * 
	 * @param startTimestamp start timestamp
	 * @param endTimestamp end timestamp
	 * @param types list of event types
	 * @param buckets number of buckets
	 * @return the distribution histograms
	 * @throws SoCTraceException 
	 */
	Histogram loadHistogram(long startTimestamp, long endTimestamp, List<EventType> types, int buckets) 
			throws SoCTraceException;
	
	/**
	 * Load an histogram containing the distribution of a single event type
	 * over a trace time interval, using a given number of histogram buckets.
	 * Start and end timestamp must be either one of the predefined values
	 * (MIN_TIMESTAMP and MAX_TIMSTAMP) or positive values.
	 * 
	 * @param startTimestamp start timestamp
	 * @param endTimestamp end timestamp
	 * @param type an event type
	 * @param buckets number of buckets
	 * @return the distribution histograms
	 * @throws SoCTraceException 
	 */
	Histogram loadHistogram(long startTimestamp, long endTimestamp, EventType type, int buckets) 
			throws SoCTraceException;
	
	/**
	 * Return the minimum positive timestamp of the trace.
	 * @return the min timestamp
	 * @throws SoCTraceException 
	 */
	long getMinTimestamp() throws SoCTraceException;
	
	/**
	 * Return the maximum positive timestamp of the trace
	 * @return the max timestamp
	 * @throws SoCTraceException 
	 */
	long getMaxTimestamp() throws SoCTraceException;

}
