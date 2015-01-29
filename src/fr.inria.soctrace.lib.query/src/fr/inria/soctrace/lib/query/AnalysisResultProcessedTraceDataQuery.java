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
import java.util.List;

import fr.inria.soctrace.lib.model.AnalysisResultData;
import fr.inria.soctrace.lib.model.AnalysisResultProcessedTraceData;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Query class for Processed Trace analysis results.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class AnalysisResultProcessedTraceDataQuery extends AnalysisResultDataQuery {

	private SystemDBObject sysDB;
	private Trace sourceTrace;
		
	/**
	 * The constructor.
	 * 
	 * @param traceDB Trace DB Object containing the result (it's the source trace DB)
	 * @param sysDB System DB Object
	 * @param sourceTrace Source Trace object
	 */
	public AnalysisResultProcessedTraceDataQuery(TraceDBObject traceDB, SystemDBObject sysDB, Trace sourceTrace) {
		super(traceDB);
		this.sysDB = sysDB;
		this.sourceTrace = sourceTrace;
	}

	@Override
	public AnalysisResultData getAnalysisResultData(long analysisResultId) 
			throws SoCTraceException {

		AnalysisResultProcessedTraceData processedTraceData = new AnalysisResultProcessedTraceData();
		
		try {

			String query = "SELECT * FROM " + FramesocTable.PROCESSED_TRACE + 
					" WHERE ANALYSIS_RESULT_ID = " + analysisResultId;
			debug(query);
			Statement stm = traceDB.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(query);
			
			int processedId;
			
			if (rs.next()) {
				processedId = rs.getInt(2);
			} else {
				return null;
			}
			stm.close();
			
			TraceQuery traceQuery = new TraceQuery(sysDB);
			traceQuery.setElementWhere(new SimpleCondition("ID", ComparisonOperation.EQ, String.valueOf(processedId)));
			List<Trace> traces = traceQuery.getList();
			
			if (traces.size()==0)
				throw new SoCTraceException("Trace with id " + processedId + " has not been found!");
			if (traces.size()>1)
				throw new SoCTraceException("More than one trace with the same id ("+ processedId +")");
			
			processedTraceData.setProcessedTrace(traces.get(0));
			processedTraceData.setSourceTrace(sourceTrace);
			
			return processedTraceData;			
			
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}

	}

}
