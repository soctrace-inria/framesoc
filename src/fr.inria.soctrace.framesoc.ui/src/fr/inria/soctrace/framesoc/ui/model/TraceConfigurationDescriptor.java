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

import java.util.List;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;

/**
 * This class extends TraceIntervalDescriptor in order to transmit more
 * information for synchronization between views.
 * 
 * This include the list of not filtered event producers, or event type, if a
 * particular event should be focused on
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class TraceConfigurationDescriptor extends TraceIntervalDescriptor {

	// List of event producers to be used 
	private List<EventProducer> eventProducers;
	
	// List of event types to be used
	private List<EventType> eventTypes;
	
	/**
	 * An event producer on which the focus should be on
	 */
	private EventProducer eventProducer = null;

	public List<EventProducer> getEventProducers() {
		return eventProducers;
	}

	public void setEventProducers(List<EventProducer> eventProducers) {
		this.eventProducers = eventProducers;
	}

	public List<EventType> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<EventType> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public EventProducer getEventProducer() {
		return eventProducer;
	}

	public void setEventProducer(EventProducer eventProducer) {
		this.eventProducer = eventProducer;
	}
	
}
