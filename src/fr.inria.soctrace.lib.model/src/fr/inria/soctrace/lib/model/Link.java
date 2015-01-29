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

import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Class representing events of LINK category.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class Link extends Event {

	private EventProducer endEventProducer = null; 
	
	public Link(long id) {
		super(id);
		try {
			setCategory(EventCategory.LINK);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return the end timestamp
	 */
	public long getEndTimestamp() {
		return getLongPar();
	}

	/**
	 * @param endTimestamp the end timestamp to set
	 */
	public void setEndTimestamp(long endTimestamp) {
		setLongPar(endTimestamp);
	}

	/**
	 * @return the end event producer
	 */
	public EventProducer getEndProducer() {
		return endEventProducer;
	}

	/**
	 * @param ep the end event producer to set
	 */
	public void setEndProducer(EventProducer ep) {
		this.endEventProducer = ep;
		setDoublePar(((Long)ep.getId()).doubleValue());
	}
	
	@Override
	public void check() throws SoCTraceException {
		super.check();
		if(endEventProducer==null)
			throw new SoCTraceException("null end event producer");
	}

}
