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
package fr.inria.soctrace.test.junit.lib.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.test.junit.utils.IModelFactory;

public class EventTest {
	
	@Test
	public void testEqualsAndHashCode() {
		// create 2 equals events
		Event e1 = IModelFactory.INSTANCE.createEvent();
		Event e2 = IModelFactory.INSTANCE.createEvent();
		assertTrue(e1.equals(e2));
		assertTrue(e2.equals(e1));
		assertTrue(e1.hashCode() == e2.hashCode());
	}

	@Test
	public void testSetEventProducer() {
		Event base = IModelFactory.INSTANCE.createEvent();
		Event e = new Event(1);
		e.setEventProducer(base.getEventProducer());
		assertEquals(base.getEventProducer(), e.getEventProducer());
	}

	@Test
	public void testSetType() throws SoCTraceException {
		Event base = IModelFactory.INSTANCE.createEvent();
		Event e = new Event(1);
		e.setType(base.getType());
		assertEquals(base.getType(), e.getType());
	}

	@Test
	public void testGetEventParams() {
		Event e1 = IModelFactory.INSTANCE.createEvent();
		Event e2 = IModelFactory.INSTANCE.createEvent();
		assertEquals(e1.getEventParams(), e2.getEventParams());
	}

	@Test
	public void testGetParamMap() {
		Event e1 = IModelFactory.INSTANCE.createEvent();
		Event e2 = IModelFactory.INSTANCE.createEvent();
		Map<String, EventParam> epm = e1.getParamMap();
		List<EventParam> epl = e2.getEventParams();
		for (EventParam ep: epl) {
			assertTrue(epm.containsKey(ep.getEventParamType().getName()));
			assertEquals(ep, epm.get(ep.getEventParamType().getName()));
		}		
	}

}
