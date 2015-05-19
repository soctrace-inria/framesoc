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
package fr.inria.soctrace.lib.storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.IModelElement;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.dbmanager.DBManager;
import fr.inria.soctrace.lib.storage.dbmanager.MySqlDBManager;
import fr.inria.soctrace.lib.storage.dbmanager.SQLiteDBManager;
import fr.inria.soctrace.lib.storage.visitors.ModelVisitor;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.DBMS;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;

/**
 * Base abstract class for DB Objects. 
 * It provides basic operations common to all DBs.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class DBObject {
	
	/**
	 * Logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(DBObject.class);

	protected DBManager dbManager = null;
	protected ModelVisitor saveVisitor = null;
	protected ModelVisitor deleteVisitor = null;
	protected ModelVisitor updateVisitor = null;
	private boolean visitorsInitialized = false;
	
	/**
	 * DB mode
	 */
	public static enum DBMode {
		DB_CREATE, /** Create the DB */
		DB_OPEN    /** Open an existing DB */
	}
	
	/**
	 * Constructor
	 * @param name DB name
	 * @param mode DB object creation mode
	 * @throws SoCTraceException
	 */	
	public DBObject(String name, DBMode mode) throws SoCTraceException
	{	
		dbManager = getDBManager(name);
		
		// create or open the database 
		switch(mode) {
			case DB_CREATE:
				createDB();
				break;
			case DB_OPEN:
				openDB();
				break;
			default:
				break;
		}
		
		logger.debug("Create DBObject: {}, {}", name, mode.name());
	}
	
	/**
	 * Check if a database with a given name exists.
	 * 
	 * @param name database name
	 * @return true if the database exists
	 * @throws SoCTraceException
	 */
	public static boolean isDBExisting(String name) throws SoCTraceException {
		DBManager dbm = getDBManager(name);
		return dbm.isDBExisting();
	}
	
	/**
	 * Check if the settings for a database are correct
	 * 
	 * @param name
	 *            database name
	 * @return true if the settings are correct
	 * @throws SoCTraceException
	 */
	public static boolean checkSettings(String name) throws SoCTraceException {
		DBManager dbm = getDBManager(name);
		return dbm.checkSettings();
	}
	
	/**
	 * Close the database.
	 * To be used in a finally block.
	 * 
	 * @param dbObj database object
	 */
	public static void finalClose(DBObject dbObj) {
		if (dbObj != null) {
			try {
				dbObj.close();
			} catch (SoCTraceException e) {
				logger.error("Error closing the DB object {}", dbObj.getDBName());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Create a new database.
	 * It creates a SystemDB or a TraceDB according to the 
	 * actual concrete class.
	 * @throws SoCTraceException
	 */
	protected abstract void createDB() throws SoCTraceException;
	
	/**
	 * Open an existing database.
	 * It opens a SystemDB or a TraceDB according to the 
	 * actual concrete class.
	 * @throws SoCTraceException
	 */
	protected abstract void openDB() throws SoCTraceException;
		
	/**
	 * Clean cache structures, if any.
	 */
	protected abstract void cleanCache();
	
	/**
	 * Commit current database transaction.
	 * @throws SoCTraceException
	 */
	public synchronized void commit() throws SoCTraceException {
		
		executeVisitorBatches();
		clearVisitorBatches();
		
		try {
			if(!dbManager.getConnection().isClosed()) { 
				dbManager.getConnection().commit();	
			}
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}	

	/**
	 * Rollback current database transaction.
	 * @throws SoCTraceException
	 */
	public synchronized void rollback() throws SoCTraceException {
		
		clearVisitorBatches();
			
		try {
			if(!dbManager.getConnection().isClosed()) { 
				dbManager.getConnection().rollback();	
			}
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}	
	
	/**
	 * Commit, close visitors, clean cache structures
	 * and close connection.
	 * @throws SoCTraceException
	 */
	public synchronized void close() throws SoCTraceException {

		commit();
		closeVisitors();	
		cleanCache();
		dbManager.closeConnection();
		
		logger.debug("Close DBObject: {}", dbManager.getDBName());
	}	
	
	/**
     * Drop the actual database and automatically close the DB object.
     * @throws SoCTraceException
     */
    public synchronized void dropDatabase() throws SoCTraceException {
    	dbManager.dropDB();
    }

	/**
	 * Connection getter. It opens the connection if closed.
	 * @return The DB connection
	 * @throws SoCTraceException 
	 */
	public Connection getConnection() throws SoCTraceException {
		return dbManager.getConnection();
	}
	
	/**
	 * DB name getter
	 * @return The DB name.
	 */
	public String getDBName() {
		return dbManager.getDBName();
	}
		
	/** 
	 * Check if the DB related to this DBObject is already existing.
	 * 
	 * @return true if this DB is already existing, false otherwise
	 * @throws SoCTraceException
	 */
	protected boolean isThisDBExisting() throws SoCTraceException {
		return dbManager.isDBExisting();
	}
				
	/**
	 * Get the first available sequential id from the DB for 
	 * a given table. Use it if you have to insert just one row.
	 * 
	 * @param tableName name of the table
	 * @param idColumnName name of the id column
	 * @return a new id 
	 * @throws SoCTraceException
	 */
	public synchronized int getNewId(String tableName, String idColumnName) throws SoCTraceException {
		try {
			Statement stm = dbManager.getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SELECT "+ idColumnName +" FROM " + tableName + " ORDER BY " + idColumnName);
			int expectedNext = 0;
			while (rs.next()) {
				int id = rs.getInt(idColumnName);
				if (id > expectedNext)
					break;
				expectedNext++;
			}
			stm.close();
			return expectedNext;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}		
	}

	/**
	 * Get the maximum id from the DB for a given table.
	 * Use it if you have to insert a sequence.
	 * 
	 * @param tableName name of the table
	 * @param idColumnName name of the id column
	 * @return the max id
	 * @throws SoCTraceException
	 */
	public synchronized int getMaxId(String tableName, String idColumnName) throws SoCTraceException {
		try {
			Statement stm = dbManager.getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SELECT MAX("+ idColumnName +") FROM " + tableName);
			int max = 0;
			if (rs.next()) {
				max = rs.getInt(1);
			}
			stm.close();
			return max;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}		
	}
	
	/**
	 * Get the number of rows in a given table
	 * @param tableName name of the table
	 * @return the count(*) for the table
	 * @throws SoCTraceException
	 */
	public synchronized int getCount(String tableName) throws SoCTraceException {
		try {
			Statement stm = dbManager.getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SELECT COUNT(*) FROM " + tableName);
			int count = 0;
			if (rs.next()) {
				count = rs.getInt(1);
			}
			stm.close();
			return count;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}				
	}
	
	/*
	 * Visitors
	 */
	
	/**
	 * Save an IModelElement into the DB.
	 * 
	 * @param element element to save
	 * @throws SoCTraceException
	 */
	public synchronized void save(IModelElement element) throws SoCTraceException {
		if (visitorsInitialized == false) {
			initializeVisitors();
			visitorsInitialized = true;
		}
		element.accept(saveVisitor);
	}

	/**
	 * Delete an IModelElement into the DB.
	 * 
	 * @param element element to delete
	 * @throws SoCTraceException
	 */
	public synchronized void delete(IModelElement element) throws SoCTraceException {
		if (visitorsInitialized == false) {
			initializeVisitors();
			visitorsInitialized = true;
		}
		element.accept(deleteVisitor);
	}

	/**
	 * Update an IModelElement into the DB.
	 * 
	 * @param element element to update
	 * @throws SoCTraceException
	 */
	public synchronized void update(IModelElement element) throws SoCTraceException {
		if (visitorsInitialized == false) {
			initializeVisitors();
			visitorsInitialized = true;
		}
		element.accept(updateVisitor);
	}

	/**
	 * Execute and clear visitor batches.
	 * @throws SoCTraceException
	 */
	public synchronized void flushVisitorBatches() throws SoCTraceException {
		executeVisitorBatches();
		clearVisitorBatches();
	}

	/*
	 * These methods must be called in a synchronized environment
	 */

	/**
	 * Initialize the visitors.
	 * 
	 * @throws SoCTraceException
	 */
	protected abstract void initializeVisitors() throws SoCTraceException;

	/**
	 * Close visitor batches
	 * @throws SoCTraceException
	 */
	private void closeVisitors() throws SoCTraceException {
		if (visitorsInitialized) {
			saveVisitor.close();
			saveVisitor = null;
			deleteVisitor.close();
			deleteVisitor = null;
			updateVisitor.close();
			updateVisitor = null;
			visitorsInitialized = false;
		}		
	}
	
	/**
	 * Execute visitor batches.
	 * @throws SoCTraceException
	 */
	private void executeVisitorBatches() throws SoCTraceException {
		if (visitorsInitialized) {
			saveVisitor.executeBatches();
			deleteVisitor.executeBatches();
			updateVisitor.executeBatches();
		}
	}

	/**
	 * Clear visitor batches.
	 * @throws SoCTraceException
	 */
	private void clearVisitorBatches() throws SoCTraceException {
		if (visitorsInitialized) {
			saveVisitor.clearBatches();
			deleteVisitor.clearBatches();
			updateVisitor.clearBatches();
		}		
	}

	// utilities
	
	private static DBManager getDBManager(String name) throws SoCTraceException {
		// create DB manager and open the connection
		String dbms = Configuration.getInstance().get(SoCTraceProperty.soctrace_dbms);
		if (dbms.equalsIgnoreCase(DBMS.SQLITE.toString())) {
			return new SQLiteDBManager(name);
		} else if (dbms.equalsIgnoreCase(DBMS.MYSQL.toString())) {
			return new MySqlDBManager(name);
		}
		throw new SoCTraceException("Unknown DBMS: " + dbms);
	}

}
