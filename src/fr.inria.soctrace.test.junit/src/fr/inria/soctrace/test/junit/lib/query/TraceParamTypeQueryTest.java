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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.inria.soctrace.lib.model.TraceParamType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.TraceParamTypeQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.test.junit.utils.BaseSystemDBTest;
import fr.inria.soctrace.test.junit.utils.TestConstants;
import fr.inria.soctrace.test.junit.utils.importer.VirtualImporter;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceParamTypeQueryTest extends BaseSystemDBTest {

	private TraceParamTypeQuery query;

	@Before
	public void setUp() throws SoCTraceException {
		query = new TraceParamTypeQuery(sysDB);
	}

	@After
	public void tearDown() {
		query.clear();
		query = null;
	}

	@Test
	public final void testGetList() throws SoCTraceException {
		List<TraceParamType> elist = query.getList();
		assertEquals(TestConstants.NUMBER_OF_TRACE_PARAM_TYPES, elist.size());
	}

	@Test
	public final void testSetElementWhere() throws SoCTraceException {
		LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		and.addCondition(new SimpleCondition("TYPE", ComparisonOperation.EQ,
				VirtualImporter.PARAMETER_TYPE));
		// remove the first
		and.addCondition(new SimpleCondition("NAME", ComparisonOperation.NOT_LIKE,
				VirtualImporter.PARAMETER_NAME_PREFIX + "0"));
		query.setElementWhere(and);
		List<TraceParamType> elist = query.getList();
		assertEquals(TestConstants.NUMBER_OF_TRACE_PARAM_TYPES - 1, elist.size());
		for (TraceParamType e : elist) {
			assertEquals(VirtualImporter.PARAMETER_TYPE, e.getType());
			assertTrue(!e.getName().equals(VirtualImporter.PARAMETER_NAME_PREFIX + "0"));
		}
	}

	@Test
	public final void testSetOrderBy() throws SoCTraceException {
		query.setOrderBy("NAME", OrderBy.DESC);
		List<TraceParamType> elist = query.getList();
		boolean first = true;
		String last = "";
		for (TraceParamType ept : elist) {
			if (first) {
				first = false;
				last = ept.getName();
				continue;
			}
			assertTrue(ept.getName().compareTo(last) <= 0);
			last = ept.getName();
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
		assertEquals(TestConstants.NUMBER_OF_TRACE_PARAM_TYPES, query.getList().size());
	}

	@Test
	public final void testSetLimit() throws SoCTraceException {
		int limits[] = { TestConstants.NUMBER_OF_TRACE_PARAM_TYPES / 10, TestConstants.NUMBER_OF_TRACE_PARAM_TYPES / 5, TestConstants.NUMBER_OF_TRACE_PARAM_TYPES / 2 };
		for (int limit : limits) {
			query.setLimit(limit);
			assertEquals(limit, query.getList().size());
		}
		query.unsetLimit();
		assertEquals(TestConstants.NUMBER_OF_TRACE_PARAM_TYPES, query.getList().size());
	}

}
