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
 * Utility class to manage punctual events.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PunctualEvents {

	private TreeMap<Long, Event> events = new TreeMap<Long, Event>();

	public void addEvent(Event e) {
		events.put(e.getTimestamp(), e);
	}
	
	public TreeMap<Long, Event> getEvents() {
		return events;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Event e: events.values()) {
			sb.append(e.toString() +"\n");
		}
		return sb.toString();
	}

	public void clear() {
		events.clear();
	}
	
	public int size() {
		return events.size();
	}
	
}
