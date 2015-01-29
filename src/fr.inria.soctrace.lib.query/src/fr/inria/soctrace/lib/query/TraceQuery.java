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

import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceParam;
import fr.inria.soctrace.lib.model.TraceParamType;
import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.conditions.ICondition;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.SoctraceUtils;


/**
 * Query class for Trace self-defining-pattern tables.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class TraceQuery extends SelfDefiningElementQuery {
	
	private ICondition typeWhere;
	
	/**
	 * The constructor.
	 * @param sysDB System DB object where the query is performed.
	 */
	public TraceQuery(SystemDBObject sysDB) {
		super(sysDB);
		clear();
	}
	
	@Override
	public void clear() {
		super.clear();
		this.typeWhere = null;
	}
	
	@Override
	public String getElementTableName() {
		return FramesocTable.TRACE.toString();
	}

	@Override
	public ParamType getParamType(String typeName, int typeId) throws SoCTraceException {
		return getTraceParamType(typeName, typeId);
	}

	@Override
	public int getTypeId(String typeName) throws SoCTraceException {
		return getTraceTypeId(typeName);
	}

	@Override
	public DBObject getDBObject() {
		return dbObj;
	}
	
	/**
	 * Set the condition to be put in the WHERE clause of TRACE_TYPE table.
	 * @param typeCondition condition to be applied to the trace type table
	 */
	public void setTypeWhere(ICondition typeCondition) {
		where = true;
		this.typeWhere = typeCondition;
	}
	
	/**
	 * Builds a list of Trace respecting the condition specified by
	 * elementWhere AND typeWhere AND parametersConditions.
	 * The different parameter conditions are evaluated in OR,
	 * since they refer to different trace types so it makes no sense
	 * having an AND.
	 * @return the Trace list.
	 * @throws SoCTraceException
	 */
	@Override
	public List<Trace> getList() throws SoCTraceException {

		try {
			boolean first = true;
			StringBuilder traceQuery = new StringBuilder("SELECT * FROM " + FramesocTable.TRACE + " ");

			if (where) {
				traceQuery.append(" WHERE ");
			}

			if (elementWhere != null) {
				first = false;
				traceQuery.append(elementWhere.getSQLString());
			}

			if (typeWhere != null) {
				if (!first)
					traceQuery.append(" AND ");
				else
					first = false;
				traceQuery.append("( TRACE_TYPE_ID IN ( SELECT ID FROM " 
					+ FramesocTable.TRACE_TYPE + " WHERE " + typeWhere.getSQLString() + " ) )");
			} 
			
			if (parametersConditions.size()>0) {
				if (!first)
					traceQuery.append(" AND ");
				else
					first = false;
				traceQuery.append( getParamConditionsString() );
			}

			if (orderBy) {
				traceQuery.append(" ORDER BY " + orderByColumn + " " + orderByCriterium);
			}
			
			if (isLimitSet()) {
				traceQuery.append(" LIMIT " + getLimit());
			}
			
			String query = traceQuery.toString();
			debug(query);

			Statement stm = dbObj.getConnection().createStatement();
			ResultSet rs = stm.executeQuery(query);
			
			List<Trace> traces = getTraces(rs);
			stm.close();
			return traces;

		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}

	}

	/**
	 * Return the list of Trace having the specified IDs.
	 * @param ids list of trace IDs
	 * @return the list of Trace
	 * @throws SoCTraceException
	 */
	public List<Trace> getTraces(List<Integer> ids) throws SoCTraceException {
		try {
			ValueListString vls = new ValueListString();
			for (Integer i: ids) {
				vls.addValue(i.toString());
			}			
			if (vls.size()==0)
				return new LinkedList<Trace>();
			
			Statement stm = dbObj.getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SELECT * FROM " + FramesocTable.TRACE+ 
					" WHERE ID IN " + vls.getValueString());
			
			return getTraces(rs);		
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}	
	}

	
	/*
	 *      U t i l i t i e s
	 */

	
	/**
	 * Rebuilds the traces corresponding to the result set.
	 * @param rs Result set corresponding to a SELECT * FROM TRACE ...
	 * @return a list of Trace
	 * @throws SoCTraceException
	 */
	private List<Trace> getTraces(ResultSet rs) throws SoCTraceException {
		
		ValueListString vls = new ValueListString();
		List<Trace> list = new LinkedList<Trace>();
		Map<Long, Trace> tmp = new HashMap<>();
		try {
			while (rs.next()) {
				Trace t = rebuildTrace(rs);
				list.add(t);
				
				// to rebuild all params
				tmp.put(t.getId(), t);
				vls.addValue(String.valueOf(t.getId()));
			}

			if (vls.size()==0)
				return list;
			
			Statement stm = dbObj.getConnection().createStatement();
			ResultSet prs = stm.executeQuery("SELECT * FROM " + FramesocTable.TRACE_PARAM + 
					" WHERE TRACE_ID IN " + vls.getValueString());
			
			while (prs.next()) {
				rebuildTraceParam(prs, tmp);
			}
			return list;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Get the trace type ID given the trace type name.
	 * @param name trace type name
	 * @return the corresponding trace type ID or -1 if not found
	 * @throws SoCTraceException
	 */
	private int getTraceTypeId(String name) throws SoCTraceException {
		try {
			Statement stm = dbObj.getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SELECT * FROM " 
					+ FramesocTable.TRACE_TYPE + " WHERE NAME='" + name + "'");
			if (rs.next()) {
				return rs.getInt("ID");
			}
			return -1;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}
	
	/**
	 * Get the trace param type with the given name, for the trace type 
	 * whose ID is passed.
	 * 
	 * TODO: this can be optimized with the type cache
	 * @param name the trace param type name
	 * @param traceTypeId the trace type ID 
	 * @return the corresponding trace param type or null if not found
	 * @throws SoCTraceException
	 */
	private ParamType getTraceParamType(String name, int traceTypeId) throws SoCTraceException {
		try {
			Statement stm = dbObj.getConnection().createStatement();
			String query = "SELECT * FROM " + FramesocTable.TRACE_PARAM_TYPE + 
					" WHERE NAME='" + name + "' AND TRACE_TYPE_ID=" + traceTypeId;
			debug(query);
			ResultSet rs = stm.executeQuery(query);
			if (rs.next()) {
				return new ParamType(rs.getInt("ID"), name, rs.getString("TYPE"));
			}
			return null;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Rebuild a Trace, given the corresponding TRACE table row.
	 * 
	 * @param rs TRACE table row
	 * @return the Trace
	 * @throws SQLException
	 * @throws SoCTraceException
	 */
	private Trace rebuildTrace(ResultSet rs) throws SQLException, SoCTraceException {
		Trace t = new Trace(rs.getLong(1));
		SystemDBObject sysDB = (SystemDBObject)dbObj;
		TraceType tt = sysDB.getTraceTypeCache().get(TraceType.class, rs.getLong(2));
		t.setType(tt);
		// XXX see note at the bottom of ModelVisitor.java
		t.setTracingDate(SoctraceUtils.stringToTimestamp(rs.getString(3)));
		t.setTracedApplication(rs.getString(4));
		t.setBoard(rs.getString(5));
		t.setOperatingSystem(rs.getString(6));
		t.setNumberOfCpus(rs.getInt(7));
		t.setNumberOfEvents(rs.getLong(8));
		t.setOutputDevice(rs.getString(9));
		t.setDescription(rs.getString(10));
		t.setProcessed(rs.getBoolean(11));
		t.setDbName(rs.getString(12));
		t.setAlias(rs.getString(13));
		t.setMinTimestamp(rs.getLong(14));
		t.setMaxTimestamp(rs.getLong(15));
		t.setTimeUnit(rs.getInt(16));
		return t;
	}
	
	/**
	 * Rebuild a TraceParam, given the corresponding TRACE_PARAM table row.
	 * 
	 * @param prs TRACE_PARAM table row
	 * @param tmp map containing the Traces returned by the query
	 * @return the TraceParam
	 * @throws SQLException
	 * @throws SoCTraceException 
	 */
	private TraceParam rebuildTraceParam(ResultSet prs, Map<Long, Trace> tmp) 
			throws SQLException, SoCTraceException {
		TraceParam tp = new TraceParam(prs.getLong(1));
		tp.setTrace(tmp.get(prs.getLong(2)));
		SystemDBObject sysDB = (SystemDBObject)dbObj;
		tp.setTraceParamType(sysDB.getTraceTypeCache().get(TraceParamType.class, prs.getLong(3)));
		tp.setValue(prs.getString(4));
		return tp;
	}
	
}
