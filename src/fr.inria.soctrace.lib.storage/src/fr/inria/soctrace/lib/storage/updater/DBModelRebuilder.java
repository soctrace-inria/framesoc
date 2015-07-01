/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Youenn Corre - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.lib.storage.updater;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Abstract class that handles the update from a system DB model to another
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public abstract class DBModelRebuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(DBModelRebuilder.class);
	
	public static final Integer MISSING_PARAMETER_VALUE = -1;
	private static final String UNSUPPORTED_MISSING_VALUE = "Updater encountered a non-updatable missing value";
	
	// Current table being updated
	protected FramesocTable table;
	
	// Query to get the info on the table
	protected String getTableQuery;

	/**
	 * Contains the list of differences between current DB model and older one.
	 * It should contain parameters that are no longer present in the table , or
	 * that are at a different position. Parameters of the current DB model but that are not
	 * in the ancient one, have their position set to MISSING_PARAMETER_VALUE
	 */
	protected Map<String, Integer> oldModelMapDiff;
	
	// The old updated system DB model
	protected SystemDBObject oldSysDB;
	// The newly filled system DB model
	protected SystemDBObject newSysDB;

	public DBModelRebuilder() {
		oldModelMapDiff = new HashMap<String, Integer>();
	}
	
	/**
	 * Set system database objects
	 * 
	 * @param oldDB
	 *            the old system object DB
	 * @param newDB
	 *            the new system object DB
	 */
	public void setDBs(SystemDBObject oldDB, SystemDBObject newDB) {
		oldSysDB = oldDB;
		newSysDB = newDB;
	}
	
	/**
	 * Copy the value of a table
	 * 
	 * @param statement
	 *            the statement in which the new values are stored
	 * @param rs
	 *            the result set of the query of the updated table
	 * @throws SoCTraceException
	 */
	protected abstract void copyValues(PreparedStatement statement, ResultSet rs)
			throws SoCTraceException;
	
	/**
	 * Get the name of the column at the position pos
	 * 
	 * @param pos
	 *            the index of the column we want to get
	 * @return the name of the column
	 */
	public abstract String getValueAt(int pos);
	
	/**
	 * Get the number of columns in a given table
	 * 
	 * @return the number of columns
	 */
	public abstract int getColumnNumber();
	
	/**
	 * Factory to instantiate DBModelRebuiltder for each table
	 * 
	 * @param modelName
	 *            the name of the table
	 * @return an instance of the a Model rebuilder corresponding to the given
	 *         table name
	 */
	public static DBModelRebuilder DBModelRebuilderFactory(FramesocTable framesocTable) {
		switch (framesocTable) {
		case TRACE:
			return new TraceModelRebuilder();
		case TRACE_TYPE:
			return new TraceTypeModelRebuilder();
		case TRACE_PARAM:
			return new TraceParamModelRebuilder();
		case TRACE_PARAM_TYPE:
			return new TraceParamTypeModelRebuilder();
		case TOOL:
			return new ToolModelRebuilder();
		default:
			return null;
		}
	}
	
	/**
	 * Copy at table from the old system db into the new one
	 */
	public void copyTable() {
		try {
			// Get all traces
			Statement stm = oldSysDB.getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SELECT * FROM " + table);

			// For each trace
			while (rs.next()) {
				PreparedStatement statement = newSysDB.getConnection()
						.prepareStatement(getTableQuery);

				// Copy each value
				copyValues(statement, rs);

				// Save into new DB
				statement.addBatch();
				statement.executeBatch();
				statement.close();
			}
			
			stm.close();
			newSysDB.commit();
		} catch (SoCTraceException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Build a statement with from the result of a query from the old DB
	 * 
	 * @param statement
	 *            the built statement
	 * @param rs
	 *            the result set from which we copy the value
	 * @param destPos
	 *            the column index of the destination DB
	 * @param srcPos
	 *            the column index of the source DB
	 * @param type
	 *            the type of the value
	 * @throws SoCTraceException
	 */
	protected void addToStatement(PreparedStatement statement, ResultSet rs,
			int destPos, int srcPos, String type) throws SoCTraceException {
		try {
			switch (type) {
			case "String":
				statement.setString(destPos, rs.getString(srcPos));
				break;
			case "Integer":
				statement.setInt(destPos, rs.getInt(srcPos));
				break;
			case "Long":
				statement.setLong(destPos, rs.getLong(srcPos));
				break;
			case "Boolean":
				statement.setBoolean(destPos, rs.getBoolean(srcPos));
				break;
			case "Float":
				statement.setFloat(destPos, rs.getFloat(srcPos));
				break;
			case "Double":
				statement.setDouble(destPos, rs.getDouble(srcPos));
				break;
			case "Short":
				statement.setShort(destPos, rs.getShort(srcPos));
				break;
			default:
				logger.error("Unsupported type {}", type);
			}
		} catch (SQLException e) {
			throw new SoCTraceException(e.getMessage());
		}
	}
	
	/**
	 * Build the statement with a specific value
	 * 
	 * @param statement
	 *            the built statement
	 * @param obj
	 *            Specific value to be added
	 * @param destPos
	 *            the column index of the destination DB
	 * @param type
	 *            the type of the value
	 * @throws SoCTraceException
	 */
	protected void addToStatement(PreparedStatement statement, Object obj,
			int destPos, String type) throws SoCTraceException {
		
		// Unsupported missing value 
		if(obj == null)
			throw new SoCTraceException(UNSUPPORTED_MISSING_VALUE);
		
		try {
			switch (type) {
			case "String":
				statement.setString(destPos, (String) obj);
				break;
			case "Integer":
				statement.setInt(destPos, (Integer) obj);
				break;
			case "Long":
				statement.setLong(destPos, (Long) obj);
				break;
			case "Boolean":
				statement.setBoolean(destPos, (Boolean) obj);
				break;
			case "Float":
				statement.setFloat(destPos, (Float) obj);
				break;
			case "Double":
				statement.setDouble(destPos, (Double) obj);
				break;
			case "Short":
				statement.setShort(destPos, (Short) obj);
				break;
			default:
				logger.error("Unsupported type {}", type);
			}
		} catch (SQLException e) {
			throw new SoCTraceException(e.getMessage());
		}
	}
	
	public Map<String, Integer> getOldModelMapDiff() {
		return oldModelMapDiff;
	}

	public void setOldModelMapDiff(Map<String, Integer> oldModelMapDiff) {
		this.oldModelMapDiff = oldModelMapDiff;
	}
	
	public FramesocTable getTable() {
		return table;
	}

	public void setTable(FramesocTable table) {
		this.table = table;
	}

	public String getGetTableQuery() {
		return getTableQuery;
	}

	public void setGetTableQuery(String getTableQuery) {
		this.getTableQuery = getTableQuery;
	}


}
