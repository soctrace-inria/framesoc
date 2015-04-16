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
package fr.inria.soctrace.test.junit.utils;

import java.io.File;
import java.util.Properties;

import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.lib.utils.DBMS;
import fr.inria.soctrace.lib.utils.Portability;

/**
 * Utility to ensure the use of a well know configuration file during tests. Usage:
 * 
 * TestConfiguration.initTest()
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TestConfiguration {

	private static Properties config;
	private static boolean initialized = false;

	static {
		config = new Properties();

		// General
		config.setProperty(SoCTraceProperty.soctrace_dbms.toString(), DBMS.SQLITE.toString());
		config.setProperty(SoCTraceProperty.soctrace_db_name.toString(), "SOCTRACE_SYSTEM_DB_TEST");
		config.setProperty(SoCTraceProperty.max_view_instances.toString(), "5");
		config.setProperty(SoCTraceProperty.trace_db_ts_indexing.toString(), "true");
		config.setProperty(SoCTraceProperty.trace_db_eid_indexing.toString(), "false");
		config.setProperty(SoCTraceProperty.ask_for_tool_removal.toString(), "false");
		config.setProperty(SoCTraceProperty.allow_view_replication.toString(), "true");

		// MySQL
		config.setProperty(SoCTraceProperty.mysql_base_db_jdbc_url.toString(),
				"jdbc:mysql://localhost");
		config.setProperty(SoCTraceProperty.mysql_db_user.toString(), "root");
		config.setProperty(SoCTraceProperty.mysql_db_password.toString(), "pass");

		// SQLite
		File dbs = new File("./resources/dbs");
		String defaultSQLiteDir = Portability.normalize(dbs.getAbsolutePath());
		config.setProperty(SoCTraceProperty.sqlite_db_directory.toString(), defaultSQLiteDir);
	}

	public static void initTest() {
		if (initialized)
			return;

		for (SoCTraceProperty p : SoCTraceProperty.values()) {
			Configuration.getInstance().set(p, config.getProperty(p.toString()));
		}
		
		initialized = true;
		
		System.out.println("------------------");
		System.out.println("Test Configuration");
		for (SoCTraceProperty p : SoCTraceProperty.values()) {
			System.out.println(p +"="+Configuration.getInstance().get(p));
		}		
		System.out.println("------------------");
	}

}
