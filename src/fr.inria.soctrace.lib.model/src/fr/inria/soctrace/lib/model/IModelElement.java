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

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Interface implemented by all data model entity, as defined in 
 * {@link fr.inria.soctrace.lib.model.utils.ModelConstants.ModelEntity}.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public interface IModelElement {
	
	/** 
	 * Get the ID of the element. This ID represent the 
	 * entity unique ID in the database.
	 * 
	 * @return the element ID.
	 */
	public int getId();
	
	/**
	 * Visitor accept method.
	 * 
	 * @param visitor the visitor
	 * @throws SoCTraceException 
	 */
	public void accept(IModelVisitor visitor) throws SoCTraceException;
		
}
