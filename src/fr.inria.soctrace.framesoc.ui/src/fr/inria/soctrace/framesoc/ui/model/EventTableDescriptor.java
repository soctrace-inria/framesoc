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

/**
 * This class is an extension of TraceIntervalDescriptor for the event table in
 * order to focus ion a specific event
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 *
 */
public class EventTableDescriptor extends TraceIntervalDescriptor {

	String eventProducerName;
	String typeName;
	String cpu;
	String category;
	long eventStartTimeStamp;

	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public long getEventStartTimeStamp() {
		return eventStartTimeStamp;
	}

	public void setEventStartTimeStamp(long eventStartTimeStamp) {
		this.eventStartTimeStamp = eventStartTimeStamp;
	}

	public String getEventProducerName() {
		return eventProducerName;
	}
	
	public void setEventProducerName(String eventProducerName) {
		this.eventProducerName = eventProducerName;
	}
	
	public String getTypeName() {
		return typeName;
	}
	
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	
}
