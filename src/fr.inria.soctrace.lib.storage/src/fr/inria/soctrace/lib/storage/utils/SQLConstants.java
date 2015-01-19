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
package fr.inria.soctrace.lib.storage.utils;

/**
 * SQL constants related to SoC-Trace databases.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class SQLConstants {

	/**
	 * SoC-Trace Infrastructure (STI) tables 
	 */
	public static enum FramesocTable {		

		// System DB
		TRACE("TRACE"),
		TRACE_TYPE("TRACE_TYPE"),
		TRACE_PARAM("TRACE_PARAM"),
		TRACE_PARAM_TYPE("TRACE_PARAM_TYPE"),
		TOOL("TOOL"),
		
		// Trace DB
		EVENT("EVENT"),
		EVENT_TYPE("EVENT_TYPE"),
		EVENT_PARAM("EVENT_PARAM"),
		EVENT_PARAM_TYPE("EVENT_PARAM_TYPE"),
		EVENT_PRODUCER("EVENT_PRODUCER"),
		FILE("FILE"),
		ANALYSIS_RESULT("ANALYSIS_RESULT"),
		ANNOTATION("ANNOTATION"),
		ANNOTATION_TYPE("ANNOTATION_TYPE"),
		ANNOTATION_PARAM("ANNOTATION_PARAM"),
		ANNOTATION_PARAM_TYPE("ANNOTATION_PARAM_TYPE"),
		/* 'ENTITY_' prefix used only because 'GROUP' is not a legal name for tables */
		ENTITY_GROUP("ENTITY_GROUP"), 
		GROUP_MAPPING("GROUP_MAPPING"),
		SEARCH("SEARCH"),
		SEARCH_MAPPING("SEARCH_MAPPING"),
		PROCESSED_TRACE("PROCESSED_TRACE");
		
		private String name;
		
		private FramesocTable(String name){
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	/*
	 * C R E A T E   T A B L E   S T A T E M E N T S 
	 */
	
	public final static String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";
	
	/*
	 * P R E P A R E D   S T A T E M E N T S   I N S E R T 
	 */
	
	public final static String PREPARED_STATEMENT_TRACE_INSERT = "INSERT INTO " + FramesocTable.TRACE + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	public final static String PREPARED_STATEMENT_TRACE_TYPE_INSERT = "INSERT INTO " + FramesocTable.TRACE_TYPE + " VALUES (?, ?);";
	public final static String PREPARED_STATEMENT_TRACE_PARAM_TYPE_INSERT = "INSERT INTO " +FramesocTable.TRACE_PARAM_TYPE + " VALUES (?, ?, ?, ?);";
	public final static String PREPARED_STATEMENT_TRACE_PARAM_INSERT = 	"INSERT INTO " + FramesocTable.TRACE_PARAM + " VALUES (?, ?, ?, ?);";
	
	public final static String PREPARED_STATEMENT_TOOL_INSERT = "INSERT INTO " + FramesocTable.TOOL + " VALUES (?, ?, ?, ?, ?, ?, ?);";
		
	public final static String PREPARED_STATEMENT_EVENT_INSERT = "INSERT INTO " + FramesocTable.EVENT + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
	public final static String PREPARED_STATEMENT_EVENT_TYPE_INSERT = "INSERT INTO " + FramesocTable.EVENT_TYPE + " VALUES (?, ?, ?);";
	public final static String PREPARED_STATEMENT_EVENT_PARAM_TYPE_INSERT = "INSERT INTO " +FramesocTable.EVENT_PARAM_TYPE + " VALUES (?, ?, ?, ?);";
	public final static String PREPARED_STATEMENT_EVENT_PARAM_INSERT = 	"INSERT INTO " + FramesocTable.EVENT_PARAM + " VALUES (?, ?, ?, ?);";
	
	public final static String PREPARED_STATEMENT_EVENT_PRODUCER_INSERT = "INSERT INTO " + FramesocTable.EVENT_PRODUCER + " VALUES (?, ?, ?, ?, ?);";
	
	public final static String PREPARED_STATEMENT_FILE_INSERT = "INSERT INTO " + FramesocTable.FILE + " VALUES (?, ?, ?);";

	public final static String PREPARED_STATEMENT_ANALYSIS_RESULT_INSERT = "INSERT INTO " + FramesocTable.ANALYSIS_RESULT + " VALUES (?, ?, ?, ?, ?);";
	
	public final static String PREPARED_STATEMENT_ANNOTATION_INSERT = "INSERT INTO " + FramesocTable.ANNOTATION + " VALUES (?, ?, ?, ?);";
	public final static String PREPARED_STATEMENT_ANNOTATION_TYPE_INSERT = "INSERT INTO " + FramesocTable.ANNOTATION_TYPE + " VALUES (?, ?, ?);";
	public final static String PREPARED_STATEMENT_ANNOTATION_PARAM_TYPE_INSERT = "INSERT INTO " +FramesocTable.ANNOTATION_PARAM_TYPE + " VALUES (?, ?, ?, ?, ?);";
	public final static String PREPARED_STATEMENT_ANNOTATION_PARAM_INSERT = "INSERT INTO " + FramesocTable.ANNOTATION_PARAM + " VALUES (?, ?, ?, ?, ?);";
	
	public final static String PREPARED_STATEMENT_GROUP_INSERT = "INSERT INTO " + FramesocTable.ENTITY_GROUP + " VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
	public final static String PREPARED_STATEMENT_GROUP_MAPPING_INSERT = "INSERT INTO " + FramesocTable.GROUP_MAPPING + " VALUES (?, ?, ?, ?, ?);";
	
	public final static String PREPARED_STATEMENT_SEARCH_INSERT = "INSERT INTO " + FramesocTable.SEARCH + " VALUES (?, ?, ?);";
	public final static String PREPARED_STATEMENT_SEARCH_MAPPING_INSERT = "INSERT INTO " + FramesocTable.SEARCH_MAPPING + " VALUES (?, ?);";
	
	public final static String PREPARED_STATEMENT_PROCESSED_TRACE_INSERT = "INSERT INTO " + FramesocTable.PROCESSED_TRACE + " VALUES (?, ?);";
	
	/*
	 * P R E P A R E D   S T A T E M E N T S   U P D A T E 
	 */
	
	public final static String PREPARED_STATEMENT_TRACE_UPDATE = "UPDATE " + FramesocTable.TRACE + " SET TRACE_TYPE_ID=?, TRACING_DATE=?, TRACED_APPLICATION=?, BOARD=?, OPERATING_SYSTEM=?, NUMBER_OF_CPUS=?, NUMBER_OF_EVENTS=?, OUTPUT_DEVICE=?, DESCRIPTION=?, PROCESSED=?, TRACE_DB_NAME=?, ALIAS=?, MIN_TIMESTAMP=?, MAX_TIMESTAMP=?, TIMEUNIT=? WHERE ID=?;";
	public final static String PREPARED_STATEMENT_TRACE_TYPE_UPDATE = "UPDATE " + FramesocTable.TRACE_TYPE + " SET NAME=? WHERE ID=?;";
	public final static String PREPARED_STATEMENT_TRACE_PARAM_TYPE_UPDATE = "UPDATE " + FramesocTable.TRACE_PARAM_TYPE + " SET TRACE_TYPE_ID=?, NAME=?, TYPE=? WHERE ID=?;";
	public final static String PREPARED_STATEMENT_TRACE_PARAM_UPDATE = 	"UPDATE " + FramesocTable.TRACE_PARAM + " SET TRACE_ID=?, TRACE_PARAM_TYPE_ID=?, VALUE=? WHERE ID=?;";
	
	public final static String PREPARED_STATEMENT_TOOL_UPDATE = "UPDATE " + FramesocTable.TOOL + " SET NAME=?, TYPE=?, COMMAND=?, IS_PLUGIN=?, DOC=?, EXTENSION_ID=? WHERE ID=?;";

	public final static String PREPARED_STATEMENT_EVENT_UPDATE = "UPDATE " + FramesocTable.EVENT + " SET EVENT_TYPE_ID=?, EVENT_PRODUCER_ID=?, TIMESTAMP=?, CPU=?, PAGE=?, CATEGORY=?, LPAR=?, DPAR=? WHERE ID=?;";
	public final static String PREPARED_STATEMENT_EVENT_TYPE_UPDATE = "UPDATE " + FramesocTable.EVENT_TYPE + " SET CATEGORY=?, NAME=? WHERE ID=?;";
	public final static String PREPARED_STATEMENT_EVENT_PARAM_TYPE_UPDATE = "UPDATE " + FramesocTable.EVENT_PARAM_TYPE + " SET EVENT_TYPE_ID=?, NAME=?, TYPE=? WHERE ID=?;";
	public final static String PREPARED_STATEMENT_EVENT_PARAM_UPDATE = "UPDATE " + FramesocTable.EVENT_PARAM + " SET EVENT_ID=?, EVENT_PARAM_TYPE_ID=?, VALUE=? WHERE ID=?;";
	public final static String PREPARED_STATEMENT_EVENT_PRODUCER_UPDATE = "UPDATE " + FramesocTable.EVENT_PRODUCER + " SET TYPE=?, LOCAL_ID=?, NAME=?, PARENT_ID=? WHERE ID=?;";
	
	public final static String PREPARED_STATEMENT_FILE_UPDATE = "UPDATE " + FramesocTable.FILE + " SET PATH=?, DESCRIPTION=? WHERE ID=?;";

	public final static String PREPARED_STATEMENT_ANALYSIS_RESULT_UPDATE = "UPDATE " + FramesocTable.ANALYSIS_RESULT + " SET TOOL_ID=?, DATE=?, DESCRIPTION=? WHERE ID=?;";
	
	public final static String PREPARED_STATEMENT_ANNOTATION_UPDATE = "UPDATE " + FramesocTable.ANNOTATION + " SET ANNOTATION_TYPE_ID=?, NAME=? WHERE ANALYSIS_RESULT_ID=? AND ANNOTATION_ID=?;";
	public final static String PREPARED_STATEMENT_ANNOTATION_TYPE_UPDATE = "UPDATE " + FramesocTable.ANNOTATION_TYPE + " SET NAME=? WHERE ANALYSIS_RESULT_ID=? AND ANNOTATION_TYPE_ID=?;";
	public final static String PREPARED_STATEMENT_ANNOTATION_PARAM_TYPE_UPDATE = "UPDATE " + FramesocTable.ANNOTATION_PARAM_TYPE + " SET ANNOTATION_TYPE_ID=?, NAME=?, TYPE=? WHERE ANALYSIS_RESULT_ID=? AND ANNOTATION_PARAM_TYPE_ID=?;";
	public final static String PREPARED_STATEMENT_ANNOTATION_PARAM_UPDATE = "UPDATE " + FramesocTable.ANNOTATION_PARAM + " SET ANNOTATION_ID=?, ANNOTATION_PARAM_TYPE_ID=?, VALUE=? WHERE ANALYSIS_RESULT_ID=? AND ANNOTATION_PARAM_ID=?;";
		
	public final static String PREPARED_STATEMENT_GROUP_UPDATE = "UPDATE " + FramesocTable.ENTITY_GROUP + " SET PARENT_GROUP_ID=?, NAME=?, TARGET_ENTITY=?, GROUPING_OPERATOR=?, ORDERED=?, SEQUENCE_NUMBER=? WHERE ANALYSIS_RESULT_ID=? AND GROUP_ID=?;";
	public final static String PREPARED_STATEMENT_GROUP_MAPPING_UPDATE = "UPDATE " + FramesocTable.GROUP_MAPPING + " SET SEQUENCE_NUMBER=? WHERE ANALYSIS_RESULT_ID=? AND MAPPING_ID=?;";
	
	public final static String PREPARED_STATEMENT_SEARCH_UPDATE = "UPDATE " + FramesocTable.SEARCH + " SET SEARCH_COMMAND=? WHERE ANALYSIS_RESULT_ID=?;";
	
	// nothing to update here: all key schemas
	//public final static String PREPARED_STATEMENT_SEARCH_MAPPING_UPDATE
	//public final static String PREPARED_STATEMENT_PROCESSED_TRACE_UPDATE


	/*
	 * P R E P A R E D   S T A T E M E N T S   D E L E T E  
	 */
	
	public final static String PREPARED_STATEMENT_TRACE_DELETE = "DELETE FROM " + FramesocTable.TRACE + " WHERE ID=?;";
	public final static String PREPARED_STATEMENT_TRACE_TYPE_DELETE = "DELETE FROM " + FramesocTable.TRACE_TYPE + " WHERE ID=?;";
	public final static String PREPARED_STATEMENT_TRACE_PARAM_TYPE_DELETE = "DELETE FROM " + FramesocTable.TRACE_PARAM_TYPE + " WHERE ID=?;";
	public final static String PREPARED_STATEMENT_TRACE_PARAM_DELETE = 	"DELETE FROM " + FramesocTable.TRACE_PARAM + " WHERE ID=?;";
	
	public final static String PREPARED_STATEMENT_TOOL_DELETE = "DELETE FROM " + FramesocTable.TOOL + " WHERE ID=?;";

	public final static String PREPARED_STATEMENT_EVENT_DELETE = "DELETE FROM " + FramesocTable.EVENT + " WHERE ID=?;";
	public final static String PREPARED_STATEMENT_EVENT_TYPE_DELETE = "DELETE FROM " + FramesocTable.EVENT_TYPE + " WHERE ID=?;";
	public final static String PREPARED_STATEMENT_EVENT_PARAM_DELETE = "DELETE FROM " + FramesocTable.EVENT_PARAM + " WHERE ID=?;";
	public final static String PREPARED_STATEMENT_EVENT_PARAM_TYPE_DELETE = "DELETE FROM " + FramesocTable.EVENT_PARAM_TYPE + " WHERE ID=?;";
	public final static String PREPARED_STATEMENT_EVENT_PRODUCER_DELETE = "DELETE FROM " + FramesocTable.EVENT_PRODUCER + " WHERE ID=?;";
	
	public final static String PREPARED_STATEMENT_FILE_DELETE = "DELETE FROM " + FramesocTable.FILE + " WHERE ID=?;";

	public final static String PREPARED_STATEMENT_ANALYSIS_RESULT_DELETE = "DELETE FROM " + FramesocTable.ANALYSIS_RESULT + " WHERE ID=?;";
	
	public final static String PREPARED_STATEMENT_ANNOTATION_DELETE = "DELETE FROM " + FramesocTable.ANNOTATION + " WHERE ANALYSIS_RESULT_ID=?;";
	public final static String PREPARED_STATEMENT_ANNOTATION_TYPE_DELETE = "DELETE FROM " + FramesocTable.ANNOTATION_TYPE + " WHERE ANALYSIS_RESULT_ID=?;";
	public final static String PREPARED_STATEMENT_ANNOTATION_PARAM_TYPE_DELETE = "DELETE FROM " + FramesocTable.ANNOTATION_PARAM_TYPE + " WHERE ANALYSIS_RESULT_ID=?;";
	public final static String PREPARED_STATEMENT_ANNOTATION_PARAM_DELETE = "DELETE FROM " + FramesocTable.ANNOTATION_PARAM + " WHERE ANALYSIS_RESULT_ID=?;";
	
	public final static String PREPARED_STATEMENT_GROUP_DELETE = "DELETE FROM " + FramesocTable.ENTITY_GROUP + " WHERE ANALYSIS_RESULT_ID=?;";
	public final static String PREPARED_STATEMENT_GROUP_MAPPING_DELETE = "DELETE FROM " + FramesocTable.GROUP_MAPPING + " WHERE ANALYSIS_RESULT_ID=?;";
	
	public final static String PREPARED_STATEMENT_SEARCH_DELETE = "DELETE FROM " + FramesocTable.SEARCH + " WHERE ANALYSIS_RESULT_ID=?;";
	public final static String PREPARED_STATEMENT_SEARCH_MAPPING_DELETE = "DELETE FROM " + FramesocTable.SEARCH_MAPPING + " WHERE ANALYSIS_RESULT_ID=?;";
	
	public final static String PREPARED_STATEMENT_PROCESSED_TRACE_DELETE = "DELETE FROM " + FramesocTable.PROCESSED_TRACE + " WHERE ANALYSIS_RESULT_ID=?;";

}
