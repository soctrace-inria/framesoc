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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.File;
import fr.inria.soctrace.lib.model.IModelVisitor;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceParam;
import fr.inria.soctrace.lib.model.TraceParamType;
import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Base class for all model visitors.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class ModelVisitor implements IModelVisitor {

	/**
	 * Prepared statement descriptor
	 */
	protected class PreparedStatementDescriptor {
		public FramesocTable table;
		public boolean visited;
		public PreparedStatement statement;

		public PreparedStatementDescriptor(PreparedStatement stm, FramesocTable t) {
			table = t;
			visited = false;
			statement = stm;
		}
	}

	/**
	 * Prepared statement managed by this visitor
	 */
	protected Map<FramesocTable, PreparedStatementDescriptor> descriptors;

	/**
	 * Base constructor.
	 */
	public ModelVisitor() {
		descriptors = new HashMap<FramesocTable, PreparedStatementDescriptor>();
	}

	/**
	 * Add a prepared statement descriptor for the given entity
	 * 
	 * @param e
	 *            model entity
	 * @param psd
	 *            prepared statement descriptor
	 */
	protected void addDescriptor(PreparedStatementDescriptor psd) {
		descriptors.put(psd.table, psd);
	}

	/**
	 * Prepared statement descriptor getter
	 * 
	 * @param e
	 *            model entity the descriptor is related with
	 * @return the corresponding prepared statement descriptor
	 */
	protected PreparedStatementDescriptor getDescriptor(FramesocTable e) {
		return descriptors.get(e);
	}

	@Override
	public void executeBatches() throws SoCTraceException {
		PreparedStatementDescriptor p = null;
		try {
			Collection<PreparedStatementDescriptor> c = descriptors.values();
			for (PreparedStatementDescriptor psd : c) {
				if (psd.visited) {
					p = psd;
					psd.statement.executeBatch();
				}
			}
			postExecuteBatches();
		} catch (SQLException e) {
			if (p != null) {
				System.err.println("Exception while visiting table " + p.table.toString());
			}
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void clearBatches() throws SoCTraceException {
		PreparedStatementDescriptor p = null;
		try {
			Collection<PreparedStatementDescriptor> c = descriptors.values();
			for (PreparedStatementDescriptor psd : c) {
				p = psd;
				psd.visited = false;
				if (psd.statement != null)
					psd.statement.clearBatch();
			}
			postClearBatches();
		} catch (SQLException e) {
			if (p != null) {
				System.err.println("Exception while visiting table " + p.table.toString());
			}
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void close() throws SoCTraceException {
		PreparedStatementDescriptor p = null;
		try {
			Collection<PreparedStatementDescriptor> c = descriptors.values();
			for (PreparedStatementDescriptor psd : c) {
				p = psd;
				if (psd.statement != null)
					psd.statement.close();
				psd.statement = null;
			}
			postClose();
		} catch (SQLException e) {
			if (p != null) {
				System.err.println("Exception while visiting table " + p.table.toString());
			}
			throw new SoCTraceException(e);
		}
	}

	/*
	 * Specific visitor "post" methods. Default implementation does nothing. Redefine if needed.
	 */

	protected void postExecuteBatches() throws SoCTraceException {
	}

	protected void postClearBatches() throws SoCTraceException {
	}

	protected void postClose() throws SoCTraceException {
	}

	/*
	 * Visits
	 */

	@Override
	public void visit(Trace trace) throws SoCTraceException {
		throw new SoCTraceException("Visit not implemented in this visitor.");
	}

	@Override
	public void visit(TraceType traceType) throws SoCTraceException {
		throw new SoCTraceException("Visit not implemented in this visitor.");
	}

	@Override
	public void visit(TraceParam traceParam) throws SoCTraceException {
		throw new SoCTraceException("Visit not implemented in this visitor.");
	}

	@Override
	public void visit(TraceParamType traceParamType) throws SoCTraceException {
		throw new SoCTraceException("Visit not implemented in this visitor.");
	}

	@Override
	public void visit(Tool tool) throws SoCTraceException {
		throw new SoCTraceException("Visit not implemented in this visitor.");
	}

	@Override
	public void visit(Event event) throws SoCTraceException {
		throw new SoCTraceException("Visit not implemented in this visitor.");
	}

	@Override
	public void visit(AnalysisResult analysisResult) throws SoCTraceException {
		throw new SoCTraceException("Visit not implemented in this visitor.");
	}

	@Override
	public void visit(EventParam eventParam) throws SoCTraceException {
		throw new SoCTraceException("Visit not implemented in this visitor.");
	}

	@Override
	public void visit(EventType eventType) throws SoCTraceException {
		throw new SoCTraceException("Visit not implemented in this visitor.");
	}

	@Override
	public void visit(EventParamType eventParamType) throws SoCTraceException {
		throw new SoCTraceException("Visit not implemented in this visitor.");
	}

	@Override
	public void visit(EventProducer eventProducer) throws SoCTraceException {
		throw new SoCTraceException("Visit not implemented in this visitor.");
	}

	@Override
	public void visit(File file) throws SoCTraceException {
		throw new SoCTraceException("Visit not implemented in this visitor.");
	}

}

/*
 * Note about TIMESTAMP attributes (ANALYSIS_RESULT.DATE and TRACE.TRACING_DATE)
 * 
 * 1. On MySQL the TIMESTAMP precision is only up to the 'second'. This is a well-known MySQL issue
 * (2013-02-14).
 * 
 * 2. PreparedStatement.setTimestamp() works differently on different DBMS: - on MySQL keeps the
 * format YYYY-MM-DD hh:mm:ss - on SQLite converts to milliseconds format It is for this reason that
 * we use setString(), saving therefore the string value.
 */
