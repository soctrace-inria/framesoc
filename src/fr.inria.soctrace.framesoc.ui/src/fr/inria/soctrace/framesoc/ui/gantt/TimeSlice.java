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

import java.util.Comparator;
import java.util.TreeSet;

import fr.inria.soctrace.lib.model.Event;

/**
 * Utility class to manage events having a duration
 * (states, links).
 * 
 * <p>
 * See Paje algorithm for gantt data structure management.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TimeSlice {

	private long start;
	
	private TreeSet<Event> events = new TreeSet<Event>(new Comparator<Event>() {
		@Override
		public int compare(Event o1, Event o2) {
			if(o1.getLongPar() > o2.getLongPar())
				return 1;
			return -1; 
			// if equals, take the second
			// this allows multiple events for the same end time
		}
	});
	
	public TimeSlice(long start) {
		this.start = start;
	}
	
	public void addEvent(Event e) {
		events.add(e);
	}
	
	
	// XXX
	public TreeSet<Event> getEvents() {
		return events;
	}

	public long getStart() {
		return start;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Slice starting at "+ start + "\n");
		for (Event e: events) {
			sb.append(e.toString() +"\n");
		}
		return sb.toString();
	}

	public int size() {
		return events.size();
	}
}
