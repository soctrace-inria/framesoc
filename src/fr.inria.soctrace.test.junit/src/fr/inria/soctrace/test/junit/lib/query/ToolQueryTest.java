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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.inria.soctrace.framesoc.core.FramesocConstants;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.ToolQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.test.junit.utils.BaseSystemDBTest;
import fr.inria.soctrace.test.junit.utils.TestConstants;

/**
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ToolQueryTest extends BaseSystemDBTest {
	
	private ToolQuery query;
	
	@Before
	public void setUp() throws SoCTraceException {
		query = new ToolQuery(sysDB);	
	}

	@After
	public void tearDown() {
		query.clear();
		query = null;
	}

	@Test
	public final void testGetList() throws SoCTraceException {
		List<Tool> tlist = query.getList();
		assertEquals(2, tlist.size());
	}

	@Test
	public final void testSetElementWhere() throws SoCTraceException {
		query.setElementWhere(new SimpleCondition("NAME", ComparisonOperation.EQ, TestConstants.JUNIT_TEST_TOOL_NAME));
		List<Tool> tlist = query.getList();
		assertEquals(1, tlist.size());
		
		Tool t = tlist.iterator().next();
		assertEquals(TestConstants.JUNIT_TEST_TOOL_NAME, t.getName());
		assertEquals(TestConstants.JUNIT_TEST_TOOL_COMMAND, t.getCommand());
		assertEquals(TestConstants.JUNIT_TEST_TOOL_DOC, t.getDoc());
		assertEquals(FramesocConstants.FramesocToolType.ANALYSIS.toString(), t.getType());
	}

	@Test
	public final void testSetOrderBy() throws SoCTraceException {
		query.setOrderBy("ID", OrderBy.DESC);
		List<Tool> tlist = query.getList();
		int last = Integer.MAX_VALUE;
		for (Tool t: tlist) {
			assertTrue(t.getId()<=last);
			last = t.getId();
		}
	}

	@Test
	public final void testClear() throws SoCTraceException {
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, "1"));
		
		// before clear
		assertEquals(1, query.getList().size());
		assertEquals(1, query.getList().size());

		// after clear
		query.clear();
		assertEquals(2, query.getList().size());
	}
	
	@Test
	public final void testSetLimit() throws SoCTraceException {
		int limits[] = { TestConstants.NUMBER_OF_TOOLS / 10, TestConstants.NUMBER_OF_TOOLS / 5,
				TestConstants.NUMBER_OF_TOOLS / 2 };
		for (int limit : limits) {
			query.setLimit(limit);
			assertEquals(limit, query.getList().size());
		}
		query.unsetLimit();
		assertEquals(TestConstants.NUMBER_OF_TOOLS, query.getList().size());
	}

}
