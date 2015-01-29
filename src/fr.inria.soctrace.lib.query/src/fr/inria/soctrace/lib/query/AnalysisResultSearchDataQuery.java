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

import fr.inria.soctrace.lib.model.AnalysisResultData;
import fr.inria.soctrace.lib.model.AnalysisResultSearchData;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.utils.ModelConstants.ModelEntity;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;

/**
 * Query class for Search analysis results.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class AnalysisResultSearchDataQuery extends AnalysisResultDataQuery {

	/**
	 * The constructor.
	 * 
	 * @param traceDB trace DB containing the result this data are associated with.
	 */
	public AnalysisResultSearchDataQuery(TraceDBObject traceDB) {
		super(traceDB);
	}

	@Override
	public AnalysisResultData getAnalysisResultData(long analysisResultId) throws SoCTraceException {
		AnalysisResultSearchData searchData = null;
		String searchCommand = "";
		String targetEntity = "";
		
		try {

			String query = "SELECT * FROM " + FramesocTable.SEARCH + 
					" WHERE ANALYSIS_RESULT_ID = " + analysisResultId;
			debug(query);
			Statement stm = traceDB.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(query);
			if (rs.next()) {
				searchCommand = rs.getString(2);
				targetEntity = rs.getString(3);
			}

			query = "SELECT * FROM " + FramesocTable.SEARCH_MAPPING + 
					" WHERE ANALYSIS_RESULT_ID = " + analysisResultId;
			debug(query);
			rs = stm.executeQuery(query);
			ValueListString vls = new ValueListString();
			while (rs.next()) {
				// add target entity ID to vls
				vls.addValue(String.valueOf(rs.getLong(2)));
			}
			stm.close();
		
			
			if (targetEntity.equals(ModelEntity.EVENT.name())) {
				searchData = new AnalysisResultSearchData(Event.class);
				searchData.setSearchCommand(searchCommand);
				EventQuery eventQuery = new EventQuery(traceDB);
				eventQuery.setElementWhere(new SimpleCondition("ID", ComparisonOperation.IN, vls.getValueString()));
				searchData.setElements(eventQuery.getList());				
			} 
			else if (targetEntity.equals(ModelEntity.EVENT_PRODUCER.name())) {
				searchData = new AnalysisResultSearchData(EventProducer.class);
				searchData.setSearchCommand(searchCommand);
				EventProducerQuery eventProducerQuery = new EventProducerQuery(traceDB);
				eventProducerQuery.setElementWhere(new SimpleCondition("ID", ComparisonOperation.IN, vls.getValueString()));
				searchData.setElements(eventProducerQuery.getList());
			}
			else {
				throw new SoCTraceException("Target entity unknown!");
			}
			
			return searchData;			
			
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	
}
