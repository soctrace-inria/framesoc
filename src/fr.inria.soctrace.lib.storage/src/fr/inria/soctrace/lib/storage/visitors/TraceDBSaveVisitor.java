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
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.AnalysisResultAnnotationData;
import fr.inria.soctrace.lib.model.AnalysisResultData.AnalysisResultType;
import fr.inria.soctrace.lib.model.AnalysisResultGroupData;
import fr.inria.soctrace.lib.model.AnalysisResultGroupData.DepthFirstIterator;
import fr.inria.soctrace.lib.model.AnalysisResultProcessedTraceData;
import fr.inria.soctrace.lib.model.AnalysisResultSearchData;
import fr.inria.soctrace.lib.model.Annotation;
import fr.inria.soctrace.lib.model.AnnotationParam;
import fr.inria.soctrace.lib.model.AnnotationParamType;
import fr.inria.soctrace.lib.model.AnnotationType;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.File;
import fr.inria.soctrace.lib.model.Group;
import fr.inria.soctrace.lib.model.Group.LeafMapping;
import fr.inria.soctrace.lib.model.ISearchable;
import fr.inria.soctrace.lib.model.OrderedGroup;
import fr.inria.soctrace.lib.model.UnorderedGroup;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.lib.utils.SoctraceUtils;

/**
 * Visitor able to save the entities of the Trace DB
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * 
 */
public class TraceDBSaveVisitor extends ModelVisitor {

	/**
	 * DB object related to this visitor
	 */
	private TraceDBObject traceDB;

	/**
	 * This variable is used to automatically assign ID to analysis results without replicating it
	 * (e.g. if 2 analysis result are saved without flushing the visitor batches). - At visitor
	 * initialization the variable gets the MAX ID in ANALYSIS_RESULT table. - At each
	 * visit(AnalysisResult) it gets incremented. - At each postClearBatches it gets the (updated)
	 * MAX ID again.
	 */
	private int analysisResultLastId;

	/**
	 * The constructor.
	 * 
	 * @param traceDB
	 *            trace DB object
	 * @throws SoCTraceException
	 */
	public TraceDBSaveVisitor(TraceDBObject traceDB) throws SoCTraceException {
		super();
		this.traceDB = traceDB;
		try {
			Connection conn = traceDB.getConnection();
			PreparedStatementDescriptor psd = null;

			// Raw trace

			// Events
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_EVENT_INSERT),
					FramesocTable.EVENT);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_EVENT_TYPE_INSERT),
					FramesocTable.EVENT_TYPE);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_EVENT_PARAM_INSERT),
					FramesocTable.EVENT_PARAM);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_EVENT_PARAM_TYPE_INSERT),
					FramesocTable.EVENT_PARAM_TYPE);
			addDescriptor(psd);
			// Event Producers
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_EVENT_PRODUCER_INSERT),
					FramesocTable.EVENT_PRODUCER);
			addDescriptor(psd);
			// File
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_FILE_INSERT),
					FramesocTable.FILE);
			addDescriptor(psd);

			// Analysis results

			// Analysis Result
			postClearBatches();
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_ANALYSIS_RESULT_INSERT),
					FramesocTable.ANALYSIS_RESULT);
			addDescriptor(psd);
			// Search
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_SEARCH_INSERT),
					FramesocTable.SEARCH);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_SEARCH_MAPPING_INSERT),
					FramesocTable.SEARCH_MAPPING);
			addDescriptor(psd);
			// Annotation
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_ANNOTATION_INSERT),
					FramesocTable.ANNOTATION);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_ANNOTATION_TYPE_INSERT),
					FramesocTable.ANNOTATION_TYPE);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_ANNOTATION_PARAM_INSERT),
					FramesocTable.ANNOTATION_PARAM);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_ANNOTATION_PARAM_TYPE_INSERT),
					FramesocTable.ANNOTATION_PARAM_TYPE);
			addDescriptor(psd);
			// Group
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_GROUP_INSERT),
					FramesocTable.ENTITY_GROUP);
			addDescriptor(psd);
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_GROUP_MAPPING_INSERT),
					FramesocTable.GROUP_MAPPING);
			addDescriptor(psd);
			// Processed Trace
			psd = new PreparedStatementDescriptor(
					conn.prepareStatement(SQLConstants.PREPARED_STATEMENT_PROCESSED_TRACE_INSERT),
					FramesocTable.PROCESSED_TRACE);
			addDescriptor(psd);
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	protected void postClearBatches() throws SoCTraceException {
		// reset analysis result last id to the max in the DB
		analysisResultLastId = traceDB.getMaxId(FramesocTable.ANALYSIS_RESULT.toString(), "ID");
	}

	/*
	 * Events
	 */

	@Override
	public void visit(Event event) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.EVENT);
			psd.visited = true;
			psd.statement.setInt(1, event.getId());
			psd.statement.setInt(2, event.getType().getId());
			psd.statement.setInt(3, event.getEventProducer().getId());
			psd.statement.setLong(4, event.getTimestamp());
			psd.statement.setInt(5, event.getCpu());
			psd.statement.setInt(6, event.getPage());
			psd.statement.setInt(7, event.getCategory());
			psd.statement.setLong(8, event.getLongPar());
			psd.statement.setDouble(9, event.getDoublePar());
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
			psd.statement.setInt(1, eventType.getId());
			psd.statement.setInt(2, eventType.getCategory());
			psd.statement.setString(3, eventType.getName());
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
			psd.statement.setInt(1, eventParam.getId());
			psd.statement.setInt(2, eventParam.getEvent().getId());
			psd.statement.setInt(3, eventParam.getEventParamType().getId());
			psd.statement.setString(4, eventParam.getValue());
			psd.statement.addBatch();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void visit(EventParamType eventParamType) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.EVENT_PARAM_TYPE);
			psd.visited = true;
			psd.statement.setInt(1, eventParamType.getId());
			psd.statement.setInt(2, eventParamType.getEventType().getId());
			psd.statement.setString(3, eventParamType.getName());
			psd.statement.setString(4, eventParamType.getType());
			psd.statement.addBatch();
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
			psd.statement.setInt(1, eventProducer.getId());
			psd.statement.setString(2, eventProducer.getType());
			psd.statement.setString(3, eventProducer.getLocalId());
			psd.statement.setString(4, eventProducer.getName());
			psd.statement.setInt(5, eventProducer.getParentId());
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
			psd.statement.setInt(1, file.getId());
			psd.statement.setString(2, file.getPath());
			psd.statement.setString(3, file.getDescription());
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
			analysisResult.setId(++analysisResultLastId);
			analysisResult.setDate(new Timestamp(new Date().getTime()));

			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.ANALYSIS_RESULT);
			psd.visited = true;
			psd.statement.setInt(1, analysisResult.getId());
			psd.statement.setInt(2, analysisResult.getTool().getId());
			psd.statement.setString(3, analysisResult.getType());
			// XXX see note at the bottom of ModelVisitor.java
			psd.statement.setString(4, SoctraceUtils.timestampToString(analysisResult.getDate()));
			psd.statement.setString(5, analysisResult.getDescription());
			psd.statement.addBatch();

			if (analysisResult.getData() == null)
				throw new SoCTraceException("Trying to save an analysis result with no data");

			String type = analysisResult.getType();
			if (type.equals(AnalysisResultType.TYPE_SEARCH.toString())) {
				saveSearchResult(analysisResult);
			} else if (type.equals(AnalysisResultType.TYPE_GROUP.toString())) {
				saveGroupResult(analysisResult);
			} else if (type.equals(AnalysisResultType.TYPE_ANNOTATION.toString())) {
				saveAnnotationResult(analysisResult);
			} else if (type.equals(AnalysisResultType.TYPE_PROCESSED_TRACE.toString())) {
				saveProcessedTraceResult(analysisResult);
			}

		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/*
	 * U t i l i t i e s
	 */

	private void saveSearchResult(AnalysisResult analysisResult) throws SQLException,
			SoCTraceException {
		AnalysisResultSearchData data = (AnalysisResultSearchData) analysisResult.getData();
		// search
		PreparedStatementDescriptor psd = getDescriptor(FramesocTable.SEARCH);
		psd.visited = true;
		psd.statement.setInt(1, analysisResult.getId());
		psd.statement.setString(2, data.getSearchCommand());
		psd.statement.setString(3, data.getTargetEntity());
		psd.statement.addBatch();
		// mapping
		psd = getDescriptor(FramesocTable.SEARCH_MAPPING);
		psd.visited = true;
		for (ISearchable e : data.getElements()) {
			psd.statement.setInt(1, analysisResult.getId());
			psd.statement.setInt(2, e.getId());
			psd.statement.addBatch();
		}
	}

	private void saveGroupResult(AnalysisResult analysisResult) throws SQLException,
			SoCTraceException {
		AnalysisResultGroupData data = (AnalysisResultGroupData) analysisResult.getData();
		PreparedStatementDescriptor psdGroup = getDescriptor(FramesocTable.ENTITY_GROUP);
		PreparedStatementDescriptor psdMapping = getDescriptor(FramesocTable.GROUP_MAPPING);

		psdGroup.visited = true;
		psdMapping.visited = true;

		DepthFirstIterator treeIt = data.getDepthFirstIterator();
		IdManager mappingIdManager = new IdManager();
		while (treeIt.hasNext()) {
			Group g = treeIt.next();
			psdGroup.statement.setInt(1, analysisResult.getId());
			psdGroup.statement.setInt(2, g.getId());
			psdGroup.statement.setInt(3, g.getParentId());
			psdGroup.statement.setString(4, g.getName());
			psdGroup.statement.setString(5, g.getTargetEntity());
			psdGroup.statement.setString(6, g.getGroupingOperator());
			psdGroup.statement.setBoolean(7, g.isOrdered());
			psdGroup.statement.setInt(8, g.getSequenceNumber());
			psdGroup.statement.addBatch();
			if (g instanceof OrderedGroup) {
				Map<Integer, LeafMapping> sons = ((OrderedGroup) g).getSonLeaves();
				Iterator<Entry<Integer, LeafMapping>> it = (Iterator<Entry<Integer, LeafMapping>>) sons
						.entrySet().iterator();
				while (it.hasNext()) {
					Entry<Integer, LeafMapping> pairs = (Entry<Integer, LeafMapping>) it.next();
					LeafMapping mapping = pairs.getValue();
					mapping.setMappingId(mappingIdManager.getNextId());
					psdMapping.statement.setInt(1, analysisResult.getId());
					psdMapping.statement.setInt(2, mapping.getMappingId());
					psdMapping.statement.setInt(3, g.getId());
					psdMapping.statement.setInt(4, mapping.getSon().getId());
					psdMapping.statement.setInt(5, pairs.getKey());
					psdMapping.statement.addBatch();
				}
			} else {
				List<LeafMapping> leaves = ((UnorderedGroup) g).getSonLeaves();
				for (LeafMapping mapping : leaves) {
					mapping.setMappingId(mappingIdManager.getNextId());
					psdMapping.statement.setInt(1, analysisResult.getId());
					psdMapping.statement.setInt(2, mapping.getMappingId());
					psdMapping.statement.setInt(3, g.getId());
					psdMapping.statement.setInt(4, mapping.getSon().getId());
					psdMapping.statement.setInt(5, -1);
					psdMapping.statement.addBatch();
				}
			}
		}
	}

	private void saveAnnotationResult(AnalysisResult analysisResult) throws SQLException,
			SoCTraceException {
		AnalysisResultAnnotationData annotationData = (AnalysisResultAnnotationData) analysisResult
				.getData();
		PreparedStatementDescriptor psdAnnotation = getDescriptor(FramesocTable.ANNOTATION);
		PreparedStatementDescriptor psdAnnotationType = getDescriptor(FramesocTable.ANNOTATION_TYPE);
		PreparedStatementDescriptor psdAnnotationParam = getDescriptor(FramesocTable.ANNOTATION_PARAM);
		PreparedStatementDescriptor psdAnnotationParamType = getDescriptor(FramesocTable.ANNOTATION_PARAM_TYPE);

		psdAnnotation.visited = true;
		psdAnnotationType.visited = true;
		psdAnnotationParam.visited = true;
		psdAnnotationParamType.visited = true;

		for (AnnotationType at : annotationData.getAnnotationTypes()) {
			psdAnnotationType.statement.setInt(1, analysisResult.getId());
			psdAnnotationType.statement.setInt(2, at.getId());
			psdAnnotationType.statement.setString(3, at.getName());
			psdAnnotationType.statement.addBatch();
			for (AnnotationParamType pt : at.getParamTypes()) {
				psdAnnotationParamType.statement.setInt(1, analysisResult.getId());
				psdAnnotationParamType.statement.setInt(2, pt.getId());
				psdAnnotationParamType.statement.setInt(3, pt.getAnnotationType().getId());
				psdAnnotationParamType.statement.setString(4, pt.getName());
				psdAnnotationParamType.statement.setString(5, pt.getType());
				psdAnnotationParamType.statement.addBatch();
			}
		}

		for (Annotation a : annotationData.getAnnotations()) {
			psdAnnotation.statement.setInt(1, analysisResult.getId());
			psdAnnotation.statement.setInt(2, a.getId());
			psdAnnotation.statement.setInt(3, a.getAnnotationType().getId());
			psdAnnotation.statement.setString(4, a.getName());
			psdAnnotation.statement.addBatch();
			for (AnnotationParam p : a.getParams()) {
				psdAnnotationParam.statement.setInt(1, analysisResult.getId());
				psdAnnotationParam.statement.setInt(2, p.getId());
				psdAnnotationParam.statement.setInt(3, a.getId());
				psdAnnotationParam.statement.setInt(4, p.getAnnotationParamType().getId());
				psdAnnotationParam.statement.setString(5, p.getValue());
				psdAnnotationParam.statement.addBatch();
			}
		}

	}

	private void saveProcessedTraceResult(AnalysisResult analysisResult) throws SQLException,
			SoCTraceException {
		AnalysisResultProcessedTraceData data = (AnalysisResultProcessedTraceData) analysisResult
				.getData();
		PreparedStatementDescriptor psd = getDescriptor(FramesocTable.PROCESSED_TRACE);
		psd.visited = true;
		psd.statement.setInt(1, analysisResult.getId());
		psd.statement.setInt(2, data.getProcessedTrace().getId());
		psd.statement.addBatch();
	}

}
