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
package fr.inria.soctrace.framesoc.ui.model;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TimeInterval {

	public long startTimestamp;
	
	public long endTimestamp;

	public TimeInterval(long start, long end) {
		this.startTimestamp = start;
		this.endTimestamp = end;
	}

	public TimeInterval(TimeInterval interval) {
		this.copy(interval);
	}

	public void copy(TimeInterval interval) {
		this.startTimestamp = interval.startTimestamp;
		this.endTimestamp = interval.endTimestamp;
	}
	
	@Override
	public String toString() {
		return "TimeInterval [startTimestamp=" + startTimestamp + ", endTimestamp=" + endTimestamp
				+ "]";
	}
	
}
