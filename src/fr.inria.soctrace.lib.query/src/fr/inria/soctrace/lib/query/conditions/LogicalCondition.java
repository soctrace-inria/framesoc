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
package fr.inria.soctrace.lib.query.conditions;

import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;

/**
 * Class representing a logical expression (AND/OR)
 * involving at least two other expressions.
 * 
 * Expressions are used to describe conditions dealing with 
 * simple table attributes.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class LogicalCondition implements ICondition {

	private LogicalOperation op;
	private List<ICondition> conditions;
	
	public LogicalCondition(LogicalOperation op) {
		conditions = new LinkedList<ICondition>();
		this.op = op;
	}
	
	public void addCondition(ICondition e) {
		conditions.add(e);
	}
	
	public int getNumberOfConditions() {
		return conditions.size();
	}
		
	@Override
	public String getSQLString() throws SoCTraceException {
		
		if (conditions.size()<2)
			throw new SoCTraceException("Missing condition in logical expression " + op.toString());
		
		String operation = " " + op.toString() + " ";
		StringBuilder sb = new StringBuilder("( ");
		boolean first = true;
		
		for (ICondition e: conditions) {
			if (!first) {
				sb.append(operation);
			} else {
				first = false;
			}
			sb.append(e.getSQLString());
		}
		sb.append(" )");
		
		return sb.toString();
	}
}
