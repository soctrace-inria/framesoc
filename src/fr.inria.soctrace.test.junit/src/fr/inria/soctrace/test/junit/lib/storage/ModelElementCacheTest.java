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
package fr.inria.soctrace.test.junit.lib.storage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.storage.utils.ModelElementCache;
import fr.inria.soctrace.test.junit.utils.IModelFactory;

/**
 * Test for model element cache object
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ModelElementCacheTest {

	private static List<Event> events;
	private static List<Trace> traces;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		events = IModelFactory.INSTANCE.createEvents(10);
		traces = IModelFactory.INSTANCE.createTraces(10);
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		events.clear();
		traces.clear();
		events = null;
		traces = null;
	}

	@Test
	public final void testAddElementMap() {
		ModelElementCache cache = new ModelElementCache();
		
		cache.addElementMap(Event.class);
		cache.addElementMap(EventType.class);
		
		// null for non existing element in map
		assertNull(cache.get(Event.class, 0));
		assertNull(cache.get(EventType.class, 0));
		
		// null for non existing map
		cache.get(EventParam.class, 0);
		cache.get(EventParamType.class, 0);
	}

	@Test
	public final void testPut() {
		ModelElementCache cache = new ModelElementCache();
		Event e = events.iterator().next();
		
		cache.addElementMap(Event.class);
		cache.put(e);
		Event tmp = cache.get(Event.class, e.getId());
		assertSame(tmp, e);
	}

	@Test
	public final void testGet() {
		ModelElementCache cache = new ModelElementCache();
		
		// get inserted elements
		cache.addElementMap(Event.class);
		for (Event e: events) {
			cache.put(e);			
		}
		for (Event e: events) {
			Event tmp =	cache.get(Event.class, e.getId());
			assertSame(tmp, e);
		}
		
		// null for non existing in non empty map
		assertNull(cache.get(Event.class, 999999));
	}

	@Test
	public final void testRemove() {
		ModelElementCache cache = new ModelElementCache();
		
		// get inserted elements
		cache.addElementMap(Trace.class);
		for (Trace t: traces) {
			cache.put(t);
		}
		for (Trace t: traces) {
			Trace tmp =	cache.get(Trace.class, t.getId());
			assertSame(tmp, t);
		}
		
		// check presence
		Trace first = traces.iterator().next();
		assertNotNull(cache.get(Trace.class, first.getId()));
		assertSame(first, cache.get(Trace.class, first.getId()));
		// remove
		cache.remove(first);
		// check removed
		assertNull(cache.get(Trace.class, first.getId()));
		
	}

	@Test
	public final void testClear() {
		ModelElementCache cache = new ModelElementCache();
		
		// get inserted elements
		cache.addElementMap(Event.class);
		for (Event e: events) {
			cache.put(e);			
		}
		for (Event e: events) {
			Event tmp =	cache.get(Event.class, e.getId());
			assertSame(tmp, e);
		}
		
		// clear
		cache.clear();
		for (Event e: events) {
			Event tmp =	cache.get(Event.class, e.getId());
			assertNull(tmp);
		}
	}

}

