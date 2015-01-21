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
import static org.junit.Assert.assertNull;

import org.junit.Test;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.hierarchy.EPHierarchyDesc;
import fr.inria.soctrace.lib.query.hierarchy.EPHierarchyDescMap;
import fr.inria.soctrace.test.junit.utils.BaseTraceDBTest;
import fr.inria.soctrace.test.junit.utils.importer.VirtualImporter;

public class EPHierarchyDescMapTest extends BaseTraceDBTest {
	
	@Test(expected=SoCTraceException.class)
	public void testGetRootException() throws SoCTraceException {
		EPHierarchyDescMap map = new EPHierarchyDescMap();
		map.getRoot();
	}
	
	@Test
	public void testGetRoot() throws SoCTraceException {
		EPHierarchyDescMap map = new EPHierarchyDescMap();
		map.load(traceDB);
		EPHierarchyDesc root = map.getRoot();
		assertNull(root.getEventProducer());
	}


	@Test
	public void testLoad() throws SoCTraceException {
		
		EPHierarchyDescMap map = new EPHierarchyDescMap();
		map.load(traceDB);
		EPHierarchyDesc root = map.getRoot();
			
		assertEquals(VirtualImporter.NUMBER_OF_PRODUCERS, root.getDirectSons().size());
		
		assertEquals(VirtualImporter.NUMBER_OF_PRODUCERS, root.getDescendants().size());
		
		for (EPHierarchyDesc d: root.getDirectSons()) {
			assertEquals(d.getDirectSons(), d.getDescendants());
			assertEquals(0, d.getRank());
		}
		
	}

}
