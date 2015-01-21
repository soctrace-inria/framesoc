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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.conditions.ICondition;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.lib.utils.SoctraceUtils;

/**
 * Query class for AnalysisResult table.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class AnalysisResultQuery extends ElementQuery {

	private ICondition toolWhere;
	private Map<Integer, Tool> toolCache;

	/**
	 * The constructor.
	 * @param traceDB Trace DB object where the query is performed.
	 */
	public AnalysisResultQuery(TraceDBObject traceDB) {
		super(traceDB);
		clear();
		toolCache = new HashMap<Integer, Tool>();
	}

	/**
	 * Set the condition to be put in the WHERE clause of TOOL table.
	 * @param toolWhere condition to be applied to the tool table
	 */
	public void setToolWhere(ICondition toolWhere) {
		where = true;
		this.toolWhere = toolWhere;
	}

	@Override
	public void clear() {
		super.clear();
		toolWhere = null;
		if (toolCache != null)
			toolCache.clear();
	}

	@Override
	public List<AnalysisResult> getList() throws SoCTraceException {

		try {

			boolean first = true;
			StringBuilder analysisQuery = new StringBuilder("SELECT * FROM " + FramesocTable.ANALYSIS_RESULT + " ");

			if (where) {
				analysisQuery.append(" WHERE ");
			}

			if (elementWhere != null) {
				first = false;
				analysisQuery.append(elementWhere.getSQLString());
			}

			if (toolWhere != null) {
				if (!first)
					analysisQuery.append(" AND ");
				else
					first = false;

				ValueListString vls = loadTools(toolWhere);
				if (vls.size()==0)
					analysisQuery.append("( TOOL_ID IN ( " + IdManager.RESERVED_NO_ID + ") )");
				else
					analysisQuery.append("( TOOL_ID IN " + vls.getValueString() + " )");				

			} 

			if (orderBy) {
				analysisQuery.append(" ORDER BY " + orderByColumn + " " + orderByCriterium);
			}

			if (isLimitSet()) {
				analysisQuery.append(" LIMIT " + getLimit());
			}

			String query = analysisQuery.toString();
			debug(query);

			Statement stm = dbObj.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(query);

			List<AnalysisResult> analysisResults = new LinkedList<AnalysisResult>();
			Map<Integer, Integer> arIdToolId = new HashMap<Integer, Integer>(); 
			while (rs.next()) {
				Integer arId = rs.getInt(1);
				Integer toolId = rs.getInt(2);
				AnalysisResult ar = new AnalysisResult(arId);
				ar.setType(rs.getString(3));
				// XXX see note at the bottom of ModelVisitor.java
				ar.setDate(SoctraceUtils.stringToTimestamp(rs.getString(4)));
				ar.setDescription(rs.getString(5));
				analysisResults.add(ar);
				arIdToolId.put(arId, toolId);
			}
			stm.close();

			loadToolsIntoAnalysisResult(analysisResults, arIdToolId);

			return analysisResults;			

		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}

	}

	/**
	 * Load the tools specified by the tool WHERE condition and return
	 * the list of their IDs.
	 * @param toolWhere tool WHERE condition
	 * @return a list containing tools ID
	 * @throws SoCTraceException
	 */
	private ValueListString loadTools(ICondition toolWhere) throws SoCTraceException {
		SystemDBObject sysDB = null;
		try {
			sysDB = new SystemDBObject(
					Configuration.getInstance().get(SoCTraceProperty.soctrace_db_name), 
					DBMode.DB_OPEN);
			ValueListString vls = new ValueListString();
			ToolQuery toolQuery = new ToolQuery(sysDB);
			toolQuery.setElementWhere(toolWhere);
			List<Tool> toolList = toolQuery.getList();
			for (Tool t: toolList) {
				toolCache.put(t.getId(), t);
				vls.addValue(String.valueOf(t.getId()));
			}
			return vls;
		} finally {
			DBObject.finalClose(sysDB);
		}
	}

	/**
	 * Load tools into Analysis Result objects
	 * @param analysisResults list of Analysis Results
	 * @param arIdToolId map linking Analysis Result ID to Tool ID
	 * @throws SoCTraceException 
	 */
	private void loadToolsIntoAnalysisResult(List<AnalysisResult> analysisResults,
			Map<Integer, Integer> arIdToolId) throws SoCTraceException {

		// prepare tool id list: only tools not present
		ValueListString vls = new ValueListString();
		for(Integer toolId: arIdToolId.values()) {
			if (toolCache.containsKey(toolId))
				continue;
			vls.addValue(String.valueOf(toolId));
		}

		// load the missing tools (if any)
		if (vls.size() != 0) {
			SystemDBObject sysDB = null;
			try {
				sysDB = new SystemDBObject(
						Configuration.getInstance().get(SoCTraceProperty.soctrace_db_name), 
						DBMode.DB_OPEN);
				Statement stm = sysDB.getConnection().createStatement();
				ResultSet rs = stm.executeQuery("SELECT * FROM " + FramesocTable.TOOL+ 
						" WHERE ID IN " + vls.getValueString());
				while (rs.next()) {
					Tool t = new Tool(rs.getInt(1));
					t.setName(rs.getString(2));
					t.setType(rs.getString(3));
					t.setCommand(rs.getString(4));
					t.setPlugin(rs.getBoolean(5));
					t.setDoc(rs.getString(6));
					t.setExtensionId(rs.getString(7));
					toolCache.put(t.getId(), t);
				}
				stm.close();
			} catch (SQLException e) {
				throw new SoCTraceException(e);
			} finally {
				DBObject.finalClose(sysDB);
			}
		} 

		// load tools into Analysis Result
		for (AnalysisResult ar: analysisResults) {
			ar.setTool(toolCache.get(arIdToolId.get(ar.getId())));
		}

	}	

}
