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
/**
 * 
 */
package fr.inria.soctrace.lib.query.distribution;

import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * Implementation of the {@link HistogramLoader} interface.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
class HistogramLoaderImpl implements HistogramLoader {

	private TraceDBObject traceDB;
	private HEventIterator iterator;
	private long minTs = Long.MIN_VALUE;
	private long maxTs = Long.MIN_VALUE;
	
	/**
	 * Constructor.
	 * @param traceDB the trace DB object, the ownership is to the client. 
	 */
	public HistogramLoaderImpl(TraceDBObject traceDB, HEventIterator iterator) {
		if (traceDB == null)
			throw new IllegalArgumentException("Null DB object passed");
		if (iterator == null)
			throw new IllegalArgumentException("Null iterator passed");
		this.traceDB = traceDB;
		this.iterator = iterator;
	}

	@Override
	public Histogram loadHistogram(long startTimestamp, long endTimestamp, List<EventType> types, int buckets) throws SoCTraceException {

		// resolve timestamps if needed
		boolean allTrace = true;
		if (startTimestamp == MIN_TIMESTAMP)
			startTimestamp = getMinTimestamp();
		else 
			allTrace = false;
		if (endTimestamp == MAX_TIMESTAMP)
			endTimestamp = getMaxTimestamp();
		else 
			allTrace = false;

		// check arguments
		if (startTimestamp >= endTimestamp)
			throw new IllegalArgumentException("Start-timestamp must be smaller than end-timestamp");
		if (startTimestamp < 0)
			throw new IllegalArgumentException("Start timestamp must be positive");
		if (endTimestamp < 0)
			throw new IllegalArgumentException("End timestamp must be positive");
		if (types==null || types.isEmpty())
			throw new IllegalArgumentException("Null or empty list of event types passed");
		if (buckets<MIN_BUCKETS || buckets>MAX_BUCKETS)
			throw new IllegalArgumentException("Illegal number of buckets");
		
		// load histogram
		Long duration = endTimestamp - startTimestamp;
		// for small durations, check the buckets value
		if (duration <= Integer.MAX_VALUE)
			buckets = Math.min(buckets, duration.intValue()); 
		long uppers[] = new long[buckets];
		long delta = Math.max((endTimestamp-startTimestamp+1)/(buckets), 1);
		uppers[buckets-1] = endTimestamp;		
		for (int i=buckets-2; i>=0; i--) {
			uppers[i] = uppers[i+1] - delta;	
		}
		Histogram histogram = DistributionFactory.INSTANCE.createHistogram(uppers);
		iterator.clear();
		iterator.setTraceDB(traceDB);
		iterator.setTypes(types);
		if (!allTrace)
			iterator.setTimestamps(startTimestamp, endTimestamp);
		while(iterator.hasNext()) {
			HEvent eh = iterator.getNext();
			if (eh==null)
				break;
			histogram.addObservation(eh.timestamp);
		}
		iterator.clear();
		return histogram;
	}

	@Override
	public Histogram loadHistogram(long startTimestamp, long endTimestamp, EventType type, int buckets) throws SoCTraceException {
		if (type == null)
			throw new IllegalArgumentException("Null type passed");
		List<EventType> types = new LinkedList<EventType>();
		types.add(type);
		return loadHistogram(startTimestamp, endTimestamp, types, buckets);
	}

	@Override
	public long getMinTimestamp() throws SoCTraceException {
		if (minTs == Long.MIN_VALUE)
			minTs = Math.max(traceDB.getMinTimestamp(), 0); 
		return minTs;
	}

	@Override
	public long getMaxTimestamp() throws SoCTraceException {
		if (maxTs == Long.MIN_VALUE)
			maxTs = Math.max(traceDB.getMaxTimestamp(), 0); 
		return maxTs;
	}
	
	/**
	 * Debug print
	 * @param verbose 
	 */
	public void print(HistogramImpl histogram, boolean verbose){
		System.out.println(histogram);
		if (!verbose)
			return;
		for (int i=0; i<histogram.getSize(); i++) {
			StringBuilder sb = new StringBuilder();
			for (int j=0; j<histogram.getCountAt(i); j++) {
				sb.append("*");
			}
			System.out.println(sb.toString());
		}
	}

}
