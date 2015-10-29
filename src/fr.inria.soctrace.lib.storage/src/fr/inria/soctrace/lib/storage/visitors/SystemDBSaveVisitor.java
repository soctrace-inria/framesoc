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
package fr.inria.soctrace.lib.storage.visitors;

import java.sql.Connection;
import java.sql.SQLException;

import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceParam;
import fr.inria.soctrace.lib.model.TraceParamType;
import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.ToolTableModel;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TraceParamTableModel;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TraceParamTypeTableModel;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TraceTypeTableModel;
import fr.inria.soctrace.lib.storage.utils.SQLConstants;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TraceTableModel;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.SoctraceUtils;

/**
 * Visitor able to save the entities of the system DB.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * 
 */
public class SystemDBSaveVisitor extends ModelVisitor {

	/**
	 * DB object related to this visitor
	 */
	private SystemDBObject sysDB;

	/**
	 * The Constructor
	 * 
	 * @param sysDB
	 *            system DB object
	 * @throws SoCTraceException
	 */
	public SystemDBSaveVisitor(SystemDBObject sysDB) throws SoCTraceException {
		super();
		this.sysDB = sysDB;

		try {
			Connection conn = sysDB.getConnection();
			PreparedStatementDescriptor psd = null;
			// Trace
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_TRACE_INSERT),
					FramesocTable.TRACE);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_TRACE_TYPE_INSERT),
					FramesocTable.TRACE_TYPE);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_TRACE_PARAM_INSERT),
					FramesocTable.TRACE_PARAM);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_TRACE_PARAM_TYPE_INSERT),
					FramesocTable.TRACE_PARAM_TYPE);
			addDescriptor(psd);
			// Tool
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_TOOL_INSERT),
					FramesocTable.TOOL);
			addDescriptor(psd);
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void visit(Trace trace) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.TRACE);
			psd.visited = true;
			psd.statement.setLong(TraceTableModel.ID.getPosition(), trace.getId());
			psd.statement.setLong(TraceTableModel.TRACE_TYPE_ID.getPosition(), trace.getType().getId());
			// XXX see note at the bottom of ModelVisitor.java
			psd.statement.setString(TraceTableModel.TRACING_DATE.getPosition(), SoctraceUtils.timestampToString(trace.getTracingDate()));
			psd.statement.setString(TraceTableModel.TRACED_APPLICATION.getPosition(), trace.getTracedApplication());
			psd.statement.setString(TraceTableModel.BOARD.getPosition(), trace.getBoard());
			psd.statement.setString(TraceTableModel.OPERATING_SYSTEM.getPosition(), trace.getOperatingSystem());
			psd.statement.setInt(TraceTableModel.NUMBER_OF_CPUS.getPosition(), trace.getNumberOfCpus());
			psd.statement.setLong(TraceTableModel.NUMBER_OF_EVENTS.getPosition(), trace.getNumberOfEvents());
			psd.statement.setString(TraceTableModel.OUTPUT_DEVICE.getPosition(), trace.getOutputDevice());
			psd.statement.setString(TraceTableModel.DESCRIPTION.getPosition(), trace.getDescription());
			psd.statement.setBoolean(TraceTableModel.PROCESSED.getPosition(), trace.isProcessed());
			psd.statement.setString(TraceTableModel.TRACE_DB_NAME.getPosition(), trace.getDbName());
			psd.statement.setString(TraceTableModel.ALIAS.getPosition(), trace.getAlias());
			psd.statement.setLong(TraceTableModel.MIN_TIMESTAMP.getPosition(), trace.getMinTimestamp());
			psd.statement.setLong(TraceTableModel.MAX_TIMESTAMP.getPosition(), trace.getMaxTimestamp());
			psd.statement.setInt(TraceTableModel.TIMEUNIT.getPosition(), trace.getTimeUnit());
			psd.statement.setInt(TraceTableModel.NUMBER_OF_PRODUCERS.getPosition(), trace.getNumberOfProducers());
			psd.statement.addBatch();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void visit(TraceType traceType) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.TRACE_TYPE);
			psd.visited = true;
			psd.statement.setLong(TraceTypeTableModel.ID.getPosition(), traceType.getId());
			psd.statement.setString(TraceTypeTableModel.NAME.getPosition(), traceType.getName());
			psd.statement.addBatch();
			sysDB.getTraceTypeCache().put(traceType);
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void visit(TraceParam traceParam) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.TRACE_PARAM);
			psd.visited = true;
			psd.statement.setLong(TraceParamTableModel.ID.getPosition(), traceParam.getId());
			psd.statement.setLong(TraceParamTableModel.TRACE_ID.getPosition(), traceParam.getTrace().getId());
			psd.statement.setLong(TraceParamTableModel.TRACE_PARAM_TYPE_ID.getPosition(), traceParam.getTraceParamType().getId());
			psd.statement.setString(TraceParamTableModel.VALUE.getPosition(), traceParam.getValue());
			psd.statement.addBatch();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void visit(TraceParamType traceParamType) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.TRACE_PARAM_TYPE);
			psd.visited = true;
			psd.statement.setLong(TraceParamTypeTableModel.ID.getPosition(), traceParamType.getId());
			psd.statement.setLong(TraceParamTypeTableModel.TRACE_TYPE_ID.getPosition(), traceParamType.getTraceType().getId());
			psd.statement.setString(TraceParamTypeTableModel.NAME.getPosition(), traceParamType.getName());
			psd.statement.setString(TraceParamTypeTableModel.TYPE.getPosition(), traceParamType.getType());
			psd.statement.addBatch();
			sysDB.getTraceTypeCache().put(traceParamType);
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void visit(Tool tool) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.TOOL);
			psd.visited = true;
			psd.statement.setLong(ToolTableModel.ID.getPosition(), tool.getId());
			psd.statement.setString(ToolTableModel.NAME.getPosition(), tool.getName());
			psd.statement.setString(ToolTableModel.TYPE.getPosition(), tool.getType());
			psd.statement.setString(ToolTableModel.COMMAND.getPosition(), tool.getCommand());
			psd.statement.setBoolean(ToolTableModel.IS_PLUGIN.getPosition(), tool.isPlugin());
			psd.statement.setString(ToolTableModel.DOC.getPosition(), tool.getDoc());
			psd.statement.setString(ToolTableModel.EXTENSION_ID.getPosition(), tool.getExtensionId());
			psd.statement.addBatch();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

}
