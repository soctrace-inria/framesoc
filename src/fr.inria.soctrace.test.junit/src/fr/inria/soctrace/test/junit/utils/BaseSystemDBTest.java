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

import org.junit.AfterClass;
import org.junit.BeforeClass;

import fr.inria.soctrace.lib.storage.SystemDBObject;

/**
 * Base class for Test on SystemDB query objects.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class BaseSystemDBTest extends BaseTestClass {

	protected static SystemDBObject sysDB;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		BaseTestClass.setUpBeforeClass();
		sysDB = SystemDBObject.openNewIstance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		sysDB.close();
		BaseTestClass.tearDownAfterClass();
	}

}
