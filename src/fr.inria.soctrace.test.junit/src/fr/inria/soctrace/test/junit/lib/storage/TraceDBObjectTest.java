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
import static org.junit.Assert.assertNull;

import org.junit.Test;

import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.ModelElementCache;
import fr.inria.soctrace.test.junit.utils.BaseTraceDBTest;
import fr.inria.soctrace.test.junit.utils.TestConstants;
import fr.inria.soctrace.test.junit.utils.importer.VirtualImporter;

/**
 * Test Trace DB specific functions
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceDBObjectTest extends BaseTraceDBTest {
	
	@Test
	public final void testGetEventTypesCache() throws SoCTraceException {
		ModelElementCache typeCache = traceDB.getEventTypeCache();
		for (int i=0; i<VirtualImporter.NUMBER_OF_CATEGORIES*VirtualImporter.NUMBER_OF_TYPES; i++) {
			EventType et = typeCache.get(EventType.class, i);
			assertEquals(VirtualImporter.TYPE_NAME_PREFIX+i, et.getName());
			assertEquals(i, et.getId());
		}
		assertNull(typeCache.get(EventType.class, VirtualImporter.NUMBER_OF_CATEGORIES*VirtualImporter.NUMBER_OF_TYPES + 1));
	}

	@Test
	public final void testGetMinPage() throws SoCTraceException {
		assertEquals(VirtualImporter.PAGE, traceDB.getMinPage());
	}

	@Test
	public final void testGetMaxPage() throws SoCTraceException {
		assertEquals(TestConstants.MAX_PAGE, traceDB.getMaxPage());
	}

	@Test
	public final void testGetMinTimestamp() throws SoCTraceException {
		assertEquals( VirtualImporter.MIN_TIMESTAMP, traceDB.getMinTimestamp());
	}

	@Test
	public final void testGetMaxTimestamp() throws SoCTraceException {
		assertEquals( VirtualImporter.getMaxTimestamp(), traceDB.getMaxTimestamp());
	}
	
	@Test
	public final void testTimestampIndex() throws SoCTraceException {
		// drop non existing index
		traceDB.dropTimestampIndex();
		traceDB.close();
		
		// create non existing index
		traceDB = TraceDBObject.openNewInstance(VirtualImporter.DB_NAME);
		traceDB.createTimestampIndex();
		traceDB.close();
		
		// create existing index
		traceDB = TraceDBObject.openNewInstance(VirtualImporter.DB_NAME);
		traceDB.createTimestampIndex();
		traceDB.close();
		
		// drop existing index
		traceDB = TraceDBObject.openNewInstance(VirtualImporter.DB_NAME);
		traceDB.dropTimestampIndex();
		traceDB.close();
		
		// reopen for other tests
		traceDB = TraceDBObject.openNewInstance(VirtualImporter.DB_NAME);
	}

}
