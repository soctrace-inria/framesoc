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
package fr.inria.soctrace.test.junit.lib.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;

public class LogicalConditionTest {

	@Test
	public final void testAddCondition() throws SoCTraceException {
		LogicalCondition condition = new LogicalCondition(LogicalOperation.OR);
		for (ComparisonOperation op: ComparisonOperation.values()) {
			if (op.equals(ComparisonOperation.IN)) 
				condition.addCondition(new SimpleCondition("A", op, "( 1, 2, 3 )"));
			else if (op.equals(ComparisonOperation.BETWEEN)) 
				condition.addCondition(new SimpleCondition("A", op, "1 AND 4"));
			else  
				condition.addCondition(new SimpleCondition("A", op, "1"));
		}
	}

	@Test
	public final void testGetNumberOfConditions() {
		LogicalCondition condition = new LogicalCondition(LogicalOperation.AND);
		condition.addCondition(new SimpleCondition("A", ComparisonOperation.EQ, "1"));
		condition.addCondition(new SimpleCondition("B", ComparisonOperation.NE, "2"));
		LogicalCondition or = new LogicalCondition(LogicalOperation.OR);
		or.addCondition(new SimpleCondition("C", ComparisonOperation.GE, "3"));
		or.addCondition(new SimpleCondition("D", ComparisonOperation.LE, "4"));
		condition.addCondition(or);
		assertEquals(3, condition.getNumberOfConditions());
		assertEquals(2, or.getNumberOfConditions());
	}

	@Test
	public final void testGetSQLString() throws SoCTraceException {
		LogicalCondition condition = new LogicalCondition(LogicalOperation.AND);
		condition.addCondition(new SimpleCondition("A", ComparisonOperation.EQ, "1"));
		condition.addCondition(new SimpleCondition("B", ComparisonOperation.NE, "2"));
		LogicalCondition or = new LogicalCondition(LogicalOperation.OR);
		or.addCondition(new SimpleCondition("C", ComparisonOperation.GE, "3"));
		or.addCondition(new SimpleCondition("D", ComparisonOperation.LE, "4"));
		condition.addCondition(or);
		assertEquals("( ( A = '1' ) AND ( B <> '2' ) AND ( ( C >= '3' ) OR ( D <= '4' ) ) )", 
				condition.getSQLString());
	}

}
