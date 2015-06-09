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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.utils.SQLConstants;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.lib.utils.DBMS;

/**
 * Abstract class providing the API for all the DBMS dependent functionalities needed by the
 * infrastructure.
 * 
 * <p>
 * Furthermore, it provides the basic implementation for all the methods used to define the
 * different DB table schema. Concrete implementation of this class can override the default
 * implementation if needed (e.g., for different SQL dialect issues).
 * 
 * <p>
 * Each table creator method assume that the statement has been created.
 * 
 * <p>
 * WARNING: When/If overriding these methods, ensure that:
 * <ul>
 * <li>all TEXT are CASE SENSITIVE.
 * <li>all VARCHAR are CASE SENSITIVE.
 * </ul>
 * 
 * <p>
 * Normal table creator utilization protocol:
 * <ul>
 * <li>createTableStatement();
 * <li>...
 * <li>initTable1()
 * <li>initTable2()
 * <li>...
 * <li>closeTableStatement();
 * </ul>
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class DBManager {

	/** Debug connections */
	private static boolean DBG = false;
	protected static Map<String, Integer> realopen = new HashMap<String, Integer>();

	protected static void incOpen(String s) {
		if (!DBG)
			return;
		if (!realopen.containsKey(s))
			realopen.put(s, 0);
		realopen.put(s, realopen.get(s) + 1);
		logger.debug("Open: {}, {}", s, realopen.get(s));
		if (realopen.get(s) == 0)
			realopen.remove(s);
	}

	protected static void decOpen(String s) {
		if (!DBG)
			return;
		if (!realopen.containsKey(s))
			realopen.put(s, 0);
		realopen.put(s, realopen.get(s) - 1);
		logger.debug("Open: {}, {}", s, realopen.get(s));
		if (realopen.get(s) == 0)
			realopen.remove(s);
	}

	/**
	 * Logger
	 */
	protected static final Logger logger = LoggerFactory.getLogger(DBManager.class);

	/**
	 * Index flags
	 */
	protected final boolean TABLE_EVENT_PARAM_COLUMN_EVENT_ID_INDEX = false;
	protected final boolean TABLE_EVENT_COLUMN_TIMESTAMP_INDEX = false;

	/**
	 * DB name
	 */
	protected String dbName;

	/**
	 * Connection with the DB
	 */
	protected Connection connection;

	/**
	 * Statement used to create DB tables
	 */
	protected Statement tableStatement;

	/**
	 * Constructor.
	 * 
	 * @param dbName
	 *            DB name
	 * @throws SoCTraceException
	 */
	public DBManager(String dbName) throws SoCTraceException {
		this.dbName = dbName;
		logger.debug("Creating DBManager for DB: {}", dbName);
	}

	/*
	 * Methods to implement (DBMS specific)
	 */

	/**
	 * Create a new connection
	 */
	protected abstract void createConnection() throws SoCTraceException;

	/**
	 * Check if the DB is existing.
	 * 
	 * @return true if the DB exists
	 * @throws SoCTraceException
	 */
	public abstract boolean isDBExisting() throws SoCTraceException;

	/**
	 * Check if the DB settings are correct
	 * 
	 * @return true if they are correct
	 * @throws SoCTraceException
	 */
	public abstract boolean checkSettings() throws SoCTraceException;

	/**
	 * Create the DB.
	 * 
	 * @return the connection
	 * @throws SoCTraceException
	 */
	public abstract Connection createDB() throws SoCTraceException;

	/**
	 * Open the DB connection
	 * 
	 * @return the connection
	 * @throws SoCTraceException
	 */
	public abstract Connection openConnection() throws SoCTraceException;

	/**
	 * Drop the DB and close the connection.
	 * 
	 * @throws SoCTraceException
	 */
	public abstract void dropDB() throws SoCTraceException;

	/**
	 * Export (dump) the DB on the given file.
	 * 
	 * @param path
	 * @throws SoCTraceException
	 */
	public abstract void exportDB(String path) throws SoCTraceException;

	/**
	 * Import the DB from the given file. The assumption is that the DB name is not already present
	 * among SoC-Trace DBs. The database is correctly added to SoC-Trace databases, but the SystemDB
	 * is not modified.
	 * 
	 * @param path
	 * @throws SoCTraceException
	 */
	public abstract void importDB(String path) throws SoCTraceException;

	/**
	 * Create an index on this database.
	 * 
	 * @param table
	 *            table name
	 * @param column
	 *            column name
	 * @param name
	 *            index name
	 * @throws SoCTraceException
	 */
	public abstract void createIndex(String table, String column, String name)
			throws SoCTraceException;

	/**
	 * Drop the given index.
	 * 
	 * @param table
	 *            table name
	 * @param name
	 *            index name
	 * @throws SoCTraceException
	 */
	public abstract void dropIndex(String table, String name) throws SoCTraceException;

	/*
	 * Common methods
	 */

	/**
	 * Static factory for the concrete DB manager. The DBMS used is read from the configuration
	 * file.
	 * 
	 * @param name
	 *            db name
	 * @return a concrete DB manager object
	 */
	public static DBManager getDBManager(String name) {
		try {
			// create DB manager and open the connection
			String dbms = Configuration.getInstance().get(SoCTraceProperty.soctrace_dbms);
			if (dbms.equals(DBMS.SQLITE.toString())) {
				return new SQLiteDBManager(name);
			} else if (dbms.equalsIgnoreCase(DBMS.MYSQL.toString())) {
				return new MySqlDBManager(name);
			}
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * DB name getter
	 * 
	 * @return the DB name
	 */
	public String getDBName() {
		return dbName;
	}

	/**
	 * Connection getter. The connection is lazy initialized.
	 * 
	 * @return the connection
	 * @throws SoCTraceException
	 */
	public Connection getConnection() throws SoCTraceException {
		try {
			if (connection == null || connection.isClosed()) {
				createConnection();
			}
		} catch (SQLException e) {
			throw new SoCTraceException();
		}
		return connection;
	}

	/**
	 * Close the connection
	 * 
	 * @throws SQLException
	 */
	public void closeConnection() throws SoCTraceException {
		try {
			if (connection != null && !connection.isClosed()) {
				decOpen(dbName);
				connection.close();
			}
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Create the statement used to initialize tables. This method must be called before any of the
	 * table creator method.
	 * 
	 * @throws SoCTraceException
	 */
	public void createTableStatement() throws SoCTraceException {
		try {
			tableStatement = getConnection().createStatement();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Close the table creation statement. This method should be called at the end of tables
	 * creation.
	 * 
	 * @throws SQLException
	 */
	public void closeTableStatement() throws SoCTraceException {
		try {
			if (tableStatement != null) {
				tableStatement.close();
				tableStatement = null;
			}
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}

	}

	/*
	 * Default table creators: override them if necessary, following the rules explained in the
	 * documentation of this class.
	 */

	public void initTrace() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + FramesocTable.TRACE
					+ "(ID INTEGER PRIMARY KEY, " + "TRACE_TYPE_ID INTEGER, "
					+ "TRACING_DATE TIMESTAMP, " + "TRACED_APPLICATION TEXT, " + "BOARD TEXT, "
					+ "OPERATING_SYSTEM TEXT, " + "NUMBER_OF_CPUS INTEGER, "
					+ "NUMBER_OF_EVENTS INTEGER, " + "OUTPUT_DEVICE TEXT," + "DESCRIPTION TEXT, "
					+ "PROCESSED BOOLEAN, " + "TRACE_DB_NAME TEXT, " + "ALIAS TEXT, "
					+ "MIN_TIMESTAMP BIGINT, " + "MAX_TIMESTAMP BIGINT, " + "TIMEUNIT INTEGER, "
					+ "NUMBER_OF_PRODUCERS INTEGER) ");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initTraceType() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.TRACE_TYPE + "(ID INTEGER PRIMARY KEY, "
					+ "NAME VARCHAR(128) UNIQUE)");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initTraceParam() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.TRACE_PARAM + "(ID INTEGER PRIMARY KEY, "
					+ "TRACE_ID INTEGER, " + "TRACE_PARAM_TYPE_ID INTEGER, " + "VALUE TEXT )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initTraceParamType() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.TRACE_PARAM_TYPE + "(ID INTEGER PRIMARY KEY, "
					+ "TRACE_TYPE_ID INTEGER, " + "NAME VARCHAR(128), " + "TYPE TEXT, "
					+ "CONSTRAINT uc_type UNIQUE(TRACE_TYPE_ID, NAME) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initTool() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + FramesocTable.TOOL
					+ "(ID INTEGER PRIMARY KEY, " + "NAME VARCHAR(128) UNIQUE, " + "TYPE TEXT, "
					+ "COMMAND TEXT, " + "IS_PLUGIN BOOLEAN, " + "DOC TEXT, "
					+ "EXTENSION_ID TEXT)");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initEvent() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + FramesocTable.EVENT
					+ "(ID INTEGER PRIMARY KEY, " + "EVENT_TYPE_ID INTEGER, "
					+ "EVENT_PRODUCER_ID INTEGER, " + "TIMESTAMP BIGINT, " + "CPU INTEGER, "
					+ "PAGE INTEGER, " + "CATEGORY INTEGER, " + "LPAR BIGINT, " + "DPAR DOUBLE )");

			if (TABLE_EVENT_COLUMN_TIMESTAMP_INDEX)
				tableStatement.execute("CREATE INDEX timestamp_index ON " + FramesocTable.EVENT
						+ " (TIMESTAMP)");

		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initEventType() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.EVENT_TYPE + "(ID INTEGER PRIMARY KEY, " + "CATEGORY INTEGER, "
					+ "NAME VARCHAR(128) UNIQUE)");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initEventParam() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.EVENT_PARAM + "(ID INTEGER PRIMARY KEY, "
					+ "EVENT_ID INTEGER, " + "EVENT_PARAM_TYPE_ID INTEGER, " + "VALUE TEXT )");

			if (TABLE_EVENT_PARAM_COLUMN_EVENT_ID_INDEX)
				tableStatement.execute("CREATE INDEX event_id_index ON "
						+ FramesocTable.EVENT_PARAM + " (EVENT_ID)");

		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initEventParamType() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.EVENT_PARAM_TYPE + "(ID INTEGER PRIMARY KEY, "
					+ "EVENT_TYPE_ID INTEGER, " + "NAME VARCHAR(128), " + "TYPE TEXT,"
					+ "CONSTRAINT uc_type UNIQUE(EVENT_TYPE_ID, NAME) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initEventProducer() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.EVENT_PRODUCER + "(ID INTEGER PRIMARY KEY, "
					+ "TYPE VARCHAR(128), " + "LOCAL_ID VARCHAR(128), " + "NAME TEXT, "
					+ "PARENT_ID INTEGER," + "CONSTRAINT uc_type UNIQUE(TYPE, LOCAL_ID) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initFile() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + FramesocTable.FILE
					+ "(ID INTEGER PRIMARY KEY, " + "PATH TEXT, " + "DESCRIPTION TEXT )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initAnalysisResult() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.ANALYSIS_RESULT + "(ID INTEGER PRIMARY KEY, "
					+ "TOOL_ID INTEGER, " + "TYPE TEXT, " + "DATE TIMESTAMP, "
					+ "DESCRIPTION TEXT )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initAnnotation() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.ANNOTATION + "(ANALYSIS_RESULT_ID INTEGER, "
					+ "ANNOTATION_ID INTEGER, " + "ANNOTATION_TYPE_ID INTEGER, " + "NAME TEXT, "
					+ "PRIMARY KEY (ANALYSIS_RESULT_ID, ANNOTATION_ID) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initAnnotationType() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.ANNOTATION_TYPE + "(ANALYSIS_RESULT_ID INTEGER, "
					+ "ANNOTATION_TYPE_ID INTEGER, " + "NAME VARCHAR(128),"
					+ "PRIMARY KEY (ANALYSIS_RESULT_ID, ANNOTATION_TYPE_ID),"
					+ "CONSTRAINT uc_type UNIQUE(ANALYSIS_RESULT_ID, NAME) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initAnnotationParam() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.ANNOTATION_PARAM + "(ANALYSIS_RESULT_ID INTEGER, "
					+ "ANNOTATION_PARAM_ID INTEGER, " + "ANNOTATION_ID INTEGER, "
					+ "ANNOTATION_PARAM_TYPE_ID INTEGER, " + "VALUE TEXT, "
					+ "PRIMARY KEY (ANALYSIS_RESULT_ID, ANNOTATION_PARAM_ID) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initAnnotationParamType() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.ANNOTATION_PARAM_TYPE + "(ANALYSIS_RESULT_ID INTEGER, "
					+ "ANNOTATION_PARAM_TYPE_ID INTEGER, " + "ANNOTATION_TYPE_ID INTEGER, "
					+ "NAME VARCHAR(128), " + "TYPE TEXT,"
					+ "PRIMARY KEY (ANALYSIS_RESULT_ID, ANNOTATION_PARAM_TYPE_ID),"
					+ "CONSTRAINT uc_type UNIQUE(ANALYSIS_RESULT_ID, ANNOTATION_TYPE_ID, NAME) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initGroup() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.ENTITY_GROUP + "(ANALYSIS_RESULT_ID INTEGER, "
					+ "GROUP_ID INTEGER, " + "PARENT_GROUP_ID INTEGER, " + "NAME TEXT, "
					+ "TARGET_ENTITY TEXT, " + "GROUPING_OPERATOR TEXT, " + "ORDERED BOOLEAN, "
					+ "SEQUENCE_NUMBER INTEGER, " + "PRIMARY KEY (ANALYSIS_RESULT_ID, GROUP_ID) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initGroupMapping() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.GROUP_MAPPING + "(ANALYSIS_RESULT_ID INTEGER, "
					+ "MAPPING_ID INTEGER, " + "GROUP_ID INTEGER, " + "TARGET_ENTITY_ID INTEGER, "
					+ "SEQUENCE_NUMBER INTEGER, "
					+ "PRIMARY KEY (ANALYSIS_RESULT_ID, MAPPING_ID) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initSearch() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + FramesocTable.SEARCH
					+ "(ANALYSIS_RESULT_ID INTEGER, " + "SEARCH_COMMAND TEXT, "
					+ "TARGET_ENTITY TEXT, " + "PRIMARY KEY (ANALYSIS_RESULT_ID) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initSearchMapping() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.SEARCH_MAPPING + "(ANALYSIS_RESULT_ID INTEGER, "
					+ "TARGET_ENTITY_ID INTEGER, "
					+ "PRIMARY KEY (ANALYSIS_RESULT_ID, TARGET_ENTITY_ID) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public void initProcessedTrace() throws SoCTraceException {
		try {
			tableStatement.execute(SQLConstants.CREATE_TABLE_IF_NOT_EXISTS
					+ FramesocTable.PROCESSED_TRACE + "(ANALYSIS_RESULT_ID INTEGER, "
					+ "PROCESSED_TRACE_ID INTEGER, " + "PRIMARY KEY (ANALYSIS_RESULT_ID) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

}
