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

import java.util.List;

import fr.inria.soctrace.lib.model.AnalysisResult;
import fr.inria.soctrace.lib.model.AnalysisResultData;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.File;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.AnalysisResultData.AnalysisResultType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.utils.EventProducerDesc;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.lib.search.utils.ParamDesc;


/**
 * Interface used to perform predefined requests in the
 * SoC-Trace Infrastructure.
 * 
 * <p>
 * IMPORTANT: in order to correctly use the search interface
 * the following protocol must be used.
 * <ul>
 * <li> instantiate a concrete class implementing this interface
 * <li> initialize it calling {@link #initialize()}
 * <li> use it
 * <li> uninitialize it calling {@link #uninitialize()}}
 * </ul>
 * 
 * <p>
 * NOTE: if not differently specified, the following applies.
 * All the functions taking as input Lists of objects
 * (e.g. List of Strings) will return an empty list of requested
 * objects (e.g. Events) if the passed List is null or contains 
 * 0 elements.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface ITraceSearch {

	/**
	 * Initialize the search object. 
	 * @return The search object reference
	 * @throws SoCTraceException 
	 */
	ITraceSearch initialize() throws SoCTraceException;
	
	/**
	 * Uninitialize the search object.
	 * @throws SoCTraceException
	 */
	void uninitialize() throws SoCTraceException;
	
	/*
	 *      T o o l s
	 */

	/**
	 * Get the tool with the given name.
	 * 
	 * @param name tool name
	 * @return the tool, or null if not found.
	 * @throws SoCTraceException
	 */
	Tool getToolByName(String name) throws SoCTraceException;
	
	/**
	 * Get a list of tools of a given type.
	 *  
	 * @param toolType string containing the tool type
	 * @return a list of tool of a given type
	 * @throws SoCTraceException
	 */
	List<Tool> getToolByType(String toolType) throws SoCTraceException;

	/**
	 * Get a list of all tools.
	 *  
	 * @return a list of all tools
	 * @throws SoCTraceException
	 */
	List<Tool> getTools() throws SoCTraceException;

	/*
	 *      T r a c e s 
	 */
		
	/**
	 * Get the trace corresponding to a trace DB name
	 * 
	 * @param dbName the trace DB name
	 * @return the corresponding Trace object, or null if not found.
	 * @throws SoCTraceException
	 */
	Trace getTraceByDBName(String dbName) throws SoCTraceException;
	
	/**
	 * Get all the traces produced between two given dates (extremes included).
	 * Dates are expressed as UNIX timestamps (seconds since standard 
	 * epoch of 01/01/1970)
	 * 
	 * @param startDate starting date
	 * @param endDate ending date
	 * @return a list of Traces
	 * @throws SoCTraceException
	 */
	List<Trace> getTracesByTracingDate(long startDate, long endDate) throws SoCTraceException;
	
	/**
	 * Get all the traces produced tracing a given application.
	 * 
	 * @param application the application name
	 * @return a list of Traces 
	 * @throws SoCTraceException
	 */
	List<Trace> getTracesByTracedApplication(String application) throws SoCTraceException;
	
	/**
	 * Get all the traces produced on a given board.
	 * 
	 * @param board the board name
	 * @return a list of Traces
	 * @throws SoCTraceException
	 */
	List<Trace> getTracesByBoard(String board) throws SoCTraceException;
	
	/**
	 * Get all the traces produced on a given operating system.
	 * 
	 * @param operatingSystem the operating system name
	 * @return a list of Traces
	 * @throws SoCTraceException
	 */
	List<Trace> getTracesByOperatingSystem(String operatingSystem) throws SoCTraceException;
	
	/**
	 * Get all the traces produced on devices with a given number of CPU.
	 * 
	 * @param numberOfCpus the number of CPUs
	 * @return a list of Traces
	 * @throws SoCTraceException
	 */
	List<Trace> getTracesByNumberOfCpus(int numberOfCpus) throws SoCTraceException;
	
	/**
	 * Get all the traces captured using a given output device (e.g. STM).
	 * 
	 * @param outputDevice the output device name
	 * @return a list of Traces
	 * @throws SoCTraceException
	 */
	List<Trace> getTracesByOutputDevice(String outputDevice) throws SoCTraceException;

	/**
	 * Get all the traces having a given description
	 * 
	 * @param description trace description
	 * @return a list of Traces
	 * @throws SoCTraceException
	 */
	List<Trace> getTracesByDescription(String description) throws SoCTraceException;

	/**
	 * Get all the traces of a given type.
	 * 
	 * @param traceType name of the type
	 * @return a list of traces of the given type
	 * @throws SoCTraceException
	 */
	List<Trace> getTracesByType(String traceType) throws SoCTraceException;
	
	/**
	 * Get all the traces whose type is among the ones specified.
	 * 
	 * @param traceTypes a list of trace type names
	 * @return a list of Trace of the given types
	 * @throws SoCTraceException
	 */
	List<Trace> getTracesByTypes(List<String> traceTypes) throws SoCTraceException;

	/**
	 * Get all the traces of a given type, which respect the
	 * 'param_name == param_value' condition passed.
	 * 
	 * @param traceType the name of the trace type
	 * @param param parameter descriptor ([name, value] couple).
	 * @return a list of Traces of the specified type respecting the 
	 * 'param_name == param_value' condition
	 * @throws SoCTraceException
	 */
	List<Trace> getTracesByParam(String traceType, ParamDesc param) throws SoCTraceException;

	/**
	 * Get all the traces of a given type, which respect all the
	 * 'param_name == param_value' conditions passed.
	 * 
	 * @param traceType the name of the trace type
	 * @param params list of parameters descriptors ([name, value] couples).
	 * @return a list of Traces of the specified type respecting all the 
	 * 'param_name == param_value' conditions
	 * @throws SoCTraceException
	 */
	List<Trace> getTracesByParams(String traceType, List<ParamDesc> params) throws SoCTraceException; 

	/**
	 * Get all the traces (processed and not-processed) 
	 *  
	 * @return a list of traces
	 * @throws SoCTraceException
	 */
	List<Trace> getTraces() throws SoCTraceException;
	
	/**
	 * Get all the raw traces (traces that are not the result of the processing
	 * of another trace).
	 *  
	 * @return a list of non-processed traces
	 * @throws SoCTraceException
	 */
	List<Trace> getRawTraces() throws SoCTraceException;

	/**
	 * Get all the processed traces (traces that are the result of the processing
	 * of another trace).
	 *  
	 * @return a list of processed traces
	 * @throws SoCTraceException
	 */
	List<Trace> getProcessedTraces() throws SoCTraceException;

	/*
	 *      F i l e s
	 */
	
	/**
	 * Get all the files related to a trace.
	 * 
	 * @param t Trace object
	 * @return a list of File objects
	 * @throws SoCTraceException 
	 */
	List<File> getFiles(Trace t) throws SoCTraceException;
	
	/*
	 *      E v e n t     P r o d u c e r s
	 */
	
	/**
	 * Get the EventProducer in the given Trace, corresponding to the given event producer descriptor.
	 * 
	 * @param t Trace object
	 * @param eventProducer event producer descriptor (type, local id)
	 * @return the EventProducer object, or null if not found.
	 * @throws SoCTraceException 
	 */
	EventProducer getEventProducer(Trace t, EventProducerDesc eventProducer) throws SoCTraceException;
	
	/**
	 * Get all the event producers of a given type for a trace
	 * 
	 * @param t Trace object
	 * @param eventProducerType string containing the event producer type
	 * @return a list of EventProducer objects
	 * @throws SoCTraceException 
	 */
	List<EventProducer> getEventProducersByType(Trace t, String eventProducerType) throws SoCTraceException;

	/**
	 * Get all the event producers of a given trace
	 * 
	 * @param t Trace object
	 * @return a list of EventProducer objects
	 * @throws SoCTraceException 
	 */
	List<EventProducer> getEventProducers(Trace t) throws SoCTraceException;

	/*
	 *      E v e n t s 
	 */
	
	/**
	 * Get the minimum timestamp for the given trace.
	 *
	 * @param t Trace object
	 * @return the minimum timestamp for trace events
	 */
	long getMinTimestamp(Trace t) throws SoCTraceException;
	
	/**
	 * Get the maximum timestamp for the given trace.
	 * 
	 * @param t Trace object
	 * @return the maximum timestamp
	 */
	long getMaxTimestamp(Trace t) throws SoCTraceException;
	
	/**
	 * Get all the Events of a given CPU.
	 * 
	 * @param t Trace object the request is related to
	 * @param cpu the CPU number
	 * @return a list of Event of the given CPU
	 * @throws SoCTraceException
	 */
	List<Event> getEventsByCpu(Trace t, int cpu) throws SoCTraceException;
	
	/**
	 * Get all the Events of a given type (only the name of the type is known)
	 * 
	 * @param t Trace object the request is related to
	 * @param eventType the event type
	 * @return a list of Event of the given type
	 * @throws SoCTraceException
	 */
	List<Event> getEventsByTypeName(Trace t, String eventType) throws SoCTraceException;

	/**
	 * Get all the Events whose type is among the ones specified
	 * (only the name of the types is known).
	 * 
	 * @param t Trace object the request is related to
	 * @param eventTypes the list of event type names
	 * @return a list of Event of the given types
	 * @throws SoCTraceException
	 */
	List<Event> getEventsByTypeNames(Trace t, List<String> eventTypes) throws SoCTraceException;

	/**
	 * Get all the Events of a given EventType.
	 * 
	 * @param t Trace object the request is related to
	 * @param eventType the event type
	 * @return a list of Event of the given type
	 * @throws SoCTraceException
	 */
	List<Event> getEventsByEventType(Trace t, EventType eventType) throws SoCTraceException;

	/**
	 * Get all the Events whose EventType is among the ones specified.
	 * 
	 * @param t Trace object the request is related to
	 * @param eventTypes the list of event type names
	 * @return a list of Event of the given types
	 * @throws SoCTraceException
	 */
    List<Event> getEventsByEventTypes(Trace t, List<EventType> eventTypes) throws SoCTraceException;

	/**
	 * Get all the Events with a given event producer
	 * whose descriptor is passed.
	 * 
	 * @param t Trace object the request is related to
	 * @param eventProducer event producer descriptor 
	 * @return a list of Event with a given event producer
	 * @throws SoCTraceException
	 */
	List<Event> getEventsByEventProducerDesc(Trace t, EventProducerDesc eventProducer) throws SoCTraceException;
		
	/**
	 * Get all the Events whose event producer is among the ones
	 * whose descriptor is passed.
	 * 
	 * @param t Trace object the request is related to
	 * @param eventProducers list of event producer descriptors
	 * @return a list of Event with one of the given event producers
	 * @throws SoCTraceException
	 */
	List<Event> getEventsByEventProducerDescs(Trace t, List<EventProducerDesc> eventProducers) throws SoCTraceException;

	/**
	 * Get all the Events with a given EventProducer.
	 * 
	 * @param t Trace object the request is related to
	 * @param eventProducer event producer descriptor 
	 * @return a list of Event with a given event producer
	 * @throws SoCTraceException
	 */
	List<Event> getEventsByEventProducer(Trace t, EventProducer eventProducer) throws SoCTraceException;
		
	/**
	 * Get all the Events whose EventProducer is among the ones specified.
	 * 
	 * @param t Trace object the request is related to
	 * @param eventProducers list of event producer descriptors
	 * @return a list of Event with one of the given event producers
	 * @throws SoCTraceException
	 */
	List<Event> getEventsByEventProducers(Trace t, List<EventProducer> eventProducers) throws SoCTraceException;

	/**
	 * Get all the Events whose timestamp is included in the 
	 * specified interval (t1 <= timestamp < t2).
	 * 
	 * @param t Trace object the request is related to
	 * @param interval the time interval
	 * @return a list of Event happened in the interval
	 * @throws SoCTraceException
	 */
	List<Event> getEventsByInterval(Trace t, IntervalDesc interval) throws SoCTraceException;
	
	/**
	 * Get all the Events whose timestamp is included in one of the 
	 * specified intervals.
	 * 
	 * @param t Trace object the request is related to
	 * @param intervals list of intervals
	 * @return a list of Events happened in one of the intervals
	 * @throws SoCTraceException
	 */
	List<Event> getEventsByIntervals(Trace t, List<IntervalDesc> intervals) throws SoCTraceException;

	/**
	 * Get all the events of a given type, which respect the 'param_name == param_value' 
	 * condition passed.
	 * 
	 * @param t Trace object the request is related to
	 * @param eventType the event type
	 * @param param parameter descriptor ([name, value] couple).
	 * @return a list of Events respecting the condition on the parameter
	 * @throws SoCTraceException
	 */
	List<Event> getEventsByParam(Trace t, String eventType, ParamDesc param) 
			throws SoCTraceException;

	/**
	 * Get all the events of a given type, which respect all the 'param_name == param_value' 
	 * conditions passed.
	 * 
	 * @param t Trace object the request is related to
	 * @param eventType the event type
	 * @param params list of parameters descriptors ([name, value] couples).
	 * @return a list of Events respecting all the conditions on params
	 * @throws SoCTraceException
	 */
	List<Event> getEventsByParams(Trace t, String eventType, List<ParamDesc> params) 
			throws SoCTraceException;
	
	/**
	 * Get all the events of a given type, which respect all the 'param_name == param_value' 
	 * conditions passed and  whose timestamp belongs (at least) to one of the 
	 * intervals specified.
	 * 
	 * @param t Trace object the request is related to
	 * @param eventType the event type
	 * @param params list of parameters descriptors ([name, value] couples).
	 * @param intervals list of time intervals
	 * @return a list of Events respecting all the conditions on params and intervals
	 * @throws SoCTraceException
	 */
	List<Event> getEventsByParamsAndIntervals(Trace t, String eventType, List<ParamDesc> params, 
			List<IntervalDesc> intervals) throws SoCTraceException;

	/**
	 * Get all the events whose type is among the ones specified, whose timestamp
	 * belongs (at least) to one of the intervals specified and whose eventProducer is among
	 * the ones specified.
	 * 
	 * @param t Trace object the request is related to
	 * @param eventTypes list of event type names (if null, it means 'any type')
	 * @param intervals list of intervals (if null, it means 'any interval')
	 * @param eventProducers list of event producers (if null, it means 'any event producer')
	 * @return a list of event respecting the type/interval/producer conditions
	 * @throws SoCTraceException
	 */
	List<Event> getEventsByTypeNamesAndIntervalsAndEventProducerDescs(Trace t, List<String> eventTypes, 
			List<IntervalDesc> intervals, List<EventProducerDesc> eventProducers) throws SoCTraceException;

	/**
	 * Get all the events whose type is among the ones specified, whose timestamp
	 * belongs (at least) to one of the intervals specified and whose eventProducer is among
	 * the ones specified.
	 * 
	 * @param t Trace object the request is related to
	 * @param eventTypes list of EventType objects (if null, it means 'any type')
	 * @param intervals list of intervals (if null, it means 'any interval')
	 * @param eventProducers list of EventProduecr objects (if null, it means 'any event producer')
	 * @return a list of event respecting the type/interval/producer conditions
	 * @throws SoCTraceException
	 */
	List<Event> getEventsByEventTypesAndIntervalsAndEventProducers(Trace t, List<EventType> eventTypes, 
			List<IntervalDesc> intervals, List<EventProducer> eventProducers) throws SoCTraceException;

	/**
	 * Get all the events of the given page
	 * 
	 * @param t Trace object the request is related to
	 * @param page page number
	 * @return a list of events of the specified page
	 * @throws SoCTraceException 
	 */
	List<Event> getEventsByPage(Trace t, Integer page) throws SoCTraceException;
	
	/*
	 *      A n a l y s i s    R e s u l t
	 */

	/**
	 * Get all the AnalysisResult objects for a given trace.
	 * Note: for efficiency, the AnalysisResultData is not filled.
	 * This shall be done explicitly by the user, only if the user 
	 * actually need the data, using the method getAnalysisResultData().
	 * 
	 * @param t Trace object the request is related to
	 * @return a list of AnalysisResult objects
	 * @throws SoCTraceException 
	 */
	List<AnalysisResult> getAnalysisResults(Trace t) throws SoCTraceException;

	/**
	 * Get all the AnalysisResult objects for a given trace of a given type.
	 * Note: for efficiency, the AnalysisResultData is not filled.
	 * This shall be done explicitly by the user, only if the user 
	 * actually need the data, using the method getAnalysisResultData().
	 * 
	 * @param t Trace object the request is related to
	 * @param type Type of the result
	 * @return a list of AnalysisResult objects
	 * @throws SoCTraceException 
	 */
	List<AnalysisResult> getAnalysisResultsByType(Trace t, AnalysisResultType type) throws SoCTraceException;

	/**
	 * Get all the AnalysisResult objects produced by a given tool.
	 * Note: for efficiency, the AnalysisResultData is not filled.
	 * This shall be done explicitly by the user, only if the user 
	 * actually need the data, using the method getAnalysisResultData().
	 * 
	 * @param t Trace object the request is related to
	 * @param tool Tool producing the result
	 * @return a list of AnalysisResult objects
	 * @throws SoCTraceException 
	 */
	List<AnalysisResult> getAnalysisResultsByTool(Trace t, Tool tool) throws SoCTraceException;
	
	/**
	 * Get all the AnalysisResult objects produced by a given tool
	 * and of a given type.
	 * Note: for efficiency, the AnalysisResultData is not filled.
	 * This shall be done explicitly by the user, only if the user 
	 * actually need the data, using the method getAnalysisResultData().
	 * 
	 * @param t Trace object the request is related to
	 * @param tool Tool producing the result
	 * @param type Type of the result
	 * @return a list of AnalysisResult objects
	 * @throws SoCTraceException 
	 */
	List<AnalysisResult> getAnalysisResultsByToolAndType(Trace t, Tool tool, AnalysisResultType type) 
			throws SoCTraceException;
	
	/**
	 * Get the analysis result data related to a given analysis result.
	 * This method sets the result data in the AnalysisResult object.
	 *  
	 * @param t Trace object
	 * @param analysisResult AnalysisResult object
	 * @return the result data
	 * @throws SoCTraceException 
	 */
	AnalysisResultData getAnalysisResultData(Trace t, AnalysisResult analysisResult) throws SoCTraceException;

}
