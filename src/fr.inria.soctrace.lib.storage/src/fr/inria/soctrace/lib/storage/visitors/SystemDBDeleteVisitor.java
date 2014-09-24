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

/**
 * Visitor able to delete the entities of the system DB.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class SystemDBDeleteVisitor extends ModelVisitor {

	/**
	 * DB object related to this visitor
	 */
	private SystemDBObject sysDB;
	
	/**
	 * The Constructor
	 * @param sysDB system DB object
	 * @throws SoCTraceException 
	 */
	public SystemDBDeleteVisitor(SystemDBObject sysDB) throws SoCTraceException {
		super();
		this.sysDB = sysDB;
		try {
			Connection conn = sysDB.getConnection();
			PreparedStatementDescriptor psd = null;
			// Trace
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_TRACE_DELETE));
			addDescriptor(FramesocTable.TRACE, psd);
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_TRACE_TYPE_DELETE));
			addDescriptor(FramesocTable.TRACE_TYPE, psd);
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_TRACE_PARAM_DELETE));
			addDescriptor(FramesocTable.TRACE_PARAM, psd);
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_TRACE_PARAM_TYPE_DELETE));
			addDescriptor(FramesocTable.TRACE_PARAM_TYPE, psd);
			// Tool
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_TOOL_DELETE));
			addDescriptor(FramesocTable.TOOL, psd);
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void visit(Trace trace) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.TRACE);
			psd.visited = true;
			psd.statement.setInt(1, trace.getId());
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
			psd.statement.setInt(1, traceType.getId());
			psd.statement.addBatch();
			sysDB.getTraceTypeCache().remove(traceType);
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void visit(TraceParam traceParam) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.TRACE_PARAM);
			psd.visited = true;
			psd.statement.setInt(1, traceParam.getId());
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
			psd.statement.setInt(1, traceParamType.getId());
			psd.statement.addBatch();
			sysDB.getTraceTypeCache().remove(traceParamType);
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void visit(Tool tool) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.TOOL);
			psd.visited = true;
			psd.statement.setInt(1, tool.getId());
			psd.statement.addBatch();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}	

}
