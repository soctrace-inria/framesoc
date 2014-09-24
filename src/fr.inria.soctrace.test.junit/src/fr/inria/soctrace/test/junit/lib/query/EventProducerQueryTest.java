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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.ValueListString;
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
public class EventProducerQueryTest extends BaseTraceDBTest {

	private EventProducerQuery query;

	@Before
	public void setUp() throws SoCTraceException {
		query = new EventProducerQuery(traceDB);
	}

	@After
	public void tearDown() {
		query.clear();
		query = null;
	}

	@Test
	public final void testGetList() throws SoCTraceException {
		List<EventProducer> elist = query.getList();
		assertEquals(VirtualImporter.NUMBER_OF_PRODUCERS, elist.size());
	}

	@Test
	public final void testSetElementWhere() throws SoCTraceException {
		LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		and.addCondition(new SimpleCondition("TYPE", ComparisonOperation.EQ, VirtualImporter.PRODUCER_TYPE));
		ValueListString vls = new ValueListString();
		vls.setQuotes(true);
		Set<String> localIds = new HashSet<>();
		for (int i = 0; i < VirtualImporter.NUMBER_OF_PRODUCERS / 3; i++) {
			localIds.add(VirtualImporter.PRODUCER_LOCAL_ID_PREFIX + i);
			vls.addValue(VirtualImporter.PRODUCER_LOCAL_ID_PREFIX + i);
		}
		if (vls.size()!=0) {
			and.addCondition(new SimpleCondition("LOCAL_ID", ComparisonOperation.IN, vls.getValueString()));
			query.setElementWhere(and);
			List<EventProducer> elist = query.getList();
			assertEquals(localIds.size(), elist.size());
			for (EventProducer e : elist) {
				assertEquals(VirtualImporter.PRODUCER_TYPE, e.getType());
				assertTrue(localIds.contains(e.getLocalId()));
			}
		}
	}

	@Test
	public final void testSetOrderBy() throws SoCTraceException {
		query.setOrderBy("ID", OrderBy.DESC);
		List<EventProducer> elist = query.getList();
		int last = Integer.MAX_VALUE;
		for (EventProducer e : elist) {
			assertTrue(e.getId() <= last);
			last = e.getId();
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
		assertEquals(VirtualImporter.NUMBER_OF_PRODUCERS, query.getList().size());
	}

	@Test
	public final void testSetLimit() throws SoCTraceException {
		int limits[] = { VirtualImporter.NUMBER_OF_PRODUCERS / 10,
				VirtualImporter.NUMBER_OF_PRODUCERS / 5, VirtualImporter.NUMBER_OF_PRODUCERS / 2 };
		for (int limit : limits) {
			query.setLimit(limit);
			assertEquals(limit, query.getList().size());
		}
		query.unsetLimit();
		assertEquals(VirtualImporter.NUMBER_OF_PRODUCERS, query.getList().size());
	}

}
