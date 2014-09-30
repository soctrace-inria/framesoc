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
package fr.inria.soctrace.framesoc.ui.eventtable.loader;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import fr.inria.soctrace.framesoc.ui.model.TimeInterval;

/**
 * Queue for streaming operations between one loader thread and one consumer
 * thread. The elements being queued are lists of objects of a generic type.
 * 
 * The loader thread, at the end of its operations, must call either
 * {@link #setComplete()} or {@link #setStop()}, in order to avoid indefinite
 * waiting.
 * 
 * The queue keeps the status of the entire load operation.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class LoaderQueue<T> {

	private Queue<List<T>> queue = new LinkedList<>();
	private TimeInterval interval = null;
	private boolean complete = false;
	private boolean stop = false;

	/**
	 * Pop a list from the queue, blocking if the queue is empty and the loading
	 * is not completed and the loading has not been stopped.
	 * 
	 * @return the list corresponding to the head of the queue or an empty list
	 * @throws InterruptedException
	 */
	public synchronized List<T> pop() throws InterruptedException {
		while (queue.isEmpty() && !complete && !stop) {
			wait();
		}
		if (stop) {
			// wake up after stop
			return new ArrayList<>();
		}
		if (!queue.isEmpty()) {
			// wake up after push
			return queue.remove();
		}
		// wake up after completed with empty queue
		return new ArrayList<>();
	}

	/**
	 * Push a new list into the queue.
	 * 
	 * @param list
	 *            new list
	 * @param interval
	 *            loaded interval so far
	 */
	public synchronized void push(List<T> list, TimeInterval interval) {
		this.queue.add(list);
		this.interval = interval;
		notify();
	}

	/**
	 * Sets the flag indicating that the load operation has been stopped.
	 */
	public synchronized void setStop() {
		this.stop = true;
		notify();
	}

	/**
	 * Returns true if the load operation has been stopped.
	 * 
	 * @return true if the load operation has been stopped.
	 */
	public synchronized boolean isStop() {
		return this.stop;
	}

	/**
	 * Sets the flag indicating that the load operation has been completed.
	 */
	public synchronized void setComplete() {
		this.complete = true;
		notify();
	}

	/**
	 * Returns true if all the requested data has been pushed into the queue.
	 * Note that the fact that the load is complete, does not mean that the
	 * queue is empty.
	 * 
	 * @return true if the load operation is complete
	 */
	public synchronized boolean isComplete() {
		return this.complete;
	}

	/**
	 * Returns true when the entire load operation is done; i.e., the loading is
	 * complete and the queue is empty, or the loading has been stopped.
	 * 
	 * @return true if all the operations are finished
	 */
	public synchronized boolean done() {
		return (queue.isEmpty() && complete) || stop;
	}

	/**
	 * Get the time interval loaded so far.
	 * 
	 * @return the time interval loaded so far or null if no time interval has
	 *         been loaded yet.
	 */
	public synchronized TimeInterval getTimeInterval() {
		return interval;
	}
}
