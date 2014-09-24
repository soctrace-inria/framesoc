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

import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * Singleton factory to create histograms and histogram loaders.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public enum DistributionFactory {

	INSTANCE;
	
	/**
	 * Create a concrete instance of the Histogram Loader
	 * @param traceDB the traceDB object
	 * @return a histogram loader
	 */
	public HistogramLoader createHistogramLoader(TraceDBObject traceDB) {
		return new HistogramLoaderImpl(traceDB, new HEventIdBlockIteratorImpl());
		//return new HistogramLoaderImpl(traceDB, new HEventBlockIteratorImpl());
		//return new HistogramLoaderImpl(traceDB, new HEventPageIteratorImpl());
	}

	/**
	 * Create a concrete instance of the Histogram Loader.
	 * To be used only if a custom implementation of the 
	 * HEventIterator must be used.
	 * Use this only if you know what you are doing.
	 * 
	 * @param traceDB the traceDB object
	 * @param iterator an HEventIterator
	 * @return a histogram loader
	 */
	public HistogramLoader createHistogramLoader(TraceDBObject traceDB, HEventIterator iterator) {
		return new HistogramLoaderImpl(traceDB, iterator);
	}
	
	/**
	 * Create a concrete instance of the Histogram with a provided list of interval bounds.
	 * By interval bounds we mean the upper values of each of the buckets/bars/bins,
	 * as explained in {@link Histogram} interface documentation.
     *
     * @param upperBounds of the intervals. Bounds must be provided in order least to greatest, and
     * lowest bound must be greater than or equal to 1.
     * @throws IllegalArgumentException if any of the upper bounds are less than or equal to zero
     * @throws IllegalArgumentException if the bounds are not in order, least to greatest
     */
	public Histogram createHistogram(final long[] upperBounds) {
		return new HistogramImpl(upperBounds);
	}

}
