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
package fr.inria.soctrace.lib.query.conditions;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Interface representing a condition involving simple
 * table attributes.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public interface ICondition {
	
	/**
	 * Get the SQL expression related to this condition.
	 * Such expression is supposed to be used for the WHERE clause 
	 * of a table.
	 * 
	 * @return The SQL string corresponding to the condition
	 * @throws SoCTraceException 
	 */
	public String getSQLString() throws SoCTraceException;
	
}
