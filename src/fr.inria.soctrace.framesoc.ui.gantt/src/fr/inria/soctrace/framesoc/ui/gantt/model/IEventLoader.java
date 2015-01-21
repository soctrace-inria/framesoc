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
package fr.inria.soctrace.framesoc.ui.gantt.model;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;

/**
 * Interface for gantt event loaders.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface IEventLoader {

	/**
	 * Set the trace the loader must work with.
	 * 
	 * @param trace
	 *            trace
	 */
	void setTrace(Trace trace);

	/**
	 * Set the queue where the lists of events must be pushed.
	 * 
	 * @param queue
	 *            loader queue
	 */
	void setQueue(LoaderQueue<ReducedEvent> queue);

	/**
	 * Get the trace event producers.
	 * 
	 * @return a map between event producer id and event producers
	 */
	Map<Integer, EventProducer> getProducers();

	/**
	 * Get the trace event types
	 * 
	 * @return a map between event type id and event types
	 */
	Map<Integer, EventType> getTypes();

	/**
	 * Load a time window for the trace set using {@link #setTrace}, filling the queue set using
	 * {@link #setQueue()}. It has to be called in a Job, whose progress monitor is passed.
	 * 
	 * The contract is that the loader must call either {@link LoaderQueue#setComplete()} or
	 * {@link LoaderQueue#setStop()} at the end of its operations. This prevents any thread waiting
	 * for data to wait indefinitely.
	 * 
	 * @param start
	 *            start timestamp
	 * @param end
	 *            end timestamp
	 * @param monitor
	 *            progress monitor
	 */
	void loadWindow(long start, long end, IProgressMonitor monitor);

	/**
	 * Release all the loader resources and clean all internal data structures. This method should
	 * be called when we are finished using the loader.
	 */
	void release();

}
