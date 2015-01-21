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
package fr.inria.soctrace.lib.query.iterators;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Interface for Event iterators.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface EventIterator {

	/**
	 * Get the next event, or null if not present.
	 * 
	 * @return the next event, or null if no next event.
	 * @throws SoCTraceException
	 */
	public Event getNext() throws SoCTraceException;
	
	/**
	 * @return true if there is a following event, false otherwise
	 * @throws SoCTraceException 
	 */
	public boolean hasNext() throws SoCTraceException;
	
	/**
	 * Release resources, if any. 
	 * After calling this function, the iterator
	 * cannot be used anymore.
	 * 
	 * @throws SoCTraceException 
	 */
	public void clear() throws SoCTraceException;
	
}
