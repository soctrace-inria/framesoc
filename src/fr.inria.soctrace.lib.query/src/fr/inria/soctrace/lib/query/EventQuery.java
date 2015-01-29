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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Link;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.conditions.ICondition;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.ModelElementCache;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Query class for Event self-defining-pattern tables.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class EventQuery extends SelfDefiningElementQuery {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(EventQuery.class);
	
	protected ICondition typeWhere;
	protected ICondition eventProducerWhere;	
	private ModelElementCache eventProducerCache;
	protected boolean loadParameters;

	/**
	 * The constructor
	 * @param traceDB Trace DB object where the query is performed.
	 */
	public EventQuery(TraceDBObject traceDB) {
		super(traceDB);
		clear();
	}

	/**
	 * Clear query: removes all the conditions.
	 * EventProducer cache is not cleared, since we are not changing trace DB.
	 */
	@Override
	public void clear() {
		super.clear();
		this.typeWhere = null;
		this.eventProducerWhere = null;
		if (eventProducerCache != null)
			eventProducerCache.clear();
		this.eventProducerCache = null;
		this.loadParameters = true;
	}

	@Override
	public String getElementTableName() {
		return FramesocTable.EVENT.toString();
	}

	@Override
	public ParamType getParamType(String typeName, int typeId) throws SoCTraceException {
		return getEventParamType(typeName, typeId);
	}

	@Override
	public int getTypeId(String typeName) throws SoCTraceException {
		return getEventTypeId(typeName);
	}

	@Override
	public DBObject getDBObject() {
		return dbObj;
	}

	/**
	 * @return the loadParameters
	 */
	public boolean isLoadParameters() {
		return loadParameters;
	}

	/**
	 * @param loadParameters the loadParameters to set
	 */
	public void setLoadParameters(boolean loadParameters) {
		this.loadParameters = loadParameters;
	}

	/**
	 * Set the condition to be put in the WHERE clause of EVENT_TYPE table.
	 * @param typeCondition condition to be applied to the event type table
	 */
	public void setTypeWhere(ICondition typeCondition) {
		where = true;
		this.typeWhere = typeCondition;
	}

	/**
	 * Set the condition to be put in the WHERE clause of EVENT_PRODUCER table. 
	 * @param eventProducerCondition condition to be applied to the event producer table
	 */
	public void setEventProducerWhere(ICondition eventProducerCondition) {
		where = true;
		this.eventProducerWhere = eventProducerCondition;
	}

	/**
	 * Builds a list of Event respecting the condition specified by
	 * elementWhere AND typeWhere AND eventProducerWhere AND parametersConditions.
	 * The different parameter conditions are evaluated in OR,
	 * since they refer to different event types so it makes no sense
	 * having an AND.
	 * @return the event list.
	 * @throws SoCTraceException
	 */
	@Override
	public List<Event> getList() throws SoCTraceException {

		try {
			DeltaManager dm = new DeltaManager();
			dm.start();

			boolean first = true;
			StringBuilder eventQuery = null;
			eventQuery = new StringBuilder("SELECT * FROM " + FramesocTable.EVENT + " ");

			if (where) {
				eventQuery.append(" WHERE ");
			}

			if (elementWhere != null) {
				first = false;
				eventQuery.append(elementWhere.getSQLString());
			}

			if (typeWhere != null) {
				if (!first)
					eventQuery.append(" AND ");
				else
					first = false;
				eventQuery.append("( EVENT_TYPE_ID IN ( SELECT ID FROM " 
						+ FramesocTable.EVENT_TYPE + " WHERE " + typeWhere.getSQLString() + " ) )");
			} 

			if (eventProducerWhere != null) {
				if (!first)
					eventQuery.append(" AND ");
				else
					first = false;
				eventQuery.append("( EVENT_PRODUCER_ID IN ( SELECT ID FROM " 
						+ FramesocTable.EVENT_PRODUCER + " WHERE " + eventProducerWhere.getSQLString() + " ) )");
			} 

			if (parametersConditions.size()>0) {
				if (!first)
					eventQuery.append(" AND ");
				else
					first = false;

				eventQuery.append(getParamConditionsString());
			}

			if (orderBy) {
				eventQuery.append(" ORDER BY " + orderByColumn + " " + orderByCriterium);
			}
			
			if (isLimitSet()) {
				eventQuery.append(" LIMIT " + getLimit());
			}

			String query = eventQuery.toString();
			logger.debug(query);

			Statement stm = dbObj.getConnection().createStatement();

			DeltaManager steps = new DeltaManager();
			steps.start();
			ResultSet rs = stm.executeQuery(query);
			logger.debug(steps.endMessage("Execute query"));
			List<Event> elist = null;
			steps.start();
			elist = rebuildEvents(rs);
			logger.debug(steps.endMessage("Rebuilds events"));

			logger.debug(dm.endMessage("EventQuery.getList()"));
			logger.debug("Results: {}", elist.size());
			
			stm.close();
			return elist;

		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}

	}

	/**
	 * Load the parameters in the passed events.
	 * The events must have no parameters, otherwise
	 * an exception is thrown.
	 * The events must have the event type with all the 
	 * parameter types properly set.
	 * 
	 * @param elist list of events without parameters
	 * @throws SoCTraceException
	 */
	public void loadParams(List<Event> elist) throws SoCTraceException {
		try {
			ValueListString vls = new ValueListString();
			Map<Long, Event> tmp = new HashMap<>();

			for (Event e: elist) {
				tmp.put(e.getId(), e);
				vls.addValue(String.valueOf(e.getId()));
			}

			if (vls.size()==0)
				return;

			Statement stm = dbObj.getConnection().createStatement();
			ResultSet prs = stm.executeQuery("SELECT * FROM " + FramesocTable.EVENT_PARAM + 
					" WHERE EVENT_ID IN " + vls.getValueString());
			while (prs.next()) {
				rebuildEventParam(prs, tmp);
			}
		} catch(SQLException e) {
			throw new SoCTraceException(e);
		}

	}

	/*
	 *      U t i l i t i e s
	 */


	/**
	 * Get the event type id, given the event type name.
	 * 
	 * TODO: this can be optimized with the type cache
	 * @param name event type name
	 * @return the corresponding event type ID 
	 * @throws SoCTraceException
	 */
	private int getEventTypeId(String name) throws SoCTraceException {
		try {
			Statement stm = dbObj.getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SELECT * FROM " 
					+ FramesocTable.EVENT_TYPE + " WHERE NAME='" + name + "'");
			if (rs.next()) {
				return rs.getInt("ID");
			}
			return -1;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Get the event param type with the given name, for the event type 
	 * whose ID is passed.
	 * 
	 * TODO: this can be optimized with the type cache
	 * @param name the event param type name
	 * @param eventTypeId the event type ID 
	 * @return the corresponding event param type
	 * @throws SoCTraceException
	 */
	private ParamType getEventParamType(String name, int eventTypeId) throws SoCTraceException {
		try {
			Statement stm = dbObj.getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SELECT * FROM " + FramesocTable.EVENT_PARAM_TYPE + 
					" WHERE NAME='" + name + "' AND EVENT_TYPE_ID="+eventTypeId);
			if (rs.next()) {
				return new ParamType(rs.getInt("ID"), name, rs.getString("TYPE"));
			}
			return null;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Build the EventProducer object corresponding to the passed ID
	 * @param id event producer ID
	 * @return a EventProducer 
	 * @throws SoCTraceException
	 */
	protected EventProducer getEventProducer(long id) throws SoCTraceException {
		if (eventProducerCache==null) {
			eventProducerCache = new ModelElementCache();
			eventProducerCache.addElementMap(EventProducer.class);
		}

		EventProducer eventProducer;
		if (( eventProducer = eventProducerCache.get(EventProducer.class, id)) != null)
			return eventProducer;

		try {
			Statement stm = dbObj.getConnection().createStatement();
			ResultSet rs = stm.executeQuery("SELECT * FROM " + FramesocTable.EVENT_PRODUCER + " WHERE ID="+id);
			if (rs.next()) {
				eventProducer = new EventProducer(id);
				eventProducer.setType(rs.getString("TYPE"));
				eventProducer.setLocalId(rs.getString("LOCAL_ID"));
				eventProducer.setName(rs.getString("NAME"));
				eventProducer.setParentId(rs.getInt("PARENT_ID"));
				eventProducerCache.put(eventProducer);
				return eventProducer;
			}
			return null;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}	
	}

	/**
	 * Rebuilds the events corresponding to the result set.
	 * @param rs Result set corresponding to a SELECT * FROM EVENT ...
	 * @return a list of Event
	 * @throws SoCTraceException
	 */
	private List<Event> rebuildEvents(ResultSet rs) throws SoCTraceException {

		ValueListString vls = new ValueListString();
		List<Event> list = new LinkedList<Event>();
		Map<Long, Event> tmp = new HashMap<>();
		try {		

			while (rs.next()) {
				Event e = rebuildEvent(rs);
				list.add(e);

				if (loadParameters) {
					// to rebuild all params
					tmp.put(e.getId(), e);
					vls.addValue(String.valueOf(e.getId()));
				}
			}

			if (vls.size()==0)
				return list;

			if (loadParameters) {
				DeltaManager dm = new DeltaManager();
				dm.start();
				Statement stm = dbObj.getConnection().createStatement();
				ResultSet prs = stm.executeQuery("SELECT * FROM " + FramesocTable.EVENT_PARAM + 
						" WHERE EVENT_ID IN " + vls.getValueString());
				while (prs.next()) {
					rebuildEventParam(prs, tmp);
				}
				logger.debug(dm.endMessage("Rebuild parameters and link to events"));
			}
			return list;
		} catch (SQLException e) {
			throw new SoCTraceException(e);
		}
	}

	/**
	 * Rebuild an Event, given the corresponding EVENT table row.
	 * 
	 * @param rs EVENT table row
	 * @param epVls 
	 * @return the Event
	 * @throws SQLException
	 * @throws SoCTraceException
	 */
	private Event rebuildEvent(ResultSet rs) throws SQLException, SoCTraceException {
		int category = rs.getInt(7);
		Event e = Event.createCategorizedEvent(category, rs.getLong(1));
		TraceDBObject traceDB = (TraceDBObject)dbObj;
		EventType et = traceDB.getEventTypeCache().get(EventType.class, rs.getLong(2));
		EventProducer s = getEventProducer(rs.getLong(3));
		e.setEventProducer(s); 
		e.setCategory(rs.getInt(7));
		e.setType(et);
		e.setTimestamp(rs.getLong(4));
		e.setCpu(rs.getInt(5));
		e.setPage(rs.getInt(6));
		e.setLongPar(rs.getLong(8));
		e.setDoublePar(rs.getDouble(9));
		if (e.getCategory() == EventCategory.LINK){
			((Link)e).setEndProducer(getEventProducer(((Double)e.getDoublePar()).intValue()));
		}
		return e;
	}

	/**
	 * Rebuild an EventParam, given the corresponding EVENT_PARAM table row.
	 * @param prs EVENT_PARAM table row
	 * @param tmp map containing the Events returned by the query
	 * @return the EventParam
	 * @throws SQLException
	 * @throws SoCTraceException 
	 */
	private EventParam rebuildEventParam(ResultSet prs, Map<Long, Event> tmp) 
			throws SQLException, SoCTraceException {
		EventParam ep = new EventParam(prs.getLong(1));
		ep.setEvent(tmp.get(prs.getLong(2)));
		TraceDBObject traceDB = (TraceDBObject)dbObj;
		ep.setEventParamType(traceDB.getEventTypeCache().get(EventParamType.class, prs.getLong(3)));
		ep.setValue(prs.getString(4));
		return ep;
	}

}
