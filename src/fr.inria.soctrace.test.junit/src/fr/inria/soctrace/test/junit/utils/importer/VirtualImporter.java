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
/**
 * 
 */
package fr.inria.soctrace.test.junit.utils.importer;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.File;
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
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.IdManager;

/**
 * Virtual importer writing into the DB a virtual trace whose parameters may be
 * easily configured.
 * 
 * <pre>
 * Conventions: 
 * - if there are N entities, the IDs range from 0 to N-1 
 * - the name of a Producer ${PRODUCER_NAME_PREFIX}_${ID} 
 * - the name of a *Type is ${TYPE_NAME_PREFIX}_${ID} 
 * - the name of a *Parameter is ${PARAMETER_NAME_PREFIX}_${ID}
 * - the local id of a Producer is ${PRODUCER_LOCAL_ID_PREFIX}_${ID}
 * </pre>
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class VirtualImporter {

	/**
	 * Virtual importer DB name
	 */
	public final static String DB_NAME = "VIRTUAL_IMPORTER_TEST_DB";

	/**
	 * Set of used event categories
	 */
	public final static Set<Integer> CATEGORIES;

	static {
		CATEGORIES = new HashSet<>();
		CATEGORIES.add(EventCategory.STATE);
		CATEGORIES.add(EventCategory.PUNCTUAL_EVENT);
		CATEGORIES.add(EventCategory.LINK);
		CATEGORIES.add(EventCategory.VARIABLE);
	}
	
	/**
	 * Number of producers in the virtual trace
	 */
	public final static int NUMBER_OF_PRODUCERS = 11;

	/**
	 * Number of types for each category for each producer
	 */
	public final static int NUMBER_OF_TYPES = 12;

	/**
	 * Number of events for each type for each category for each producer
	 */
	public final static int NUMBER_OF_EVENTS = 13;

	/**
	 * Number of parameters (for Trace and Event)
	 */
	public final static int NUMBER_OF_PARAMETERS = 2;

	/**
	 * Number of files
	 */
	public final static int NUMBER_OF_FILES = 3;

	/**
	 * Min timestamp
	 */
	public final static long MIN_TIMESTAMP = 0;

	/**
	 * Trace ID
	 */
	public final static int TRACE_ID = 0;

	/**
	 * Trace Type ID
	 */
	public final static int TRACE_TYPE_ID = 0;

	/**
	 * Page
	 */
	public final static int PAGE = 0;

	/**
	 * CPU
	 */
	public final static int CPU = 0;

	/**
	 * String constant to be used for all Trace metadata not known.
	 */
	public final static String METADATA = "VIRTUAL";

	/**
	 * Prefix for type entity names
	 */
	public final static String TYPE_NAME_PREFIX = "TYPE_";

	/**
	 * Prefix for producer names
	 */
	public final static String PRODUCER_NAME_PREFIX = "PRODUCER_";

	/**
	 * Producer type
	 */
	public final static String PRODUCER_TYPE = "VIRTUAL_PRODUCER";

	/**
	 * Producer local id prefix
	 */
	public final static String PRODUCER_LOCAL_ID_PREFIX = "PRODUCER_LOCAL_ID_";

	/**
	 * Prefix for parameter type names
	 */
	public final static String PARAMETER_NAME_PREFIX = "PARAMETER_";

	/**
	 * Prefix for file paths and descriptions
	 */
	public final static String FILE_INFO_PREFIX = "FILE_";

	/**
	 * Type of all parameters
	 */
	public final static String PARAMETER_TYPE = "INTEGER";

	/**
	 * Value of all parameters
	 */
	public final static String PARAMETER_VALUE = "10";

	/**
	 * Duration for entities having an end timestamp.
	 */
	public final static long DURATION = 10;

	/*
	 * Short-cuts
	 */

	public static final int NUMBER_OF_CATEGORIES = CATEGORIES.size();

	public final static String TRACE_TYPE_NAME = VirtualImporter.TYPE_NAME_PREFIX
			+ VirtualImporter.TRACE_TYPE_ID;

	public final static int NUMBER_OF_PARAMETER_TYPES = NUMBER_OF_CATEGORIES * NUMBER_OF_TYPES
			* NUMBER_OF_PARAMETERS;
	
	public final static int TOTAL_NUMBER_OF_EVENTS = NUMBER_OF_PRODUCERS * NUMBER_OF_TYPES
			* NUMBER_OF_EVENTS * NUMBER_OF_CATEGORIES;

	public static int getEventsPerCategory() {
		if (NUMBER_OF_CATEGORIES == 0)
			return 0;
		return TOTAL_NUMBER_OF_EVENTS / CATEGORIES.size();
	}

	public static int getNumberOfEvents(int category) {
		return getEventsPerCategory() * (CATEGORIES.contains(category) ? 1 : 0);
	}

	public static long getMaxTimestamp() {
		int punctuals = getNumberOfEvents(EventCategory.PUNCTUAL_EVENT)
				+ getNumberOfEvents(EventCategory.VARIABLE);
		int nonpunctuals = getNumberOfEvents(EventCategory.STATE)
				+ getNumberOfEvents(EventCategory.LINK);
		return MIN_TIMESTAMP + punctuals + nonpunctuals * (VirtualImporter.DURATION + 1) - 1;
	}

	/**
	 * Current timestamp used during import
	 */
	private long currentTimestamp = MIN_TIMESTAMP;

	/**
	 * Import a virtual trace into a trace DB according to the constants set.
	 * 
	 * @throws SoCTraceException
	 */
	public void virtualImport() throws SoCTraceException {

		/*
		 * Trace events
		 */

		TraceDBObject traceDB = new TraceDBObject(DB_NAME, DBMode.DB_CREATE);

		IdManager eIdManager = new IdManager();
		IdManager etIdManager = new IdManager();
		IdManager epIdManager = new IdManager();
		IdManager eptIdManager = new IdManager();
		IdManager tpIdManager = new IdManager();
		IdManager tptIdManager = new IdManager();
		IdManager producerIdManager = new IdManager();

		// event category, types
		List<List<EventType>> typesList = new ArrayList<>();

		for (Integer category : CATEGORIES) {
			List<EventType> catTypes = createTypes(traceDB, category, etIdManager, eptIdManager);
			typesList.add(catTypes);
		}

		for (int i = 0; i < NUMBER_OF_PRODUCERS; i++) {
			EventProducer ep = new EventProducer(producerIdManager.getNextId());
			ep.setName(PRODUCER_NAME_PREFIX + ep.getId());
			ep.setType(PRODUCER_TYPE);
			ep.setLocalId(PRODUCER_LOCAL_ID_PREFIX + ep.getId());
			traceDB.save(ep);
			for (List<EventType> types : typesList) {
				for (EventType et : types) {
					for (int j = 0; j < NUMBER_OF_EVENTS; j++) {
						createEvent(traceDB, et, ep, eIdManager, epIdManager);
					}
				}
			}
		}
		
		IdManager fileIdManager = new IdManager();
		for (int i=0; i<NUMBER_OF_FILES; i++) {
			File file = new File(fileIdManager.getNextId());
			file.setPath(FILE_INFO_PREFIX+file.getId());
			file.setDescription(FILE_INFO_PREFIX+file.getId());
			traceDB.save(file);
		}

		traceDB.close();

		/*
		 * Trace metadata
		 */

		SystemDBObject sysDB = SystemDBObject.openNewIstance();
		TraceType tt = new TraceType(TRACE_TYPE_ID);
		tt.setName(TYPE_NAME_PREFIX + tt.getId());
		for (int i = 0; i < NUMBER_OF_PARAMETERS; i++) {
			TraceParamType tpt = new TraceParamType(tptIdManager.getNextId());
			tpt.setName(PARAMETER_NAME_PREFIX + tpt.getId());
			tpt.setType(PARAMETER_TYPE);
			tpt.setTraceType(tt);
			sysDB.save(tpt);
		}
		sysDB.save(tt);
		Trace t = new Trace(TRACE_ID);
		t.setAlias(METADATA);
		t.setBoard(METADATA);
		t.setDbName(DB_NAME);
		t.setDescription(METADATA);
		t.setNumberOfCpus(1);
		t.setNumberOfEvents(TOTAL_NUMBER_OF_EVENTS);
		t.setOperatingSystem(METADATA);
		t.setOutputDevice(METADATA);
		t.setProcessed(false);
		t.setMinTimestamp(MIN_TIMESTAMP);
		t.setMaxTimestamp(currentTimestamp-1);
		t.setTimeUnit(TimeUnit.NANOSECONDS.getInt());
		t.setTracedApplication(METADATA);
		t.setTracingDate(new Timestamp(new Date().getTime()));
		t.setType(tt);
		for (TraceParamType tpt : tt.getTraceParamTypes()) {
			TraceParam tp = new TraceParam(tpIdManager.getNextId());
			tp.setTraceParamType(tpt);
			tp.setTrace(t);
			tp.setValue(PARAMETER_VALUE);
			sysDB.save(tp);
		}
		sysDB.save(t);

		sysDB.close();
	}

	private void createEvent(TraceDBObject traceDB, EventType et, EventProducer prod,
			IdManager eIdManager, IdManager epIdManager) throws SoCTraceException {

		Event e = null;

		switch (et.getCategory()) {
		case EventCategory.PUNCTUAL_EVENT:
			e = new PunctualEvent(eIdManager.getNextId());
			e.setTimestamp(currentTimestamp);
			currentTimestamp++;
			break;
		case EventCategory.STATE:
			State s = new State(eIdManager.getNextId());
			s.setTimestamp(currentTimestamp);
			s.setEndTimestamp(currentTimestamp + DURATION);
			s.setImbricationLevel(0); // XXX
			currentTimestamp = currentTimestamp + DURATION + 1;
			e = s;
			break;
		case EventCategory.LINK:
			Link l = new Link(eIdManager.getNextId());
			l.setTimestamp(currentTimestamp);
			l.setEndTimestamp(currentTimestamp + DURATION);
			l.setEndProducer(prod); // XXX
			currentTimestamp = currentTimestamp + DURATION + 1;
			e = l;
			break;
		case EventCategory.VARIABLE:
			Variable v = new Variable(eIdManager.getNextId());
			v.setTimestamp(currentTimestamp);
			v.setEndTimestamp(0); // XXX
			currentTimestamp++; // XXX
			e = v;
			break;
		}

		Assert.isNotNull(e, "Null event: wrong category");

		e.setCategory(et.getCategory());
		e.setType(et);
		e.setEventProducer(prod);
		e.setCpu(CPU);
		e.setPage(PAGE);

		for (EventParamType ept : et.getEventParamTypes()) {
			EventParam ep = new EventParam(epIdManager.getNextId());
			ep.setEvent(e);
			ep.setEventParamType(ept);
			ep.setValue(PARAMETER_VALUE);
			traceDB.save(ep);
		}
		traceDB.save(e);
	}

	private static List<EventType> createTypes(TraceDBObject traceDB, int category,
			IdManager etIdManager, IdManager eptIdManager) throws SoCTraceException {
		List<EventType> types = new ArrayList<>();
		for (int i = 0; i < NUMBER_OF_TYPES; i++) {
			EventType et = new EventType(etIdManager.getNextId(), category);
			et.setName(TYPE_NAME_PREFIX + et.getId());
			for (int j = 0; j < NUMBER_OF_PARAMETERS; j++) {
				EventParamType ept = new EventParamType(eptIdManager.getNextId());
				ept.setName(PARAMETER_NAME_PREFIX + ept.getId());
				ept.setType(PARAMETER_TYPE);
				ept.setEventType(et);
				traceDB.save(ept);
			}
			traceDB.save(et);
			types.add(et);
		}
		return types;
	}

}
