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
package fr.inria.soctrace.test.junit.lib.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.dbmanager.DBManager;
import fr.inria.soctrace.test.junit.utils.BaseTestClass;
import fr.inria.soctrace.test.junit.utils.TestUtils;
import fr.inria.soctrace.test.junit.utils.importer.VirtualImporter;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class DBManagerTest extends BaseTestClass {

	@Test
	public final void testOpenCloseConnection() throws SoCTraceException, SQLException {
		DBManager dm = DBManager.getDBManager(VirtualImporter.DB_NAME);
		Connection c = dm.openConnection();
		assertFalse(c.isClosed());
		dm.closeConnection();
		assertTrue(c.isClosed());
	}
	
	@Test
	public final void testIsDBExisting() throws SoCTraceException {
		String dbname = TestUtils.getRandomDBName();
		DBManager dm0 = DBManager.getDBManager(dbname);
		assertFalse(dm0.isDBExisting());		
		
		DBManager dm1 = DBManager.getDBManager(VirtualImporter.DB_NAME);
		assertTrue(dm1.isDBExisting());
	}

	@Test
	public final void testCreateDropDB() throws SoCTraceException {
		String dbname = "TEST_" + TestUtils.getRandomDBName(); // ensure the presence of starting letters (MySQL DB name constraint)
		DBManager dm0 = DBManager.getDBManager(dbname);
		dm0.createDB();
		assertTrue(dm0.isDBExisting());
		dm0.dropDB();
		assertFalse(dm0.isDBExisting());
	}

	@Test
	public final void testGetDBName() {
		DBManager dm1 = DBManager.getDBManager(VirtualImporter.DB_NAME);
		assertEquals(VirtualImporter.DB_NAME, dm1.getDBName());
	}
		
}
