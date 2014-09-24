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
/**
 * 
 */
package fr.inria.soctrace.test.junit.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.Portability;


/**
 * Utility to ensure the use of a well know configuration file during tests.
 * Usage:
 * 
 * TestConfiguration.initTest()
 * 
 * .. all tests ..
 * 
 * TestConfiguration.deinitTest()
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TestConfiguration {
	
	private static final String dbms = "sqlite";
	
	private static final String defaultDbs = "./resources/dbs";
	
	private static final String confFilePath = getConfFilePath();
	
	private static String trailer = ".bkp";
	
	private static boolean alreadyInitialized = false;
	
	public static void initTest() {
		if (alreadyInitialized) 
			return;
		
		// compute trailer
		trailer = TestUtils.getRandomDBName();
		
		// save bkp if any
		File oldFile = new File(confFilePath);
		if (oldFile.exists()) {
			oldFile.renameTo(new File(confFilePath+trailer));
		}
		
		// create the new test configuration file
		generateTestConfigurationFile();
		
		alreadyInitialized = true;
	}
	
	public static void deinitTest() {
		if (!alreadyInitialized)
			return;
		
		// remove default
		File oldFileDefault = new File(confFilePath);
		oldFileDefault.delete();
		
		// restore old if any
		File realOldFile = new File(confFilePath+trailer);
		if (realOldFile.exists()) {
			realOldFile.renameTo(new File(confFilePath));
		}		
		alreadyInitialized = false;
	}
	
	private static String getConfFilePath() {
		return Portability.normalize(Portability.getUserHome() + "/.soctrace.conf");
	}
	
	private static void generateTestConfigurationFile() {
		try {
			File dbs = new File(defaultDbs);
			File file = new File(confFilePath);
			FileWriter fw;	
			fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("## TEST CONF FILE ##\n");
			bw.write("mysql_db_user=root\n");
			bw.write("sqlite_db_directory="+dbs.getAbsolutePath()+"\n");
			bw.write("max_view_instances=5\n");
			bw.write("mysql_db_password=pass\n");
			bw.write("soctrace_dbms="+dbms+"\n");
			bw.write("mysql_base_db_jdbc_url=jdbc\\:mysql\\://localhost\n");
			bw.write("soctrace_db_name=SOCTRACE_SYSTEM_DB_TEST\n");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Test main
	 */
	public static void main(String[] args) throws SoCTraceException {
			
		TestConfiguration.initTest();
		
		TestConfiguration.deinitTest();
	}

}
