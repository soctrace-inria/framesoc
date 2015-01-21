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

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.query.hierarchy.EPHierarchyDesc;
import fr.inria.soctrace.test.junit.utils.IModelFactory;

public class EPHierarchyDescTest {

	private static EPHierarchyDesc desc = new EPHierarchyDesc();
	private final static int DIRECT_SONS = 3;
	private final static int DESCENDANTS = DIRECT_SONS + 4;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		for (int i=0;i<DIRECT_SONS; ++i) {
			EPHierarchyDesc d = new EPHierarchyDesc();
			desc.getDirectSons().add(d);
			desc.getDescendants().add(d);
		}
		for (int i=0;i<DESCENDANTS - DIRECT_SONS; ++i) {
			EPHierarchyDesc d = new EPHierarchyDesc();
			desc.getDescendants().add(d);
		}
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		desc = null;
	}

	@Test
	public void testSetRank() {
		int r = 12;
		desc.setRank(r);
		assertEquals(r, desc.getRank());
	}

	@Test
	public void testSetEp() {
		EventProducer ep = IModelFactory.INSTANCE.createEvent().getEventProducer();
		desc.setEventProducer(ep);
		assertEquals(ep, desc.getEventProducer());
	}

	@Test
	public void testGetDirectSons() {
		assertEquals(DIRECT_SONS, desc.getDirectSons().size());
	}

	@Test
	public void testGetDescendants() {
		assertEquals(DESCENDANTS, desc.getDescendants().size());
	}

}
