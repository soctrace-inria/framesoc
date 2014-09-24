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
package fr.inria.soctrace.test.junit.lib.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.inria.soctrace.lib.model.State;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;

public class StateTest {

	@Test
	public void testState() {
		State s = new State(0);
		assertEquals(EventCategory.STATE, s.getCategory());
	}

	@Test
	public void testGetEndTimestamp() {
		State s = new State(0);
		s.setEndTimestamp(Long.MAX_VALUE);
		assertEquals(Long.MAX_VALUE, s.getEndTimestamp());
	}

	@Test
	public void testGetImbricationLevel() {
		State s = new State(0);
		s.setImbricationLevel(Integer.MAX_VALUE);
		assertEquals(Integer.MAX_VALUE, s.getImbricationLevel());
	}
}
