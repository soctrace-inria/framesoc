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
package fr.inria.soctrace.test.junit.lib.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.inria.soctrace.lib.model.File;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.FileQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.test.junit.utils.BaseTraceDBTest;
import fr.inria.soctrace.test.junit.utils.importer.VirtualImporter;

/**
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FileQueryTest extends BaseTraceDBTest {

	private FileQuery query;

	@Before
	public void setUp() throws SoCTraceException {
		query = new FileQuery(traceDB);
	}

	@After
	public void tearDown() {
		query.clear();
		query = null;
	}

	@Test
	public final void testGetList() throws SoCTraceException {
		List<File> elist = query.getList();
		assertEquals(VirtualImporter.NUMBER_OF_FILES, elist.size());	
	}

	@Test
	public final void testSetElementWhere() throws SoCTraceException {
		query.setElementWhere(new SimpleCondition("DESCRIPTION", ComparisonOperation.LIKE, VirtualImporter.FILE_INFO_PREFIX+"%"));
		List<File> flist = query.getList();
		assertEquals(VirtualImporter.NUMBER_OF_FILES, flist.size());
		for (File f: flist) {
			assertTrue(f.getDescription().contains(VirtualImporter.FILE_INFO_PREFIX));
		}
	}

	@Test
	public final void testSetOrderBy() throws SoCTraceException {
		query.setOrderBy("PATH", OrderBy.DESC);
		List<File> flist = query.getList();
		boolean first = true;
		String last = "";
		for (File f : flist) {
			if (first) {
				first = false;
				last = f.getPath();
				continue;
			}
			assertTrue(f.getPath().compareTo(last) <= 0);
			last = f.getPath();
		}
	}

	@Test
	public final void testClear() throws SoCTraceException {
		query.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, "0"));

		// before clear
		assertEquals(1, query.getList().size());
		assertEquals(1, query.getList().size());

		// after clear
		query.clear();
		assertEquals(VirtualImporter.NUMBER_OF_FILES, query.getList().size());
	}

	@Test
	public final void testSetLimit() throws SoCTraceException {
		int limits[] = { VirtualImporter.NUMBER_OF_FILES / 10, VirtualImporter.NUMBER_OF_FILES / 5,
				VirtualImporter.NUMBER_OF_FILES / 2 };
		for (int limit : limits) {
			query.setLimit(limit);
			assertEquals(limit, query.getList().size());
		}
		query.unsetLimit();
		assertEquals(VirtualImporter.NUMBER_OF_FILES, query.getList().size());
	}

}
