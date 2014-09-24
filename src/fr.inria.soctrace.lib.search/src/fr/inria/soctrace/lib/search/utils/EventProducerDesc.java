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
package fr.inria.soctrace.lib.search.utils;

/**
 * Utility class containing the attributes that
 * uniquely identify an event EventProducer.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EventProducerDesc {

	public String type;
	public String local_id;
	
	/**
	 * @param type event producer type
	 * @param local_id event producer local identifier (e.g., for a process, the PID)
	 */
	public EventProducerDesc(String type, String local_id) {
		this.type = type;
		this.local_id = local_id;
	}
	
}
