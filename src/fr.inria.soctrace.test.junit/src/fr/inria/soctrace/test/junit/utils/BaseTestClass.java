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
package fr.inria.soctrace.test.junit.utils;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import fr.inria.soctrace.test.junit.utils.importer.VirtualImporter;

public abstract class BaseTestClass {
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestConfiguration.initTest();
		PrepareTestResources.prepareTestResources();
		// at least 1 event in test traces
		assertTrue(VirtualImporter.TOTAL_NUMBER_OF_EVENTS >= 1);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		TestConfiguration.deinitTest();
	}

}
