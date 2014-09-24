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
/**
 * 
 */
package fr.inria.soctrace.test.junit.lib.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TimeUnitTest {

	/**
	 * Test method for {@link fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit#getLabel()}.
	 */
	@Test
	public void testGetLabel() {
		assertEquals("ns", TimeUnit.NANOSECONDS.getLabel());
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit#getInt()}.
	 */
	@Test
	public void testGetInt() {
		assertEquals(-9, TimeUnit.NANOSECONDS.getInt());
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit#getLabel(int)}.
	 */
	@Test
	public void testGetLabelStatic() {
		assertEquals(" x 10^-7 s", TimeUnit.getLabel(-7));
	}

}
