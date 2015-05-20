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
package fr.inria.soctrace.lib.storage.dbmanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.lib.utils.Portability;

/**
 * DB Manager class for SQLite DBMS (see {@link DBManager}).
 * 
 * Note that for SQLite the default table creators are OK.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class SQLiteDBManager extends DBManager {

	/**
	 * Enable/disable connection tuning through PRAGMAs
	 */
	private final static boolean CONNECTION_TUNING = false;
	

	public SQLiteDBManager(String dbName) throws SoCTraceException {
		super(dbName);
	}

	@Override
	protected void createConnection() throws SoCTraceException {
		try {
			// clean existing connection, if any
			if (connection != null) {
				if (!connection.isClosed()) {
					connection.close();
					decOpen(dbName);
				}
				connection = null;
			} 	
			// create the connection
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:"+getDBPath());
			
			// Connection tuning
			if (CONNECTION_TUNING) {
				Statement stm = connection.createStatement();	
				stm.executeUpdate("PRAGMA synchronous = OFF;");
				stm.executeUpdate("PRAGMA default_synchronous=OFF;");
				stm.executeUpdate("PRAGMA default_cache_size=80000;");	
				stm.executeUpdate("PRAGMA cache_size=80000;");	
				stm.executeUpdate("PRAGMA temp_store=memory;");
				stm.close();
			}			
			connection.setAutoCommit(false);
			
			incOpen(dbName);

		} catch (Exception e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public boolean isDBExisting() throws SoCTraceException {
		File f = new File(getDBPath());
		if (f.exists()) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean checkSettings() throws SoCTraceException {
		File f = new File(Configuration.getInstance().get(
				SoCTraceProperty.sqlite_db_directory));
		if (f.canWrite()) {
			return true;
		} else {
			throw new SoCTraceException(
					"The current directory does not have the write permission.");
		}
	}

	@Override
	public Connection createDB() throws SoCTraceException {
		// nothing to do: connection creation already creates the DB file
		return getConnection();
	}

	@Override
	public Connection openConnection() throws SoCTraceException {
		if (!isDBExisting())
			throw new SoCTraceException("SQLite database '" + dbName + "' does not exist.");
		return getConnection();
	}

	@Override
	public void dropDB() throws SoCTraceException {
		closeConnection();
		File f = new File(getDBPath());
		if (f.exists())
			if (!f.delete())
				throw new SoCTraceException("Error deleting DB file: "
						+ getDBPath());
	}
	
	@Override
	public void exportDB(String path) throws SoCTraceException {
		File sf = new File(getDBPath());
		File sd = new File(path);
		try {
			Files.copy(sf.toPath(), sd.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			System.err.println(e);
			throw new SoCTraceException("Error copying DB file: " + getDBPath());
		}
	}

	@Override
	public void importDB(String path) throws SoCTraceException {
		File sf = new File(path);
		File sd = new File(getDBPath());
		try {
			Files.copy(sf.toPath(), sd.toPath());
		} catch (IOException e) {
			System.err.println(e);
			throw new SoCTraceException("Error copying DB file: " + getDBPath());
		}
	}
	
	/**
	 * @return the path of the file containing the SQLite DB
	 * @throws SoCTraceException 
	 */
	private String getDBPath() throws SoCTraceException {
		String sqlitePath = Configuration.getInstance().get(
				SoCTraceProperty.sqlite_db_directory); 
		File dir = new File(sqlitePath);
		if (!dir.exists())
			throw new SoCTraceException(
					"Directory " + sqlitePath + " does not exists. \n" +
					"Create it or write the correct path in the configuration file " + 
					"("+Configuration.SoCTraceProperty.sqlite_db_directory+" property).");
		return Portability.normalize(sqlitePath + "/" + dbName);
	}

	@Override
	public void createIndex(String table, String column, String name) throws SoCTraceException {
		try {
			Statement stm = getConnection().createStatement();
			stm.execute("CREATE INDEX IF NOT EXISTS " + name + " ON " + table + "(" + column + ");");
			stm.close();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void dropIndex(String table, String name) throws SoCTraceException {
		try {
			Statement stm = getConnection().createStatement();
			stm.execute("DROP INDEX IF EXISTS " + name + ";");
			stm.close();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}
	/*
	 * Default table creators are OK.
	 */
}
