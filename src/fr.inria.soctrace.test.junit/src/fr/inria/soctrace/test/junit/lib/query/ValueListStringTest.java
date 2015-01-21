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
package fr.inria.soctrace.test.junit.lib.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.ValueListString;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ValueListStringTest {

	@Test
	public final void testAddValue() throws SoCTraceException {

		ValueListString vls = new ValueListString();
		vls.addValue("1");
		vls.addValue("2");
		vls.addValue("3");
		vls.addValue("4");
		assertEquals("List size", 4, vls.size());
		assertEquals("List value", "(1, 2, 3, 4)", vls.getValueString());
		
		vls.clear();
		assertEquals("List size", 0, vls.size());
		
		vls.addValue("5");
		vls.addValue("6");
		assertEquals("List size", 2, vls.size());
		assertEquals("List value", "(5, 6)", vls.getValueString());
		
		vls.clear();
		assertEquals("List size", 0, vls.size());
		
		try {
			vls.getValueString();
			fail("Exception non launched");
		} catch (IllegalStateException e) {}
		
	}

	@Test
	public final void testSetQuotes() throws SoCTraceException {

		ValueListString vls = new ValueListString();
		vls.setQuotes(true);
		vls.addValue("1");
		vls.addValue("2");
		vls.addValue("3");
		vls.addValue("4");
		assertEquals("List size", 4, vls.size());
		assertEquals("List value", "('1', '2', '3', '4')", vls.getValueString());
		
		vls.clear();
		assertEquals("List size", 0, vls.size());
		
		vls.setQuotes(false);
		vls.addValue("5");
		vls.addValue("6");
		assertEquals("List size", 2, vls.size());
		assertEquals("List value", "(5, 6)", vls.getValueString());
		
		vls.clear();
		assertEquals("List size", 0, vls.size());
		
		try {
			vls.getValueString();
			fail("Exception non launched");
		} catch (IllegalStateException e) {}
		
	}

}
