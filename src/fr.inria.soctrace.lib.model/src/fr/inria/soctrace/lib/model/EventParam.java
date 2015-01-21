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
package fr.inria.soctrace.lib.model;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Class representing the EVENT_PARAM entity of the data model.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class EventParam implements IModelElement {

	private final int id;
	private Event event;
	private EventParamType eventParamType;
	private String value;
	
	/**
	 * @return the event
	 */
	public Event getEvent() {
		return event;
	}

	/**
	 * Note: the EventParam is added to the Event params list.
	 * @param event the event to set
	 */
	public void setEvent(Event event) {
		this.event = event;
		this.event.addEventParam(this);
	}

	/**
	 * @return the eventParamType
	 */
	public EventParamType getEventParamType() {
		return eventParamType;
	}

	/**
	 * @param eventParamType the eventParamType to set
	 */
	public void setEventParamType(EventParamType eventParamType) {
		this.eventParamType = eventParamType;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Constructor 
	 * @param id the entity unique id
	 */
	public EventParam(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "EventParam[(id:" + getId() + ")," +
				"(event.id:" + getEvent().getId() + ")," +
				"(value:\"" + getValue() + "\")," +
				"(event_param_type.name:\"" + getEventParamType().getName() + "\"," +
				"event_param_type.type:\"" + getEventParamType().getType() + "\")]";
	}

	@Override
	public void accept(IModelVisitor visitor) throws SoCTraceException {
		visitor.visit(this);
	}

	/* Note to equals and hashCode.
	 * 
	 * Compare only ID and VALUE to avoid recursive check.
	 */
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof EventParam))
			return false;
		EventParam other = (EventParam) obj;
		if (id != other.id)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}	
	
}
