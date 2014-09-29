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
package fr.inria.soctrace.framesoc.ui.eventtable.model;

import fr.inria.soctrace.framesoc.ui.model.TableRow;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.Link;
import fr.inria.soctrace.lib.model.State;
import fr.inria.soctrace.lib.model.Variable;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;

/**
 * Model element for a row in the Event table
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class EventTableRow extends TableRow {

	private long timestamp;

	/**
	 * Constructor used to create a table row related to a given event.
	 * @param event the event
	 */
	public EventTableRow(Event event) {
		timestamp = event.getTimestamp();
		fields.put(EventTableColumn.TIMESTAMP, String.valueOf(event.getTimestamp()));
		fields.put(EventTableColumn.CPU, String.valueOf(event.getCpu()));
		fields.put(EventTableColumn.PRODUCER_NAME, event.getEventProducer().getName());
		fields.put(EventTableColumn.CATEGORY, EventCategory.categoryToString(event.getCategory()));
		fields.put(EventTableColumn.TYPE_NAME, event.getType().getName());
		StringBuilder tmp = new StringBuilder();
		boolean first = true;


		switch(event.getCategory()) {
		case EventCategory.PUNCTUAL_EVENT:
			break;
		case EventCategory.STATE:
			first = false;
			State state = (State) event;
			tmp.append("END_TIMESTAMP='" + state.getEndTimestamp() + "'");
			tmp.append(", ");
			tmp.append("IMBRICATION='" + state.getImbricationLevel() + "'");
			break;
		case EventCategory.LINK:
			first = false;
			Link link = (Link) event;
			tmp.append("END_TIMESTAMP='" + link.getEndTimestamp() + "'");
			tmp.append(", ");
			tmp.append("END_PRODUCER='" + link.getEndProducer().getName() + "'");
			break;
		case EventCategory.VARIABLE:
			first = false;
			Variable var = (Variable) event;
			tmp.append("ID='" + var.getId() + "'");
			tmp.append(", ");
			tmp.append("VALUE='" + var.getValue() + "'");				
			break;				
		}


		for (EventParam ep: event.getEventParams()) {
			if (first) { 
				first = false;
			} else {
				tmp.append(", ");
			}
			tmp.append(ep.getEventParamType().getName() + "='" + ep.getValue()+"'");
		}
		fields.put(EventTableColumn.PARAMS, tmp.toString());
	}

	/**
	 * Empty table row, to be used for filters.
	 */
	public EventTableRow() {
		fields.put(EventTableColumn.TIMESTAMP, "a"+((long)(Math.random()*100)));
		fields.put(EventTableColumn.CPU, "a");
		fields.put(EventTableColumn.PRODUCER_NAME, "ddddddddddddddddddddddddddddddddddddddddddddddddddddda"+((long)(Math.random()*100)));
		fields.put(EventTableColumn.CATEGORY, "asssssssssssssssssssssssssssssssssssssssssssss");
		fields.put(EventTableColumn.TYPE_NAME, "a");
		fields.put(EventTableColumn.PARAMS, "a");
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}
	
	/**
	 * 
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
