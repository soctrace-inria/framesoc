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

/**
 * Interface for all Framesoc Notification Bus listeners.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface IFramesocBusListener {
	
	/**
	 * Handle a notification for a given topic containing given data. 
	 * The type of the data depends on the topic.
	 * Topics are documented in {@link FramesocBusTopic}.
	 * 
	 * @param topic notification topic
	 * @param data notification data
	 */
	void handle(FramesocBusTopic topic, Object data);

}
