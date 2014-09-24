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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.test.junit.utils.BaseSystemDBTest;
import fr.inria.soctrace.test.junit.utils.importer.VirtualImporter;

/**
 * Test System DB specific functions
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class SystemDBObjectTest extends BaseSystemDBTest {
		
	@Test
	public final void testIsTraceTypePresent() throws SoCTraceException {
		assertTrue(sysDB.isTraceTypePresent(VirtualImporter.TRACE_TYPE_NAME));
		assertFalse(sysDB.isTraceTypePresent("NOT_PRESENT"));
	}

	@Test
	public final void testGetTraceType() throws SoCTraceException {
		TraceType tt = sysDB.getTraceType(VirtualImporter.TRACE_TYPE_NAME);
		assertEquals(VirtualImporter.TRACE_TYPE_NAME, tt.getName());
		assertEquals(VirtualImporter.NUMBER_OF_PARAMETERS, tt.getTraceParamTypes().size());
		
		TraceType tt1 = sysDB.getTraceType("NOT_PRESENT");
		assertNull(tt1);
		
		TraceType tt2 = sysDB.getTraceTypeCache().get(TraceType.class, VirtualImporter.TRACE_TYPE_ID);
		assertSame(tt, tt2);
	}

	@Test
	public final void testGetTraceTypesCache() throws SoCTraceException {
		TraceType tt = sysDB.getTraceTypeCache().get(TraceType.class, VirtualImporter.TRACE_TYPE_ID);
		assertEquals(VirtualImporter.TRACE_TYPE_NAME, tt.getName());
		assertEquals(VirtualImporter.NUMBER_OF_PARAMETERS, tt.getTraceParamTypes().size());
		
		TraceType tt1 = sysDB.getTraceTypeCache().get(TraceType.class, 100);
		assertNull(tt1);
	}

}
