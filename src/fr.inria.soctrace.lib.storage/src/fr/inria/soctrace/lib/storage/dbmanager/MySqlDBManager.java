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
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.utils.SQLConstants;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;

/**
 * DB Manager class for MySQL DBMS (see {@link DBManager}).
 * 
 * Note that some default table creators are overridden
 * to use MySQL specific collate.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class MySqlDBManager extends DBManager {

	public MySqlDBManager(String dbName) throws SoCTraceException {
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
			String dbBaseUrl = Configuration.getInstance().get(SoCTraceProperty.mysql_base_db_jdbc_url);
			String dbUser = Configuration.getInstance().get(SoCTraceProperty.mysql_db_user);
			String dbPassword = Configuration.getInstance().get(SoCTraceProperty.mysql_db_password);
	    	Class.forName("com.mysql.jdbc.Driver").newInstance();   	    	
	    	connection = DriverManager.getConnection(dbBaseUrl, dbUser, dbPassword);
			connection.setAutoCommit(false); // for efficiency	
			
			incOpen(dbName);
			
		} catch(Exception e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public boolean isDBExisting() throws SoCTraceException {
		try {
			DatabaseMetaData meta = getConnection().getMetaData();
			ResultSet rs = meta.getCatalogs();
			while (rs.next()) {
				String n = rs.getString("TABLE_CAT");
				if (n.equals(dbName))
					return true;
			}
			return false;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}
	
	@Override
	public boolean checkSettings() throws SoCTraceException {
		return true;
	}

	@Override
	public Connection createDB() throws SoCTraceException {
		try {		
			Statement stm = getConnection().createStatement();
			stm.execute("CREATE DATABASE IF NOT EXISTS " + dbName);
			stm.close();
			getConnection().setCatalog(dbName);
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
		return getConnection();		
	}

	@Override
	public Connection openConnection() throws SoCTraceException {
		try {
			if (!isDBExisting())
				throw new SoCTraceException("MySQL database '" + dbName + "' does not exist.");
			
			getConnection().setCatalog(dbName);
			return getConnection();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void dropDB() throws SoCTraceException {
    	try {
    		Statement stm = getConnection().createStatement();
    		stm.executeUpdate("DROP DATABASE "+dbName);
    		stm.close();
		    getConnection().commit();
		    closeConnection();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}
	
	@Override
	public void exportDB(String path) throws SoCTraceException {
		String user = Configuration.getInstance().get(SoCTraceProperty.mysql_db_user);
		String password = Configuration.getInstance().get(SoCTraceProperty.mysql_db_password);
		String executeCmd = "";
		if (!password.trim().equals("")) {
			executeCmd = "mysqldump -u " + user + " -p" + password + " " + dbName + " -r " + path;	
		} else {
			executeCmd = "mysqldump -u " + user + " " + dbName + " -r " + path;
		}
        
        Process runtimeProcess;
        try {
            runtimeProcess = Runtime.getRuntime().exec(executeCmd);
            checkComplete("dump db", runtimeProcess.waitFor());
         } catch (Exception ex) {
            ex.printStackTrace();
        }
	}

	@Override
	public void importDB(String path) throws SoCTraceException {
		String user = Configuration.getInstance().get(SoCTraceProperty.mysql_db_user);
		String password = Configuration.getInstance().get(SoCTraceProperty.mysql_db_password);

		// commands
		String[] createCmd;
		String[] executeCmd;
		if (!password.trim().equals("")) {
			createCmd = new String[]{"mysqladmin", "--user=" + user, "--password=" + password, 
					"create", dbName};;
			executeCmd = new String[]{"mysql", "--user=" + user, "--password=" + password, 
					dbName, "-e", "source "+path};
		} else {
			createCmd = new String[]{"mysqladmin", "--user=" + user, "create", dbName};;
			executeCmd = new String[]{"mysql", "--user=" + user, dbName, "-e", "source "+path};
		}
		 
        Process runtimeProcess;
        try {
        	runtimeProcess = Runtime.getRuntime().exec(createCmd);
        	checkComplete("create db", runtimeProcess.waitFor());
        	
            runtimeProcess = Runtime.getRuntime().exec(executeCmd);
            checkComplete("restore db", runtimeProcess.waitFor());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
	}
	
	@Override
	public void createIndex(String table, String column, String name) throws SoCTraceException {
		if (indexExists(table, name))
			return;
		try {
			Statement stm = getConnection().createStatement();
			stm.execute("CREATE INDEX " + name + " ON " + table + "(" + column + ");");
			stm.close();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}		
	}

	@Override
	public void dropIndex(String table, String name) throws SoCTraceException {
		if (!indexExists(table, name))
			return;
		try {
			Statement stm = getConnection().createStatement();
			String query = "DROP INDEX " + name +  " ON " + table;
			stm.execute(query); 
			stm.close();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}
	
	private boolean indexExists(String table, String index) throws SoCTraceException {
		try {		
			Statement stm = getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SHOW INDEX FROM " + table + " WHERE Key_name='"+index+"'");
			if (rs.next()) {
				stm.close();
				return true;
			}
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
		return false;
	}

	/**
	 * Check if operation completed 
	 * @param operation operation name
	 * @param complete complete flag
	 * @throws SoCTraceException 
	 */
	private void checkComplete(String operation, int complete) throws SoCTraceException {
        if (complete == 0) {
            logger.debug(operation + " done");
        } else {
        	logger.error(operation + " not done");
            throw new SoCTraceException("Operation " + operation + " not completed. Exit value " + complete);
        }		
	}
	
	/*
	 * Table creators
	 */
	
	@Override
	public void initTrace() throws SoCTraceException {
		try {	
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS +
					FramesocTable.TRACE + 
					"(ID INTEGER PRIMARY KEY, " +
					"TRACE_TYPE_ID INTEGER, " +
					"TRACING_DATE TIMESTAMP, " +
					"TRACED_APPLICATION TEXT COLLATE latin1_general_cs, " +
					"BOARD TEXT COLLATE latin1_general_cs, " +
					"OPERATING_SYSTEM TEXT COLLATE latin1_general_cs, " +
					"NUMBER_OF_CPUS INTEGER, " +
					"NUMBER_OF_EVENTS INTEGER, " +
					"OUTPUT_DEVICE TEXT COLLATE latin1_general_cs," +
					"DESCRIPTION TEXT COLLATE latin1_general_cs, " +
					"PROCESSED BOOLEAN, " +
					"TRACE_DB_NAME TEXT COLLATE latin1_general_cs, " +
					"ALIAS TEXT COLLATE latin1_general_cs, " +
					"MIN_TIMESTAMP BIGINT, " +
					"MAX_TIMESTAMP BIGINT, " +
					"TIMEUNIT INTEGER, " +
					"NUMBER_OF_PRODUCERS INTEGER)");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void initTraceType() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.TRACE_TYPE + 
					"(ID INTEGER PRIMARY KEY, " +
					"NAME VARCHAR(128) UNIQUE COLLATE utf8_bin)");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void initTraceParam() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.TRACE_PARAM + 
					"(ID INTEGER PRIMARY KEY, " +
					"TRACE_ID INTEGER, " +
					"TRACE_PARAM_TYPE_ID INTEGER, " +
					"VALUE TEXT COLLATE latin1_general_cs)");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void initTraceParamType() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.TRACE_PARAM_TYPE + 
					"(ID INTEGER PRIMARY KEY, " +
					"TRACE_TYPE_ID INTEGER, " +
					"NAME VARCHAR(128) COLLATE utf8_bin, " +
					"TYPE TEXT COLLATE latin1_general_cs, " +
					"CONSTRAINT uc_type UNIQUE(TRACE_TYPE_ID, NAME) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void initTool() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.TOOL + 
					"(ID INTEGER PRIMARY KEY, " +
					"NAME VARCHAR(128) UNIQUE COLLATE utf8_bin, " +
					"TYPE TEXT COLLATE latin1_general_cs, " +
					"COMMAND TEXT COLLATE latin1_general_cs, " +
					"IS_PLUGIN BOOLEAN, " +
					"DOC TEXT COLLATE latin1_general_cs, " + 
					"EXTENSION_ID TEXT COLLATE latin1_general_cs)");

		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}
	
	// Default is OK
	// public void initEvent() throws SoCTraceException { }
	
	@Override
	public void initEventType() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.EVENT_TYPE + 
					"(ID INTEGER PRIMARY KEY, " +
					"CATEGORY INTEGER, " +
					"NAME VARCHAR(128) UNIQUE COLLATE utf8_bin)");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void initEventParam() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.EVENT_PARAM + 
					"(ID INTEGER PRIMARY KEY, " +
					"EVENT_ID INTEGER, " +
					"EVENT_PARAM_TYPE_ID INTEGER, " +
					"VALUE TEXT COLLATE latin1_general_cs)");

			if (TABLE_EVENT_PARAM_COLUMN_EVENT_ID_INDEX)
				tableStatement.execute("CREATE INDEX event_id_index ON " 
						+ FramesocTable.EVENT_PARAM + " (EVENT_ID)");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void initEventParamType() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.EVENT_PARAM_TYPE + 
					"(ID INTEGER PRIMARY KEY, " +
					"EVENT_TYPE_ID INTEGER, " +
					"NAME VARCHAR(128) COLLATE utf8_bin, " +
					"TYPE TEXT COLLATE latin1_general_cs," +
					"CONSTRAINT uc_type UNIQUE(EVENT_TYPE_ID, NAME) )");			
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}
	
	@Override
	public void initEventProducer() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.EVENT_PRODUCER + 
					"(ID INTEGER PRIMARY KEY, " +
					"TYPE VARCHAR(128) COLLATE utf8_bin, " +
					"LOCAL_ID VARCHAR(128) COLLATE utf8_bin, " +
					"NAME TEXT COLLATE latin1_general_cs, " +
					"PARENT_ID INTEGER," +
					"CONSTRAINT uc_type UNIQUE(TYPE, LOCAL_ID) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void initFile() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.FILE + 
					"(ID INTEGER PRIMARY KEY, " +
					"PATH TEXT COLLATE latin1_general_cs, " +
					"DESCRIPTION TEXT COLLATE latin1_general_cs)");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}
	
	@Override
	public void initAnalysisResult() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.ANALYSIS_RESULT + 
					"(ID INTEGER PRIMARY KEY, " +
					"TOOL_ID INTEGER, " +
					"TYPE TEXT COLLATE latin1_general_cs, " +
					"DATE TIMESTAMP, " +
					"DESCRIPTION TEXT COLLATE latin1_general_cs)");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}	

	@Override
	public void initAnnotation() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.ANNOTATION + 
					"(ANALYSIS_RESULT_ID INTEGER, " +
					"ANNOTATION_ID INTEGER, " +
					"ANNOTATION_TYPE_ID INTEGER, " +
					"NAME TEXT COLLATE latin1_general_cs, " +
					"PRIMARY KEY (ANALYSIS_RESULT_ID, ANNOTATION_ID) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void initAnnotationType() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.ANNOTATION_TYPE + 
					"(ANALYSIS_RESULT_ID INTEGER, " +
					"ANNOTATION_TYPE_ID INTEGER, " +
					"NAME VARCHAR(128) COLLATE utf8_bin," +
					"PRIMARY KEY (ANALYSIS_RESULT_ID, ANNOTATION_TYPE_ID)," +
					"CONSTRAINT uc_type UNIQUE(ANALYSIS_RESULT_ID, NAME) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void initAnnotationParam() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.ANNOTATION_PARAM + 
					"(ANALYSIS_RESULT_ID INTEGER, " +
					"ANNOTATION_PARAM_ID INTEGER, " +
					"ANNOTATION_ID INTEGER, " +
					"ANNOTATION_PARAM_TYPE_ID INTEGER, " +
					"VALUE TEXT COLLATE latin1_general_cs, " +
					"PRIMARY KEY (ANALYSIS_RESULT_ID, ANNOTATION_PARAM_ID) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void initAnnotationParamType() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.ANNOTATION_PARAM_TYPE + 
					"(ANALYSIS_RESULT_ID INTEGER, " +
					"ANNOTATION_PARAM_TYPE_ID INTEGER, " +
					"ANNOTATION_TYPE_ID INTEGER, " +
					"NAME VARCHAR(128) COLLATE utf8_bin, " +
					"TYPE TEXT COLLATE latin1_general_cs," +
					"PRIMARY KEY (ANALYSIS_RESULT_ID, ANNOTATION_PARAM_TYPE_ID)," +
					"CONSTRAINT uc_type UNIQUE(ANALYSIS_RESULT_ID, ANNOTATION_TYPE_ID, NAME) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}
	
	@Override
	public void initGroup() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.ENTITY_GROUP + 
					"(ANALYSIS_RESULT_ID INTEGER, " +
					"GROUP_ID INTEGER, " +
					"PARENT_GROUP_ID INTEGER, " +
					"NAME TEXT COLLATE latin1_general_cs, " +
					"TARGET_ENTITY TEXT COLLATE latin1_general_cs, " +
					"GROUPING_OPERATOR TEXT COLLATE latin1_general_cs, " +
					"ORDERED BOOLEAN, " +
					"SEQUENCE_NUMBER INTEGER, " +
					"PRIMARY KEY (ANALYSIS_RESULT_ID, GROUP_ID) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	// Default is OK
	// public void initGroupMapping() throws SoCTraceException { }

	@Override
	public void initSearch() throws SoCTraceException {
		try {
			tableStatement.execute(
					SQLConstants.CREATE_TABLE_IF_NOT_EXISTS + 
					FramesocTable.SEARCH + 
					"(ANALYSIS_RESULT_ID INTEGER, " +
					"SEARCH_COMMAND TEXT COLLATE latin1_general_cs, " +
					"TARGET_ENTITY TEXT COLLATE latin1_general_cs, " +
					"PRIMARY KEY (ANALYSIS_RESULT_ID) )");
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public String getTableInfoQuery(FramesocTable framesocTable) {
		return "DESC " + framesocTable.name();
	}

	// Default is OK
	// public void initSearchMapping() throws SoCTraceException { }

	// Default is OK
	// public void initProcessedTrace() throws SoCTraceException { }
	
}
