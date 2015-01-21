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
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ConfigurationTest {

	private static Configuration conf = Configuration.getInstance();
	private static Map<SoCTraceProperty, String> bkpProperties = new HashMap<SoCTraceProperty, String>();
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		for (SoCTraceProperty p: SoCTraceProperty.values()) {
			bkpProperties.put(p, conf.get(p));
			conf.set(p, "please");
		}
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		for (SoCTraceProperty p: SoCTraceProperty.values()) {
			conf.set(p, bkpProperties.get(p));
		}
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.utils.Configuration#get(java.lang.String)}.
	 */
	@Test
	public final void testGetString() {
		for (SoCTraceProperty p: SoCTraceProperty.values()) {
			assertEquals("please", conf.get(p.toString()));
		}
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.utils.Configuration#getDefault(java.lang.String)}.
	 */
	@Test
	public final void testGetDefaultString() {
		for (SoCTraceProperty p: SoCTraceProperty.values()) {
			assertFalse(conf.getDefault(p.toString()).equals("please"));
		}
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.utils.Configuration#get(fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty)}.
	 */
	@Test
	public final void testGetSoCTraceProperty() {
		for (SoCTraceProperty p: SoCTraceProperty.values()) {
			assertEquals("please", conf.get(p));
		}
	}

	/**
	 * Test method for {@link fr.inria.soctrace.lib.utils.Configuration#getDefault(fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty)}.
	 */
	@Test
	public final void testGetDefaultSoCTraceProperty() {
		for (SoCTraceProperty p: SoCTraceProperty.values()) {
			assertFalse(conf.getDefault(p).equals("please"));
		}
	}

}
