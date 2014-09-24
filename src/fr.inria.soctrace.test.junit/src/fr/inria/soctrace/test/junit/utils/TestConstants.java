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

import fr.inria.soctrace.test.junit.utils.importer.VirtualImporter;

/**
 * Test constants
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TestConstants {
	
	public static final int NUMBER_OF_TOOLS = 2;
	
	public static final int NUMBER_OF_TRACES = 2;
	
	public static final int PROCESSED_TRACE_ID = VirtualImporter.TRACE_ID+1;
	
	public static final int PROCESSED_TRACE_TYPE_ID = VirtualImporter.TRACE_TYPE_ID+1;
	
	public static final String PROCESSED_TRACE_TYPE_NAME = "junit.type";
	
	public static final String PROCESSED_TRACE_DB_NAME = "TEST_DB_2";
	
	public static final String PROCESSED_TRACE_METADATA = "PROCESSED_METADATA";

	public static final String JUNIT_TEST_TOOL_NAME = "Junit Test Tool";
	
	public static final String JUNIT_TEST_TOOL_COMMAND = "junit command";
	
	public static final String JUNIT_TEST_TOOL_DOC = "junit doc";
	
	public static final String VIRTUAL_IMPORTER_TOOL_NAME = "virtual importer";

	public static final String VIRTUAL_IMPORTER_TOOL_COMMAND = "virtual command";

	public static final String VIRTUAL_IMPORTER_TOOL_DOC = "virtual doc";
	
	public static final int NUMBER_OF_RESULTS = 5;

	public static final int MAX_PAGE = VirtualImporter.PAGE + 1;
	
}
