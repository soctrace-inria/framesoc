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
			psd.statement.setLong(1, trace.getId());
			psd.statement.setLong(2, trace.getType().getId());
			// XXX see note at the bottom of ModelVisitor.java
			psd.statement.setString(3, SoctraceUtils.timestampToString(trace.getTracingDate()));
			psd.statement.setString(4, trace.getTracedApplication());
			psd.statement.setString(5, trace.getBoard());
			psd.statement.setString(6, trace.getOperatingSystem());
			psd.statement.setInt(7, trace.getNumberOfCpus());
			psd.statement.setInt(8, trace.getNumberOfEvents());
			psd.statement.setString(9, trace.getOutputDevice());
			psd.statement.setString(10, trace.getDescription());
			psd.statement.setBoolean(11, trace.isProcessed());
			psd.statement.setString(12, trace.getDbName());
			psd.statement.setString(13, trace.getAlias());
			psd.statement.setLong(14, trace.getMinTimestamp());
			psd.statement.setLong(15, trace.getMaxTimestamp());
			psd.statement.setInt(16, trace.getTimeUnit());
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
			psd.statement.setLong(1, traceType.getId());
			psd.statement.setString(2, traceType.getName());
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
			psd.statement.setLong(1, traceParam.getId());
			psd.statement.setLong(2, traceParam.getTrace().getId());
			psd.statement.setLong(3, traceParam.getTraceParamType().getId());
			psd.statement.setString(4, traceParam.getValue());
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
			psd.statement.setLong(1, traceParamType.getId());
			psd.statement.setLong(2, traceParamType.getTraceType().getId());
			psd.statement.setString(3, traceParamType.getName());
			psd.statement.setString(4, traceParamType.getType());
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
			psd.statement.setLong(1, tool.getId());
			psd.statement.setString(2, tool.getName());
			psd.statement.setString(3, tool.getType());
			psd.statement.setString(4, tool.getCommand());
			psd.statement.setBoolean(5, tool.isPlugin());
			psd.statement.setString(6, tool.getDoc());
			psd.statement.setString(7, tool.getExtensionId());
			psd.statement.addBatch();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

}
