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
			psd.statement.setInt(1, trace.getType().getId());
			// XXX see note at the bottom of ModelVisitor.java
			psd.statement.setString(2, SoctraceUtils.timestampToString(trace.getTracingDate()));
			psd.statement.setString(3, trace.getTracedApplication());
			psd.statement.setString(4, trace.getBoard());
			psd.statement.setString(5, trace.getOperatingSystem());
			psd.statement.setInt(6, trace.getNumberOfCpus());
			psd.statement.setInt(7, trace.getNumberOfEvents());
			psd.statement.setString(8, trace.getOutputDevice());
			psd.statement.setString(9, trace.getDescription());
			psd.statement.setBoolean(10, trace.isProcessed());
			psd.statement.setString(11, trace.getDbName());
			psd.statement.setString(12, trace.getAlias());
			psd.statement.setLong(13, trace.getMinTimestamp());
			psd.statement.setLong(14, trace.getMaxTimestamp());
			psd.statement.setInt(15, trace.getTimeUnit());
			psd.statement.setInt(16, trace.getId());
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
			psd.statement.setString(1, traceType.getName());
			psd.statement.setInt(2, traceType.getId());
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
			psd.statement.setInt(1, traceParam.getTrace().getId());
			psd.statement.setInt(2, traceParam.getTraceParamType().getId());
			psd.statement.setString(3, traceParam.getValue());
			psd.statement.setInt(4, traceParam.getId());
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
			psd.statement.setInt(1, traceParamType.getTraceType().getId());
			psd.statement.setString(2, traceParamType.getName());
			psd.statement.setString(3, traceParamType.getType());
			psd.statement.setInt(4, traceParamType.getId());
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
			psd.statement.setString(1, tool.getName());
			psd.statement.setString(2, tool.getType());
			psd.statement.setString(3, tool.getCommand());
			psd.statement.setBoolean(4, tool.isPlugin());
			psd.statement.setString(5, tool.getDoc());
			psd.statement.setString(6, tool.getExtensionId());
			psd.statement.setInt(7, tool.getId());
			psd.statement.addBatch();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

}
