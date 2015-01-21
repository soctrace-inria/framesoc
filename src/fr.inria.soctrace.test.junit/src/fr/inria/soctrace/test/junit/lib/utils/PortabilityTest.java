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
package fr.inria.soctrace.test.junit.lib.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import fr.inria.soctrace.lib.utils.Portability;
import fr.inria.soctrace.lib.utils.Portability.OSTYPE;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PortabilityTest {
	
	/**
	 * Test method for {@link fr.inria.soctrace.lib.utils.Portability#getUserHome()}.
	 */
	@Test
	public final void testGetUserHome() {
		assertEquals(System.getProperty("user.home"), Portability.getUserHome());
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.utils.Portability#getOSName()}.
	 */
	@Test
	public final void testGetOSName() {
		assertEquals(System.getProperty("os.name").toLowerCase(), Portability.getOSName());
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.utils.Portability#getOSType()}.
	 */
	@Test
	public final void testGetOSType() {
		assertTrue(Portability.getOSType().equals(OSTYPE.UNIX) || Portability.getOSType().equals(OSTYPE.WIN));
		assertFalse(Portability.getOSType().equals(OSTYPE.UNKNOWN));
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.utils.Portability#normalize(java.lang.String)}.
	 */
	@Test
	public final void testNormalize() {
		List<String> paths = new LinkedList<String>();
		paths.add("/my/unix/path");
		paths.add("\\my\\windows\\path");
		paths.add("//a//double//slash//unix//path");
		boolean slash = false, back = false;
		for (String p: paths) {
			if ( Portability.normalize(p).indexOf("/") >= 0)
				slash = true;
			if ( Portability.normalize(p).indexOf("\\") >= 0)
				back = true;
		}
		assertTrue(slash ^ back); // XOR true: one and only one is true
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.utils.Portability#getPathSeparator()}.
	 */
	@Test
	public final void testGetPathSeparator() {
		if (Portability.getOSType().equals(OSTYPE.UNIX))
			assertEquals("/", Portability.getPathSeparator());
		if (Portability.getOSType().equals(OSTYPE.WIN))
			assertEquals("\\", Portability.getPathSeparator());
		if (Portability.getOSType().equals(OSTYPE.UNKNOWN))
			assertEquals("/", Portability.getPathSeparator());
	}

}
