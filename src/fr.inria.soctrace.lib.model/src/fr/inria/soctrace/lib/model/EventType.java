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
package fr.inria.soctrace.lib.model;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Class representing the EVENT_TYPE entity of the data model.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class EventType implements IModelElement, IGroupable {

	private final int id;
	private final int category;
	private String name;
	private List<EventParamType> paramTypes;


	/**
	 * Constructor 
	 * @param id the entity unique id
	 * @param category the category
	 */
	public EventType(int id, int category) {
		this.id = id;
		this.category = category;
		paramTypes = new ArrayList<EventParamType>();
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return the category
	 */
	public int getCategory() {
		return category;
	}

	/**
	 * This method has protected visibility in order to 
	 * prevent clients to call it. This method should be 
	 * called only by {@link EventParamType#setEventType()}.
	 * 
	 * @param eventParamType
	 */
	protected void addEventParamType(EventParamType eventParamType) {
		paramTypes.add(eventParamType);
	}
	
	/**
	 * 
	 * @return the event param types
	 */
	public List<EventParamType> getEventParamTypes() {
		return paramTypes;
	}	
	
	@Override
	public String toString() {
		return "EventType[(id:" + getId() + "),(category:" + getCategory() + "), (name:\"" + getName() + "\")]";
	}
	
	@Override
	public void accept(IModelVisitor visitor) throws SoCTraceException {
		visitor.visit(this);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + category;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((paramTypes == null) ? 0 : paramTypes.hashCode());
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
		if (!(obj instanceof EventType))
			return false;
		EventType other = (EventType) obj;
		if (id != other.id)
			return false;
		if (category != other.category)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (paramTypes == null) {
			if (other.paramTypes != null)
				return false;
		} else if (!paramTypes.equals(other.paramTypes))
			return false;
		return true;
	}
	
}
