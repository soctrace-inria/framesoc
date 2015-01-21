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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceParam;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.SelfDefiningElementQuery.ParamType;
import fr.inria.soctrace.lib.query.TraceQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.ParamLogicalCondition;
import fr.inria.soctrace.lib.query.conditions.ParamSimpleCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.test.junit.utils.BaseSystemDBTest;
import fr.inria.soctrace.test.junit.utils.TestConstants;
import fr.inria.soctrace.test.junit.utils.importer.VirtualImporter;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceQueryTest extends BaseSystemDBTest {

	private TraceQuery query;
	
	@Before
	public void setUp() throws SoCTraceException {
		query = new TraceQuery(sysDB);	
	}

	@After
	public void tearDown() {
		query.clear();
		query = null;
	}
	
	@Test
	public final void testClear() throws SoCTraceException {
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(VirtualImporter.TRACE_ID)));
		
		// before clear
		assertEquals(1, query.getList().size());
		assertEquals(1, query.getList().size());

		// after clear
		query.clear();
		assertEquals(2, query.getList().size());
	}

	@Test
	public final void testGetParamType() throws SoCTraceException {
		ParamType type = query.getParamType(VirtualImporter.PARAMETER_NAME_PREFIX+0, VirtualImporter.TRACE_TYPE_ID);
		assertEquals(VirtualImporter.PARAMETER_NAME_PREFIX+0, type.name);
		assertEquals(VirtualImporter.PARAMETER_TYPE, type.type);
		
		type = query.getParamType(VirtualImporter.PARAMETER_NAME_PREFIX+1, VirtualImporter.TRACE_TYPE_ID);
		assertEquals(1, type.id);
		assertEquals(VirtualImporter.PARAMETER_NAME_PREFIX+1, type.name);
		assertEquals(VirtualImporter.PARAMETER_TYPE, type.type);
	}

	@Test
	public final void testGetTypeId() throws SoCTraceException {
		assertEquals(VirtualImporter.TRACE_TYPE_ID, query.getTypeId(VirtualImporter.TRACE_TYPE_NAME));
		assertEquals(TestConstants.PROCESSED_TRACE_TYPE_ID, query.getTypeId(TestConstants.PROCESSED_TRACE_TYPE_NAME));
		assertEquals(-1, query.getTypeId("NOT_FOUND"));	
	}

	@Test
	public final void testGetElementTableName() {
		assertEquals(FramesocTable.TRACE.toString(), query.getElementTableName());
	}

	@Test
	public final void testGetDBObject() {
		assertSame(sysDB, query.getDBObject());
	}

	@Test
	public final void testSetTypeWhere() throws SoCTraceException {
		query.setTypeWhere(new SimpleCondition("NAME", ComparisonOperation.EQ, VirtualImporter.TYPE_NAME_PREFIX+VirtualImporter.TRACE_TYPE_ID));
		List<Trace> elist = query.getList();
		assertEquals(1, elist.size());
		for (Trace e: elist) {
			assertEquals(VirtualImporter.TYPE_NAME_PREFIX+VirtualImporter.TRACE_TYPE_ID, e.getType().getName());
		}
	}

	@Test
	public final void testGetList() throws SoCTraceException {
		List<Trace> tlist = query.getList();
		assertEquals(TestConstants.NUMBER_OF_TRACES, tlist.size());
	}

	@Test
	public final void testGetTraces() throws SoCTraceException {
		Set<Integer> ids = new HashSet<Integer>();
		List<Integer> idl = new LinkedList<Integer>();
		idl.add(TestConstants.PROCESSED_TRACE_ID);
		idl.add(236432); // not present
		for (Integer i: idl)
			ids.add(i);
				
		List<Trace> tlist = query.getTraces(idl);
		assertEquals(1, tlist.size());
		for (Trace t: tlist) {
			assertTrue(ids.contains(t.getId()));
		}
	}

	@Test
	public final void testAddParamCondition() throws SoCTraceException {

		ParamLogicalCondition and = new ParamLogicalCondition(LogicalOperation.AND);
		for (int i=0; i<VirtualImporter.NUMBER_OF_PARAMETERS; i++) {
			and.addCondition(new ParamSimpleCondition(VirtualImporter.PARAMETER_NAME_PREFIX+i, ComparisonOperation.EQ, VirtualImporter.PARAMETER_VALUE));
		}
		query.addParamCondition(VirtualImporter.TRACE_TYPE_NAME, and);
		
		List<Trace> elist = query.getList();
		assertEquals(1, elist.size());
		for (Trace e: elist) {
			assertEquals(VirtualImporter.TRACE_TYPE_NAME, e.getType().getName());
			Map<String, TraceParam> params = e.getParamMap();
			for (int i=0; i<VirtualImporter.NUMBER_OF_PARAMETERS; i++) {
				assertEquals(VirtualImporter.PARAMETER_VALUE, params.get(VirtualImporter.PARAMETER_NAME_PREFIX+i).getValue());
			}
		}		
	}

	@Test
	public final void testSetElementWhere() throws SoCTraceException {
		LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		and.addCondition(new SimpleCondition("BOARD", ComparisonOperation.EQ, VirtualImporter.METADATA));
		and.addCondition(new SimpleCondition("OUTPUT_DEVICE", ComparisonOperation.EQ, VirtualImporter.METADATA));
		query.setElementWhere(and);
		List<Trace> elist = query.getList();
		assertEquals(1, elist.size());
		for (Trace e: elist) {
			assertEquals(VirtualImporter.METADATA, e.getBoard());
			assertEquals(VirtualImporter.METADATA, e.getOutputDevice());
		}
	}

	@Test
	public final void testMinMaxTimestamp() throws SoCTraceException {
		LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		and.addCondition(new SimpleCondition("MIN_TIMESTAMP", ComparisonOperation.EQ, String.valueOf(VirtualImporter.MIN_TIMESTAMP)));
		and.addCondition(new SimpleCondition("MAX_TIMESTAMP", ComparisonOperation.EQ, String.valueOf(VirtualImporter.getMaxTimestamp())));
		query.setElementWhere(and);
		assertEquals(1, query.getList().size());
		
		LogicalCondition and2 = new LogicalCondition(LogicalOperation.AND);
		and2.addCondition(new SimpleCondition("MIN_TIMESTAMP", ComparisonOperation.EQ, String.valueOf(VirtualImporter.MIN_TIMESTAMP+34)));
		and2.addCondition(new SimpleCondition("MAX_TIMESTAMP", ComparisonOperation.EQ, String.valueOf(VirtualImporter.MIN_TIMESTAMP+33)));
		query.setElementWhere(and2);
		assertEquals(0, query.getList().size());

	}
	
	@Test
	public final void testSetOrderBy() throws SoCTraceException {
		query.setOrderBy("NUMBER_OF_CPUS", OrderBy.DESC);
		List<Trace> tlist = query.getList();
		int last = Integer.MAX_VALUE;
		for (Trace t: tlist) {
			assertTrue(t.getNumberOfCpus()<=last);
			last = t.getNumberOfCpus();
		}
	}
	
	@Test
	public final void testSetLimit() throws SoCTraceException {
		int limits[] = { TestConstants.NUMBER_OF_TRACES / 10, TestConstants.NUMBER_OF_TRACES / 5,
				TestConstants.NUMBER_OF_TRACES / 2 };
		for (int limit : limits) {
			query.setLimit(limit);
			assertEquals(limit, query.getList().size());
		}
		query.unsetLimit();
		assertEquals(TestConstants.NUMBER_OF_TRACES, query.getList().size());
	}

}
