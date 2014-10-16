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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (endTimestamp ^ (endTimestamp >>> 32));
		result = prime * result + (int) (startTimestamp ^ (startTimestamp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TimeInterval other = (TimeInterval) obj;
		if (endTimestamp != other.endTimestamp)
			return false;
		if (startTimestamp != other.startTimestamp)
			return false;
		return true;
	}
	
}
