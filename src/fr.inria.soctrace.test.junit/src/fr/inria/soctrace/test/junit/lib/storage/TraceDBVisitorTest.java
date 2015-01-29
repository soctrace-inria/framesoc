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
package fr.inria.soctrace.test.junit.lib.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.AnalysisResultAnnotationData;
import fr.inria.soctrace.lib.model.AnalysisResultGroupData;
import fr.inria.soctrace.lib.model.AnalysisResultGroupData.DepthFirstIterator;
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
import fr.inria.soctrace.lib.model.File;
import fr.inria.soctrace.lib.model.Group;
import fr.inria.soctrace.lib.model.Group.LeafMapping;
import fr.inria.soctrace.lib.model.IModelElement;
import fr.inria.soctrace.lib.model.Link;
import fr.inria.soctrace.lib.model.OrderedGroup;
import fr.inria.soctrace.lib.model.State;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.UnorderedGroup;
import fr.inria.soctrace.lib.model.Variable;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.AnalysisResultAnnotationDataQuery;
import fr.inria.soctrace.lib.query.AnalysisResultGroupDataQuery;
import fr.inria.soctrace.lib.query.AnalysisResultProcessedTraceDataQuery;
import fr.inria.soctrace.lib.query.AnalysisResultQuery;
import fr.inria.soctrace.lib.query.AnalysisResultSearchDataQuery;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.FileQuery;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.utils.SQLConstants.FramesocTable;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.test.junit.utils.BaseTestClass;
import fr.inria.soctrace.test.junit.utils.IModelFactory;
import fr.inria.soctrace.test.junit.utils.ResourceLoader;
import fr.inria.soctrace.test.junit.utils.TestUtils;

/**
 * Test all visitors for Trace DB.
 * 
 * The pattern for each test is:
 * 
 * 0 
 * - create the elements of the model
 * 
 * 1
 * - save them
 * - retrieve and check they are OK
 * 
 * 2
 * - update them
 * - retrieve and check they are OK
 * 
 * 3
 * - delete them
 * - retrieve and check they are no more there
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class TraceDBVisitorTest extends BaseTestClass {

	private TraceDBObject traceDB;

	@Before
	public void setUp() throws Exception {
		traceDB = new TraceDBObject("TMP_"+TestUtils.getRandomDBName(), DBMode.DB_CREATE);
	}

	@After
	public void tearDown() throws Exception {
		traceDB.dropDatabase();
	}

	@Test
	public final void testVisitEvent() throws SoCTraceException {

		// 0. create
		List<Event> events = IModelFactory.INSTANCE.createEvents(10);

		// 1. save and check
		saveEvents(events);
		Map<Long, Event> eventMap = new HashMap<>();
		for (Event e: events) {
			eventMap.put(e.getId(), e);
		}

		EventQuery query = new EventQuery(traceDB);
		List<Event> res = query.getList();
		for (Event er: res) {
			assertTrue(er.equals(eventMap.get(er.getId())));
		}

		// 2. update and check
		Event first = events.iterator().next();
		first.getEventProducer().setLocalId("please");
		first.getEventProducer().setName("please");
		first.getEventProducer().setParentId(123);
		first.getEventProducer().setType("please");
		traceDB.update(first.getEventProducer());
		first.getType().setName("please");
		traceDB.update(first.getType());
		int i = 0;
		for (EventParamType ept: first.getType().getEventParamTypes()) {
			ept.setName("please"+(i++));
			ept.setType("please");
			traceDB.update(ept);
		}
		for (Event e: events) {
			e.setCpu(123);
			e.setPage(123);
			e.setTimestamp(123);
			traceDB.update(e);
			for (EventParam ep: e.getEventParams()) {
				ep.setValue("please");
				traceDB.update(ep);
			}			
		}
		traceDB.flushVisitorBatches();

		query.clear();
		res = query.getList();
		for (Event er: res) {
			assertTrue(er.equals(eventMap.get(er.getId())));
		}

		// 3. delete and check
		first = events.iterator().next();
		traceDB.delete(first.getEventProducer());
		traceDB.delete(first.getType());
		for (EventParamType ept: first.getType().getEventParamTypes()) {
			traceDB.delete(ept);
		}
		for (Event e: events) {
			traceDB.delete(e);
			for (EventParam ep: e.getEventParams()) {
				traceDB.delete(ep);
			}			
		}
		traceDB.flushVisitorBatches();

		assertEquals(0, traceDB.getCount(FramesocTable.EVENT.toString()));
		assertEquals(0, traceDB.getCount(FramesocTable.EVENT_TYPE.toString()));	
		assertEquals(0, traceDB.getCount(FramesocTable.EVENT_PARAM.toString()));
		assertEquals(0, traceDB.getCount(FramesocTable.EVENT_PARAM_TYPE.toString()));
		assertEquals(0, traceDB.getCount(FramesocTable.EVENT_PRODUCER.toString()));
	}

	@Test @SuppressWarnings("unchecked")
	public final void testVisitAnalysisResultSearch() throws SoCTraceException {

		// 0. create
		Tool virtualImporter = ResourceLoader.getVirtualImporterTool();
		AnalysisResult ar = IModelFactory.INSTANCE.createSearchResult(new IdManager(), "description");
		AnalysisResultSearchData data = (AnalysisResultSearchData)ar.getData();
		List<Event> events = (List<Event>)(List<?>)data.getElements();
		saveEvents(events);

		// 1. save and check
		traceDB.save(ar);
		traceDB.flushVisitorBatches();
		AnalysisResultQuery arq = new AnalysisResultQuery(traceDB);
		AnalysisResult res = arq.getList().iterator().next();
		AnalysisResultSearchDataQuery ardq = new AnalysisResultSearchDataQuery(traceDB);
		res.setData(ardq.getAnalysisResultData(res.getId()));
		assertTrue(res.equals(ar));

		// 2. update and check
		ar.setDate(new Timestamp(new Date().getTime()));
		ar.setDescription("please");
		ar.setTool(virtualImporter);
		data.setSearchCommand("please");
		traceDB.update(ar);
		traceDB.flushVisitorBatches();
		res = arq.getList().iterator().next();
		res.setData(ardq.getAnalysisResultData(res.getId()));
		assertTrue(res.equals(ar));

		// 3. delete and check
		traceDB.delete(ar);
		traceDB.flushVisitorBatches();
		assertEquals(0, traceDB.getCount(FramesocTable.ANALYSIS_RESULT.toString()));
		assertEquals(0, traceDB.getCount(FramesocTable.SEARCH.toString()));
		assertEquals(0, traceDB.getCount(FramesocTable.SEARCH_MAPPING.toString()));

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
	@Test
	public final void testVisitAnalysisResultGroup() throws SoCTraceException {

		// 0. create
		Tool virtualImporter = ResourceLoader.getVirtualImporterTool();
		AnalysisResult ar = IModelFactory.INSTANCE.createGroupResult(new IdManager(), "description");
		AnalysisResultGroupData data = (AnalysisResultGroupData)ar.getData();
		UnorderedGroup root = (UnorderedGroup)data.getRoot();
		Set<Long> eventId = new HashSet<>();
		Set<Long> typeId = new HashSet<>();
		boolean producerSaved = false;
		List<Group> sons = root.getSonGroups();
		for (Group son: sons) {
			Collection<LeafMapping> leaves = ((OrderedGroup)son).getSonLeaves().values();
			for (LeafMapping lm: leaves) {
				IModelElement me = (IModelElement) lm.getSon();
				if (me instanceof Event) {
					Event e = (Event) me;
					if (!eventId.contains(e.getId())) {
						eventId.add(e.getId());
						traceDB.save(e);
						if (!producerSaved) {
							producerSaved = true;
							traceDB.save(e.getEventProducer());
						}
						for (EventParam ep: e.getEventParams()) {
							traceDB.save(ep);
						}
					}
				}
				else {
					EventType et = (EventType) me;
					if (!typeId.contains(et.getId())) {
						typeId.add(et.getId());
						traceDB.save(et);
						for (EventParamType ept: et.getEventParamTypes()) {
							traceDB.save(ept);
						}
					}					
				} 
			}
		}
		traceDB.flushVisitorBatches();

		// 1. save and check
		traceDB.save(ar);
		traceDB.flushVisitorBatches();
		AnalysisResultQuery arq = new AnalysisResultQuery(traceDB);
		AnalysisResult res = arq.getList().iterator().next();
		AnalysisResultGroupDataQuery ardq = new AnalysisResultGroupDataQuery(traceDB);
		res.setData(ardq.getAnalysisResultData(res.getId()));
		assertTrue(res.equals(ar));		

		// 2. update and check
		ar.setDate(new Timestamp(new Date().getTime()));
		ar.setDescription("please");
		ar.setTool(virtualImporter);
		DepthFirstIterator dit = data.getDepthFirstIterator();
		while (dit.hasNext()) {
			// group metadata
			Group g = dit.next();
			g.setName("please");
			g.setGroupingOperator("please");
			// for ordered group, change the sequence number of leaves
			if (g instanceof OrderedGroup) {
				OrderedGroup og = (OrderedGroup)g;
				Map<Integer, LeafMapping> leaves = og.getSonLeaves();
				Iterator<Entry<Integer, LeafMapping>> it = leaves.entrySet().iterator();
				Map<Integer, LeafMapping> tmp = new HashMap<Integer, LeafMapping>();
				while (it.hasNext()) {
					Entry<Integer, LeafMapping> pairs = it.next();
					LeafMapping mapping = pairs.getValue();
					Integer position = pairs.getKey();
					og.getSons().remove(position); // remove also from all sons
					it.remove();
					tmp.put(position+10, mapping);	
				}
				it = tmp.entrySet().iterator();
				while(it.hasNext()) {
					Entry<Integer, LeafMapping> pairs = it.next();
					LeafMapping mapping = pairs.getValue();
					Integer position = pairs.getKey();
					og.addSon(mapping.getSon(), position + 10, mapping.getMappingId());	
				}					
			} 				
		}
		traceDB.update(ar);
		traceDB.flushVisitorBatches();
		res = arq.getList().iterator().next();
		res.setData(ardq.getAnalysisResultData(res.getId()));
		assertTrue(res.equals(ar));

		// 3. delete and check
		traceDB.delete(ar);
		traceDB.flushVisitorBatches();
		assertEquals(0, traceDB.getCount(FramesocTable.ANALYSIS_RESULT.toString()));
		assertEquals(0, traceDB.getCount(FramesocTable.ENTITY_GROUP.toString()));
		assertEquals(0, traceDB.getCount(FramesocTable.GROUP_MAPPING.toString()));

	}

	@Test
	public final void testVisitAnalysisResultAnnotation() throws SoCTraceException {

		// 0. create
		Tool virtualImporter = ResourceLoader.getVirtualImporterTool();
		AnalysisResult ar = IModelFactory.INSTANCE.createAnnotationResult(new IdManager(), "description");
		AnalysisResultAnnotationData data = (AnalysisResultAnnotationData)ar.getData();

		// 1. save and check
		traceDB.save(ar);
		traceDB.flushVisitorBatches();
		AnalysisResultQuery arq = new AnalysisResultQuery(traceDB);
		AnalysisResult res = arq.getList().iterator().next();
		AnalysisResultAnnotationDataQuery ardq = new AnalysisResultAnnotationDataQuery(traceDB);
		res.setData(ardq.getAnalysisResultData(res.getId()));
		assertTrue(res.equals(ar));

		// 2. update and check
		ar.setDate(new Timestamp(new Date().getTime()));
		ar.setDescription("please");
		ar.setTool(virtualImporter);
		Collection<AnnotationType> types = data.getAnnotationTypes();
		for (AnnotationType at: types) {
			at.setName("please");
			int i = 0;
			for (AnnotationParamType apt: at.getParamTypes()) {
				apt.setName("please"+(i++));
				apt.setType("please");
			}
		}
		List<Annotation> annotations = data.getAnnotations();
		for (Annotation a: annotations) {
			a.setName("please");
			for (AnnotationParam ap: a.getParams()) {
				ap.setValue("please");
			}
		}
		traceDB.update(ar);
		traceDB.flushVisitorBatches();
		res = arq.getList().iterator().next();
		res.setData(ardq.getAnalysisResultData(res.getId()));
		assertTrue(res.equals(ar));


		// 3. delete and check
		traceDB.delete(ar);
		traceDB.flushVisitorBatches();
		assertEquals(0, traceDB.getCount(FramesocTable.ANALYSIS_RESULT.toString()));
		assertEquals(0, traceDB.getCount(FramesocTable.ANNOTATION.toString()));
		assertEquals(0, traceDB.getCount(FramesocTable.ANNOTATION_TYPE.toString()));
		assertEquals(0, traceDB.getCount(FramesocTable.ANNOTATION_PARAM.toString()));
		assertEquals(0, traceDB.getCount(FramesocTable.ANNOTATION_PARAM_TYPE.toString()));

	}

	/**
	 * Note: in this test we save the trace result in a trace DB which
	 * is actually NOT the one related to the source trace.
	 * This is done to avoid polluting the real trace DB.
	 * It has no consequences on the test value since we just want to check
	 * that the correct values are written and read.
	 */
	@Test
	public final void testVisitAnalysisResultTrace() throws SoCTraceException {

		// 0. create
		SystemDBObject sysDB = SystemDBObject.openNewIstance();
		Tool virtualImporter = ResourceLoader.getVirtualImporterTool();
		AnalysisResult ar = IModelFactory.INSTANCE.createTraceResult(new IdManager(), "description");
		AnalysisResultProcessedTraceData data = (AnalysisResultProcessedTraceData)ar.getData();

		// 1. save and check
		traceDB.save(ar);
		traceDB.flushVisitorBatches();

		AnalysisResultQuery arq = new AnalysisResultQuery(traceDB);
		AnalysisResult res = arq.getList().iterator().next();
		AnalysisResultProcessedTraceDataQuery ardq = new AnalysisResultProcessedTraceDataQuery(traceDB, sysDB, data.getSourceTrace());
		res.setData(ardq.getAnalysisResultData(res.getId()));
		assertTrue(res.equals(ar));

		// 2. update and check
		ar.setDate(new Timestamp(new Date().getTime()));
		ar.setDescription("please");
		ar.setTool(virtualImporter);
		traceDB.update(ar);
		traceDB.flushVisitorBatches();
		res = arq.getList().iterator().next();
		res.setData(ardq.getAnalysisResultData(res.getId()));
		assertTrue(res.equals(ar));

		// data cannot be changed for processed traces
		Trace tmp = data.getProcessedTrace();
		data.setProcessedTrace(data.getSourceTrace()); // set dest == source just to test
		traceDB.update(ar);
		traceDB.flushVisitorBatches();
		res = arq.getList().iterator().next();
		res.setData(ardq.getAnalysisResultData(res.getId()));
		assertTrue(!res.equals(ar));
		data.setProcessedTrace(tmp);

		// 3. delete and check
		traceDB.delete(ar);
		traceDB.flushVisitorBatches();
		assertEquals(0, traceDB.getCount(FramesocTable.ANALYSIS_RESULT.toString()));
		assertEquals(0, traceDB.getCount(FramesocTable.PROCESSED_TRACE.toString()));
	}

	@Test
	public final void testVisitFile() throws SoCTraceException {

		// 0. create
		File file = new File(0);
		file.setDescription("Description");
		file.setPath("/my/test/path");

		// 1. save and check
		traceDB.save(file);
		traceDB.flushVisitorBatches();
		FileQuery query = new FileQuery(traceDB);
		File res = query.getList().iterator().next();
		assertTrue(res.equals(file));

		// 2. update and check
		file.setDescription("please");
		file.setPath("please");
		traceDB.update(file);
		traceDB.flushVisitorBatches();
		res = query.getList().iterator().next();
		assertTrue(res.equals(file));

		// 3. delete and check
		traceDB.delete(file);
		traceDB.flushVisitorBatches();
		assertEquals(0, traceDB.getCount(FramesocTable.EVENT_PARAM_TYPE.toString()));
	}

	// utilities

	/**
	 * Save the events, the type and the producer
	 * (same type and producer for all events).
	 * @throws SoCTraceException 
	 */
	private void saveEvents(List<Event> events) throws SoCTraceException {
		Set<Long> types = new HashSet<>();
		Event first = events.iterator().next();
		EventProducer prod = first.getEventProducer();
		traceDB.save(prod);
		for (Event e: events) {
			traceDB.save(e);
			for (EventParam ep: e.getEventParams()) {
				traceDB.save(ep);
			}
			if (!types.contains(e.getType().getId())) {
				types.add(e.getType().getId());
				EventType et = e.getType();
				traceDB.save(et);
				for (EventParamType ept: et.getEventParamTypes()) {
					traceDB.save(ept);
				}				
			}
		}
		traceDB.flushVisitorBatches();
	}

	@Test
	public final void testVisitPajeEvent() throws SoCTraceException {

		// 0. create
		List<Event> events = IModelFactory.INSTANCE.createCategorizedEvents(3);

		// 1. save and check
		saveEvents(events);
		Map<Long, Event> eventMap = new HashMap<>();
		for (Event e: events) {
			eventMap.put(e.getId(), e);
		}

		EventQuery query = new EventQuery(traceDB);
		List<Event> res = query.getList();
		for (Event er: res) {
			assertTrue(er.equals(eventMap.get(er.getId())));
		}

		// 2. update and check
		// save a new producer first
		EventProducer newEp = new EventProducer(666);
		traceDB.save(newEp);
		Event first = events.iterator().next();
		// update producer and type, taking the reference from the first event
		first.getEventProducer().setLocalId("please");
		first.getEventProducer().setName("please");
		first.getEventProducer().setParentId(123);
		first.getEventProducer().setType("please");
		traceDB.update(first.getEventProducer());
		first.getType().setName("please");
		traceDB.update(first.getType());
		int i = 0;
		for (EventParamType ept: first.getType().getEventParamTypes()) {
			ept.setName("please"+(i++));
			ept.setType("please");
			traceDB.update(ept);
		}
		// update the events
		for (Event e: events) {
			e.setCpu(123);
			e.setPage(123);
			e.setTimestamp(123);
			switch(e.getCategory()) {
			case EventCategory.PUNCTUAL_EVENT:
				break;
			case EventCategory.LINK:
				((Link)e).setEndTimestamp(666);
				((Link)e).setEndProducer(newEp); // put the same, since there are no other producers
				break;
			case EventCategory.STATE:
				((State)e).setEndTimestamp(666);
				((State)e).setImbricationLevel(666);
				break;
			case EventCategory.VARIABLE:
				((Variable)e).setValue(666);
				((Variable)e).setEndTimestamp(666);
				break;
			}
			traceDB.update(e);
			for (EventParam ep: e.getEventParams()) {
				ep.setValue("please");
				traceDB.update(ep);
			}			
		}
		traceDB.flushVisitorBatches();

		query.clear();
		res = query.getList();
		for (Event er: res) {
			assertTrue(er.equals(eventMap.get(er.getId())));
		}

		// 3. delete and check
		traceDB.delete(newEp);
		first = events.iterator().next();
		traceDB.delete(first.getEventProducer());
		Set<Long> deletedTypes = new HashSet<>();
		for (Event e: events) {
			traceDB.delete(e);
			for (EventParam ep: e.getEventParams()) {
				traceDB.delete(ep);
			}			

			if (!deletedTypes.contains(e.getType().getId())) {
				deletedTypes.add(e.getType().getId());
				traceDB.delete(e.getType());
				for (EventParamType ept: e.getType().getEventParamTypes()) {
					traceDB.delete(ept);
				}
			}
		}
		traceDB.flushVisitorBatches();

		assertEquals(0, traceDB.getCount(FramesocTable.EVENT.toString()));
		assertEquals(0, traceDB.getCount(FramesocTable.EVENT_TYPE.toString()));	
		assertEquals(0, traceDB.getCount(FramesocTable.EVENT_PARAM.toString()));
		assertEquals(0, traceDB.getCount(FramesocTable.EVENT_PARAM_TYPE.toString()));
		assertEquals(0, traceDB.getCount(FramesocTable.EVENT_PRODUCER.toString()));
	}
}
