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

import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.TraceTypeQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.test.junit.utils.BaseSystemDBTest;
import fr.inria.soctrace.test.junit.utils.TestConstants;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceTypeQueryTest extends BaseSystemDBTest {

	private TraceTypeQuery query;

	@Before
	public void setUp() throws SoCTraceException {
		query = new TraceTypeQuery(sysDB);
	}

	@After
	public void tearDown() {
		query.clear();
		query = null;
	}

	@Test
	public final void testGetList() throws SoCTraceException {
		List<TraceType> elist = query.getList();
		assertEquals(TestConstants.NUMBER_OF_TRACE_TYPES, elist.size());
	}

	@Test
	public final void testSetElementWhere() throws SoCTraceException {
		query.setElementWhere(new SimpleCondition("NAME", ComparisonOperation.EQ, String
				.valueOf(TestConstants.PROCESSED_TRACE_TYPE_NAME)));
		List<TraceType> elist = query.getList();
		assertEquals(1, elist.size());
		for (TraceType e : elist) {
			assertEquals(TestConstants.PROCESSED_TRACE_TYPE_NAME, e.getName());
		}
	}

	@Test
	public final void testSetOrderBy() throws SoCTraceException {
		query.setOrderBy("NAME", OrderBy.DESC);
		List<TraceType> elist = query.getList();
		boolean first = true;
		String last = "";
		for (TraceType ept : elist) {
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
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, "0"));

		// before clear
		assertEquals(1, query.getList().size());
		assertEquals(1, query.getList().size());

		// after clear
		query.clear();
		assertEquals(TestConstants.NUMBER_OF_TRACE_TYPES, query.getList().size());
	}

	@Test
	public final void testSetLimit() throws SoCTraceException {
		int types = TestConstants.NUMBER_OF_TRACE_TYPES;
		int limits[] = { types / 10, types / 5, types / 2 };
		for (int limit : limits) {
			query.setLimit(limit);
			assertEquals(limit, query.getList().size());
		}
		query.unsetLimit();
		assertEquals(types, query.getList().size());
	}
}
