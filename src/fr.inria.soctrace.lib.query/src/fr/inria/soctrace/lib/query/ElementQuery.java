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
package fr.inria.soctrace.lib.query;

import java.util.List;

import fr.inria.soctrace.lib.model.IModelElement;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.query.conditions.ICondition;
import fr.inria.soctrace.lib.storage.DBObject;

/**
 * Base abstract class to perform queries over Elements 
 * (DB Entities or Tables) stored in the DB.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public abstract class ElementQuery  {
	
	/**
	 * Enable debug message printing.
	 */
	private boolean debug = false;

	/**
	 * DB object where the query is performed
	 */
	protected DBObject dbObj;
	
	/**
	 * Boolean stating if the query has a where clause
	 */
	protected boolean where;
	
	/**
	 * Condition to be put in the where clause, referring to
	 * the element table attributes.
	 */
	protected ICondition elementWhere;

	/**
	 * Boolean stating if the query has a order by clause
	 */
	protected boolean orderBy;
	
	/**
	 * Column to be used in order by clause
	 */
	protected String orderByColumn;
	
	/**
	 * Order by criterium
	 */
	protected OrderBy orderByCriterium;
	
	/**
	 * Limit in the number of object retrieved (-1 means no limit).
	 */
	protected int limit;
	
	/**
	 * The constructor
	 * @param dbObject DB object to work with
	 */
	public ElementQuery(DBObject dbObject) {
		this.dbObj = dbObject;
		this.where = false;
		this.orderBy = false;
		this.limit = -1;
	}
	
	/**
	 * Set the condition to be put in the WHERE clause, referring
	 * to the attributes of the element table.
	 * @param elementCondition WHERE clause condition
	 */
	public void setElementWhere(ICondition elementCondition) {
		where = true;
		this.elementWhere = elementCondition;
	}
	
	/**
	 * Set the order by clause
	 * @param column column to use in order by clause
	 * @param criterium order by criterium
	 */
	public void setOrderBy(String column, OrderBy criterium) {
		this.orderBy = true;
		this.orderByColumn = column;
		this.orderByCriterium = criterium;
	}
	
	/**
	 * Set the limit
	 * @param limit limit to set
	 */
	public void setLimit(int limit) {
		this.limit = limit;
	}
	
	/**
	 * Get the limit
	 * @return the limit
	 */
	public int getLimit() {
		return limit;
	}
	
	/**
	 * Unset the limit
	 */
	public void unsetLimit() {
		this.limit = -1;
	}
	
	/**
	 * Return true if an actual limit is set.
	 * @return true if an actual limit is set
	 */
	public boolean isLimitSet() {
		return this.limit!=-1;
	}
	
	/**
	 * Clear all the conditions.
	 * 
	 * Each subclass implementation should call super.clear()
	 * at the beginning.
	 */
	public void clear() {
		this.limit = -1;
		this.where = false;
		this.orderBy = false;
		this.elementWhere = null;
		this.orderByColumn = null;
		this.orderByCriterium = null;
	}
	
	/**
	 * Get a list of ITraceElements respecting all the conditions specified.
	 * @return a list of ITraceElements
	 */
	public abstract List<? extends IModelElement> getList() throws SoCTraceException;
	
	/**
	 * Print a debug message if the debug flag is activated.
	 * 
	 * @param msg debug message
	 */
	protected void debug(String msg) {
		if (debug) 
			System.out.println(msg);
	}
}
