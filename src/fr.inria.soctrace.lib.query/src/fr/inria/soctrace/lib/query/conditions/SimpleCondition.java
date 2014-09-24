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

import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;

/**
 * Simple expression describing a condition involving a 
 * simple table attribute.
 * The expression has the format:
 * 		<attribute_name> <comparison_operator> <attribute_value>
 * 		(e.g. TIMESTAMP < 1000000 )
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class SimpleCondition implements ICondition {

	private String name;
	private ComparisonOperation op;
	private String value;
	
	/**
	 * Constructor.
	 * 
	 * @param name column name
	 * @param op comparison operator
	 * @param value column value to compare with
	 */
	public SimpleCondition(String name, ComparisonOperation op, String value) {
		this.name = name;
		this.op = op;
		this.value = value;
	}

	@Override
	public String getSQLString() {
		// fix for IN/BETWEEN operator: no ''
		if (op==ComparisonOperation.IN || op==ComparisonOperation.BETWEEN)
			return "( " + name + op.toString() + value +" )";
		return "( " + name + op.toString() + "'"+value+"' )";
	}

}
