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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.AnalysisResultAnnotationData;
import fr.inria.soctrace.lib.model.AnalysisResultData.AnalysisResultType;
import fr.inria.soctrace.lib.model.AnalysisResultGroupData;
import fr.inria.soctrace.lib.model.AnalysisResultGroupData.DepthFirstIterator;
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
import fr.inria.soctrace.lib.model.OrderedGroup;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.SoctraceUtils;

/**
 * Visitor able to update the entities of the trace DB.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class TraceDBUpdateVisitor extends ModelVisitor {

	/**
	 * DB object related to this visitor
	 */
	private TraceDBObject traceDB;
	
	/**
	 * The constructor.
	 * @param traceDB trace database object
	 * @throws SoCTraceException
	 */
	public TraceDBUpdateVisitor(TraceDBObject traceDB) throws SoCTraceException {
		super();	
		this.traceDB = traceDB;
		try {
			Connection conn = traceDB.getConnection();
			PreparedStatementDescriptor psd = null;
			
			// Raw trace
			
			// Events
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_EVENT_UPDATE));
			addDescriptor(FramesocTable.EVENT, psd);
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_EVENT_TYPE_UPDATE));
			addDescriptor(FramesocTable.EVENT_TYPE, psd);
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_EVENT_PARAM_UPDATE));
			addDescriptor(FramesocTable.EVENT_PARAM, psd);
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_EVENT_PARAM_TYPE_UPDATE));
			addDescriptor(FramesocTable.EVENT_PARAM_TYPE, psd);
			// Event Producers
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_EVENT_PRODUCER_UPDATE));
			addDescriptor(FramesocTable.EVENT_PRODUCER, psd);
			// File
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_FILE_UPDATE));
			addDescriptor(FramesocTable.FILE, psd);

			// Analysis results
			
			// Analysis Result
			postClearBatches();
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_ANALYSIS_RESULT_UPDATE));
			addDescriptor(FramesocTable.ANALYSIS_RESULT, psd);
			// Search
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_SEARCH_UPDATE));
			addDescriptor(FramesocTable.SEARCH, psd);
			// Annotation
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_ANNOTATION_UPDATE));
			addDescriptor(FramesocTable.ANNOTATION, psd);
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_ANNOTATION_TYPE_UPDATE));
			addDescriptor(FramesocTable.ANNOTATION_TYPE, psd);
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_ANNOTATION_PARAM_UPDATE));
			addDescriptor(FramesocTable.ANNOTATION_PARAM, psd);
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_ANNOTATION_PARAM_TYPE_UPDATE));
			addDescriptor(FramesocTable.ANNOTATION_PARAM_TYPE, psd);				
			// Group
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_GROUP_UPDATE));
			addDescriptor(FramesocTable.ENTITY_GROUP, psd);
			psd = new PreparedStatementDescriptor(conn.prepareStatement(
					SQLConstants.PREPARED_STATEMENT_GROUP_MAPPING_UPDATE));
			addDescriptor(FramesocTable.GROUP_MAPPING, psd);
			
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}
	
	@Override
	public void visit(Event event) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.EVENT);
			psd.visited = true;
			psd.statement.setInt(1, event.getType().getId());
			psd.statement.setInt(2, event.getEventProducer().getId());
			psd.statement.setLong(3, event.getTimestamp());
			psd.statement.setInt(4, event.getCpu());
			psd.statement.setInt(5, event.getPage());
			psd.statement.setInt(6, event.getCategory());
			psd.statement.setLong(7, event.getLongPar());
			psd.statement.setDouble(8, event.getDoublePar());			
			psd.statement.setInt(9, event.getId());
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
			psd.statement.setInt(1, eventType.getCategory());
			psd.statement.setString(2, eventType.getName());
			psd.statement.setInt(3, eventType.getId());
			psd.statement.addBatch();
			traceDB.getEventTypeCache().put(eventType);
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	@Override
	public void visit(EventParam eventParam) throws SoCTraceException {
		try {
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.EVENT_PARAM);
			psd.visited = true;
			psd.statement.setInt(1, eventParam.getEvent().getId());
			psd.statement.setInt(2, eventParam.getEventParamType().getId());
			psd.statement.setString(3, eventParam.getValue());
			psd.statement.setInt(4, eventParam.getId());
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
			psd.statement.setInt(1, eventParamType.getEventType().getId());
			psd.statement.setString(2, eventParamType.getName());
			psd.statement.setString(3, eventParamType.getType());
			psd.statement.setInt(4, eventParamType.getId());
			psd.statement.addBatch();
			traceDB.getEventTypeCache().put(eventParamType);
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
			psd.statement.setString(1, eventProducer.getType());
			psd.statement.setString(2, eventProducer.getLocalId());
			psd.statement.setString(3, eventProducer.getName());
			psd.statement.setInt(4, eventProducer.getParentId());
			psd.statement.setInt(5, eventProducer.getId());
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
			psd.statement.setString(1, file.getPath());
			psd.statement.setString(2, file.getDescription());
			psd.statement.setInt(3, file.getId());
			psd.statement.addBatch();
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/*
	 * Results
	 */
	
	/**
	 * Note 1: the TYPE of an analysis result CANNOT be updated to avoid DB
	 * inconsistencies.
	 * E.g. : 
	 * - search result saved
	 * - search result updated to group result in the AnalysisResult object
	 * - the update operation tries to update groups before not existing!
	 * The correct approach in this kind of situation is:
	 * - delete the old result
	 * - create a new result of a different type
	 * 
	 * Note 2: processed trace results cannot be updated because not meaningful.
	 * 
	 * Note 3: if your AnalysisResultData are not changed and only AnalysisResult
	 * metadata must be updated, just put data to null and they will be skipped.
	 */
	@Override
	public void visit(AnalysisResult analysisResult) throws SoCTraceException {
		try {		
			PreparedStatementDescriptor psd = getDescriptor(FramesocTable.ANALYSIS_RESULT);
			psd.visited = true;			
			psd.statement.setInt(1, analysisResult.getTool().getId());
			// XXX see note at the bottom of ModelVisitor.java
			psd.statement.setString(2, SoctraceUtils.timestampToString(analysisResult.getDate()));			
			psd.statement.setString(3, analysisResult.getDescription());
			psd.statement.setInt(4, analysisResult.getId());
			psd.statement.addBatch();

			if (analysisResult.getData()==null)
				return;
			
			String type = analysisResult.getType(); 
			if (type.equals(AnalysisResultType.TYPE_SEARCH.toString())) {
				updateSearchResult(analysisResult);
			} else if (type.equals(AnalysisResultType.TYPE_GROUP.toString())) {
				updateGroupResult(analysisResult);
			} else if (type.equals(AnalysisResultType.TYPE_ANNOTATION.toString())) {
				updateAnnotationResult(analysisResult);
			} else if (type.equals(AnalysisResultType.TYPE_PROCESSED_TRACE.toString())) {
				; // nothing to do.
			}
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/*
	 *     U t i l i t i e s
	 */
	
	/**
	 * TARGET_ENTITY cannot be updated to avoid inconsistencies in the DB.
	 */
	private void updateSearchResult(AnalysisResult analysisResult) throws SQLException, SoCTraceException {
		AnalysisResultSearchData data = (AnalysisResultSearchData)analysisResult.getData();
		// Note: only the SEARCH table may be updated
		PreparedStatementDescriptor psd = getDescriptor(FramesocTable.SEARCH);
		psd.visited = true;			
		psd.statement.setString(1, data.getSearchCommand());
		psd.statement.setInt(2, analysisResult.getId());
		psd.statement.addBatch();
	}

	private void updateGroupResult(AnalysisResult analysisResult) throws SQLException, SoCTraceException {
		AnalysisResultGroupData data = (AnalysisResultGroupData)analysisResult.getData();
		PreparedStatementDescriptor psdGroup = getDescriptor(FramesocTable.ENTITY_GROUP);
		PreparedStatementDescriptor psdMapping = getDescriptor(FramesocTable.GROUP_MAPPING);
		
		psdGroup.visited = true;
		psdMapping.visited = true;

		DepthFirstIterator treeIt = data.getDepthFirstIterator();
		while (treeIt.hasNext()) {
			Group g = treeIt.next();
			psdGroup.statement.setInt(1, g.getParentId());
			psdGroup.statement.setString(2, g.getName());
			psdGroup.statement.setString(3, g.getTargetEntity());
			psdGroup.statement.setString(4, g.getGroupingOperator());
			psdGroup.statement.setBoolean(5, g.isOrdered());
			psdGroup.statement.setInt(6, g.getSequenceNumber());
			psdGroup.statement.setInt(7, analysisResult.getId());
			psdGroup.statement.setInt(8, g.getId());
			psdGroup.statement.addBatch();
			// For leafs only the position currently updated.
			// This is meaningful only for ordered groups.
			if (g instanceof OrderedGroup) { 
				Map<Integer, LeafMapping> leaves = ((OrderedGroup)g).getSonLeaves();
				Iterator<Entry<Integer, LeafMapping>> it = leaves.entrySet().iterator();
				while (it.hasNext()) {
					Entry<Integer, LeafMapping> entry = (Entry<Integer, LeafMapping>) it.next();
					psdMapping.statement.setInt(1, entry.getKey());
					psdMapping.statement.setInt(2, analysisResult.getId());
					psdMapping.statement.setInt(3, entry.getValue().getMappingId());
					psdMapping.statement.addBatch();
				}
			}							
		}
	}
	
	private void updateAnnotationResult(AnalysisResult analysisResult) throws SQLException, SoCTraceException {

		AnalysisResultAnnotationData annotationData = (AnalysisResultAnnotationData)analysisResult.getData();		
		PreparedStatementDescriptor psdAnnotation = getDescriptor(FramesocTable.ANNOTATION);
		PreparedStatementDescriptor psdAnnotationType = getDescriptor(FramesocTable.ANNOTATION_TYPE);
		PreparedStatementDescriptor psdAnnotationParam = getDescriptor(FramesocTable.ANNOTATION_PARAM);
		PreparedStatementDescriptor psdAnnotationParamType = getDescriptor(FramesocTable.ANNOTATION_PARAM_TYPE);
		
		psdAnnotation.visited = true;
		psdAnnotationType.visited = true;
		psdAnnotationParam.visited = true;
		psdAnnotationParamType.visited = true;
		
		for (AnnotationType at: annotationData.getAnnotationTypes()) {
			psdAnnotationType.statement.setString(1, at.getName());
			psdAnnotationType.statement.setInt(2, analysisResult.getId());
			psdAnnotationType.statement.setInt(3, at.getId());
			psdAnnotationType.statement.addBatch();
			for (AnnotationParamType pt: at.getParamTypes()) {
				psdAnnotationParamType.statement.setInt(1, pt.getAnnotationType().getId());
				psdAnnotationParamType.statement.setString(2, pt.getName());
				psdAnnotationParamType.statement.setString(3, pt.getType());
				psdAnnotationParamType.statement.setInt(4, analysisResult.getId());
				psdAnnotationParamType.statement.setInt(5, pt.getId());
				psdAnnotationParamType.statement.addBatch();
			}
		}
		
		for (Annotation a: annotationData.getAnnotations()) {
			psdAnnotation.statement.setInt(1, a.getAnnotationType().getId());
			psdAnnotation.statement.setString(2, a.getName());
			psdAnnotation.statement.setInt(3, analysisResult.getId());
			psdAnnotation.statement.setInt(4, a.getId());
			psdAnnotation.statement.addBatch();
			for (AnnotationParam p: a.getParams()) {
				psdAnnotationParam.statement.setInt(1, a.getId());
				psdAnnotationParam.statement.setInt(2, p.getAnnotationParamType().getId());
				psdAnnotationParam.statement.setString(3, p.getValue());
				psdAnnotationParam.statement.setInt(4, analysisResult.getId());
				psdAnnotationParam.statement.setInt(5, p.getId());
				psdAnnotationParam.statement.addBatch();
			}
		}
		
	}

}
