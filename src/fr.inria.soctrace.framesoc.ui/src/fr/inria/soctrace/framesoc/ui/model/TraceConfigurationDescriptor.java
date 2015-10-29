/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Youenn Corre - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.model;

import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;

/**
 * This class extends TraceIntervalDescriptor in order to transmit more
 * information for synchronization between views.
 * 
 * This include the list of filtered event producers, or event types and if
 * a particular event should be focused on.
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class TraceConfigurationDescriptor extends TraceIntervalDescriptor {

	// List of filtered event producers (is typed as object since the views use
	// EventProducerNode --- except Gantt Chart)
	private List<Object> eventProducers = new ArrayList<Object>();

	// List of filtered event types (is typed as object since the views use
	// EventTypeNode)
	private List<Object> eventTypes = new ArrayList<Object>();
	
	/**
	 * The minimum amount of time unit to show around an event when focusing on
	 * it in the Gantt chart
	 */
	public static final int MIN_TIME_UNIT_SHOWING = 200;
	
	/**
	 * Should we focus on a particular event
	 */
	private boolean focusOnEvent = false;
	
	/**
	 * An event producer on which the focus should be on
	 */
	private EventProducer eventProducer = null;

	/*
	 * Getters and Setters
	 */
	public List<Object> getEventProducers() {
		return eventProducers;
	}

	public void setEventProducers(List<Object> eventProducers) {
		this.eventProducers = eventProducers;
	}

	public List<Object> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<Object> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public EventProducer getEventProducer() {
		return eventProducer;
	}

	public void setEventProducer(EventProducer eventProducer) {
		this.eventProducer = eventProducer;
	}

	public boolean isFocusOnEvent() {
		return focusOnEvent;
	}

	public void setFocusOnEvent(boolean focusOnEvent) {
		this.focusOnEvent = focusOnEvent;
	}
	
}
