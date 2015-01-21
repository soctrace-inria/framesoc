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
package fr.inria.soctrace.lib.search;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.AnalysisResultData;
import fr.inria.soctrace.lib.model.AnalysisResultData.AnalysisResultType;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.File;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.AnalysisResultAnnotationDataQuery;
import fr.inria.soctrace.lib.query.AnalysisResultGroupDataQuery;
import fr.inria.soctrace.lib.query.AnalysisResultProcessedTraceDataQuery;
import fr.inria.soctrace.lib.query.AnalysisResultQuery;
import fr.inria.soctrace.lib.query.AnalysisResultSearchDataQuery;
import fr.inria.soctrace.lib.query.EventProducerQuery;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.FileQuery;
import fr.inria.soctrace.lib.query.ToolQuery;
import fr.inria.soctrace.lib.query.TraceQuery;
import fr.inria.soctrace.lib.query.ValueListString;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.LogicalOperation;
import fr.inria.soctrace.lib.query.conditions.LogicalCondition;
import fr.inria.soctrace.lib.query.conditions.ParamLogicalCondition;
import fr.inria.soctrace.lib.query.conditions.ParamSimpleCondition;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.search.utils.EventProducerDesc;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.lib.search.utils.ParamDesc;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;


/**
 * Implementation of the {@link ITraceSearch} interface.
 * 
 * <p>
 * It must be used following the protocol described in 
 * {@link ITraceSearch} documentation.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceSearch implements ITraceSearch {

	/*
	 * DB Objects used to implement a simple caching strategy.
	 * If you have to do sequential operations, you may avoid
	 * opening/closing the connection each time.
	 * Note that some DBMS do not allow multiple writers (a second
	 * connection cannot write if a previous one is still open).
	 * If you have a similar situation, uninitialize the search
	 * interface before the next write. 
	 */
	private SystemDBObject sysDB = null;
	protected TraceDBObject traceDB = null; //XXX changed by Damien

	@Override
	public ITraceSearch initialize() throws SoCTraceException {
		Configuration conf = Configuration.getInstance();
		if (sysDB!=null)
			return this;
		sysDB = new SystemDBObject(conf.get(SoCTraceProperty.soctrace_db_name), DBMode.DB_OPEN);
		return this;
	}

	@Override
	public void uninitialize() throws SoCTraceException {
		if (sysDB != null) {
			sysDB.close();
			sysDB=null;
		}
		if (traceDB != null) {
			traceDB.close();
			traceDB = null;
		}
	}

	/**
	 * Static utility to uninitialize the search object.
	 * To be used in a finally block.
	 * 
	 * @param search search object to uninitialize
	 */
	public static void finalUninitialize(ITraceSearch search) {
		try {
			if (search!=null) {
				search.uninitialize();
			} 
		} catch(SoCTraceException e) {
			e.printStackTrace();
		}
	}

	/*
	 *      T o o l s
	 */

	@Override
	public Tool getToolByName(String name) throws SoCTraceException {
		checkSystemDBObject();
		ToolQuery query = new ToolQuery(sysDB);
		query.setElementWhere(new SimpleCondition("NAME", ComparisonOperation.EQ, name));
		List<Tool> tlist = query.getList();
		if (tlist == null) // XXX cannot return null!
			return null;
		if (tlist.size() > 1)
			throw new SoCTraceException("System Error: more than one tool with the same name");
		if (tlist.size() == 0)
			return null;
		return tlist.get(0);
	}

	@Override
	public List<Tool> getToolByType(String toolType) throws SoCTraceException {
		checkSystemDBObject();
		ToolQuery query = new ToolQuery(sysDB);
		query.setElementWhere(new SimpleCondition("TYPE", ComparisonOperation.EQ, toolType));
		return query.getList();
	}

	@Override
	public List<Tool> getTools() throws SoCTraceException {
		checkSystemDBObject();
		ToolQuery query = new ToolQuery(sysDB);
		return query.getList();		
	}

	/*
	 *      T r a c e s 
	 */

	@Override
	public Trace getTraceByDBName(String dbName) throws SoCTraceException {
		checkSystemDBObject();
		TraceQuery query = new TraceQuery(sysDB);
		query.setElementWhere(new SimpleCondition("TRACE_DB_NAME", ComparisonOperation.EQ, dbName));
		List<Trace> tlist = query.getList();
		if (tlist == null)
			return null;
		if (tlist.size() > 1)
			throw new SoCTraceException("System Error: more than one trace with the same DB name");
		if (tlist.size() == 0)
			return null;
		return tlist.iterator().next();
	}

	@Override
	public List<Trace> getTracesByTracingDate(long startDate, long endDate)
			throws SoCTraceException {
		checkSystemDBObject();
		TraceQuery query = new TraceQuery(sysDB);
		LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		and.addCondition(new SimpleCondition("TRACING_DATE", ComparisonOperation.GE, new Timestamp(startDate).toString()));
		and.addCondition(new SimpleCondition("TRACING_DATE", ComparisonOperation.LE, new Timestamp(endDate).toString()));
		query.setElementWhere(and);
		return query.getList();
	}

	@Override
	public List<Trace> getTracesByTracedApplication(String application)
			throws SoCTraceException {
		checkSystemDBObject();
		TraceQuery query = new TraceQuery(sysDB);
		query.setElementWhere(new SimpleCondition("TRACED_APPLICATION", ComparisonOperation.EQ, application));
		return query.getList();
	}

	@Override
	public List<Trace> getTracesByBoard(String board) throws SoCTraceException {
		checkSystemDBObject();
		TraceQuery query = new TraceQuery(sysDB);
		query.setElementWhere(new SimpleCondition("BOARD", ComparisonOperation.EQ, board));
		return query.getList();
	}

	@Override
	public List<Trace> getTracesByOperatingSystem(String operatingSystem)
			throws SoCTraceException {
		checkSystemDBObject();
		TraceQuery query = new TraceQuery(sysDB);
		query.setElementWhere(new SimpleCondition("OPERATING_SYSTEM", ComparisonOperation.EQ, operatingSystem));
		return query.getList();
	}

	@Override
	public List<Trace> getTracesByNumberOfCpus(int numberOfCpus)
			throws SoCTraceException {
		checkSystemDBObject();
		TraceQuery query = new TraceQuery(sysDB);
		query.setElementWhere(new SimpleCondition("NUMBER_OF_CPUS", ComparisonOperation.EQ, String.valueOf(numberOfCpus)));
		return query.getList();
	}

	@Override
	public List<Trace> getTracesByOutputDevice(String outputDevice)
			throws SoCTraceException {
		checkSystemDBObject();
		TraceQuery query = new TraceQuery(sysDB);
		query.setElementWhere(new SimpleCondition("OUTPUT_DEVICE", ComparisonOperation.EQ, outputDevice));
		return query.getList();
	}

	@Override
	public List<Trace> getTracesByDescription(String description) 
			throws SoCTraceException {
		checkSystemDBObject();
		TraceQuery query = new TraceQuery(sysDB);
		query.setElementWhere(new SimpleCondition("DESCRIPTION", ComparisonOperation.EQ, description));
		return query.getList();		
	}

	@Override
	public List<Trace> getTracesByType(String traceType)
			throws SoCTraceException {
		checkSystemDBObject();
		TraceQuery query = new TraceQuery(sysDB);
		query.setTypeWhere(new SimpleCondition("NAME", ComparisonOperation.EQ, traceType));
		return query.getList();
	}

	@Override
	public List<Trace> getTracesByTypes(List<String> traceTypes)
			throws SoCTraceException {

		if (traceTypes == null || traceTypes.size() == 0)
			return new LinkedList<Trace>();

		if (traceTypes.size() == 1)
			return getTracesByType(traceTypes.get(0));

		checkSystemDBObject();
		TraceQuery query = new TraceQuery(sysDB);
		LogicalCondition or = new LogicalCondition(LogicalOperation.OR);
		for (String s: traceTypes) {
			or.addCondition(new SimpleCondition("NAME", ComparisonOperation.EQ, s));
		}
		query.setTypeWhere(or);
		return query.getList();
	}

	@Override
	public List<Trace> getTracesByParam(String traceType, ParamDesc param)
			throws SoCTraceException {
		checkSystemDBObject();
		TraceQuery query = new TraceQuery(sysDB);
		query.addParamCondition(traceType, new ParamSimpleCondition(param.name, ComparisonOperation.EQ, param.value));
		return query.getList();
	}

	@Override
	public List<Trace> getTracesByParams(String traceType, List<ParamDesc> params) 
			throws SoCTraceException {

		if (params == null || params.size() == 0)
			return new LinkedList<Trace>();

		if(params.size()==1)
			return getTracesByParam(traceType, params.get(0));

		checkSystemDBObject();
		TraceQuery query = new TraceQuery(sysDB);
		ParamLogicalCondition and = new ParamLogicalCondition(LogicalOperation.AND);
		for (ParamDesc nv: params) {
			and.addCondition(new ParamSimpleCondition(nv.name, ComparisonOperation.EQ, nv.value));
		}
		query.addParamCondition(traceType, and);
		return query.getList();
	}

	@Override
	public List<Trace> getTraces() throws SoCTraceException {
		checkSystemDBObject();
		TraceQuery query = new TraceQuery(sysDB);
		return query.getList();
	}

	@Override
	public List<Trace> getRawTraces() throws SoCTraceException {
		checkSystemDBObject();
		TraceQuery query = new TraceQuery(sysDB);
		query.setElementWhere(new SimpleCondition("PROCESSED", ComparisonOperation.EQ, "0"));
		return query.getList();
	}

	@Override
	public List<Trace> getProcessedTraces() throws SoCTraceException {
		checkSystemDBObject();
		TraceQuery query = new TraceQuery(sysDB);
		query.setElementWhere(new SimpleCondition("PROCESSED", ComparisonOperation.EQ, "1"));
		return query.getList();
	}

	/*
	 *      F i l e s  
	 */

	@Override
	public List<File> getFiles(Trace t) throws SoCTraceException {
		openTraceDBObject(t);
		FileQuery query = new FileQuery(traceDB);
		return query.getList();		
	}

	/*
	 *      E v e n t    P r o d u c e r s
	 */

	@Override
	public EventProducer getEventProducer(Trace t, EventProducerDesc eventProducer) throws SoCTraceException {
		openTraceDBObject(t);
		EventProducerQuery query = new EventProducerQuery(traceDB);
		query.setElementWhere(buildProducerCondition(eventProducer));
		List<EventProducer> slist = query.getList();
		if (slist.size() > 1)
			throw new SoCTraceException("System Error: more than one EventProducer with the same <TYPE, LOCAL_ID> pair");
		if (slist.size() == 0)
			return null;
		return slist.get(0);
	}

	@Override
	public List<EventProducer> getEventProducersByType(Trace t, String eventProducerType) throws SoCTraceException {
		openTraceDBObject(t);
		EventProducerQuery query = new EventProducerQuery(traceDB);
		query.setElementWhere(new SimpleCondition("TYPE", ComparisonOperation.EQ, eventProducerType));
		return query.getList();
	}

	@Override
	public List<EventProducer> getEventProducers(Trace t) throws SoCTraceException {
		openTraceDBObject(t);
		EventProducerQuery query = new EventProducerQuery(traceDB);
		return query.getList();		
	}

	/*
	 *      E v e n t s 
	 */

	@Override
	public long getMinTimestamp(Trace t) throws SoCTraceException {
		openTraceDBObject(t);
		return traceDB.getMinTimestamp();
	}

	@Override
	public long getMaxTimestamp(Trace t) throws SoCTraceException {
		openTraceDBObject(t);
		return traceDB.getMaxTimestamp();
	}

	@Override
	public List<Event> getEventsByCpu(Trace t, int cpu) throws SoCTraceException {
		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("CPU", ComparisonOperation.EQ, String.valueOf(cpu)));
		return query.getList();		
	}

	@Override
	public List<Event> getEventsByTypeName(Trace t, String eventType)
			throws SoCTraceException {
		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);
		query.setTypeWhere(new SimpleCondition("NAME", ComparisonOperation.EQ, eventType));
		return query.getList();		
	}

	@Override
	public List<Event> getEventsByTypeNames(Trace t, List<String> eventTypes)
			throws SoCTraceException {

		if (eventTypes == null || eventTypes.size() == 0)
			return new LinkedList<Event>();

		if (eventTypes.size() == 1)
			return getEventsByTypeName(t, eventTypes.get(0));

		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);
		LogicalCondition or = new LogicalCondition(LogicalOperation.OR);
		for (String s: eventTypes) {
			or.addCondition(new SimpleCondition("NAME", ComparisonOperation.EQ, s));
		}
		query.setTypeWhere(or);
		return query.getList();
	}

	@Override
	public List<Event> getEventsByEventType(Trace t, EventType eventType)
			throws SoCTraceException {
		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("EVENT_TYPE_ID", ComparisonOperation.EQ, String.valueOf(eventType.getId())));
		return query.getList();		
	}

	@Override
	public List<Event> getEventsByEventTypes(Trace t, List<EventType> eventTypes)
			throws SoCTraceException {

		if (eventTypes == null || eventTypes.size() == 0)
			return new LinkedList<Event>();

		if (eventTypes.size() == 1)
			return getEventsByEventType(t, eventTypes.get(0));

		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);
		ValueListString vls = new ValueListString();
		for (EventType et: eventTypes) {
			vls.addValue(String.valueOf(et.getId()));
		}
		query.setElementWhere(new SimpleCondition("EVENT_TYPE_ID", ComparisonOperation.IN, vls.getValueString()));
		return query.getList();
	}

	@Override
	public List<Event> getEventsByEventProducerDesc(Trace t, EventProducerDesc eventProducer) 
			throws SoCTraceException {
		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);
		query.setEventProducerWhere(buildProducerCondition(eventProducer));
		return query.getList();
	}

	@Override
	public List<Event> getEventsByEventProducerDescs(Trace t, List<EventProducerDesc> eventProducers) 
			throws SoCTraceException {

		if (eventProducers == null || eventProducers.size() == 0)
			return new LinkedList<Event>();

		if (eventProducers.size() == 1)
			return getEventsByEventProducerDesc(t, eventProducers.get(0));

		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);
		LogicalCondition or = new LogicalCondition(LogicalOperation.OR);
		for (EventProducerDesc eventProducer: eventProducers) {
			or.addCondition(buildProducerCondition(eventProducer));
		}
		query.setEventProducerWhere(or);
		return query.getList();
	}

	@Override
	public List<Event> getEventsByEventProducer(Trace t,
			EventProducer eventProducer) throws SoCTraceException {
		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.EQ, String.valueOf(eventProducer.getId())));
		return query.getList();		
	}

	@Override
	public List<Event> getEventsByEventProducers(Trace t,
			List<EventProducer> eventProducers) throws SoCTraceException {

		if (eventProducers == null || eventProducers.size() == 0)
			return new LinkedList<Event>();

		if (eventProducers.size() == 1)
			return getEventsByEventProducer(t, eventProducers.get(0));

		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);
		ValueListString vls = new ValueListString();
		for (EventProducer et: eventProducers) {
			vls.addValue(String.valueOf(et.getId()));
		}
		query.setElementWhere(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.IN, vls.getValueString()));
		return query.getList();
	}

	@Override
	public List<Event> getEventsByInterval(Trace t, IntervalDesc interval)
			throws SoCTraceException {
		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);
		query.setElementWhere(buildIntervalCondition(interval));
		return query.getList();
	}

	@Override
	public List<Event> getEventsByIntervals(Trace t, List<IntervalDesc> intervals)
			throws SoCTraceException {

		if (intervals == null || intervals.size() == 0)
			return new LinkedList<Event>();

		if (intervals.size() == 1)
			return getEventsByInterval(t, intervals.get(0));

		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);
		LogicalCondition or = new LogicalCondition(LogicalOperation.OR);
		for (IntervalDesc interval: intervals) {
			or.addCondition(buildIntervalCondition(interval));
		}
		query.setElementWhere(or);
		return query.getList();
	}

	@Override
	public List<Event> getEventsByParam(Trace t, String eventType, ParamDesc param) 
			throws SoCTraceException {
		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);
		query.addParamCondition(eventType, new ParamSimpleCondition(param.name, ComparisonOperation.EQ, param.value));
		return query.getList();
	}

	@Override
	public List<Event> getEventsByParams(Trace t, String eventType,
			List<ParamDesc> params) throws SoCTraceException {

		if (params == null || params.size() == 0)
			return new LinkedList<Event>();

		if (params.size() == 1)
			return getEventsByParam(t, eventType, params.get(0));

		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);
		ParamLogicalCondition and = new ParamLogicalCondition(LogicalOperation.AND);
		for (ParamDesc nv: params) {
			and.addCondition(new ParamSimpleCondition(nv.name, ComparisonOperation.EQ, nv.value));
		}
		query.addParamCondition(eventType, and);
		return query.getList();
	}

	@Override
	public List<Event> getEventsByParamsAndIntervals(Trace t, String eventType,
			List<ParamDesc> params, List<IntervalDesc> intervals)
					throws SoCTraceException {

		if (params == null || params.size() == 0)
			return new LinkedList<Event>();

		if (intervals == null || intervals.size() == 0)
			return new LinkedList<Event>();

		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);

		// params
		if (params.size() == 1) {
			ParamDesc param = params.get(0);
			query.addParamCondition(eventType, new ParamSimpleCondition(param.name, ComparisonOperation.EQ, param.value));
		} else {
			ParamLogicalCondition pAnd = new ParamLogicalCondition(LogicalOperation.AND);
			for (ParamDesc nv: params) {
				pAnd.addCondition(new ParamSimpleCondition(nv.name, ComparisonOperation.EQ, nv.value));
			}
			query.addParamCondition(eventType, pAnd);
		}

		// intervals
		if (intervals.size() == 1) {
			IntervalDesc interval = intervals.get(0);
			query.setElementWhere(buildIntervalCondition(interval));
		} else {
			LogicalCondition iOr = new LogicalCondition(LogicalOperation.OR);
			for (IntervalDesc interval: intervals) {
				iOr.addCondition(buildIntervalCondition(interval));
			}
			query.setElementWhere(iOr);
		}
		return query.getList();
	}

	@Override
	public List<Event> getEventsByTypeNamesAndIntervalsAndEventProducerDescs(Trace t, List<String> eventTypes,
			List<IntervalDesc> intervals, List<EventProducerDesc> eventProducers) throws SoCTraceException {
		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);

		// types
		if (eventTypes!=null) {
			if (eventTypes.size() == 0)
				return new LinkedList<Event>();
			if (eventTypes.size() == 1) {
				String s = eventTypes.get(0);
				query.setTypeWhere(new SimpleCondition("NAME", ComparisonOperation.EQ, s));
			} else {
				LogicalCondition tOr = new LogicalCondition(LogicalOperation.OR);
				for (String s: eventTypes) {
					tOr.addCondition(new SimpleCondition("NAME", ComparisonOperation.EQ, s));
				}
				query.setTypeWhere(tOr);				
			}
		}

		// intervals
		if (intervals!=null) {
			if (intervals.size() == 0)
				return new LinkedList<Event>();
			if (intervals.size() == 1) {
				IntervalDesc interval = intervals.get(0);
				query.setElementWhere(buildIntervalCondition(interval));
			} else {
				LogicalCondition iOr = new LogicalCondition(LogicalOperation.OR);
				for (IntervalDesc interval: intervals) {
					iOr.addCondition(buildIntervalCondition(interval));
				}
				query.setElementWhere(iOr);
			}
		}

		// eventProducers
		if (eventProducers!=null) {
			if (eventProducers.size() == 0)
				return new LinkedList<Event>();
			if (eventProducers.size() == 1) {
				EventProducerDesc eventProducer = eventProducers.get(0);
				query.setEventProducerWhere(buildProducerCondition(eventProducer));
			} else {
				LogicalCondition sOr = new LogicalCondition(LogicalOperation.OR);
				for (EventProducerDesc eventProducer: eventProducers) {
					sOr.addCondition(buildProducerCondition(eventProducer));
				}
				query.setEventProducerWhere(sOr);							
			}
		}

		return query.getList();
	}

	@Override
	public List<Event> getEventsByEventTypesAndIntervalsAndEventProducers(
			Trace t, List<EventType> eventTypes, List<IntervalDesc> intervals,
			List<EventProducer> eventProducers) throws SoCTraceException {
		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);

		LogicalCondition and = new LogicalCondition(LogicalOperation.AND);

		// types
		if (eventTypes!=null) {
			if (eventTypes.size() == 0)
				return new LinkedList<Event>();
			ValueListString vls = new ValueListString();
			for (EventType et : eventTypes) {
				vls.addValue(String.valueOf(et.getId()));
			}
			and.addCondition(new SimpleCondition("EVENT_TYPE_ID", ComparisonOperation.IN, vls.getValueString()));
		}

		// intervals
		if (intervals!=null) {
			if (intervals.size() == 0)
				return new LinkedList<Event>();
			if (intervals.size() == 1) {
				IntervalDesc interval = intervals.get(0);
				and.addCondition(buildIntervalCondition(interval));
			} else {
				LogicalCondition iOr = new LogicalCondition(LogicalOperation.OR);
				for (IntervalDesc interval: intervals) {
					iOr.addCondition(buildIntervalCondition(interval));
				}
				and.addCondition(iOr);
			}
		}

		// eventProducers
		if (eventProducers!=null) {
			if (eventProducers.size() == 0)
				return new LinkedList<Event>();
			ValueListString vls = new ValueListString();
			for (EventProducer ep : eventProducers) {
				vls.addValue(String.valueOf(ep.getId()));
			}
			and.addCondition(new SimpleCondition("EVENT_PRODUCER_ID", ComparisonOperation.IN, vls.getValueString()));
		}

		query.setElementWhere(and);
		return query.getList();
	}

	@Override
	public List<Event> getEventsByPage(Trace t, Integer page) throws SoCTraceException {
		openTraceDBObject(t);
		EventQuery query = new EventQuery(traceDB);
		query.setElementWhere(new SimpleCondition("PAGE", ComparisonOperation.EQ, page.toString()));
		return query.getList();		
	}

	/*
	 *      A n a l y s i s    R e s u l t
	 */

	@Override
	public List<AnalysisResult> getAnalysisResults(Trace t) throws SoCTraceException {
		openTraceDBObject(t);
		AnalysisResultQuery query = new AnalysisResultQuery(traceDB);		
		return query.getList();
	}

	@Override
	public List<AnalysisResult> getAnalysisResultsByType(Trace t, AnalysisResultType type) throws SoCTraceException {
		openTraceDBObject(t);
		AnalysisResultQuery query = new AnalysisResultQuery(traceDB);
		query.setElementWhere(new SimpleCondition("TYPE", ComparisonOperation.EQ, type.toString()));
		return query.getList();
	}

	@Override
	public List<AnalysisResult> getAnalysisResultsByTool(Trace t, Tool tool) throws SoCTraceException {
		openTraceDBObject(t);
		AnalysisResultQuery query = new AnalysisResultQuery(traceDB);		
		query.setElementWhere(new SimpleCondition("TOOL_ID", ComparisonOperation.EQ, String.valueOf(tool.getId())));
		return query.getList();
	}

	@Override
	public List<AnalysisResult> getAnalysisResultsByToolAndType(Trace t, Tool tool, AnalysisResultType type) throws SoCTraceException {
		openTraceDBObject(t);
		AnalysisResultQuery query = new AnalysisResultQuery(traceDB);
		LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		and.addCondition(new SimpleCondition("TOOL_ID", ComparisonOperation.EQ, String.valueOf(tool.getId())));
		and.addCondition(new SimpleCondition("TYPE", ComparisonOperation.EQ, type.toString()));
		query.setElementWhere(and);
		return query.getList();
	}

	@Override
	public AnalysisResultData getAnalysisResultData(Trace t, AnalysisResult analysisResult) throws SoCTraceException {
		checkSystemDBObject();
		openTraceDBObject(t);
		AnalysisResultData data = null;
		String type = analysisResult.getType(); 
		if (type.equals(AnalysisResultType.TYPE_SEARCH.toString())) {
			AnalysisResultSearchDataQuery query = new AnalysisResultSearchDataQuery(traceDB);
			data = query.getAnalysisResultData(analysisResult.getId());
		} else if (type.equals(AnalysisResultType.TYPE_GROUP.toString())) {
			AnalysisResultGroupDataQuery query = new AnalysisResultGroupDataQuery(traceDB);
			data = query.getAnalysisResultData(analysisResult.getId());
		} else if (type.equals(AnalysisResultType.TYPE_ANNOTATION.toString())) {
			AnalysisResultAnnotationDataQuery query = new AnalysisResultAnnotationDataQuery(traceDB);
			data = query.getAnalysisResultData(analysisResult.getId());
		} else if (type.equals(AnalysisResultType.TYPE_PROCESSED_TRACE.toString())) {
			AnalysisResultProcessedTraceDataQuery query = new AnalysisResultProcessedTraceDataQuery(traceDB, sysDB, t);
			data = query.getAnalysisResultData(analysisResult.getId());
		}
		analysisResult.setData(data);
		return data;
	}

	/*
	 *      U t i l i t i e s 
	 */

	/**
	 * Check if the search object has been initialized, 
	 * that is, if the System DB object has been opened.
	 * This check is necessary only for the request on the 
	 * System DB.
	 * 
	 * @throws SoCTraceException
	 */
	private void checkSystemDBObject() throws SoCTraceException {
		if (sysDB == null)
			throw new SoCTraceException("SearchTrace object not initialized. Call initialize()!");
	}

	/**
	 * Open the Trace DB related to the Trace object passed.
	 * A very simple caching strategy is implemented: the trace DB
	 * object is not closed after a request, so if the following
	 * request uses the same DB as the previous one, the DB connection
	 * is not opened again. Otherwise, the old connection is closed.
	 * 
	 * @param t Trace object
	 * @throws SoCTraceException
	 */
	private void openTraceDBObject(Trace t) throws SoCTraceException {
		if (traceDB != null) {
			if (traceDB.getDBName() == t.getDbName())
				return; // correct trace DB already opened
			else
				traceDB.close(); // close the current trace DB
		}

		traceDB = new TraceDBObject(t.getDbName(), DBMode.DB_OPEN);
	}

	/**
	 * Build the logical condition for the Event table, corresponding 
	 * to a given time interval (t1 <= timestamp < t2).
	 * 
	 * @param interval interval descriptor
	 * @return the logical condition 
	 */
	private LogicalCondition buildIntervalCondition(IntervalDesc interval) {
		LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.GE, String.valueOf(interval.t1)));
		and.addCondition(new SimpleCondition("TIMESTAMP", ComparisonOperation.LT, String.valueOf(interval.t2)));
		return and;
	}

	/**
	 * Build the logical condition for the EventProducer table, corresponding
	 * to a given producer (type == t AND local_id == i).
	 * 
	 * @param eventProducer event producer descriptor
	 * @return the logical condition
	 */
	LogicalCondition buildProducerCondition(EventProducerDesc eventProducer) {
		LogicalCondition and = new LogicalCondition(LogicalOperation.AND);
		and.addCondition(new SimpleCondition("TYPE", ComparisonOperation.EQ, eventProducer.type));
		and.addCondition(new SimpleCondition("LOCAL_ID", ComparisonOperation.EQ, eventProducer.local_id));
		return and;
	}

	/*===================*/

	/**
	 * Getter for the Trace DB object.
	 * 
	 * @return the DB object
	 * @throws SoCTraceException 
	 */
	public TraceDBObject getTraceDBObject(Trace t) throws SoCTraceException {
		openTraceDBObject(t);
		return traceDB;
	}

	/**
	 * Getter for the System DB object.
	 * 
	 * @return the DB object
	 * @throws SoCTraceException 
	 */
	public SystemDBObject getSystemDBObject() throws SoCTraceException {
		checkSystemDBObject();
		return sysDB;
	}

}
