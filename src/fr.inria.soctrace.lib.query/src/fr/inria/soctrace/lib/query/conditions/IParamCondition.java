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
import fr.inria.soctrace.lib.query.SelfDefiningElementQuery;

/**
 * Interface representing a condition involving simple
 * self-defining-elements parameters, belonging to the
 * same element-type.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public interface IParamCondition {
	
	/**
	 * Get the SQL expression related to this condition.
	 * Such expression will go in the WHERE clause of a query
	 * on a self-defining Element table.
	 * 
	 * @param access query object to which the condition is attached
	 * @param typeId id of the type of self-defining Element 
	 * @return the string containing the SQL expression
	 * @throws SoCTraceException
	 */
	public String getSQLString(SelfDefiningElementQuery access, int typeId) throws SoCTraceException;
	
}
