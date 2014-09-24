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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Class representing the EVENT entity of the data model.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class Event implements IModelElement, ISearchable, IGroupable {

	public static final int UNKNOWN_INT = -1;

	private final int id;
	private EventProducer eventProducer;
	private EventType eventType;
	private long timestamp;
	private int cpu;
	private int page;
	private List<EventParam> params;

	// Paje model support
	protected int category;
	private long longPar;
	private double doublePar;

	/**
	 * 
	 * @return the event category
	 */
	public int getCategory() {
		return category;
	}

	/**
	 * To be used by subclasses. 
	 * Note that the category should be set BEFORE setting the event type,
	 * which must have the same category.
	 * @param category category to set in subclasses.
	 * @throws SoCTraceException if an event type with a different category 
	 * has been already set.
	 */
	public void setCategory(int category) throws SoCTraceException {
		if (eventType!=null)
			if (category!=eventType.getCategory())
				throw new SoCTraceException("Event category and event type category differ!");
		this.category = category;
	}

	/**
	 * @return the longPar
	 */
	public long getLongPar() {
		return longPar;
	}

	/**
	 * @param longPar the longPar to set
	 */
	public void setLongPar(long longPar) {
		this.longPar = longPar;
	}

	/**
	 * @return the doublePar
	 */
	public double getDoublePar() {
		return doublePar;
	}

	/**
	 * @param doublePar the doublePar to set
	 */
	public void setDoublePar(double doublePar) {
		this.doublePar = doublePar;
	}

	/**
	 * Static factory method
	 * @param category event category
	 * @param id event id
	 * @return an event of the given category
	 */
	public static Event createCategorizedEvent(int category, int id) {
		switch(category) {
		case EventCategory.PUNCTUAL_EVENT:
			return new PunctualEvent(id);
		case EventCategory.LINK:
			return new Link(id);
		case EventCategory.STATE:
			return new State(id);
		case EventCategory.VARIABLE:
			return new Variable(id);
		default:
			return new Event(id);
		}
	}

	/**
	 * Constructor 
	 * @param id the entity unique id
	 */
	public Event(int id) {
		this.id = id;
		params = new ArrayList<EventParam>();
		timestamp = UNKNOWN_INT;
		cpu = 0;
		page = 0;
		longPar = UNKNOWN_INT;
		doublePar = UNKNOWN_INT;
		category = EventCategory.PUNCTUAL_EVENT;
	}

	/**
	 * @return the eventProducer
	 */
	public EventProducer getEventProducer() {
		return eventProducer;
	}

	/**
	 * @param eventProducer the eventProducer to set
	 */
	public void setEventProducer(EventProducer eventProducer) {
		this.eventProducer = eventProducer;
	}

	/**
	 * @return the eventType
	 */
	public EventType getType() {
		return eventType;
	}

	/**
	 * @param type the eventType to set, which must have the same category as the event.
	 * @throws SoCTraceException if the event type category differs from the category
	 * of the event (which must be set before setting the type).
	 */
	public void setType(EventType type) throws SoCTraceException {
		if (type!=null && type.getCategory()!=category)
			throw new SoCTraceException("Event category and event type category differ! "
					+ "Have you set the right category in the Event?");
		this.eventType = type;
	}

	/**
	 * @return the timestamp
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @param timestamp the timestamp to set
	 */
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * @return the CPU
	 */
	public int getCpu() {
		return cpu;
	}

	/**
	 * @param cpu the CPU to set
	 */
	public void setCpu(int cpu) {
		this.cpu = cpu;
	}

	/**
	 * @return the page
	 */
	public int getPage() {
		return page;
	}

	/**
	 * @param page the page to set
	 */
	public void setPage(int page) {
		this.page = page;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * This method has protected visibility in order to 
	 * prevent clients to call it. This method should be 
	 * called only by {@link EventParam#setEvent()}.
	 * 
	 * @param eventParam the event parameter to add
	 */
	protected void addEventParam(EventParam eventParam) {
		params.add(eventParam);
	}	

	/**
	 * 
	 * @return the event params
	 */
	public List<EventParam> getEventParams() {
		return params;
	}

	@Override
	public String toString() {
		return "Event[(id:" + id + "),(event_type.name:'" + getType().getName() + "'),"
				+ "(timestamp:" + timestamp + "),(cpu:" + cpu + "),(category:" + category + "),"
				+ "(longPar:" + longPar + "),(doublePar:" + doublePar + ")]";
	}

	@Override
	public void accept(IModelVisitor visitor) throws SoCTraceException {
		visitor.visit(this);
	}

	/**
	 * Get a Map : event param name <-> event param reference.
	 * The map is built on the fly.
	 * @return the map of event parameters
	 */
	public Map<String, EventParam> getParamMap() {
		Map<String, EventParam> map = new HashMap<String, EventParam>();
		for (EventParam param : params) {
			map.put(param.getEventParamType().getName(), param);
		}
		return map;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + category;
		result = prime * result + cpu;
		long temp;
		temp = Double.doubleToLongBits(doublePar);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((eventProducer == null) ? 0 : eventProducer.hashCode());
		result = prime * result + ((eventType == null) ? 0 : eventType.hashCode());
		result = prime * result + id;
		result = prime * result + (int) (longPar ^ (longPar >>> 32));
		result = prime * result + page;
		result = prime * result + ((params == null) ? 0 : params.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
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
		if (!(obj instanceof Event))
			return false;
		Event other = (Event) obj;
		if (category != other.category)
			return false;
		if (cpu != other.cpu)
			return false;
		if (Double.doubleToLongBits(doublePar) != Double.doubleToLongBits(other.doublePar))
			return false;
		if (eventProducer == null) {
			if (other.eventProducer != null)
				return false;
		} else if (!eventProducer.equals(other.eventProducer))
			return false;
		if (eventType == null) {
			if (other.eventType != null)
				return false;
		} else if (!eventType.equals(other.eventType))
			return false;
		if (id != other.id)
			return false;
		if (longPar != other.longPar)
			return false;
		if (page != other.page)
			return false;
		if (params == null) {
			if (other.params != null)
				return false;
		} else if (!params.equals(other.params))
			return false;
		if (timestamp != other.timestamp)
			return false;
		return true;
	}

	/**
	 * Check that both the event producer and the event type
	 * are set. If it is not the case, an exception is thrown.
	 * @throws SoCTraceException if the event producer or the event type
	 * are not set.
	 */
	public void check() throws SoCTraceException {
		if (eventProducer == null)
			throw new SoCTraceException("null event producer");
		if (eventType == null)
			throw new SoCTraceException("null event type");
	}
	
	/**
	 * Debug methods
	 */

	/**
	 * Print the event, without the event params.
	 */
	public void print() {
		print(false);
	}

	/**
	 * Print the event.
	 * @param verbose if true, also the event params are printed
	 */
	public void print(boolean verbose) {	
		System.out.println(toString());
		if (!verbose)
			return;
		System.out.println("  " + eventProducer);
		for (EventParam ep: params) {
			System.out.println("  " + ep.toString());
		}
	}
}
