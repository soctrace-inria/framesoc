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
package fr.inria.soctrace.lib.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import fr.inria.soctrace.lib.model.AnalysisResultAnnotationData;
import fr.inria.soctrace.lib.model.AnalysisResultData;
import fr.inria.soctrace.lib.model.Annotation;
import fr.inria.soctrace.lib.model.AnnotationParam;
import fr.inria.soctrace.lib.model.AnnotationParamType;
import fr.inria.soctrace.lib.model.AnnotationType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Query class for Annotation analysis results.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class AnalysisResultAnnotationDataQuery extends AnalysisResultDataQuery {

	/*
	 * Utility maps to rebuild all the annotations of a given analysis
	 */
	
	private Map<Long, AnnotationType> typeMap = new HashMap<>();
	private Map<Long, AnnotationParamType> paramTypeMap = new HashMap<>();
	private Map<Long, Annotation> annotationMap = new HashMap<>();
	/**
	 * The constructor.
	 * 
	 * @param traceDB trace DB containing the result
	 */
	public AnalysisResultAnnotationDataQuery(TraceDBObject traceDB) {
		super(traceDB);
	}

	@Override
	public AnalysisResultData getAnalysisResultData(long analysisResultId)
			throws SoCTraceException {
		
		
		AnalysisResultAnnotationData annotationData = new AnalysisResultAnnotationData();
		
		try {
			Statement stm = traceDB.getConnection().createStatement();
			
			buildTypes(analysisResultId, stm);
			buildParamTypes(analysisResultId, stm);
			buildAnnotations(analysisResultId, stm);
			buildParams(analysisResultId, stm);

			stm.close();
			
			for (Annotation a: annotationMap.values()) {
				annotationData.addAnnotation(a);
			}
			
			clear();
			
			return annotationData;			
			
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}
	
	/**
	 * Rebuild all the annotation types.
	 * Fills type map.
	 * 
	 * @param analysisResultId analysis result id
	 * @param stm SQL statement
	 * @throws SQLException
	 */
	private void buildTypes(long analysisResultId, Statement stm) throws SQLException {
		String query = "SELECT * FROM " + FramesocTable.ANNOTATION_TYPE + 
				" WHERE ANALYSIS_RESULT_ID = " + analysisResultId;
		debug(query);
		ResultSet rs = stm.executeQuery(query);
		while (rs.next()) {
			AnnotationType type = new AnnotationType(rs.getInt(2));
			type.setName(rs.getString(3));
			typeMap.put(type.getId(), type);
		}		
	}
	
	/**
	 * Rebuild all the annotation parameter types.
	 * Uses the type map.
	 * Fills the parameter type map.
	 * 
	 * @param analysisResultId analysis result id
	 * @param stm SQL statement
	 * @throws SQLException
	 */
	private void buildParamTypes(long analysisResultId, Statement stm) throws SQLException {
		String query = "SELECT * FROM " + FramesocTable.ANNOTATION_PARAM_TYPE + 
				" WHERE ANALYSIS_RESULT_ID = " + analysisResultId;
		debug(query);
		ResultSet rs = stm.executeQuery(query);
		while (rs.next()) {
			AnnotationParamType paramType = new AnnotationParamType(rs.getInt(2));
			AnnotationType type = typeMap.get(rs.getInt(3));
			paramType.setAnnotationType(type);
			paramType.setName(rs.getString(4));
			paramType.setType(rs.getString(5));
			paramTypeMap.put(paramType.getId(), paramType);
		}
	}

	/**
	 * Rebuild all the annotations.
	 * Uses the type map.
	 * Fills the annotation map.
	 * 
	 * @param analysisResultId analysis result id
	 * @param stm SQL statement
	 * @throws SQLException
	 */
	private void buildAnnotations(long analysisResultId, Statement stm) throws SQLException {
		String query = "SELECT * FROM " + FramesocTable.ANNOTATION + 
				" WHERE ANALYSIS_RESULT_ID = " + analysisResultId;
		debug(query);
		ResultSet rs = stm.executeQuery(query);
		while (rs.next()) {
			Annotation annotation = new Annotation(rs.getInt(2));
			AnnotationType type = typeMap.get(rs.getInt(3));
			annotation.setAnnotationType(type);
			annotation.setName(rs.getString(4));
			annotationMap.put(annotation.getId(), annotation);
		}
	}

	/**
	 * Rebuild all the annotation parameters.
	 * Uses the annotation map and the parameter type map.
	 *	
	 * @param analysisResultId analysis result id
	 * @param stm SQL statement
	 * @throws SQLException
	 */
	private void buildParams(long analysisResultId, Statement stm) throws SQLException {
		String query = "SELECT * FROM " + FramesocTable.ANNOTATION_PARAM + 
				" WHERE ANALYSIS_RESULT_ID = " + analysisResultId;
		debug(query);
		ResultSet rs = stm.executeQuery(query);
		while (rs.next()) {
			AnnotationParam param = new AnnotationParam(rs.getInt(2));
			Annotation annotation = annotationMap.get(rs.getInt(3));
			AnnotationParamType paramType = paramTypeMap.get(rs.getInt(4));
			param.setAnnotation(annotation);
			param.setAnnotationParamType(paramType);
			param.setValue(rs.getString(5));
		}
	}

	/**
	 * Clear maps
	 */
	private void clear() {
		typeMap.clear();
		paramTypeMap.clear();
		annotationMap.clear();
	}

}
