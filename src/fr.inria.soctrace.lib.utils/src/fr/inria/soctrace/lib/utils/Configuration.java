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
package fr.inria.soctrace.lib.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton for managing the configuration variables of Framesoc
 * Infrastructure.
 * 
 * <p>
 * Properties are accessed via the {@link #get(SoCTraceProperty)} or
 * {@link #get(String)} methods.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class Configuration {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory
			.getLogger(Configuration.class);

	/**
	 * Constant corresponding to an infinite number of views for the
	 * {@link SoCTraceProperty.max_view_instances} property.
	 */
	public final static int INFINITE_VIEWS = -1;
	
	private static boolean configDirHome = false;
	
	/**
	 * Enumeration for SoC-Trace configuration variables names.
	 */
	public enum SoCTraceProperty {

		// General properties

		/** DBMS used: mysql, sqlite */
		soctrace_dbms,

		/** System DB name */
		soctrace_db_name,

		/** Max number of instances for a Framesoc view */
		max_view_instances,

		/**
		 * Flag stating if automatic timestamp indexing is done after import:
		 * true, false
		 */
		trace_db_ts_indexing,

		/**
		 * Flag stating if automatic event id indexing is done after import:
		 * true, false
		 */
		trace_db_eid_indexing,

		/**
		 * Flag stating if tools and their results are automatically removed if
		 * not found in runtime: true, false
		 */
		ask_for_tool_removal,

		/**
		 * Flag allowing the existence of multiple views of a given type for the
		 * same trace: true, false.
		 */
		allow_view_replication,

		// MySQL specific

		/** Base URL to create DB connection */
		mysql_base_db_jdbc_url,

		/** MySQL DB user */
		mysql_db_user,

		/** MySQL DB password */
		mysql_db_password,

		// SQLite specific

		/** Full path of the directory containing all DBs */
		sqlite_db_directory;

	}

	/**
	 * Configuration file name.
	 */
	private final static String CONF_FILE_NAME = "soctrace.conf";

	/**
	 * Configuration directory, relative to eclipse installation directory.
	 */
	private final static String CONF_DIR = "configuration" + File.separator
			+ Activator.PLUGIN_ID + File.separator;

	/**
	 * Configuration file full path. Statically initialized.
	 */
	private static String ConfFilePath = Platform.getInstallLocation()
			.getURL().getPath()
			+ CONF_DIR + CONF_FILE_NAME;

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
	 * 
	 * @return the singleton instance.
	 */
	public static Configuration getInstance() {
		return instance;
	}

	/**
	 * Return the value of the configuration variable corresponding to the given
	 * key, or null if not found.
	 * 
	 * @param key
	 *            variable name
	 * @return the variable value or null if the key is not found
	 */
	public String get(String key) {
		return config.getProperty(key);
	}

	/**
	 * Return the default value of the configuration variable corresponding to
	 * the given key, or null if not found.
	 * 
	 * @param key
	 *            variable name
	 * @return the variable value or null if the key is not found
	 */
	public String getDefault(String key) {
		return defaults.getProperty(key);
	}

	/**
	 * Return the value of the configuration variable corresponding to the given
	 * key, or null if not found.
	 * 
	 * @param key
	 *            variable name
	 * @return the variable value or null if the key is not found
	 */
	public String get(SoCTraceProperty key) {
		return config.getProperty(key.toString());
	}

	/**
	 * Return the default value of the configuration variable corresponding to
	 * the given key, or null if not found.
	 * 
	 * @param key
	 *            variable name
	 * @return the variable value or null if the key is not found
	 */
	public String getDefault(SoCTraceProperty key) {
		return defaults.getProperty(key.toString());
	}

	/**
	 * Set the given property. This method is to be used when initializing the
	 * system.
	 * 
	 * @param key
	 *            variable name
	 * @param value
	 *            variable value
	 */
	public void set(SoCTraceProperty key, String value) {
		config.setProperty(key.toString(), value);
	}

	/**
	 * Save current values onto the configuration file.
	 */
	public void saveOnFile() {
		File dir = new File(Platform.getInstallLocation().getURL().getPath()
				+ CONF_DIR);
		
		if (!dir.exists()) {
			if (dir.canWrite()) {
				dir.mkdir();
			}
		}
		
		if (!dir.canWrite()) {
			// Set as default in the home directory
			dir = new File(System.getProperty("user.home") + File.separator + CONF_DIR);

			configDirHome = true;
			if (!dir.exists())
				dir.mkdirs();
		}
		
		ConfFilePath = dir + File.separator + CONF_FILE_NAME;
		File file = new File(ConfFilePath);
		
		checkPaths();
		try {
			config.store(new FileOutputStream(file), HEADING);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Private constructor. Load the map with default values, then read the
	 * configuration file: soctrace.conf. Such a file MUST be in the user's
	 * home.
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
		defaults.setProperty(SoCTraceProperty.soctrace_dbms.toString(),
				DBMS.SQLITE.toString());
		defaults.setProperty(SoCTraceProperty.soctrace_db_name.toString(),
				"SOCTRACE_SYSTEM_DB");
		defaults.setProperty(SoCTraceProperty.max_view_instances.toString(),
				"5");
		defaults.setProperty(SoCTraceProperty.trace_db_ts_indexing.toString(),
				"true");
		defaults.setProperty(SoCTraceProperty.trace_db_eid_indexing.toString(),
				"false");
		defaults.setProperty(SoCTraceProperty.ask_for_tool_removal.toString(),
				"false");
		defaults.setProperty(
				SoCTraceProperty.allow_view_replication.toString(), "true");

		// MySQL
		defaults.setProperty(
				SoCTraceProperty.mysql_base_db_jdbc_url.toString(),
				"jdbc:mysql://localhost");
		defaults.setProperty(SoCTraceProperty.mysql_db_user.toString(), "root");
		defaults.setProperty(SoCTraceProperty.mysql_db_password.toString(), "");

		// SQLite
		String defaultSQLiteDir = Portability.normalize(Portability
				.getUserHome());
		defaults.setProperty(SoCTraceProperty.sqlite_db_directory.toString(),
				defaultSQLiteDir);

		for (Object key : defaults.keySet()) {
			config.setProperty((String) key, defaults.getProperty((String) key));
		}
	}

	/**
	 * Load the configuration file.
	 */
	private void loadConfFile() {
		try {
			File file = new File(getConfFile());
			if (!file.exists()) {
				logger.debug("");
				logger.debug("##########################################################################");
				logger.debug("Configuration file not found at: "
						+ ConfFilePath);
				logger.debug("It will be automatically created with default values.");

				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				config.store(baos, HEADING);
				logger.debug(">>> PLEASE CHECK THESE VALUES! <<< \n\n"
						+ baos.toString());
				logger.debug("##########################################################################");

				saveOnFile();
				
				// If the configuration was located inside the home directory
				if (configDirHome)
					// Display a warning
					MessageDialog
							.openError(
									Display.getCurrent().getActiveShell(),
									"Configuration File Location",
									"Warning: Configuration file will not in default location since the eclipse directory does not the write permission. The configuration file will be placed in "
											+ ConfFilePath);
			} else {
				logger.debug("Configuration file: " + ConfFilePath);
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
		if (!s.endsWith(end)) {
			config.setProperty(SoCTraceProperty.sqlite_db_directory.toString(),
					s + end);
		}
	}
	
	private String getConfFile() {
		if (fileExists())
			return ConfFilePath;

		// Set ConfFilePath in the home directory
		return System.getProperty("user.home") + File.separator + CONF_DIR + File.separator + CONF_FILE_NAME;
	}

	/**
	 * Print the configuration variables.
	 */
	public void printConfiguration() {

		System.out
				.println("--------------------------------------------------------------------------------");
		System.out.println("SoC-Trace Configuration Variables");
		System.out
				.println("--------------------------------------------------------------------------------");
		try {
			config.store(System.out, HEADING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out
				.println("--------------------------------------------------------------------------------");
	}

	public boolean fileExists() {
		// Check default directory
		File file = new File(ConfFilePath);
		if (file.exists())
			return true;

		// Check in home directory
		file = new File(System.getProperty("user.home") + File.separator + CONF_DIR
				+ File.separator + CONF_FILE_NAME);
		if (file.exists()) {
			ConfFilePath = System.getProperty("user.home") + File.separator + CONF_DIR
					+ File.separator + CONF_FILE_NAME;
			return true;
		}
		return false;
	}

	/**
	 * Save the properties as a Map
	 * 
	 * @return the saved properties
	 */
	public Map<String, String> saveProperties() {
		Map<String, String> propertyValues = new HashMap<String, String>();
		for (Object key : config.keySet()) {
			propertyValues.put((String) key, config.getProperty((String) key));
		}

		return propertyValues;
	}

	/**
	 * Set the properties to the values in the map given as parameters
	 * 
	 * @param propertyValues
	 *            a map containing the values to the properties
	 */
	public void setProperties(Map<String, String> propertyValues) {
		for (String key : propertyValues.keySet()) {
			config.setProperty(key, propertyValues.get(key));
		}
	}

}
