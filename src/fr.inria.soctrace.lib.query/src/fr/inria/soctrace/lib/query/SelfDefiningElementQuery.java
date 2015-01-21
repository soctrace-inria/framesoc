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
package fr.inria.soctrace.lib.query;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.conditions.IParamCondition;
import fr.inria.soctrace.lib.storage.DBObject;

/**
 * Base abstract class to perform queries over elements implemented 
 * in the DB with the 4-tables self-defining-pattern.
 * Provides the support to deal with parameters conditions.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public abstract class SelfDefiningElementQuery extends ElementQuery {
	
	/**
	 * The constructor
	 * @param dbObject DB object to work with
	 */
	public SelfDefiningElementQuery(DBObject dbObject) {
		super(dbObject);
	}

	/**
	 * Conditions to be put in the where clause, referring to
	 * the parameters (xParam) of the element.
	 */
	protected Map<Integer, IParamCondition> parametersConditions;

	/**
	 * Clear all the conditions.
	 */
	@Override
	public void clear() {
		super.clear();
		this.parametersConditions = new HashMap<Integer, IParamCondition>();
	}

	/**
	 * Add a parameter condition for a given type of element.
	 * @param typeName name of the element type (e.g. event type name)
	 * @param condition condition related to the parameters of this type
	 * @throws SoCTraceException
	 */
	public void addParamCondition(String typeName, IParamCondition condition) throws SoCTraceException {
		where = true;
		int typeId = getTypeId(typeName);
		parametersConditions.put(typeId, condition);
	}
	
	/**
	 * Get the ParamType, given the xParamType name and the xType id.
	 * @param paramTypeName param type name
	 * @param typeId type id
	 * @return the corresponding ParamType
	 * @throws SoCTraceException
	 */
	public abstract ParamType getParamType(String paramTypeName, int typeId) throws SoCTraceException;
	
	/**
	 * Get the xType id, given the name
	 * @param typeName xType name
	 * @return the corresponding xType id
	 * @throws SoCTraceException
	 */
	public abstract int getTypeId(String typeName) throws SoCTraceException;
		
	/**
	 * 
	 * @return The element table name (e.g. Event, Trace, ..)
	 */
	public abstract String getElementTableName();
	
	/**
	 * 
	 * @return The DB object where the element table is.
	 */
	public abstract DBObject getDBObject();
		
	/**
	 * Create the string to be put in the WHERE clause of the Element table
	 * corresponding to the parametersConditions.
	 * This composite condition is composed by 0 or more "ID IN (..)",
	 * where each group of ID is related to a single parameter simple condition.
	 * 
	 * @return the part of the where clause related to parameter conditions
	 * @throws SoCTraceException
	 */
	protected String getParamConditionsString() throws SoCTraceException {
		Iterator<Map.Entry<Integer, IParamCondition>> it = parametersConditions.entrySet().iterator();
		
		boolean firstType = true;
		StringBuilder sb = new StringBuilder("( ");
		while (it.hasNext()) {
			if (!firstType) 
				sb.append(" OR ");
			else
				firstType = false;
			Map.Entry<Integer, IParamCondition> pairs = (Map.Entry<Integer, IParamCondition>)it.next();
			sb.append(pairs.getValue().getSQLString(this, pairs.getKey()));
		}
		sb.append(" )"); 
		return sb.toString();
	}
	
	/**
	 * Utility class. Internal use only.
	 */
	public class ParamType {
		
		public int id;
		public String name;
		public String type;

		public ParamType(int id, String name, String type) {
			this.id = id;
			this.name = name;
			this.type = type;
		}
	}
	
}
