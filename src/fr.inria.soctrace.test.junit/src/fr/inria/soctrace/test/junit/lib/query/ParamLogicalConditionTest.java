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
package fr.inria.soctrace.test.junit.lib.query;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.ParamLogicalCondition;
import fr.inria.soctrace.lib.query.conditions.ParamSimpleCondition;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.test.junit.utils.BaseTraceDBTest;

public class ParamLogicalConditionTest extends BaseTraceDBTest {

	@Test
	public final void testAddCondition() throws SoCTraceException {
		ParamLogicalCondition condition = new ParamLogicalCondition(LogicalOperation.OR);
		boolean first = true;
		StringBuilder sb = new StringBuilder("( ");
		for (ComparisonOperation op: ComparisonOperation.values()) {
			
			if (first)
				first = false;
			else 
				sb.append("OR ");
			sb.append("ID IN ( " + IdManager.RESERVED_NO_ID + " ) ");

			if (op.equals(ComparisonOperation.IN)) 
				condition.addCondition(new ParamSimpleCondition("A", op, "( 1, 2, 3 )"));
			else if (op.equals(ComparisonOperation.BETWEEN)) 
				condition.addCondition(new ParamSimpleCondition("A", op, "1 AND 4"));
			else  
				condition.addCondition(new ParamSimpleCondition("A", op, "1"));
			
		}
		sb.append(")");
				
		EventQuery query = new EventQuery(traceDB);
		assertEquals(sb.toString(), condition.getSQLString(query, 0));
		query.clear();
	}

}
