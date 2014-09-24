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

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventTypeQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.test.junit.utils.BaseTraceDBTest;
import fr.inria.soctrace.test.junit.utils.importer.VirtualImporter;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EventTypeQueryTest extends BaseTraceDBTest {

	private EventTypeQuery query;

	@Before
	public void setUp() throws SoCTraceException {
		query = new EventTypeQuery(traceDB);
	}

	@After
	public void tearDown() {
		query.clear();
		query = null;
	}

	@Test
	public final void testGetList() throws SoCTraceException {
		List<EventType> elist = query.getList();
		assertEquals(VirtualImporter.NUMBER_OF_TYPES * VirtualImporter.NUMBER_OF_CATEGORIES,
				elist.size());
	}

	@Test
	public final void testSetElementWhere() throws SoCTraceException {
		LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		and.addCondition(new SimpleCondition("CATEGORY", ComparisonOperation.EQ, String
				.valueOf(EventCategory.PUNCTUAL_EVENT)));
		and.addCondition(new SimpleCondition("NAME", ComparisonOperation.LIKE,
				VirtualImporter.TYPE_NAME_PREFIX + "%"));
		query.setElementWhere(and);
		List<EventType> elist = query.getList();
		boolean hasPuncEvents = VirtualImporter.CATEGORIES.contains(EventCategory.PUNCTUAL_EVENT);
		int numberOfTypes = (hasPuncEvents ? 1 : 0) * VirtualImporter.NUMBER_OF_TYPES;
		assertEquals(numberOfTypes, elist.size());
		for (EventType e : elist) {
			assertEquals(EventCategory.PUNCTUAL_EVENT, e.getCategory());
			assertTrue(e.getName().contains(VirtualImporter.TYPE_NAME_PREFIX));
		}
	}

	@Test
	public final void testSetOrderBy() throws SoCTraceException {
		query.setOrderBy("NAME", OrderBy.DESC);
		List<EventType> elist = query.getList();
		boolean first = true;
		String last = "";
		for (EventType ept : elist) {
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
		assertEquals(VirtualImporter.NUMBER_OF_TYPES * VirtualImporter.NUMBER_OF_CATEGORIES, query
				.getList().size());
	}

	@Test
	public final void testSetLimit() throws SoCTraceException {
		int types = VirtualImporter.NUMBER_OF_TYPES * VirtualImporter.NUMBER_OF_CATEGORIES;
		int limits[] = { types / 10, types / 5, types / 2 };
		for (int limit : limits) {
			query.setLimit(limit);
			assertEquals(limit, query.getList().size());
		}
		query.unsetLimit();
		assertEquals(types, query.getList().size());
	}
}
