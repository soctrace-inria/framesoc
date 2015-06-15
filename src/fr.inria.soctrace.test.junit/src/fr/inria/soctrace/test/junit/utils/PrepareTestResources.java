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
package fr.inria.soctrace.test.junit.utils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.eclipse.core.runtime.Assert;

import fr.inria.soctrace.framesoc.core.FramesocConstants.FramesocToolType;
import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.AnalysisResultAnnotationData;
import fr.inria.soctrace.lib.model.AnalysisResultData;
import fr.inria.soctrace.lib.model.AnalysisResultGroupData;
import fr.inria.soctrace.lib.model.AnalysisResultProcessedTraceData;
import fr.inria.soctrace.lib.model.AnalysisResultSearchData;
import fr.inria.soctrace.lib.model.Annotation;
import fr.inria.soctrace.lib.model.AnnotationParam;
import fr.inria.soctrace.lib.model.AnnotationParamType;
import fr.inria.soctrace.lib.model.AnnotationType;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.OrderedGroup;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.TraceType;
import fr.inria.soctrace.lib.model.UnorderedGroup;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.OrderBy;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.test.junit.utils.importer.VirtualImporter;

/**
 * Prepare the resources to run the tests with SQLite.
 * 
 * If a System DB is already present in ./resources nothing is done. More details in the README.
 * 
 * The class depends on TestConstants and TestConfiguration.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PrepareTestResources {

	public static void prepareTestResources() throws SoCTraceException {

		TestConfiguration.initTest();
		
		if (FramesocManager.getInstance().isSystemDBExisting())
			return;

		Assert.isTrue(VirtualImporter.TOTAL_NUMBER_OF_EVENTS >= 1);

		// create system DB
		System.out.println("* Creating SystemDB...");
		FramesocManager.getInstance().createSystemDB();

		// register virtual importer
		System.out.println("* Registering Virtual Importer...");
		FramesocManager.getInstance().registerTool(TestConstants.VIRTUAL_IMPORTER_TOOL_NAME,
				TestConstants.VIRTUAL_IMPORTER_TOOL_COMMAND, FramesocToolType.IMPORT.toString(),
				false, TestConstants.VIRTUAL_IMPORTER_TOOL_DOC,
				TestConstants.VIRTUAL_IMPORTER_TOOL_EXT_ID);

		// register junit dummy tool
		System.out.println("* Registering JUnit dummy tool...");
		FramesocManager.getInstance().registerTool(TestConstants.JUNIT_TEST_TOOL_NAME,
				TestConstants.JUNIT_TEST_TOOL_COMMAND, FramesocToolType.ANALYSIS.toString(), false,
				TestConstants.JUNIT_TEST_TOOL_DOC, TestConstants.JUNIT_TEST_TOOL_EXT_ID);

		// import virtual trace
		System.out.println("* Importing virtual test trace...");
		virtualImport();

		// change the page on the last event
		System.out.println("* Changing page on last event...");
		modifyLast();

		// add analysis results

		IdManager arId = new IdManager();
		ITraceSearch search = new TraceSearch().initialize();
		Tool tool = search.getToolByName(TestConstants.JUNIT_TEST_TOOL_NAME);
		search.uninitialize();

		// search
		System.out.println("* Registering dummy search result...");
		saveEventSearchResult(arId, tool);
		saveEventProucerSearchResult(arId, tool);

		// group
		System.out.println("* Registering dummy group result...");
		saveGroupResult(arId, tool);

		// annotation
		System.out.println("* Registering dummy annotation result...");
		saveAnnotationResult(arId, tool);

		// processed trace and related result
		System.out.println("* Registering dummy processed trace...");
		saveProcessedTrace();
		saveProcessedTraceResult(arId, tool);

		System.out.println("* Test resources created. Exit.");

	}

	private static void saveEventProucerSearchResult(IdManager arId, Tool tool)
			throws SoCTraceException {
		ITraceSearch search = new TraceSearch().initialize();
		Trace trace = search.getTraceByDBName(VirtualImporter.DB_NAME);
		List<EventProducer> producers = search.getEventProducers(trace);
		search.uninitialize();
		AnalysisResultSearchData searchData = new AnalysisResultSearchData(EventProducer.class);
		searchData.setSearchCommand("search result event producers");
		searchData.setElements(producers);
		saveAnalysisResult(arId, tool, searchData);
	}

	private static void saveAnnotationResult(IdManager arId, Tool tool) throws SoCTraceException {

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

		saveAnalysisResult(arId, tool, annotationData);
	}

	/**
	 * ROOT - TYPES 1. type1 2. type2 3. EMPTY - EVENTS 1. event1 2. event2 3. EMPTY
	 */
	private static void saveGroupResult(IdManager arId, Tool tool) throws SoCTraceException {

		ITraceSearch search = new TraceSearch().initialize();
		Trace trace = search.getTraceByDBName(VirtualImporter.DB_NAME);
		List<Event> elist1 = search.getEventsByTypeName(trace, VirtualImporter.TYPE_NAME_PREFIX
				+ "0");
		List<Event> elist2 = search.getEventsByTypeName(trace, VirtualImporter.TYPE_NAME_PREFIX
				+ "1");
		search.uninitialize();

		Event e1 = elist1.iterator().next();
		Event e2 = elist2.iterator().next();

		IdManager idManager = new IdManager();
		UnorderedGroup root = new UnorderedGroup(idManager.getNextId(), null);
		root.setName("ROOT");

		OrderedGroup pattern = new OrderedGroup(idManager.getNextId(), EventType.class);
		pattern.setName("TYPES");
		pattern.addSon(e1.getType(), 0);
		pattern.addSon(e2.getType(), 1);
		UnorderedGroup empty = new UnorderedGroup(idManager.getNextId(), null);
		empty.setName("EMPTY");
		pattern.addSon(empty, 2);

		OrderedGroup example = new OrderedGroup(idManager.getNextId(), Event.class);
		example.setName("EVENTS");
		example.addSon(e1, 0);
		example.addSon(e2, 1);
		empty = new UnorderedGroup(idManager.getNextId(), null);
		empty.setName("EMPTY");
		example.addSon(empty, 2);

		root.addSon(pattern);
		root.addSon(example);

		AnalysisResultGroupData groupData = new AnalysisResultGroupData(root);

		saveAnalysisResult(arId, tool, groupData);
	}

	private static void saveEventSearchResult(IdManager arId, Tool tool) throws SoCTraceException {
		ITraceSearch search = new TraceSearch().initialize();
		Trace trace = search.getTraceByDBName(VirtualImporter.DB_NAME);
		List<Event> events = search.getEventsByTypeName(trace, VirtualImporter.TYPE_NAME_PREFIX
				+ "0");
		search.uninitialize();
		AnalysisResultSearchData searchData = new AnalysisResultSearchData(Event.class);
		searchData.setSearchCommand("search result events");
		searchData.setElements(events);

		saveAnalysisResult(arId, tool, searchData);
	}

	private static void saveProcessedTraceResult(IdManager arId, Tool tool)
			throws SoCTraceException {
		ITraceSearch search = new TraceSearch().initialize();
		Trace source = search.getTraceByDBName(VirtualImporter.DB_NAME);
		Trace dest = search.getTraceByDBName(TestConstants.PROCESSED_TRACE_DB_NAME);
		search.uninitialize();

		AnalysisResultProcessedTraceData processedTraceData = new AnalysisResultProcessedTraceData();
		processedTraceData.setSourceTrace(source);
		processedTraceData.setProcessedTrace(dest);

		saveAnalysisResult(arId, tool, processedTraceData);
	}

	private static void saveProcessedTrace() throws SoCTraceException {
		SystemDBObject sysDB = SystemDBObject.openNewInstance();

		Trace dest = new Trace(TestConstants.PROCESSED_TRACE_ID);
		dest.setProcessed(true);
		dest.setAlias(TestConstants.PROCESSED_TRACE_METADATA);
		dest.setDescription(TestConstants.PROCESSED_TRACE_METADATA);
		dest.setDbName(TestConstants.PROCESSED_TRACE_DB_NAME);
		TraceType tt = new TraceType(TestConstants.PROCESSED_TRACE_TYPE_ID);
		tt.setName(TestConstants.PROCESSED_TRACE_TYPE_NAME);
		dest.setType(tt);

		sysDB.save(dest);
		sysDB.save(tt);
		sysDB.close();
	}

	private static void modifyLast() throws SoCTraceException {
		TraceDBObject traceDB = TraceDBObject.openNewInstance(VirtualImporter.DB_NAME);
		EventQuery query = new EventQuery(traceDB);
		query.setOrderBy("TIMESTAMP", OrderBy.DESC);
		Event last = query.getList().iterator().next();
		last.setPage(TestConstants.MAX_PAGE);
		traceDB.update(last);
		traceDB.close();
	}

	private static void virtualImport() throws SoCTraceException {
		VirtualImporter importer = new VirtualImporter();
		importer.virtualImport();
	}

	private static void saveAnalysisResult(IdManager aid, Tool tool, AnalysisResultData data)
			throws SoCTraceException {
		AnalysisResult ar = new AnalysisResult(aid.getNextId());
		ar.setTool(tool);
		ar.setDescription("dummy result " + data.getType());
		ar.setDate(new Timestamp(new Date().getTime()));
		ar.setType(data.getType().toString());
		ar.setData(data);
		TraceDBObject traceDB = TraceDBObject.openNewInstance(VirtualImporter.DB_NAME);
		traceDB.save(ar);
		traceDB.close();
	}

}
