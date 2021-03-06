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
package fr.inria.soctrace.framesoc.core.bus;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to manage Framesoc Bus topics.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FramesocBusTopicList {

	private List<FramesocBusTopic> topics = new ArrayList<>();
	IFramesocBusListener listener;
	
	/**
	 * Constructor
	 * @param listener the Framesoc Bus listener
	 */
	public FramesocBusTopicList(IFramesocBusListener listener) {
		this.listener = listener;
	}
	
	/**
	 * Add the topic to the list without registering.
	 * @param topic topic of interest
	 */
	public void addTopic(FramesocBusTopic topic) {
		topics.add(topic);
	}
	
	/**
	 * Register all the topic that are in the list.
	 */
	public void registerAll(){
		for (FramesocBusTopic topic: topics) {
			FramesocBus.getInstance().register(topic, listener);
		}
	}
	
	/**
	 * Unregister all the topic that are in the list.
	 */
	public void unregisterAll(){
		for (FramesocBusTopic topic: topics) {
			FramesocBus.getInstance().unregister(topic, listener);
		}
	}
	
}
