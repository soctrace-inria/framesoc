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
package fr.inria.soctrace.lib.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton for managing the configuration variables 
 * of SoC-Trace Infrastructure.
 * 
 * <p>
 * Properties are accessed via the {@link #get(SoCTraceProperty)} 
 * or {@link #get(String)} methods.
 * 
 * <p>
 * Note: it is not possible to use IWorkspace Eclipse facilities 
 * if we want to let external application (non-Eclipse application) 
 * to use the Configuration class.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class Configuration {
	
	/**
	 * Enumeration for SoC-Trace configuration variables names.
	 */
	public enum SoCTraceProperty {

		// General properties
		soctrace_dbms,           /** DBMS used: mysql, sqlite */
		soctrace_db_name,        /** System DB name */
		max_view_instances,      /** Max number of instances for a Framesoc view*/
		trace_db_indexing,       /** Flag stating if automatic indexing is done after import: true, false */
		
		// MySQL specific
		mysql_base_db_jdbc_url,  /** Base URL to create DB connection */
		mysql_db_user,	         /** MySQL DB user */
		mysql_db_password,       /** MySQL DB password */
		
		// SQLite specific
		sqlite_db_directory;     /** Full path of the directory containing all DBs */
		
	}

	/**
	 * Configuration file path.
	 * It is in the user's home and its name is hard-coded in the static 
	 * initialization.
	 */
	public final static String CONF_FILE_PATH;
	
	static {
		CONF_FILE_PATH = Portability.normalize(Portability.getUserHome() + "/.soctrace.conf");
	}

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(Configuration.class);	
	
	/** 
	 * File heading
	 */
	private final static String HEADING = "SoC-Trace Configuration File";
	
	/**
	 * The instance
	 */
	private static Configuration instance = new Configuration();
	
	/**
	 * Map containing configuration variables values
	 */
	private Properties config;
	
	/**
	 * Map containing the default values
	 */
	private Properties defaults;
	
	/**
	 * Get the instance.
	 * @return the singleton instance.
	 */
	public static Configuration getInstance() {
		return instance;
	}
	
	/**
	 * Return the value of the configuration variable corresponding
	 * to the given key, or null if not found.
	 * @param key variable name
	 * @return the variable value or null if the key is not found
	 */
	public String get(String key) {
		return config.getProperty(key);
	}
	
	/**
	 * Return the default value of the configuration variable corresponding
	 * to the given key, or null if not found.
	 * @param key variable name
	 * @return the variable value or null if the key is not found
	 */
	public String getDefault(String key) {
		return defaults.getProperty(key);
	}

	/**
	 * Return the value of the configuration variable corresponding
	 * to the given key, or null if not found.
	 * @param key variable name
	 * @return the variable value or null if the key is not found
	 */
	public String get(SoCTraceProperty key) {
		return config.getProperty(key.toString());
	}
	
	/**
	 * Return the default value of the configuration variable corresponding
	 * to the given key, or null if not found.
	 * @param key variable name
	 * @return the variable value or null if the key is not found
	 */
	public String getDefault(SoCTraceProperty key) {
		return defaults.getProperty(key.toString());
	}
	
	/**
	 * Set the given property. This method is to be used when initializing 
	 * the system.
	 * @param key variable name
	 * @param value variable value
	 */
	public void set(SoCTraceProperty key, String value) {
		config.setProperty(key.toString(), value);
	}
	
	/**
	 * Save current values onto the configuration file.
	 */
	public void saveOnFile() {
		File file = new File(CONF_FILE_PATH);
		checkPaths();
		try {
			config.store(new FileOutputStream(file), HEADING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Private constructor.
	 * Load the map with default values, then read the configuration
	 * file: soctrace.conf. Such a file MUST be in the user's home.
	 */
	private Configuration() {
		
		defaults = new Properties();
		config = new Properties();
	
		loadDefaults();
		
		loadConfFile();
	}
	
	/**
	 * Load default values for some variables.
	 */
	private void loadDefaults() {
		
		// General 
		defaults.setProperty(SoCTraceProperty.soctrace_dbms.toString(), DBMS.SQLITE.toString());
		defaults.setProperty(SoCTraceProperty.soctrace_db_name.toString(), "SOCTRACE_SYSTEM_DB");
		defaults.setProperty(SoCTraceProperty.max_view_instances.toString(), "5");
		defaults.setProperty(SoCTraceProperty.trace_db_indexing.toString(), "true");
		
		// MySQL
		defaults.setProperty(SoCTraceProperty.mysql_base_db_jdbc_url.toString(), "jdbc:mysql://localhost");
		defaults.setProperty(SoCTraceProperty.mysql_db_user.toString(), "root");
		defaults.setProperty(SoCTraceProperty.mysql_db_password.toString(), "");
	
		// SQLite
		String defaultSQLiteDir = Portability.normalize(Portability.getUserHome());
		defaults.setProperty(SoCTraceProperty.sqlite_db_directory.toString(), defaultSQLiteDir);
		
		for (Object key: defaults.keySet()) {
			config.setProperty((String)key, defaults.getProperty((String)key));
		}		
	}
	
	/**
	 * Load the configuration file. 
	 */
	private void loadConfFile() {
		try {			
			File file = new File(CONF_FILE_PATH);
			if (!file.exists()) {
				
				logger.debug("");
				logger.debug("##########################################################################");
				logger.debug("Configuration file not found at: " + CONF_FILE_PATH);
				logger.debug("It will be automatically created with default values.");

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				config.store(baos, HEADING);
				logger.debug(">>> PLEASE CHECK THESE VALUES! <<< \n\n" + baos.toString());
				logger.debug("##########################################################################");
				
				config.store(new FileOutputStream(file), HEADING);
				
			} else {
				logger.debug("Configuration file: " + CONF_FILE_PATH);
				config.load(new FileInputStream(file));			
				checkPaths();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Check that all paths ends with the path separator.
	 */
	private void checkPaths() {
		String s;
		String end = Portability.getPathSeparator();
		
		s = Portability.normalize(get(SoCTraceProperty.sqlite_db_directory));
		if(!s.endsWith(end)){
			config.setProperty(SoCTraceProperty.sqlite_db_directory.toString(), s + end );
		}
	}
	
	/**
	 * Print the configuration variables.
	 */
	public void printConfiguration() {
		
		System.out.println("--------------------------------------------------------------------------------");
		System.out.println("SoC-Trace Configuration Variables");
		System.out.println("--------------------------------------------------------------------------------");
		try {
			config.store(System.out, HEADING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("--------------------------------------------------------------------------------");	
	}
	
}
