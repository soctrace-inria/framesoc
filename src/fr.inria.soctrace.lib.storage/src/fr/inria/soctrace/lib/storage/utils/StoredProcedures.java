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

import java.sql.SQLException;
import java.sql.Statement;


import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;

/**
 * Class containing the SQL commands for some stored procedures 
 * that can be used for manually inspecting the databases content.
 * 
 * Note: SQLite DBMS does not support stored procedures
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
@Deprecated
public class StoredProcedures {

	/*
	 *   C o n s t a n t s
	 */
	
	/**
	 * Shows the parameters values of a given trace.
	 * call TraceParamNameValue(trace-id);
	 */
	public final static String CREATE_STORED_PROCEDURE_TRACE_PARAM_NAME_VALUE = 
			"CREATE PROCEDURE TraceParamNameValue(IN tid INT) " +
			"SELECT TRACE_PARAM_TYPE.NAME as NAME, TRACE_PARAM.VALUE as VALUE " +
			"FROM TRACE_PARAM_TYPE INNER JOIN TRACE_PARAM on TRACE_PARAM_TYPE.ID = TRACE_PARAM.TRACE_PARAM_TYPE_ID " +
			"WHERE TRACE_PARAM.TRACE_ID = tid";

	/**
	 * Shows the parameters values of a given event.
	 * call EventParamNameValue(event-id);
	 */
	public final static String CREATE_STORED_PROCEDURE_EVENT_PARAM_NAME_VALUE = 
				"CREATE PROCEDURE EventParamNameValue(IN eid INT) " +
				"SELECT EVENT_PARAM_TYPE.NAME as NAME, EVENT_PARAM.VALUE as VALUE " +
				"FROM EVENT_PARAM_TYPE INNER JOIN EVENT_PARAM on EVENT_PARAM_TYPE.ID = EVENT_PARAM.EVENT_PARAM_TYPE_ID " +
				"WHERE EVENT_PARAM.EVENT_ID = eid";
	
	/**
	 * Shows the event producer details for an event.
	 * call EventProducer(event-id);
	 */
	public final static String CREATE_STORED_PROCEDURE_EVENT_PRODUCER = 
				"CREATE PROCEDURE EventProducer(IN eid INT) " +
				"SELECT EVENT.ID as EVENT_ID, EVENT.TIMESTAMP as TIMESTAMP, " +
				"EVENT_PRODUCER.TYPE as EVENT_PRODUCER_TYPE, EVENT_PRODUCER.NAME as EVENT_PRODUCER_NAME, EVENT_PRODUCER.LOCAL_ID as EVENT_PRODUCER_LOCAL_ID " +
				"FROM EVENT INNER JOIN EVENT_PRODUCER on EVENT.EVENT_PRODUCER_ID = EVENT_PRODUCER.ID " +
				"WHERE EVENT.ID = eid";
	
	/*
	 *   S t a t i c    m e t h o d s
	 */
	
	public static void createSPTraceParamNameValue(Statement statement) throws SoCTraceException {
		try {
			checkDBMS();
			statement.execute(StoredProcedures.CREATE_STORED_PROCEDURE_TRACE_PARAM_NAME_VALUE);
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	public static void createSPEventParamNameValue(Statement statement) throws SoCTraceException {
		try {
			checkDBMS();
			statement.execute(StoredProcedures.CREATE_STORED_PROCEDURE_EVENT_PARAM_NAME_VALUE);
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}		
	}
	
	public static void createSPEventProducer(Statement statement) throws SoCTraceException {
		try {
			checkDBMS();
			statement.execute(StoredProcedures.CREATE_STORED_PROCEDURE_EVENT_PRODUCER);
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}		
	}
	
	private static void checkDBMS() throws SoCTraceException {
		String dbms = Configuration.getInstance().get(SoCTraceProperty.soctrace_dbms);
		if (dbms.equals("sqlite"))
			throw new SoCTraceException("DBMS '" + dbms + "' does not support Stored Procedures.");
	}
}
