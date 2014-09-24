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
package fr.inria.soctrace.framesoc.core.bus;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Framesoc Notification Bus singleton.
 * 
 * <p>
 * This notification bus is used to exchange events among
 * UI modules.
 * The singleton enables also context variable storage.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FramesocBus {
	
	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(FramesocBus.class);
	
	/**
	 * Listeners map: topic - list of listeners
	 */
	private Map<String, List<IFramesocBusListener>> listeners;
	
	/**
	 * Variables map
	 */
	private Map<String, Object> variables;
	
	/**
	 * Single instance of the FB
	 */
	private static FramesocBus instance = null;
		
	/**
	 * Instance getter
	 * @return the manager instance
	 */
	public static FramesocBus getInstance() {
		if (instance == null)
			instance = new FramesocBus();
		return instance;
	}

	/**
	 * Send a notification on the Framesoc Bus
	 * @param topic topic of the notification
	 * @param data data of the notification (may be null)
	 */
	public void send(String topic, Object data) {
		logger.debug("send {} for topic {}", data, topic);		
		if (listeners.containsKey(topic)) {
			List<IFramesocBusListener> list = listeners.get(topic);
			for (IFramesocBusListener listener: list) {
				try {
					listener.handle(topic, data);
				} catch (Exception e) {
					// Continue handling the event for other listeners
					// even if one of them produces an unchecked exception
					logger.debug("Exception in listener " + listener);
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Register a listener for a given topic notification
	 * @param topic topic of the notification
	 * @param listener Framesoc Bus listener
	 */
	public void register(String topic, IFramesocBusListener listener) {
		logger.debug("register {} for topic {}", listener, topic);
		if (!listeners.containsKey(topic)) {
			listeners.put(topic, new LinkedList<IFramesocBusListener>());
		}
		if (!listeners.get(topic).contains(listener))
			listeners.get(topic).add(listener);
	}
	
	/**
	 * Unregister a listener from a given topic notification
	 * @param topic topic of the notification
	 * @param listener Framesoc Bus listener
	 */
	public void unregister(String topic, IFramesocBusListener listener) {
		logger.debug("unregister {} for topic {}", listener, topic);
		if (listeners.containsKey(topic)) {
			List<IFramesocBusListener> list = listeners.get(topic);
			list.remove(listener);
			if (list.size() == 0) 
				listeners.remove(topic);
		} 
	}

	/**
	 * Set a variable.
	 * @param name variable name
	 * @param value variable value
	 */
	public synchronized void setVariable(String name, Object value) {
		logger.debug("set variable: name={}, value={}", name, value);
		variables.put(name, value);
	} 
	
	/**
	 * Get a given variable. May return null if the variable is not set.
	 * @param name variable name
	 * @return the variable value, or null if not set
	 */
	public synchronized Object getVariable(String name) {
		logger.debug("get variable: name={}, value={}", name, variables.get(name));
		return variables.get(name);
	}
	
	/**
	 * Private constructor. Prevents instantiation.
	 */
	private FramesocBus() {
		logger.debug("instance created");
		listeners = new HashMap<String, List<IFramesocBusListener>>();
		variables = new HashMap<String, Object>();
	};
	
}
