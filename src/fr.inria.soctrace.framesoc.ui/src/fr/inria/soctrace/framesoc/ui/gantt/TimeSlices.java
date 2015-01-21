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
package fr.inria.soctrace.framesoc.ui.gantt;

import java.util.TreeMap;

import fr.inria.soctrace.lib.model.Event;

/**
 * Utility class to manage several time slices ({@link TimeSlice})
 * according to the Paje algorithm for gantt data structure management.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TimeSlices {
	
	private TreeMap<Long, TimeSlice> slices = new TreeMap<Long, TimeSlice>();
	
	public void addTimeSlice(TimeSlice t) {
		slices.put(t.getStart(), t);
	}
	
	/**
	 * Add the event in the right time slice
	 * @param e
	 */
	public void addEvent(Event e) {
		TimeSlice ts = slices.floorEntry(e.getTimestamp()).getValue();
		ts.addEvent(e);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("TIME SLICES \n");
		for (TimeSlice s: slices.values()) {
			sb.append(s.toString());
		}
		return sb.toString();
	}
	
	// XXX
	public TreeMap<Long, TimeSlice> getSlices() {
		return slices;
	}

	public void clear() {
		slices.clear();
	}

	public int size() {
		int s = 0;
		for (TimeSlice ts: slices.values()) {
			s+=ts.size();
		}
		return s;
	}
}
