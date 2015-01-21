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
/**
 * 
 */
package fr.inria.soctrace.lib.query.distribution;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Example of typical usage of distribution classes.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TypicalUsage {

	private static Logger logger = LoggerFactory.getLogger(TypicalUsage.class);
	
	/**
	 * Note: the following code is only an example
	 * and will not work 
	 * @param args
	 * @throws SoCTraceException 
	 */
	public static void main(String[] args) throws SoCTraceException {
		
		DeltaManager dm = new DeltaManager();
		dm.start();
		if (args.length<1)
			throw new IllegalArgumentException("missing db name");
		String dbname = args[0];
		
		// -----------------------------------------------------------------------------------
		
		// 1. open the trace DB
		TraceDBObject traceDB = TraceDBObject.openNewIstance(dbname);

		// 2. prepare the type (or the types) you are interested in
		List<EventType> etl = getEventTypesOfInterest(traceDB);

		// -----------------------------------------------------------------------------------
		
		// 3. create the loader
		HistogramLoader loader = DistributionFactory.INSTANCE.createHistogramLoader(traceDB);
		
		// 4. load the histogram
		Histogram histogram = loader.loadHistogram(HistogramLoader.MIN_TIMESTAMP, 
				HistogramLoader.MAX_TIMESTAMP, etl, 1000);
		
		// -----------------------------------------------------------------------------------
		
		logger.debug(dm.endMessage("End"));
		logger.debug("Histogram for all event types (Observations: "+ histogram.getCount() +")");
		logger.debug(histogram.toString());	
	}
	
	/*
	 * In this simple example, we want all the types.
	 */
	@SuppressWarnings({ "unchecked" })
	private static List<EventType> getEventTypesOfInterest(TraceDBObject traceDB) throws SoCTraceException {
		List<EventType> etl = new LinkedList<EventType>();
		etl.addAll((Collection<? extends EventType>) traceDB.getEventTypeCache().getElementMap(EventType.class).values());
		return etl;		
	}

}
