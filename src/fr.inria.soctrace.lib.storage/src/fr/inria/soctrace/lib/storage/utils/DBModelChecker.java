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
package fr.inria.soctrace.lib.storage.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.dbmanager.DBManager;
import fr.inria.soctrace.lib.storage.updater.DBModelRebuilder;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;

/**
 * Class checking that an existing database system model is similar to the
 * current one. If automatically try to update and import the from the old data
 * model into the new one.
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class DBModelChecker {

	private final static FramesocTable[] checkedTableModel = {
			FramesocTable.TRACE, FramesocTable.TRACE_TYPE,
			FramesocTable.TRACE_PARAM_TYPE, FramesocTable.TRACE_PARAM,
			FramesocTable.TOOL };
	
	public static final String NEW_SYSTEM_DB_SUFFIX = ".tmp";
	public static final int NAME_COLUMN_INDEX = 2;

	private List<DBModelRebuilder> dbModelRebuilders = new ArrayList<DBModelRebuilder>();
	
	/**
	 * Find and store all the differences between the systemdbObject and the
	 * current DB model in Framesoc
	 * 
	 * @param systemDB
	 *            the tested systemDb object
	 * @param modelBuilder
	 *            a model builder for a given table of the systemDB
	 * @return true if they are similar, false otherwise
	 * @throws SoCTraceException
	 */
	public void getDatabaseModelDifferences(SystemDBObject systemDB,
			DBModelRebuilder modelBuilder) throws SoCTraceException {
		boolean tableHasDifference = false;
		int cpt = 1;
		FramesocTable framesocTable = modelBuilder.getTable();
		dbModelRebuilders.add(modelBuilder);
		
		try {
			Statement stm = systemDB.getConnection().createStatement();
			String query = systemDB.getTraceInfoQuery(framesocTable);
			Map<String, Integer> oldModel = new HashMap<String, Integer>();
			tableHasDifference = false;
			cpt = 1;

			// Get info
			ResultSet rs = stm.executeQuery(query);
			while (rs.next()) {
				String columnName = rs.getString(NAME_COLUMN_INDEX);
				oldModel.put(columnName, cpt);
				// Check that column names are similar
				if (!columnName.equals(modelBuilder.getValueAt(cpt))) {
					// Save the diff column name and its position in the old
					// model
					modelBuilder.getOldModelMapDiff().put(columnName, cpt);
					tableHasDifference = true;
				}

				cpt++;
			}

			int currentModelLength = modelBuilder.getColumnNumber();

			// If size is different return false (missing field)
			if (cpt - 1 != currentModelLength)
				tableHasDifference = true;

			// If a diff was found
			if (tableHasDifference) {
				// Also check in reverse for potential missing parameter
				for (int i = 1; i <= currentModelLength; i++) {
					String currentModelColName = modelBuilder.getValueAt(cpt);
					if (!oldModel.containsKey(currentModelColName))
						modelBuilder.getOldModelMapDiff().put(
								currentModelColName,
								DBModelRebuilder.MISSING_PARAMETER_VALUE);
				}
			}

			stm.close();
		} catch (SQLException | SoCTraceException | SecurityException
				| IllegalArgumentException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Try to update the database from one model to the current one
	 */
	public void updateDB() throws SoCTraceException {
		String tmpDbName = Configuration.getInstance().get(
				SoCTraceProperty.soctrace_db_name)
				+ NEW_SYSTEM_DB_SUFFIX;

		// Create a new DB with the current model
		try {
			SystemDBObject newSysDB = new SystemDBObject(tmpDbName,
					DBMode.DB_CREATE);

			SystemDBObject oldsysDB = new SystemDBObject(Configuration
					.getInstance().get(SoCTraceProperty.soctrace_db_name),
					DBMode.DB_OPEN);

			for (FramesocTable framesocTable : checkedTableModel) {
				getDatabaseModelDifferences(oldsysDB,
						DBModelRebuilder.DBModelRebuilderFactory(framesocTable));
			}

			// Copy similar things and import the rest
			for (DBModelRebuilder dbModelrebuider : dbModelRebuilders) {
				dbModelrebuider.setDBs(oldsysDB, newSysDB);
				dbModelrebuider.copyTable();
			}

			oldsysDB.close();
			newSysDB.close();

			// Erase the old one and replace with the new one
			DBManager.getDBManager(
					Configuration.getInstance().get(
							SoCTraceProperty.soctrace_db_name)).replaceDB();
		} catch (SoCTraceException e) {
			// TODO Clean up potential mess
			throw new SoCTraceException(e);
		}
	}

}
