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
/**
 * 
 */
package fr.inria.soctrace.test.junit.lib.search;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import fr.inria.soctrace.lib.search.TraceSearch;
import fr.inria.soctrace.test.junit.utils.BaseTestClass;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceSearchTest extends BaseTestClass {

	private TraceSearch search = new TraceSearch();
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		search.initialize();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		search.uninitialize();
	}

	/**
	 * Dummy Test method: remove this if at least one real test is implemented 
	 */
	@Test
	public final void testDummy() {
		assertTrue(true);
	}
	
	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getToolByName(java.lang.String)}.
	 */
	@Ignore
	public final void testGetToolByName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getToolByType(java.lang.String)}.
	 */
	@Ignore
	public final void testGetToolByType() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getTools()}.
	 */
	@Ignore
	public final void testGetTools() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getTraceByDBName(java.lang.String)}.
	 */
	@Ignore
	public final void testGetTraceByDBName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getTracesByTracingDate(long, long)}.
	 */
	@Ignore
	public final void testGetTracesByTracingDate() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getTracesByTracedApplication(java.lang.String)}.
	 */
	@Ignore
	public final void testGetTracesByTracedApplication() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getTracesByBoard(java.lang.String)}.
	 */
	@Ignore
	public final void testGetTracesByBoard() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getTracesByOperatingSystem(java.lang.String)}.
	 */
	@Ignore
	public final void testGetTracesByOperatingSystem() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getTracesByNumberOfCpus(int)}.
	 */
	@Ignore
	public final void testGetTracesByNumberOfCpus() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getTracesByOutputDevice(java.lang.String)}.
	 */
	@Ignore
	public final void testGetTracesByOutputDevice() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getTracesByDescription(java.lang.String)}.
	 */
	@Ignore
	public final void testGetTracesByDescription() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getTracesByType(java.lang.String)}.
	 */
	@Ignore
	public final void testGetTracesByType() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getTracesByTypes(java.util.List)}.
	 */
	@Ignore
	public final void testGetTracesByTypes() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getTracesByParam(java.lang.String, fr.inria.soctrace.lib.search.utils.ParamDesc)}.
	 */
	@Ignore
	public final void testGetTracesByParam() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getTracesByParams(java.lang.String, java.util.List)}.
	 */
	@Ignore
	public final void testGetTracesByParams() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getTraces()}.
	 */
	@Ignore
	public final void testGetTraces() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getRawTraces()}.
	 */
	@Ignore
	public final void testGetRawTraces() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getProcessedTraces()}.
	 */
	@Ignore
	public final void testGetProcessedTraces() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getFiles(fr.inria.soctrace.lib.model.Trace)}.
	 */
	@Ignore
	public final void testGetFiles() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventProducer(fr.inria.soctrace.lib.model.Trace, fr.inria.soctrace.lib.search.utils.EventProducerDesc)}.
	 */
	@Ignore
	public final void testGetEventProducer() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventProducersByType(fr.inria.soctrace.lib.model.Trace, java.lang.String)}.
	 */
	@Ignore
	public final void testGetEventProducersByType() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventProducers(fr.inria.soctrace.lib.model.Trace)}.
	 */
	@Ignore
	public final void testGetEventProducers() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getMinTimestamp(fr.inria.soctrace.lib.model.Trace)}.
	 */
	@Ignore
	public final void testGetMinTimestamp() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getMaxTimestamp(fr.inria.soctrace.lib.model.Trace)}.
	 */
	@Ignore
	public final void testGetMaxTimestamp() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByCpu(fr.inria.soctrace.lib.model.Trace, int)}.
	 */
	@Ignore
	public final void testGetEventsByCpu() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByTypeName(fr.inria.soctrace.lib.model.Trace, java.lang.String)}.
	 */
	@Ignore
	public final void testGetEventsByTypeName() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByTypeNames(fr.inria.soctrace.lib.model.Trace, java.util.List)}.
	 */
	@Ignore
	public final void testGetEventsByTypeNames() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByEventType(fr.inria.soctrace.lib.model.Trace, fr.inria.soctrace.lib.model.EventType)}.
	 */
	@Ignore
	public final void testGetEventsByEventType() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByEventTypes(fr.inria.soctrace.lib.model.Trace, java.util.List)}.
	 */
	@Ignore
	public final void testGetEventsByEventTypes() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByEventProducerDesc(fr.inria.soctrace.lib.model.Trace, fr.inria.soctrace.lib.search.utils.EventProducerDesc)}.
	 */
	@Ignore
	public final void testGetEventsByEventProducerDesc() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByEventProducerDescs(fr.inria.soctrace.lib.model.Trace, java.util.List)}.
	 */
	@Ignore
	public final void testGetEventsByEventProducerDescs() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByEventProducer(fr.inria.soctrace.lib.model.Trace, fr.inria.soctrace.lib.model.EventProducer)}.
	 */
	@Ignore
	public final void testGetEventsByEventProducer() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByEventProducers(fr.inria.soctrace.lib.model.Trace, java.util.List)}.
	 */
	@Ignore
	public final void testGetEventsByEventProducers() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByInterval(fr.inria.soctrace.lib.model.Trace, fr.inria.soctrace.lib.search.utils.IntervalDesc)}.
	 */
	@Ignore
	public final void testGetEventsByInterval() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByIntervals(fr.inria.soctrace.lib.model.Trace, java.util.List)}.
	 */
	@Ignore
	public final void testGetEventsByIntervals() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByParam(fr.inria.soctrace.lib.model.Trace, java.lang.String, fr.inria.soctrace.lib.search.utils.ParamDesc)}.
	 */
	@Ignore
	public final void testGetEventsByParam() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByParams(fr.inria.soctrace.lib.model.Trace, java.lang.String, java.util.List)}.
	 */
	@Ignore
	public final void testGetEventsByParams() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByParamsAndIntervals(fr.inria.soctrace.lib.model.Trace, java.lang.String, java.util.List, java.util.List)}.
	 */
	@Ignore
	public final void testGetEventsByParamsAndIntervals() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByTypeNamesAndIntervalsAndEventProducerDescs(fr.inria.soctrace.lib.model.Trace, java.util.List, java.util.List, java.util.List)}.
	 */
	@Ignore
	public final void testGetEventsByTypeNamesAndIntervalsAndEventProducerDescs() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByEventTypesAndIntervalsAndEventProducers(fr.inria.soctrace.lib.model.Trace, java.util.List, java.util.List, java.util.List)}.
	 */
	@Ignore
	public final void testGetEventsByEventTypesAndIntervalsAndEventProducers() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getEventsByPage(fr.inria.soctrace.lib.model.Trace, java.lang.Integer)}.
	 */
	@Ignore
	public final void testGetEventsByPage() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getAnalysisResults(fr.inria.soctrace.lib.model.Trace)}.
	 */
	@Ignore
	public final void testGetAnalysisResults() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getAnalysisResultsByType(fr.inria.soctrace.lib.model.Trace, fr.inria.soctrace.lib.model.AnalysisResultData.AnalysisResultType)}.
	 */
	@Ignore
	public final void testGetAnalysisResultsByType() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getAnalysisResultsByTool(fr.inria.soctrace.lib.model.Trace, fr.inria.soctrace.lib.model.Tool)}.
	 */
	@Ignore
	public final void testGetAnalysisResultsByTool() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getAnalysisResultsByToolAndType(fr.inria.soctrace.lib.model.Trace, fr.inria.soctrace.lib.model.Tool, fr.inria.soctrace.lib.model.AnalysisResultData.AnalysisResultType)}.
	 */
	@Ignore
	public final void testGetAnalysisResultsByToolAndType() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getAnalysisResultData(fr.inria.soctrace.lib.model.Trace, fr.inria.soctrace.lib.model.AnalysisResult)}.
	 */
	@Ignore
	public final void testGetAnalysisResultData() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getTraceDBObject(fr.inria.soctrace.lib.model.Trace)}.
	 */
	@Ignore
	public final void testGetTraceDBObject() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.search.TraceSearch#getSystemDBObject()}.
	 */
	@Ignore
	public final void testGetSystemDBObject() {
		fail("Not yet implemented"); // TODO
	}

}
