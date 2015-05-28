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
package fr.inria.soctrace.test.junit.lib.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceParam;
import fr.inria.soctrace.lib.model.TraceParamType;
import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.query.ToolQuery;
import fr.inria.soctrace.lib.query.TraceQuery;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.test.junit.utils.IModelFactory;
import fr.inria.soctrace.test.junit.utils.TestUtils;

/**
 * Test all visitors for System DB
 * 
 * The pattern for each test is:
 * 
 * 0 
 * - create the elements of the model
 * 
 * 1
 * - save them
 * - retrieve and check they are OK
 * 
 * 2
 * - update them
 * - retrieve and check they are OK
 * 
 * 3
 * - delete them
 * - retrieve and check they are no more there
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class SystemDBVisitorTest {

	private SystemDBObject sysDB;
	
	@Before
	public void setUp() throws Exception {
		sysDB = new SystemDBObject("TMP_"+TestUtils.getRandomDBName(), DBMode.DB_CREATE);
	}

	@After
	public void tearDown() throws Exception {
		sysDB.dropDatabase();
	}
	
	@Test
	public final void testVisitTrace() throws SoCTraceException {

		// 0. create
		List<Trace> traces = IModelFactory.INSTANCE.createTraces(10);

		// 1. save and check
		TraceType tt = traces.iterator().next().getType();
		sysDB.save(tt);
		for (TraceParamType tpt: tt.getTraceParamTypes()) {
			sysDB.save(tpt);
		}
		Map<Integer, Trace> traceMap = new HashMap<Integer, Trace>();
		for (Trace t: traces) {
			traceMap.put(t.getId(), t);
			sysDB.save(t);
			for (TraceParam tp: t.getParams()) {
				sysDB.save(tp);
			}
		}
		sysDB.flushVisitorBatches();
		TraceQuery query = new TraceQuery(sysDB);
		List<Trace> res = query.getList();
		assertEquals(traces.size(), res.size());
		for (Trace tr: res) {
			assertTrue(tr.equals(traceMap.get(tr.getId())));
		}
		
		// 2. update and check
		Trace first = traces.iterator().next();
		first.getType().setName("please");
		sysDB.update(first.getType());
		int i = 0;
		for (TraceParamType tpt: first.getType().getTraceParamTypes()) {
			tpt.setName("please"+(i++));
			tpt.setType("please");
			sysDB.update(tpt);
		}
		for (Trace t: traces) {
			t.setAlias("please");
			t.setTimeUnit(TimeUnit.MILLISECONDS.getInt());
			t.setBoard("please");
			t.setDbName("please");
			t.setDescription("please");
			t.setNumberOfCpus(123);
			t.setNumberOfEvents(123123);
			t.setMinTimestamp(123456789);
			t.setMaxTimestamp(987654321);
			t.setOperatingSystem("please");
			t.setOutputDevice("please");
			t.setProcessed(true);
			t.setTracedApplication("please");
			t.setTracingDate(new Timestamp(new Date().getTime()));
			t.setNumberOfProducers(123);
			sysDB.update(t);
			for (TraceParam tp: t.getParams()) {
				tp.setValue("please");
				sysDB.update(tp);
			}			
		}
		sysDB.flushVisitorBatches();
		
		query.clear();
		res = query.getList();
		
		for (Trace tr: res) {	
			assertTrue(tr.equals(traceMap.get(tr.getId())));
		}

		// 3. delete and check
		first = traces.iterator().next();
		sysDB.delete(first.getType());
		for (TraceParamType tpt: first.getType().getTraceParamTypes()) {
			sysDB.delete(tpt);
		}
		for (Trace t: traces) {
			sysDB.delete(t);
			for (TraceParam tp: t.getParams()) {
				sysDB.delete(tp);
			}			
		}
		sysDB.flushVisitorBatches();
		
		assertEquals(0, sysDB.getCount(FramesocTable.TRACE.toString()));
		assertEquals(0, sysDB.getCount(FramesocTable.TRACE_TYPE.toString()));	
		assertEquals(0, sysDB.getCount(FramesocTable.TRACE_PARAM.toString()));
		assertEquals(0, sysDB.getCount(FramesocTable.TRACE_PARAM_TYPE.toString()));
	
	}

	/**
	 * Bug scenario.
	 * 
	 * The TraceType object created and the TraceType object
	 * in the db cache are different objects even if with the same
	 * values.
	 * 
	 * Adding in cache at save operation solves the issue.
	 * 
	 * Investigate:
	 * without adding to cache at save, the type in the trace
	 * object and the type in the trace param type are different
	 * (only the second is the updated one).
	 * 
	 * 
	 * @throws SoCTraceException
	 */
	@Ignore
	public final void testVisitTraceSimple() throws SoCTraceException {

		// 0. create
		Trace trace = IModelFactory.INSTANCE.createTrace();

		// 1. save and check
		TraceType tt = trace.getType();
		
		System.out.println("created");
		System.out.println(System.identityHashCode(tt));
		sysDB.save(tt);
		for (TraceParamType tpt: tt.getTraceParamTypes()) {
			sysDB.save(tpt);
		}
		sysDB.save(trace);
		for (TraceParam tp: trace.getParams()) {
			sysDB.save(tp);
		}
		sysDB.flushVisitorBatches();
				
		TraceQuery query = new TraceQuery(sysDB);		

		/**
		 * Commenting the following 2 lines does not work
		 * if the cache is not written at save operation!
		 * 
		 * In fact, if save() does not write the cache and
		 * update() writes the cache:
		 * - created event type is not in the cache, since never updated()
		 *   so the cache will contain a newly created event type object
		 * - when the cache is loaded, a new event param type object is written
		 *   inside, but the update() operation write a new event param type object
		 *   in the cache (the one created at the beginning of this test program)
		 * - so the next query will find in the cache:
		 *   - a new event type (created at cache loading)
		 *   - the old event param type (written in the cache at update)
		 * - the assert true at the end will fail because the event type reachable
		 *   by the trace object (the one created at cache loading) contains  
		 */
		
		// *************** LINES COMMENTED *************** 
		//trace.getType().setName("please"); 
		//sysDB.update(trace.getType());
		// *************** LINES COMMENTED *************** 
		
		int i = 0;
		for (TraceParamType tpt: tt.getTraceParamTypes()) {
			tpt.setName("please"+(i++));
			tpt.setType("please");
			sysDB.update(tpt); 
			/* this update load the OLD values from the cache
			 * and after, only the new tpt is written in the cache.
			 * So the cache is inconsistent:
			 * - the tt in cache points to the old tpt
			 * - the tpt in cache is the new one!
			 */
		}
		sysDB.flushVisitorBatches();
		
		TraceType ttt = sysDB.getTraceTypeCache().get(TraceType.class, 0);
		System.out.println("cached");
		System.out.println(System.identityHashCode(ttt));
		
		query.clear();
		Trace res = query.getList().iterator().next();
		
		System.out.println("trace after query: different types referred");
		System.out.println("via the trace: ");
		System.out.println(System.identityHashCode(res.getType()));
		System.out.println("via the param: ");
		System.out.println(System.identityHashCode(res.getParams().iterator().next().getTraceParamType().getTraceType()));
		
		/**
		 * If the above two lines are commented and the cache is not
		 * written at save operation:
		 * - res contains old param types
		 * - trace contains new param types
		 */
		assertTrue(res.equals(trace));
	
	}
	
	@Test
	public final void testVisitTool() throws SoCTraceException {
		
		// 0. create		
		Tool tool = IModelFactory.INSTANCE.createAnalysisTool();
		
		// 1. save and check
		sysDB.save(tool);
		sysDB.flushVisitorBatches();
		ToolQuery query = new ToolQuery(sysDB);
		Tool res = query.getList().iterator().next();
		assertTrue(res.equals(tool));
		
		// 2. update and check
		tool.setCommand("please");
		tool.setDoc("please");
		tool.setName("please");
		tool.setPlugin(false);
		tool.setType("please");
		sysDB.update(tool);
		sysDB.flushVisitorBatches();
		res = query.getList().iterator().next();
		assertTrue(res.equals(tool));
		
		// 3. delete and check
		sysDB.delete(tool);
		sysDB.flushVisitorBatches();
		assertEquals(0, sysDB.getCount(FramesocTable.TOOL.toString()));
	}

}
