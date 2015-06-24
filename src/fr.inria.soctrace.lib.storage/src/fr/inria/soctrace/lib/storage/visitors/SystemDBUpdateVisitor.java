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
import fr.inria.soctrace.lib.storage.utils.SQLConstants;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.ToolTableModel;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TraceParamTableModel;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TraceParamTypeTableModel;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TraceTableModel;
import fr.inria.soctrace.lib.storage.utils.DBModelConstants.TraceTypeTableModel;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.SoctraceUtils;

/**
 * Visitor able to update the entities of the system DB.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * 
 */
public class SystemDBUpdateVisitor extends ModelVisitor {

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
	public SystemDBUpdateVisitor(SystemDBObject sysDB) throws SoCTraceException {
		super();
		this.sysDB = sysDB;
		try {
			Connection conn = sysDB.getConnection();
			PreparedStatementDescriptor psd = null;
			// Trace
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_TRACE_UPDATE),
					FramesocTable.TRACE);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_TRACE_TYPE_UPDATE),
					FramesocTable.TRACE_TYPE);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_TRACE_PARAM_UPDATE),
					FramesocTable.TRACE_PARAM);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_TRACE_PARAM_TYPE_UPDATE),
					FramesocTable.TRACE_PARAM_TYPE);
			addDescriptor(psd);
			// Tool
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_TOOL_UPDATE),
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
			psd.statement.setInt(TraceTableModel.TRACE_TYPE_ID.getPosition() - 1, trace.getType().getId());
			// XXX see note at the bottom of ModelVisitor.java
			psd.statement.setString(TraceTableModel.TRACING_DATE.getPosition() - 1, SoctraceUtils.timestampToString(trace.getTracingDate()));
			psd.statement.setString(
					TraceTableModel.TRACED_APPLICATION.getPosition() - 1, trace.getTracedApplication());
			psd.statement.setString(TraceTableModel.BOARD.getPosition() - 1, trace.getBoard());
			psd.statement.setString(
					TraceTableModel.OPERATING_SYSTEM.getPosition() - 1, trace.getOperatingSystem());
			psd.statement.setInt(TraceTableModel.NUMBER_OF_CPUS.getPosition() - 1, trace.getNumberOfCpus());
			psd.statement.setInt(TraceTableModel.NUMBER_OF_EVENTS.getPosition() - 1, trace.getNumberOfEvents());
			psd.statement.setString(TraceTableModel.OUTPUT_DEVICE.getPosition() - 1, trace.getOutputDevice());
			psd.statement.setString(TraceTableModel.DESCRIPTION.getPosition() - 1, trace.getDescription());
			psd.statement.setBoolean(TraceTableModel.PROCESSED.getPosition() - 1, trace.isProcessed());
			psd.statement.setString(TraceTableModel.TRACE_DB_NAME.getPosition() - 1, trace.getDbName());
			psd.statement.setString(TraceTableModel.ALIAS.getPosition() - 1, trace.getAlias());
			psd.statement.setLong(TraceTableModel.MIN_TIMESTAMP.getPosition() - 1, trace.getMinTimestamp());
			psd.statement.setLong(TraceTableModel.MAX_TIMESTAMP.getPosition() - 1, trace.getMaxTimestamp());
			psd.statement.setInt(TraceTableModel.TIMEUNIT.getPosition() - 1, trace.getTimeUnit());
			psd.statement.setInt(TraceTableModel.NUMBER_OF_PRODUCERS.getPosition() - 1, trace.getNumberOfProducers());
			psd.statement.setInt(TraceTableModel.numberOfColumns(), trace.getId());
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
			psd.statement.setString(TraceTypeTableModel.NAME.getPosition() - 1, traceType.getName());
			psd.statement.setInt(TraceTypeTableModel.numberOfColumns(), traceType.getId());
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
			psd.statement.setInt(TraceParamTableModel.TRACE_ID.getPosition() - 1, traceParam.getTrace().getId());
			psd.statement.setInt(TraceParamTableModel.TRACE_PARAM_TYPE_ID.getPosition() - 1, traceParam.getTraceParamType().getId());
			psd.statement.setString(TraceParamTableModel.VALUE.getPosition() - 1, traceParam.getValue());
			psd.statement.setInt(TraceParamTableModel.numberOfColumns(), traceParam.getId());
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
			psd.statement.setInt(TraceParamTypeTableModel.TRACE_TYPE_ID.getPosition() - 1, traceParamType.getTraceType().getId());
			psd.statement.setString(TraceParamTypeTableModel.NAME.getPosition() - 1, traceParamType.getName());
			psd.statement.setString(TraceParamTypeTableModel.TYPE.getPosition() - 1, traceParamType.getType());
			psd.statement.setInt(TraceParamTypeTableModel.numberOfColumns(), traceParamType.getId());
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
			psd.statement.setString(ToolTableModel.NAME.getPosition() - 1, tool.getName());
			psd.statement.setString(ToolTableModel.TYPE.getPosition() - 1, tool.getType());
			psd.statement.setString(ToolTableModel.COMMAND.getPosition() - 1, tool.getCommand());
			psd.statement.setBoolean(ToolTableModel.IS_PLUGIN.getPosition() - 1, tool.isPlugin());
			psd.statement.setString(ToolTableModel.DOC.getPosition() - 1, tool.getDoc());
			psd.statement.setString(ToolTableModel.EXTENSION_ID.getPosition() - 1, tool.getExtensionId());
			psd.statement.setInt(ToolTableModel.numberOfColumns(), tool.getId());
			psd.statement.addBatch();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

}
