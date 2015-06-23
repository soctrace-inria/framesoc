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
			psd.statement.setInt(TraceTableModel.ID.getPos(), trace.getId());
			psd.statement.setInt(TraceTableModel.TRACE_TYPE_ID.getPos(), trace.getType().getId());
			// XXX see note at the bottom of ModelVisitor.java
			psd.statement.setString(TraceTableModel.TRACING_DATE.getPos(), SoctraceUtils.timestampToString(trace.getTracingDate()));
			psd.statement.setString(TraceTableModel.TRACED_APPLICATION.getPos(), trace.getTracedApplication());
			psd.statement.setString(TraceTableModel.BOARD.getPos(), trace.getBoard());
			psd.statement.setString(TraceTableModel.OPERATING_SYSTEM.getPos(), trace.getOperatingSystem());
			psd.statement.setInt(TraceTableModel.NUMBER_OF_CPUS.getPos(), trace.getNumberOfCpus());
			psd.statement.setInt(TraceTableModel.NUMBER_OF_EVENTS.getPos(), trace.getNumberOfEvents());
			psd.statement.setString(TraceTableModel.OUTPUT_DEVICE.getPos(), trace.getOutputDevice());
			psd.statement.setString(TraceTableModel.DESCRIPTION.getPos(), trace.getDescription());
			psd.statement.setBoolean(TraceTableModel.PROCESSED.getPos(), trace.isProcessed());
			psd.statement.setString(TraceTableModel.TRACE_DB_NAME.getPos(), trace.getDbName());
			psd.statement.setString(TraceTableModel.ALIAS.getPos(), trace.getAlias());
			psd.statement.setLong(TraceTableModel.MIN_TIMESTAMP.getPos(), trace.getMinTimestamp());
			psd.statement.setLong(TraceTableModel.MAX_TIMESTAMP.getPos(), trace.getMaxTimestamp());
			psd.statement.setInt(TraceTableModel.TIMEUNIT.getPos(), trace.getTimeUnit());
			psd.statement.setInt(TraceTableModel.NUMBER_OF_PRODUCERS.getPos(), trace.getNumberOfProducers());
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
			psd.statement.setInt(TraceTypeTableModel.ID.getPos(), traceType.getId());
			psd.statement.setString(TraceTypeTableModel.NAME.getPos(), traceType.getName());
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
			psd.statement.setInt(TraceParamTableModel.ID.getPos(), traceParam.getId());
			psd.statement.setInt(TraceParamTableModel.TRACE_ID.getPos(), traceParam.getTrace().getId());
			psd.statement.setInt(TraceParamTableModel.TRACE_PARAM_TYPE_ID.getPos(), traceParam.getTraceParamType().getId());
			psd.statement.setString(TraceParamTableModel.VALUE.getPos(), traceParam.getValue());
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
			psd.statement.setInt(TraceParamTypeTableModel.ID.getPos(), traceParamType.getId());
			psd.statement.setInt(TraceParamTypeTableModel.TRACE_TYPE_ID.getPos(), traceParamType.getTraceType().getId());
			psd.statement.setString(TraceParamTypeTableModel.NAME.getPos(), traceParamType.getName());
			psd.statement.setString(TraceParamTypeTableModel.TYPE.getPos(), traceParamType.getType());
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
			psd.statement.setInt(ToolTableModel.ID.getPos(), tool.getId());
			psd.statement.setString(ToolTableModel.NAME.getPos(), tool.getName());
			psd.statement.setString(ToolTableModel.TYPE.getPos(), tool.getType());
			psd.statement.setString(ToolTableModel.COMMAND.getPos(), tool.getCommand());
			psd.statement.setBoolean(ToolTableModel.IS_PLUGIN.getPos(), tool.isPlugin());
			psd.statement.setString(ToolTableModel.DOC.getPos(), tool.getDoc());
			psd.statement.setString(ToolTableModel.EXTENSION_ID.getPos(), tool.getExtensionId());
			psd.statement.addBatch();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

}
