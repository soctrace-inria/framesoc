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

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.AnalysisResultData.AnalysisResultType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.AnalysisResultQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.test.junit.utils.BaseTraceDBTest;
import fr.inria.soctrace.test.junit.utils.TestConstants;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class AnalysisResultQueryTest extends BaseTraceDBTest {

	private AnalysisResultQuery query;
	
	@Before
	public void setUp() throws SoCTraceException {
		query = new AnalysisResultQuery(traceDB);	
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
		assertEquals(TestConstants.NUMBER_OF_RESULTS, query.getList().size());

	}

	@Test
	public final void testSetToolWhere() throws SoCTraceException {
		
		// junit tool results
		query.setToolWhere(new SimpleCondition("NAME", ComparisonOperation.EQ, TestConstants.JUNIT_TEST_TOOL_NAME));
		assertEquals(TestConstants.NUMBER_OF_RESULTS, query.getList().size());
		query.clear();
		
		// non existing tool results
		query.setToolWhere(new SimpleCondition("NAME", ComparisonOperation.EQ, "please"));
		assertEquals(0, query.getList().size());
		
		// existing tool with 0 results
		query.setToolWhere(new SimpleCondition("NAME", ComparisonOperation.EQ, TestConstants.VIRTUAL_IMPORTER_TOOL_NAME));
		assertEquals(0, query.getList().size());

	}

	@Test
	public final void testGetList() throws SoCTraceException {
		List<AnalysisResult> alist = query.getList();
		assertEquals(TestConstants.NUMBER_OF_RESULTS, alist.size());
	}

	@Test
	public final void testSetElementWhere() throws SoCTraceException {
		// retrieve 2 search and 1 annotation
		LogicalCondition or = new LogicalCondition(LogicalOperation.OR);
		or.addCondition(new SimpleCondition("TYPE", ComparisonOperation.EQ, AnalysisResultType.TYPE_SEARCH.toString()));
		or.addCondition(new SimpleCondition("TYPE", ComparisonOperation.EQ, AnalysisResultType.TYPE_ANNOTATION.toString()));
		query.setElementWhere(or);
		List<AnalysisResult> alist = query.getList();
		assertEquals(3, alist.size());
		int search = 0;
		int annotation = 0;
		for (AnalysisResult ar: alist) {
			if (ar.getType().equals(AnalysisResultType.TYPE_SEARCH.toString()))
				search++;
			else if (ar.getType().equals(AnalysisResultType.TYPE_ANNOTATION.toString()))
				annotation++;
		}
		assertEquals(2, search);
		assertEquals(1, annotation);
	}

	@Test
	public final void testSetOrderBy() throws SoCTraceException {
		query.setOrderBy("ID", OrderBy.DESC);
		List<AnalysisResult> alist = query.getList();
		long last = Integer.MAX_VALUE;
		for (AnalysisResult ar: alist) {
			assertTrue(ar.getId()<=last);
			last = ar.getId();
		}
	}
	
	@Test
	public final void testSetLimit() throws SoCTraceException {
		int limits[] = { TestConstants.NUMBER_OF_RESULTS / 10,
				TestConstants.NUMBER_OF_RESULTS / 5, TestConstants.NUMBER_OF_RESULTS / 2 };
		for (int limit : limits) {
			query.setLimit(limit);
			assertEquals(limit, query.getList().size());
		}
		query.unsetLimit();
		assertEquals(TestConstants.NUMBER_OF_RESULTS, query.getList().size());		
	}

}
