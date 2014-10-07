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

import fr.inria.soctrace.lib.model.Variable;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;

public class VariableTest {

	@Test
	public void testVariable() {
		Variable v = new Variable(0);
		assertEquals(EventCategory.VARIABLE, v.getCategory());
	}

	@Test
	public void testGetVariableId() {
		Variable v = new Variable(0);
		v.setEndTimestamp(Long.MAX_VALUE);
		assertEquals(Long.MAX_VALUE, v.getEndTimestamp());
	}

	@Test
	public void testGetValue() {
		Variable v = new Variable(0);
		v.setValue(Double.MAX_VALUE);
		assertEquals(Double.MAX_VALUE, v.getValue(), 0.00000001);
	}
}
