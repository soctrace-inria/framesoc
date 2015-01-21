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

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceParam;
import fr.inria.soctrace.test.junit.utils.IModelFactory;

public class TraceTest {

	@Test
	public void testEqualsAndHashCode() {
		Trace t1 = IModelFactory.INSTANCE.createTrace();
		Trace t2 = IModelFactory.INSTANCE.createTrace();
		assertTrue(t1.equals(t2));
		assertTrue(t2.equals(t1));
		assertTrue(t1.hashCode() == t2.hashCode());
	}

	@Test
	public void testSetType() {
		Trace base = IModelFactory.INSTANCE.createTrace();
		Trace t = new Trace(1);
		t.setType(base.getType());
		assertEquals(base.getType(), t.getType());
	}

	@Test
	public void testGetParams() {
		Trace t1 = IModelFactory.INSTANCE.createTrace();
		Trace t2 = IModelFactory.INSTANCE.createTrace();
		assertEquals(t1.getParams(), t2.getParams());
	}

	@Test
	public void testGetParamMap() {
		Trace t1 = IModelFactory.INSTANCE.createTrace();
		Trace t2 = IModelFactory.INSTANCE.createTrace();
		Map<String, TraceParam> epm = t1.getParamMap();
		List<TraceParam> epl = t2.getParams();
		for (TraceParam ep: epl) {
			assertTrue(epm.containsKey(ep.getTraceParamType().getName()));
			assertEquals(ep, epm.get(ep.getTraceParamType().getName()));
		}		
	}

	@Test
	public void testCopyMetadata() {
		Trace base = IModelFactory.INSTANCE.createTrace();
		Trace t = new Trace(base.getId());
		assertTrue(!(t.equals(base)));
		
		t.setParams(base.getParams());
		assertTrue(!(t.equals(base)));
		
		t.setType(base.getType());	
		assertTrue(!(t.equals(base)));
		
		t.setNumberOfCpus(base.getNumberOfCpus()+1); // something different
		assertTrue(!(t.equals(base)));
		
		t.copyMetadata(base);
		assertTrue(t.equals(base));
	}

	@Test
	public void testSynchWith() {
		Trace t1 = IModelFactory.INSTANCE.createTrace();
		Trace t2 = IModelFactory.INSTANCE.createTrace();
		
		TraceParam tp1 = t1.getParams().get(0);
		t2.getParamMap().get(tp1.getTraceParamType().getName()).setValue(tp1.getValue()+"_different");
		assertTrue(!(t1.equals(t2)));
		
		t2.synchWith(t1);
		assertTrue(t1.equals(t2));
	}

}
