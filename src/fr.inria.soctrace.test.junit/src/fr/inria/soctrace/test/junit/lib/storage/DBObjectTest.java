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
package fr.inria.soctrace.test.junit.lib.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.IModelElement;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.ElementQuery;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.TraceQuery;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.test.junit.utils.BaseTestClass;
import fr.inria.soctrace.test.junit.utils.IModelFactory;
import fr.inria.soctrace.test.junit.utils.TestUtils;
import fr.inria.soctrace.test.junit.utils.importer.VirtualImporter;

/**
 * Test class for DBObject functionalities.
 * 
 * Each test is run twice, for Trace and System DB objects.
 * Nevertheless, only common functionalities to concrete db classes
 * are tested.
 * 
 * The tests on visitor methods (save, update, delete) are not exhaustive, 
 * and are performed only to test DBObject functionalities. 
 * Visitors classes have their own tests.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
@RunWith(Parameterized.class)
public class DBObjectTest extends BaseTestClass {

	// set by constructor
	private IModelElement element;
	
	// set/unset before/after each test
	private DBObject db;
	private ElementQuery query;

	public DBObjectTest(IModelElement element) {
		this.element = element;
	}

	@Parameters
	public static Collection<Object[]> data() throws SoCTraceException {		
		Trace t = IModelFactory.INSTANCE.createTrace();
		Event e = IModelFactory.INSTANCE.createEvent();
		Object[][] data = new Object[][] { { t }, { e }};
		return Arrays.asList(data);
	}
	
	@Before
	public void setUp() throws Exception {
		if (element instanceof Event) {
			db = new TraceDBObject("TMP_"+TestUtils.getRandomDBName(), DBMode.DB_CREATE);
			query = new EventQuery((TraceDBObject)db);
			db.save(((Event)element).getType());
		} else {
			db = new SystemDBObject("TMP_"+TestUtils.getRandomDBName(), DBMode.DB_CREATE);
			query = new TraceQuery((SystemDBObject)db);
			db.save(((Trace)element).getType());
		}
	}

	@After
	public void tearDown() throws Exception {
		query.clear();
		db.dropDatabase();
	}

	// tests
	
	@Test // isDBExisting, getDBName
	public final void testIsDBExisting() throws SoCTraceException {
		assertTrue(DBObject.isDBExisting(VirtualImporter.DB_NAME));
		assertFalse(DBObject.isDBExisting(TestUtils.getRandomDBName()));
		assertTrue(DBObject.isDBExisting(db.getDBName()));
	}
	
	@Test // save, delete, commit
	public final void testCommit() throws SoCTraceException {

		// empty
		assertEquals(0, query.getList().size());

		// still empty
		db.save(element);
		assertEquals(0, query.getList().size());

		// 1 after commit
		db.commit();
		assertEquals(1, query.getList().size());

		// still 1
		db.delete(element);
		assertEquals(1, query.getList().size());

		// 0 after commit
		db.commit();
		assertEquals(0, query.getList().size());

	}

	@Test // save, flushVisitorBatches, rollback
	public final void testRollback() throws SoCTraceException {

		// empty
		assertEquals(0, query.getList().size());
		
		// 1 after flush (before commit)
		db.save(element);
		db.flushVisitorBatches();
		assertEquals(1, query.getList().size());

		// 0 after rollback
		db.rollback();
		assertEquals(0, query.getList().size());
		
	}

	@Test // getConnection, close
	public final void testClose() throws SoCTraceException, SQLException {
		Connection c = db.getConnection();
		assertFalse(c.isClosed());
		db.close();
		assertTrue(c.isClosed());
	}

	@Test // save, flushVisitorBatches, getNewId
	public final void testGetNewId() throws SoCTraceException, InstantiationException, IllegalAccessException {

		IdManager m = new IdManager();
		IdManager pm = new IdManager();
		
		if (element instanceof Event) {
			// empty table
			assertEquals(0, db.getNewId(FramesocTable.EVENT.toString(), "ID"));

			// table with a hole
			db.save(IModelFactory.INSTANCE.createEvent(m, pm)); // 0
			db.save(IModelFactory.INSTANCE.createEvent(m, pm)); // 1
			db.save(IModelFactory.INSTANCE.createEvent(m, pm)); // 2
			m.setNextId(4);
			db.save(IModelFactory.INSTANCE.createEvent(m, pm)); // 4
			db.flushVisitorBatches();
			assertEquals(3, db.getNewId(FramesocTable.EVENT.toString(), "ID"));
			
		} else {
			// empty table
			assertEquals(0, db.getNewId(FramesocTable.TRACE.toString(), "ID"));
		
			// table with a hole
			db.save(IModelFactory.INSTANCE.createTrace(m, pm)); // 0
			db.save(IModelFactory.INSTANCE.createTrace(m, pm)); // 1
			db.save(IModelFactory.INSTANCE.createTrace(m, pm)); // 2
			m.setNextId(4);
			db.save(IModelFactory.INSTANCE.createTrace(m, pm)); // 4
			db.flushVisitorBatches();
			assertEquals(3, db.getNewId(FramesocTable.TRACE.toString(), "ID"));
		}	

	}

	@Test // save, flushVisitorBatches, getMaxId
	public final void testGetMaxId() throws SoCTraceException {
		IdManager m = new IdManager();
		IdManager pm = new IdManager();
		
		if (element instanceof Event) {
			// empty table
			assertEquals(0, db.getNewId(FramesocTable.EVENT.toString(), "ID"));

			// table with a hole
			db.save(IModelFactory.INSTANCE.createEvent(m, pm)); // 0
			db.save(IModelFactory.INSTANCE.createEvent(m, pm)); // 1
			db.save(IModelFactory.INSTANCE.createEvent(m, pm)); // 2
			m.setNextId(45);
			db.save(IModelFactory.INSTANCE.createEvent(m, pm)); // 4
			db.flushVisitorBatches();
			assertEquals(45, db.getMaxId(FramesocTable.EVENT.toString(), "ID"));
			
		} else {
			// empty table
			assertEquals(0, db.getNewId(FramesocTable.TRACE.toString(), "ID"));
		
			// table with a hole
			db.save(IModelFactory.INSTANCE.createTrace(m, pm)); // 0
			db.save(IModelFactory.INSTANCE.createTrace(m, pm)); // 1
			db.save(IModelFactory.INSTANCE.createTrace(m, pm)); // 2
			m.setNextId(45);
			db.save(IModelFactory.INSTANCE.createTrace(m, pm)); // 4
			db.flushVisitorBatches();
			assertEquals(45, db.getMaxId(FramesocTable.TRACE.toString(), "ID"));
		}	
	}

	@Test // save, flushVisitorBatches, update
	public final void testUpdate() throws SoCTraceException {
		db.save(element);
		db.flushVisitorBatches();
		
		if (element instanceof Event) {
			Event oldDBEvent = (Event) query.getList().iterator().next();
			Event e = (Event) element;
			
			assertEquals(oldDBEvent.getCpu(), e.getCpu());
			assertEquals(oldDBEvent.getPage(), e.getPage());
			
			e.setCpu(e.getCpu()+1);
			e.setPage(e.getPage()+1);
			
			db.update(e);
			db.flushVisitorBatches();
			Event newDBEvent = (Event) query.getList().iterator().next();

			assertEquals(oldDBEvent.getCpu()+1, newDBEvent.getCpu());
			assertEquals(oldDBEvent.getPage()+1, newDBEvent.getPage());
		} else {
			Trace oldDBTrace = (Trace) query.getList().iterator().next();
			Trace t = (Trace) element;
			
			assertEquals(oldDBTrace.getBoard(), t.getBoard());
			assertEquals(oldDBTrace.getNumberOfCpus(), t.getNumberOfCpus());
			
			t.setBoard(t.getBoard()+"_updated");
			t.setNumberOfCpus(t.getNumberOfCpus()+1);
			
			db.update(t);
			db.flushVisitorBatches();
			Trace newDBTrace = (Trace) query.getList().iterator().next();

			assertEquals(oldDBTrace.getBoard()+"_updated", newDBTrace.getBoard());
			assertEquals(oldDBTrace.getNumberOfCpus()+1, newDBTrace.getNumberOfCpus());			
		}
	}

}

