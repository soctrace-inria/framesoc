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
package fr.inria.soctrace.test.junit.lib.utils;

import static org.junit.Assert.*;

import org.junit.Test;

import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.lib.utils.IdManager.Direction;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class IdManagerTest {

	/**
	 * Test method for {@link fr.inria.soctrace.lib.utils.IdManager#getNextId()}.
	 */
	@Test
	public final void testGetNextId() {
		IdManager manager = new IdManager();
		assertEquals(0, manager.getNextId());
		assertEquals(1, manager.getNextId());
		assertEquals(2, manager.getNextId());
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.utils.IdManager#resetNextId()}.
	 */
	@Test
	public final void testResetNextId() {
		IdManager manager = new IdManager();
		assertEquals(0, manager.getNextId());
		assertEquals(1, manager.getNextId());
		assertEquals(2, manager.getNextId());
		manager.resetNextId();
		assertEquals(0, manager.getNextId());
		assertEquals(1, manager.getNextId());
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.utils.IdManager#setNextId(int)}.
	 */
	@Test
	public final void testSetNextId() {
		IdManager manager = new IdManager();
		assertEquals(0, manager.getNextId());
		assertEquals(1, manager.getNextId());
		manager.setNextId(56);
		assertEquals(56, manager.getNextId());
		assertEquals(57, manager.getNextId());
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.utils.IdManager#getDirection()}.
	 */
	@Test
	public final void testGetDirection() {
		IdManager manager = new IdManager();
		assertEquals(Direction.ASCENDING, manager.getDirection());
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.utils.IdManager#setDirection(fr.inria.soctrace.lib.utils.IdManager.Direction)}.
	 */
	@Test
	public final void testSetDirection() {
		IdManager manager = new IdManager();
		assertEquals(Direction.ASCENDING, manager.getDirection());
		manager.setDirection(Direction.DESCENDING);
		assertEquals(Direction.DESCENDING, manager.getDirection());
	}

}
