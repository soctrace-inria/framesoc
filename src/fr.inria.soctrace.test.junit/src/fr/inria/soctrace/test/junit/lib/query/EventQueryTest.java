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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.SelfDefiningElementQuery.ParamType;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.ParamLogicalCondition;
import fr.inria.soctrace.lib.query.conditions.ParamSimpleCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.test.junit.utils.BaseTraceDBTest;
import fr.inria.soctrace.test.junit.utils.importer.VirtualImporter;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class EventQueryTest extends BaseTraceDBTest {

	private EventQuery query;

	@Before
	public void setUp() throws SoCTraceException {
		query = new EventQuery(traceDB);
	}

	@After
	public void tearDown() {
		query.clear();
		query = null;
	}

	@Test
	public final void testClear() throws SoCTraceException {
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, "1"));

		// before clear
		assertEquals(1, query.getList().size());
		assertEquals(1, query.getList().size());

		// after clear
		query.clear();
		assertEquals(VirtualImporter.TOTAL_NUMBER_OF_EVENTS, query.getList().size());
	}

	@Test
	public final void testGetParamType() throws SoCTraceException {
		ParamType type = query.getParamType(VirtualImporter.PARAMETER_NAME_PREFIX + 0, 0);
		assertEquals(0, type.id);
		assertEquals(VirtualImporter.PARAMETER_NAME_PREFIX + 0, type.name);
		assertEquals(VirtualImporter.PARAMETER_TYPE, type.type);
	}

	@Test
	public final void testGetTypeId() throws SoCTraceException {
		assertEquals(0, query.getTypeId(VirtualImporter.TYPE_NAME_PREFIX+"0"));
		assertEquals(-1, query.getTypeId("non existing type"));
	}

	@Test
	public final void testGetElementTableName() {
		assertEquals(FramesocTable.EVENT.toString(), query.getElementTableName());
	}

	@Test
	public final void testGetDBObject() {
		assertSame(traceDB, query.getDBObject());
	}

	@Test
	public final void testSetTypeWhere() throws SoCTraceException {
		query.setTypeWhere(new SimpleCondition("NAME", ComparisonOperation.EQ,
				VirtualImporter.TYPE_NAME_PREFIX + "0"));
		List<Event> elist = query.getList();
		assertEquals(VirtualImporter.NUMBER_OF_PRODUCERS * VirtualImporter.NUMBER_OF_EVENTS,
				elist.size());
		for (Event e : elist) {
			assertEquals(VirtualImporter.TYPE_NAME_PREFIX + "0", e.getType().getName());
		}
	}

	@Test
	public final void testSetEventProducerWhere() throws SoCTraceException {
		LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		and.addCondition(new SimpleCondition("TYPE", ComparisonOperation.EQ,
				VirtualImporter.PRODUCER_TYPE));
		and.addCondition(new SimpleCondition("LOCAL_ID", ComparisonOperation.EQ,
				VirtualImporter.PRODUCER_LOCAL_ID_PREFIX + "0"));
		query.setEventProducerWhere(and);
		List<Event> elist = query.getList();
		assertEquals(VirtualImporter.NUMBER_OF_CATEGORIES * VirtualImporter.NUMBER_OF_TYPES
				* VirtualImporter.NUMBER_OF_EVENTS, elist.size());
		for (Event e : elist) {
			assertEquals(VirtualImporter.PRODUCER_TYPE, e.getEventProducer().getType());
			assertEquals(VirtualImporter.PRODUCER_LOCAL_ID_PREFIX + "0", e.getEventProducer()
					.getLocalId());
		}
	}

	@Test
	public final void testGetList() throws SoCTraceException {
		List<Event> elist = query.getList();
		assertEquals(VirtualImporter.TOTAL_NUMBER_OF_EVENTS, elist.size());
	}

	@Test
	public final void testAddParamCondition() throws SoCTraceException {
		ParamLogicalCondition and = new ParamLogicalCondition(LogicalOperation.AND);
		for (int i = 0; i < VirtualImporter.NUMBER_OF_PARAMETERS; i++) {
			and.addCondition(new ParamSimpleCondition(VirtualImporter.PARAMETER_NAME_PREFIX + i,
					ComparisonOperation.EQ, VirtualImporter.PARAMETER_VALUE));
		}
		query.addParamCondition(VirtualImporter.TYPE_NAME_PREFIX + "0", and);
		List<Event> elist = query.getList();
		assertEquals(VirtualImporter.NUMBER_OF_PRODUCERS*VirtualImporter.NUMBER_OF_EVENTS, elist.size());
		for (Event e : elist) {
			assertEquals(VirtualImporter.TYPE_NAME_PREFIX + "0", e.getType().getName());
			Map<String, EventParam> params = e.getParamMap();
			for (int i = 0; i < VirtualImporter.NUMBER_OF_PARAMETERS; i++) {
				assertEquals(VirtualImporter.PARAMETER_VALUE,
						params.get(VirtualImporter.PARAMETER_NAME_PREFIX + i).getValue());
			}
		}
	}

	@Test
	public final void testSetElementWhere() throws SoCTraceException {
		LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		and.addCondition(new SimpleCondition("CPU", ComparisonOperation.EQ, String.valueOf(VirtualImporter.CPU)));
		and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.GE, String.valueOf(VirtualImporter.MIN_TIMESTAMP)));
		query.setElementWhere(and);
		List<Event> elist = query.getList();
		assertEquals(VirtualImporter.TOTAL_NUMBER_OF_EVENTS, elist.size());
		for (Event e : elist) {
			assertTrue(e.getTimestamp() >= VirtualImporter.MIN_TIMESTAMP);
			assertEquals(VirtualImporter.CPU, e.getCpu());
		}
	}

	@Test
	public final void testSetOrderBy() throws SoCTraceException {
		query.setOrderBy("TIMESTAMP", OrderBy.DESC);
		List<Event> elist = query.getList();
		long last = Long.MAX_VALUE;
		for (Event e : elist) {
			assertTrue(e.getTimestamp() <= last);
			last = e.getTimestamp();
		}
	}

	@Test
	public final void testSetLoadParameters() throws SoCTraceException {
		List<Event> elist = query.getList();
		int params = 0;
		Map<Long, Event> emap = new HashMap<>();
		for (Event e : elist) {
			params += e.getEventParams().size();
			emap.put(e.getId(), e);
		}
		// at least one param to ensure test utility
		assertTrue(params > 0);

		query.clear();
		query.setLoadParameters(false);
		List<Event> elistNoPar = query.getList();
		for (Event e : elistNoPar) {
			assertEquals(0, e.getEventParams().size());
		}

		query.clear();
		query.loadParams(elistNoPar);
		int paramsLoaded = 0;
		for (Event e : elistNoPar) {
			Map<String, EventParam> epm = e.getParamMap();
			Map<String, EventParam> epmRef = emap.get(e.getId()).getParamMap();
			for (String pname : epmRef.keySet()) {
				paramsLoaded++;
				assertTrue(epm.containsKey(pname));
				assertEquals(epmRef.get(pname), epm.get(pname));
			}
		}
		assertEquals(params, paramsLoaded);

	}

	@Test
	public final void testSetLimit() throws SoCTraceException {
		int events = VirtualImporter.TOTAL_NUMBER_OF_EVENTS;
		int limits[] = { events / 10, events / 5, events / 2 };
		for (int limit : limits) {
			query.setLimit(limit);
			assertEquals(limit, query.getList().size());
		}
		query.unsetLimit();
		assertEquals(events, query.getList().size());
	}

}
