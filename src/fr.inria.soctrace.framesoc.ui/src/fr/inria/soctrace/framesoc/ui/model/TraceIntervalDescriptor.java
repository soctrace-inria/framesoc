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
package fr.inria.soctrace.framesoc.ui.model;

import fr.inria.soctrace.lib.model.Trace;

/**
 * Trace time interval descriptor. It is used for inter-view communication.
 * 
 * Only views belonging to the same group will synchronize using this message.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceIntervalDescriptor {

	/**
	 * Trace to be processed
	 */
	private Trace trace;

	/**
	 * Timestamp at which we start processing
	 */
	private long startTimestamp;

	/**
	 * Timestamp at which we end processing
	 */
	private long endTimestamp;

	/**
	 * View group. A group of view is a group of different view related to the same trace that will
	 * synchronize over the Pub/Sub.
	 */
	private int group;

	/**
	 * @return the trace
	 */
	public Trace getTrace() {
		return trace;
	}

	/**
	 * @param trace
	 *            the trace to set
	 */
	public void setTrace(Trace trace) {
		this.trace = trace;
	}

	/**
	 * @return the startTimestamp
	 */
	public long getStartTimestamp() {
		return startTimestamp;
	}

	/**
	 * @param startTimestamp
	 *            the startTimestamp to set
	 */
	public void setStartTimestamp(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	/**
	 * @return the endTimestamp
	 */
	public long getEndTimestamp() {
		return endTimestamp;
	}

	/**
	 * @param endTimestamp
	 *            the endTimestamp to set
	 */
	public void setEndTimestamp(long endTimestamp) {
		this.endTimestamp = endTimestamp;
	}

	/**
	 * Set the time interval
	 * 
	 * @param interval
	 *            time interval to set
	 */
	public void setTimeInterval(TimeInterval interval) {
		this.startTimestamp = interval.startTimestamp;
		this.endTimestamp = interval.endTimestamp;
	}

	/**
	 * Get the time interval
	 * 
	 * @return the time interval
	 */
	public TimeInterval getTimeInterval() {
		return new TimeInterval(this.startTimestamp, this.endTimestamp);
	}

	/**
	 * @return the group
	 */
	public int getGroup() {
		return group;
	}

	/**
	 * @param group
	 *            the group to set
	 */
	public void setGroup(int group) {
		this.group = group;
	}

	@Override
	public String toString() {
		return "TraceIntervalDescriptor [trace=" + trace + ", startTimestamp=" + startTimestamp
				+ ", endTimestamp=" + endTimestamp + ", group=" + group + "]";
	}

}
