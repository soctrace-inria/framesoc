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
package fr.inria.soctrace.test.junit.lib.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.iterators.EventIterator;
import fr.inria.soctrace.lib.query.iterators.IntervalEventIterator;
import fr.inria.soctrace.lib.query.iterators.PageEventIterator;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.test.junit.utils.BaseTestClass;
import fr.inria.soctrace.test.junit.utils.importer.VirtualImporter;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
@RunWith(Parameterized.class)
public class EventIteratorTest extends BaseTestClass {

	private EventIterator iterator;
	private TraceDBObject traceDB;
	private Class<? extends EventIterator> iteratorClass;

	public EventIteratorTest(Class<? extends EventIterator> iteratorClass) {
		this.iteratorClass = iteratorClass;
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] { { IntervalEventIterator.class }, { PageEventIterator.class } };
		return Arrays.asList(data);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		traceDB = TraceDBObject.openNewInstance(VirtualImporter.DB_NAME);
		if (iteratorClass.equals(IntervalEventIterator.class))
			iterator = new IntervalEventIterator(traceDB, 10000);
		else if (iteratorClass.equals(PageEventIterator.class))
			iterator = new PageEventIterator(traceDB);	
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		iterator.clear();
		traceDB.close();
	}

	@Test
	public final void testGetNext() throws SoCTraceException {
		Event e = iterator.getNext();
		assertEquals(VirtualImporter.MIN_TIMESTAMP, e.getTimestamp());
	}

	@Test
	public final void testHasNext() throws SoCTraceException {
		assertTrue(iterator.hasNext());
		int n = 0;
		while (iterator.hasNext()) {
			iterator.getNext();
			n++;
		}
		assertFalse(iterator.hasNext());
		assertEquals(VirtualImporter.TOTAL_NUMBER_OF_EVENTS, n);
	}

	@Test
	public final void testClear() throws SoCTraceException {
		Event e = iterator.getNext();
		assertEquals(VirtualImporter.MIN_TIMESTAMP, e.getTimestamp());
		iterator.clear();
		try {
			iterator.hasNext();			
			fail("Exception not launched");
		} catch (SoCTraceException ex) {}
		try {
			iterator.getNext();			
			fail("Exception not launched");
		} catch (SoCTraceException ex) {}
	}

}
