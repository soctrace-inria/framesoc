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
package fr.inria.soctrace.test.junit.utils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.framesoc.core.FramesocConstants;
import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.AnalysisResultAnnotationData;
import fr.inria.soctrace.lib.model.AnalysisResultData.AnalysisResultType;
import fr.inria.soctrace.lib.model.AnalysisResultGroupData;
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
import fr.inria.soctrace.lib.model.Link;
import fr.inria.soctrace.lib.model.OrderedGroup;
import fr.inria.soctrace.lib.model.PunctualEvent;
import fr.inria.soctrace.lib.model.State;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceParam;
import fr.inria.soctrace.lib.model.TraceParamType;
import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.UnorderedGroup;
import fr.inria.soctrace.lib.model.Variable;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.IdManager;

/**
 * Create Self Contained model elements that can be used 
 * to test write/read operation on clean DBs.
 * 
 * All create methods create an element of the model
 * with all its dependencies: e.g. a group result and 
 * all the entities (events, event types) being grouped.
 * 
 * The user must save all the dependencies in order
 * to correctly retrieve an element of the model:
 * e.g. if a group must be saved, the entities being
 * grouped must be saved as well.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
class ModelFactory implements IModelFactory {
	
	@Override
	public Event createEvent(int category) {
		IdManager eId = new IdManager();
		IdManager epId = new IdManager();
		switch(category) {
		case EventCategory.PUNCTUAL_EVENT:
			return createPunctualEvent(eId, epId, createEventType(0, EventCategory.PUNCTUAL_EVENT, new IdManager()), createEventProducer());
		case EventCategory.LINK:
			return createLink(eId, epId, createEventType(0, EventCategory.LINK, new IdManager()), createEventProducer());
		case EventCategory.STATE:
			return createState(eId, epId, createEventType(0, EventCategory.STATE, new IdManager()), createEventProducer());
		case EventCategory.VARIABLE:
			return createVariable(eId, epId, createEventType(0, EventCategory.VARIABLE, new IdManager()), createEventProducer());
		}
		return null;
	}
	
	@Override
	public Event createEvent() {
		IdManager eId = new IdManager();
		IdManager epId = new IdManager();
		return createEvent(eId, epId, createEventType(0, EventCategory.PUNCTUAL_EVENT, new IdManager()), createEventProducer());
	}

	@Override
	public Event createEvent(IdManager eId, IdManager epId) {
		return createEvent(eId, epId, createEventType(0, EventCategory.PUNCTUAL_EVENT, new IdManager()), createEventProducer());
	}

	@Override
	public List<Event> createEvents(int n) {
		IdManager eId = new IdManager();
		IdManager epId = new IdManager();
		EventType et = createEventType(0, EventCategory.PUNCTUAL_EVENT, new IdManager());
		EventProducer ep = createEventProducer();
		List<Event> elist = new LinkedList<Event>();
		for (int i=0; i<n; ++i) {
			elist.add(createEvent(eId, epId, et, ep));
		}
		return elist;
	}
	
	@Override
	public List<Event> createCategorizedEvents(int n) {
		IdManager eId = new IdManager();
		IdManager epId = new IdManager();
		EventProducer ep = createEventProducer();
		List<Event> elist = new LinkedList<Event>();
		EventType et = null;
		IdManager etdm = new IdManager();
		IdManager eptIdm = new IdManager();
		et = createEventType(etdm.getNextId(), EventCategory.PUNCTUAL_EVENT, eptIdm);
		for (int i=0; i<n; ++i) {
			elist.add(createPunctualEvent(eId, epId, et, ep));
		}
		et = createEventType(etdm.getNextId(), EventCategory.STATE, eptIdm);
		for (int i=0; i<n; ++i) {
			elist.add(createState(eId, epId, et, ep));
		}
		et = createEventType(etdm.getNextId(), EventCategory.VARIABLE, eptIdm);
		for (int i=0; i<n; ++i) {
			elist.add(createVariable(eId, epId, et, ep));
		}
		et = createEventType(etdm.getNextId(), EventCategory.LINK, eptIdm);
		for (int i=0; i<n; ++i) {
			elist.add(createLink(eId, epId, et, ep));
		}
		return elist;
	}


	@Override
	public Trace createTrace() {
		IdManager tId = new IdManager();
		IdManager tpId = new IdManager();
		return createTrace(tId, tpId, createTraceType());
	}

	@Override
	public Trace createTrace(IdManager tId, IdManager tpId) {
		return createTrace(tId, tpId, createTraceType());
	}

	@Override
	public List<Trace> createTraces(int n) {
		IdManager tId = new IdManager();
		IdManager tpId = new IdManager();
		TraceType tt = createTraceType();
		List<Trace> tlist = new LinkedList<Trace>();
		for (int i=0; i<n; ++i) {
			tlist.add(createTrace(tId, tpId, tt));
		}
		return tlist;
	}
	
	@Override
	public AnalysisResult createTraceResult(IdManager aid, String desc) {
		AnalysisResult ar = initAnalysisResult(aid, desc);
		ar.setType(AnalysisResultType.TYPE_PROCESSED_TRACE.toString());
		
		// RESULT DATA
		AnalysisResultProcessedTraceData processedTraceData = new AnalysisResultProcessedTraceData();
		processedTraceData.setSourceTrace(ResourceLoader.getVirtualTrace());
		processedTraceData.setProcessedTrace(ResourceLoader.getJunitProcessedTrace());
		ar.setData(processedTraceData);

		return ar;
	}

	@Override
	public AnalysisResult createSearchResult(IdManager aid, String desc) {
		AnalysisResult ar = initAnalysisResult(aid, desc);
		ar.setType(AnalysisResultType.TYPE_SEARCH.toString());
		
		// RESULT DATA
		try {
			AnalysisResultSearchData searchData = new AnalysisResultSearchData(Event.class);
			searchData.setSearchCommand("search result events");
			searchData.setElements(createEvents(10));
			ar.setData(searchData);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}

		return ar;
	}

	/**
	 * ROOT
	 * - TYPES
	 *   1. type1
	 *   2. type2
	 *   3. EMPTY
	 * - EVENTS
	 *   1. event1
	 *   2. event2
	 *   3. EMPTY
	 */
	@Override
	public AnalysisResult createGroupResult(IdManager aid, String desc) {
		AnalysisResult ar = initAnalysisResult(aid, desc);
		ar.setType(AnalysisResultType.TYPE_GROUP.toString());
		
		// RESULT DATA
		IdManager eIdManager = new IdManager();
		IdManager epIdManager = new IdManager();
		Event e1 = createEvent(eIdManager, epIdManager);
		Event e2 = createEvent(eIdManager, epIdManager);
		EventType et1 = e1.getType();
		EventType et2 = e2.getType();
		
		// Prepare the groups
		try {
			IdManager idManager = new IdManager();
			UnorderedGroup root = new UnorderedGroup(idManager.getNextId(), null);
			root.setName("ROOT");
	
			OrderedGroup pattern = new OrderedGroup(idManager.getNextId(), EventType.class);
			pattern.setName("TYPES");	
			pattern.addSon(et1, 0);
			pattern.addSon(et2, 1);
			UnorderedGroup empty = new UnorderedGroup(idManager.getNextId(), null);
			empty.setName("EMPTY");
			pattern.addSon(empty, 2);
	
			OrderedGroup example = new OrderedGroup(idManager.getNextId(), Event.class);
			example.setName("EVENTS");	
			example.addSon(e1, 0);
			example.addSon(e2, 1);
			empty = new UnorderedGroup(idManager.getNextId(), null);
			empty.setName("EMPTY");
			example.addSon(empty , 2);
			
			root.addSon(pattern);
			root.addSon(example);
	
			AnalysisResultGroupData groupData = new AnalysisResultGroupData(root);
			ar.setData(groupData);
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}

		return ar;
	}

	/**
	 * Create two annotations of the same type.
	 */
	@Override
	public AnalysisResult createAnnotationResult(IdManager aid, String desc) {
		AnalysisResult ar = initAnalysisResult(aid, desc);
		ar.setType(AnalysisResultType.TYPE_ANNOTATION.toString());

		// RESULT DATA
		
		// ANNOTATION TYPE
		IdManager typeIdManager = new IdManager();
		AnnotationType annotationType = new AnnotationType(typeIdManager.getNextId());
		annotationType.setName("DECODING_MEMORY_USAGE");
		
		// ANNOTATION PARAM TYPE
		IdManager paramTypeIdManager = new IdManager();
		AnnotationParamType fx = new AnnotationParamType(paramTypeIdManager.getNextId());
		AnnotationParamType fy = new AnnotationParamType(paramTypeIdManager.getNextId());
		fx.setAnnotationType(annotationType);
		fy.setAnnotationType(annotationType);
		fx.setName("FUNCTION_X_MB");
		fy.setName("FUNCTION_Y_MB");
		fx.setType("INTEGER");
		fy.setType("INTEGER");
		
		// ANNOTATION
		IdManager annotationIdManager = new IdManager();
		Annotation firstCpuAnnotation = new Annotation(annotationIdManager.getNextId());
		Annotation secondCpuAnnotation = new Annotation(annotationIdManager.getNextId());
		firstCpuAnnotation.setAnnotationType(annotationType);
		secondCpuAnnotation.setAnnotationType(annotationType);
		firstCpuAnnotation.setName("CPU0_MEMORY_USAGE");
		secondCpuAnnotation.setName("CPU1_MEMORY_USAGE");

		// ANNOTATION PARAM
		IdManager annotationParamIdManager = new IdManager();
		AnnotationParam firstFx = new AnnotationParam(annotationParamIdManager.getNextId());
		firstFx.setAnnotation(firstCpuAnnotation);
		firstFx.setAnnotationParamType(fx);
		firstFx.setValue("10");
		AnnotationParam firstFy = new AnnotationParam(annotationParamIdManager.getNextId());
		firstFy.setAnnotation(firstCpuAnnotation);
		firstFy.setAnnotationParamType(fy);
		firstFy.setValue("20");
		AnnotationParam secondFx = new AnnotationParam(annotationParamIdManager.getNextId());
		secondFx.setAnnotation(secondCpuAnnotation);
		secondFx.setAnnotationParamType(fx);
		secondFx.setValue("5");
		AnnotationParam secondFy = new AnnotationParam(annotationParamIdManager.getNextId());
		secondFy.setAnnotation(secondCpuAnnotation);
		secondFy.setAnnotationParamType(fy);
		secondFy.setValue("15");
	
		// RESULT DATA
		AnalysisResultAnnotationData annotationData = new AnalysisResultAnnotationData();
		annotationData.addAnnotation(firstCpuAnnotation);
		annotationData.addAnnotation(secondCpuAnnotation);
		ar.setData(annotationData);

		return ar;
	}
	
	@Override
	public Tool createAnalysisTool() {
		Tool t = new Tool(10);
		t.setCommand("tool_command");
		t.setDoc("tool_doc");
		t.setName("tool_name");
		t.setPlugin(false);
		t.setType(FramesocConstants.FramesocToolType.ANALYSIS.toString());
		return t;
	}

	// utilities

	/**
	 * Common analysis result object initialization
	 */
	private AnalysisResult initAnalysisResult(IdManager aid, String desc) {
		AnalysisResult ar = new AnalysisResult(aid.getNextId());
		ar.setTool(ResourceLoader.getJunitTestTool());
		ar.setDescription(desc);
		ar.setDate(new Timestamp(new Date().getTime()));
		return ar;
	}
	
	/**
	 * Create a complete event producer
	 * @return
	 */
	private EventProducer createEventProducer() { 
		EventProducer p = new EventProducer(0);
		p.setLocalId("local_id");
		p.setName("producer_name");
		p.setType("producer_type");
		return p;
	}

	/**
	 * Create a complete event type
	 * @return
	 */
	private EventType createEventType(int id, int category, IdManager eptIdm) {
		EventType et = new EventType(id, category);
		et.setName("event_type_"+id);
		final int n = 2;
		for (int i=0; i<n; i++) {
			EventParamType ept = new EventParamType(eptIdm.getNextId());
			ept.setName("param_type_name_"+i);
			ept.setType("param_type_type_"+i);
			ept.setEventType(et);			
		}
		return et;
	}	
			
	/**
	 * Create a complete event
	 * @return
	 */
	private Event createEvent(IdManager eId, IdManager epId, EventType type, EventProducer prod) {
		Event e = new Event(eId.getNextId());
		e.setCpu(0);
		e.setPage(0);
		e.setTimestamp(0);
		e.setEventProducer(prod);
		try {
			e.setCategory(type.getCategory());
			e.setType(type);
		} catch (SoCTraceException e1) {
			e1.printStackTrace();
		}
		for (EventParamType ept: type.getEventParamTypes()) {
			EventParam ep = new EventParam(epId.getNextId());
			ep.setEventParamType(ept);
			ep.setEvent(e);
			ep.setValue("param_value_"+ep.getId());
		}
		return e;
	}
	
	/**
	 * Create a complete link
	 * @return
	 * @throws SoCTraceException 
	 */
	private Link createLink(IdManager eId, IdManager epId, EventType type, EventProducer prod) {
		Link e = new Link(eId.getNextId());
		e.setCpu(0);
		e.setPage(0);
		e.setTimestamp(0);
		e.setEventProducer(prod);
		try {
			e.setType(type);
		} catch (SoCTraceException e1) {
			e1.printStackTrace();
		}
		e.setEndProducer(prod);
		e.setEndTimestamp(Long.MAX_VALUE);
		return e;
	}
	
	/**
	 * Create a complete punctual event
	 * @return
	 */
	private PunctualEvent createPunctualEvent(IdManager eId, IdManager epId, EventType type, EventProducer prod) {
		PunctualEvent e = new PunctualEvent(eId.getNextId());
		e.setCpu(0);
		e.setPage(0);
		e.setTimestamp(0);
		e.setEventProducer(prod);
		try {
			e.setType(type);
		} catch (SoCTraceException e1) {
			e1.printStackTrace();
		}
		return e;
	}
	
	/**
	 * Create a complete variable
	 * @return
	 */
	private Variable createVariable(IdManager eId, IdManager epId, EventType type, EventProducer prod) {
		Variable e = new Variable(eId.getNextId());
		e.setCpu(0);
		e.setPage(0);
		e.setTimestamp(0);
		e.setEventProducer(prod);
		try {
			e.setType(type);
		} catch (SoCTraceException e1) {
			e1.printStackTrace();
		}
		e.setValue(0.007);
		e.setVariableId(10);
		return e;
	}
	
	/**
	 * Create a complete state
	 * @return
	 */
	private State createState(IdManager eId, IdManager epId, EventType type, EventProducer prod) {
		State e = new State(eId.getNextId());
		e.setCpu(0);
		e.setPage(0);
		e.setTimestamp(0);
		e.setEventProducer(prod);
		try {
			e.setType(type);
		} catch (SoCTraceException e1) {
			e1.printStackTrace();
		}
		e.setImbricationLevel(0);
		e.setEndTimestamp(Long.MAX_VALUE);
		return e;
	}

	/**
	 * Create a complete trace type
	 * @return
	 */
	private TraceType createTraceType() {
		TraceType tt = new TraceType(0);
		tt.setName("trace_type");
		final int n = 2;
		for (int i=0; i<n; i++) {
			TraceParamType tpt = new TraceParamType(i);
			tpt.setName("param_type_name_"+i);
			tpt.setType("param_type_type_"+i);
			tpt.setTraceType(tt);			
		}
		return tt;
	}	
	
	/**
	 * Create a complete trace
	 * @return
	 */
	private Trace createTrace(IdManager tId, IdManager tpId, TraceType type) {
		Trace t = new Trace(tId.getNextId());
		t.setAlias("alias");
		t.setBoard("board");
		t.setDbName("dbname");
		t.setDescription("description");
		t.setNumberOfCpus(1);
		t.setNumberOfEvents(10);
		t.setOperatingSystem("os");
		t.setOutputDevice("device");
		t.setProcessed(false);
		t.setMinTimestamp(0);
		t.setMaxTimestamp(1000000);
		t.setTimeUnit(TimeUnit.NANOSECONDS.getInt());
		t.setTracedApplication("app");
		t.setTracingDate(new Timestamp(new Date().getTime()));
		t.setType(type);
		for (TraceParamType tpt: type.getTraceParamTypes()) {
			TraceParam tp = new TraceParam(tpId.getNextId());
			tp.setTraceParamType(tpt);
			tp.setTrace(t);
			tp.setValue("param_value_"+tp.getId());
		}
		return t;
	}

}
