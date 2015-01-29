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

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.AnalysisResultData.AnalysisResultType;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.File;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Visitor able to delete the entities of the trace DB.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * 
 */
public class TraceDBDeleteVisitor extends ModelVisitor {

	/**
	 * DB object related to this visitor
	 */
	private TraceDBObject traceDB;

	/**
	 * The constructor.
	 * 
	 * @param traceDB
	 *            trace database object
	 * @throws SoCTraceException
	 */
	public TraceDBDeleteVisitor(TraceDBObject traceDB) throws SoCTraceException {
		super();
		this.traceDB = traceDB;
		try {
			Connection conn = traceDB.getConnection();
			PreparedStatementDescriptor psd = null;

			// Raw trace

			// Events
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_EVENT_DELETE),
					FramesocTable.EVENT);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_EVENT_TYPE_DELETE),
					FramesocTable.EVENT_TYPE);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_EVENT_PARAM_DELETE),
					FramesocTable.EVENT_PARAM);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_EVENT_PARAM_TYPE_DELETE),
					FramesocTable.EVENT_PARAM_TYPE);
			addDescriptor(psd);
			// Event Producers
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_EVENT_PRODUCER_DELETE),
					FramesocTable.EVENT_PRODUCER);
			addDescriptor(psd);
			// File
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_FILE_DELETE),
					FramesocTable.FILE);
			addDescriptor(psd);

			// Analysis results

			// Analysis Result
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_ANALYSIS_RESULT_DELETE),
					FramesocTable.ANALYSIS_RESULT);
			addDescriptor(psd);
			// Search
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_SEARCH_DELETE),
					FramesocTable.SEARCH);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_SEARCH_MAPPING_DELETE),
					FramesocTable.SEARCH_MAPPING);
			addDescriptor(psd);
			// Annotation
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_ANNOTATION_DELETE),
					FramesocTable.ANNOTATION);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_ANNOTATION_TYPE_DELETE),
					FramesocTable.ANNOTATION_TYPE);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_ANNOTATION_PARAM_DELETE),
					FramesocTable.ANNOTATION_PARAM);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_ANNOTATION_PARAM_TYPE_DELETE),
					FramesocTable.ANNOTATION_PARAM_TYPE);
			addDescriptor(psd);
			// Group
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_GROUP_DELETE),
					FramesocTable.ENTITY_GROUP);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_GROUP_MAPPING_DELETE),
					FramesocTable.GROUP_MAPPING);
			addDescriptor(psd);
			// Processed Trace
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_PROCESSED_TRACE_DELETE),
					FramesocTable.PROCESSED_TRACE);
			addDescriptor(psd);

			// TODO: no need to use the whole key: analysis result id is enough
			// because we delete results with AR granularity

		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void visit(Event event) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.EVENT);
			psd.visited = true;
			psd.statement.setLong(1, event.getId());
			psd.statement.addBatch();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void visit(EventParam eventParam) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.EVENT_PARAM);
			psd.visited = true;
			psd.statement.setLong(1, eventParam.getId());
			psd.statement.addBatch();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void visit(EventType eventType) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.EVENT_TYPE);
			psd.visited = true;
			psd.statement.setLong(1, eventType.getId());
			psd.statement.addBatch();
			traceDB.getEventTypeCache().remove(eventType);
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}

	}

	@Override
	public void visit(EventParamType eventParamType) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.EVENT_PARAM_TYPE);
			psd.visited = true;
			psd.statement.setLong(1, eventParamType.getId());
			psd.statement.addBatch();
			traceDB.getEventTypeCache().remove(eventParamType);
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/*
	 * Event Producers
	 */

	@Override
	public void visit(EventProducer eventProducer) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.EVENT_PRODUCER);
			psd.visited = true;
			psd.statement.setLong(1, eventProducer.getId());
			psd.statement.addBatch();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/*
	 * File
	 */

	@Override
	public void visit(File file) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.FILE);
			psd.visited = true;
			psd.statement.setLong(1, file.getId());
			psd.statement.addBatch();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/*
	 * Results
	 */

	@Override
	public void visit(AnalysisResult analysisResult) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.ANALYSIS_RESULT);
			psd.visited = true;
			psd.statement.setLong(1, analysisResult.getId());
			psd.statement.addBatch();

			String type = analysisResult.getType();
			if (type.equals(AnalysisResultType.TYPE_SEARCH.toString())) {
				deleteSearchResult(analysisResult);
			} else if (type.equals(AnalysisResultType.TYPE_GROUP.toString())) {
				deleteGroupResult(analysisResult);
			} else if (type.equals(AnalysisResultType.TYPE_ANNOTATION.toString())) {
				deleteAnnotationResult(analysisResult);
			} else if (type.equals(AnalysisResultType.TYPE_PROCESSED_TRACE.toString())) {
				deleteProcessedTraceResult(analysisResult);
			}

		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/*
	 * U t i l i t i e s
	 */

	private void deleteSearchResult(AnalysisResult analysisResult) throws SQLException {
		PreparedStatementDescriptor psdSearch = getDescriptor(FramesocTable.SEARCH);
		PreparedStatementDescriptor psdMapping = getDescriptor(FramesocTable.SEARCH_MAPPING);

		psdSearch.statement.setLong(1, analysisResult.getId());
		psdMapping.statement.setLong(1, analysisResult.getId());

		psdSearch.statement.addBatch();
		psdMapping.statement.addBatch();

		psdSearch.visited = true;
		psdMapping.visited = true;
	}

	private void deleteGroupResult(AnalysisResult analysisResult) throws SQLException,
			SoCTraceException {
		PreparedStatementDescriptor psdGroup = getDescriptor(FramesocTable.ENTITY_GROUP);
		PreparedStatementDescriptor psdMapping = getDescriptor(FramesocTable.GROUP_MAPPING);

		psdGroup.statement.setLong(1, analysisResult.getId());
		psdMapping.statement.setLong(1, analysisResult.getId());

		psdGroup.statement.addBatch();
		psdMapping.statement.addBatch();

		psdGroup.visited = true;
		psdMapping.visited = true;
	}

	private void deleteAnnotationResult(AnalysisResult analysisResult) throws SQLException {
		PreparedStatementDescriptor psdAnnotation = getDescriptor(FramesocTable.ANNOTATION);
		PreparedStatementDescriptor psdAnnotationType = getDescriptor(FramesocTable.ANNOTATION_TYPE);
		PreparedStatementDescriptor psdAnnotationParam = getDescriptor(FramesocTable.ANNOTATION_PARAM);
		PreparedStatementDescriptor psdAnnotationParamType = getDescriptor(FramesocTable.ANNOTATION_PARAM_TYPE);

		psdAnnotation.statement.setLong(1, analysisResult.getId());
		psdAnnotationType.statement.setLong(1, analysisResult.getId());
		psdAnnotationParam.statement.setLong(1, analysisResult.getId());
		psdAnnotationParamType.statement.setLong(1, analysisResult.getId());

		psdAnnotation.statement.addBatch();
		psdAnnotationType.statement.addBatch();
		psdAnnotationParam.statement.addBatch();
		psdAnnotationParamType.statement.addBatch();

		psdAnnotation.visited = true;
		psdAnnotationType.visited = true;
		psdAnnotationParam.visited = true;
		psdAnnotationParamType.visited = true;
	}

	private void deleteProcessedTraceResult(AnalysisResult analysisResult) throws SQLException {
		PreparedStatementDescriptor psd = getDescriptor(FramesocTable.PROCESSED_TRACE);
		psd.statement.setLong(1, analysisResult.getId());
		psd.statement.addBatch();
		psd.visited = true;
	}

}
