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
package fr.inria.soctrace.framesoc.ui.eventtable.view;

import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Link;
import fr.inria.soctrace.lib.model.PunctualEvent;
import fr.inria.soctrace.lib.model.State;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceParam;
import fr.inria.soctrace.lib.model.TraceParamType;
import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.Variable;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.ModelConstants.TimeUnit;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.IdManager;

class ModelFactory  {
	
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
	
	public Event createEvent() {
		IdManager eId = new IdManager();
		IdManager epId = new IdManager();
		return createEvent(eId, epId, createEventType(0, EventCategory.PUNCTUAL_EVENT, new IdManager()), createEventProducer());
	}

	public Event createEvent(IdManager eId, IdManager epId) {
		return createEvent(eId, epId, createEventType(0, EventCategory.PUNCTUAL_EVENT, new IdManager()), createEventProducer());
	}

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


	public Trace createTrace() {
		IdManager tId = new IdManager();
		IdManager tpId = new IdManager();
		return createTrace(tId, tpId, createTraceType());
	}

	public Trace createTrace(IdManager tId, IdManager tpId) {
		return createTrace(tId, tpId, createTraceType());
	}
	
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
	
	// utilities

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
