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
package fr.inria.soctrace.tools.importer.gstreamer.core;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.tools.management.ArgumentsManager;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.IdManager;

/**
 * GStreamer Parser core class.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 * @author "Damien Rousseau"
 */
public class GStreamerParser {

	private final static Logger logger = LoggerFactory.getLogger(GStreamerParser.class);
	
	private SystemDBObject sysDB;
	private TraceDBObject traceDB;
	private String traceFile;
	
	private String FRAME_START_TYPE = GStreamerConstants.DEFAULT_FRAME_START;
	private boolean FRAMES_OVERLAPPING = GStreamerConstants.DEFAULT_FRAME_OVELAPPING;
	
	private Map<Integer, Map<String, EventProducer>> producersMap = new HashMap<Integer, Map<String,EventProducer>>();
	private Map<String, EventType> types = new HashMap<String, EventType>();
	private int startFrameEventTypeId = -1;
	private int numberOfFrames = 0;
	private int numberOfEvents = 0;	
	private long minTimestamp = Long.MAX_VALUE;
	private long maxTimestamp = Long.MIN_VALUE;
	
	public GStreamerParser(SystemDBObject sysDB, TraceDBObject traceDB,
			ArgumentsManager argsm) throws SoCTraceException {
		
		if (!checkArgs(argsm))
			throw new SoCTraceException("Wrong arguments");
		
		traceFile = argsm.getTokens().get(0);
		if (argsm.getOptions().containsKey(GStreamerConstants.FRAME_START_OPT)) {
			FRAME_START_TYPE = argsm.getOptions().get(GStreamerConstants.FRAME_START_OPT);
		}
		if (argsm.getFlags().contains(GStreamerConstants.FRAME_OVELAPPING_FLAG)) {
			FRAMES_OVERLAPPING = true;
		}

		this.sysDB = sysDB;
		this.traceDB = traceDB;
				
	}

	/**
	 * TODO use the monitor
	 * 
	 * @param monitor progress monitor (may be null)
	 * @throws SoCTraceException
	 */
	public void parseTrace(IProgressMonitor monitor) throws SoCTraceException {
		
		logger.debug("Trace file: {}", traceFile);
		logger.debug("Type: {}", FRAME_START_TYPE);
		logger.debug("Overlapping: {}", FRAMES_OVERLAPPING);
		
		// Sort file, if necessary
		try {
			ExternalSort.sort(new File(traceFile));
		} catch (IOException e) {
			throw new SoCTraceException(e);
		}
				
		// Trace Events, EventTypes and Producers
		IdManager producerIdManager = new IdManager();
		parseRawTrace(producerIdManager);
		saveProducers(producerIdManager);
		saveTypes();
		
		// Trace metadata
		GStreamerTraceMetadata traceMetadata = new GStreamerTraceMetadata(sysDB, traceDB.getDBName());
		traceMetadata.computeMetadata(startFrameEventTypeId, numberOfFrames, numberOfEvents, minTimestamp, maxTimestamp);
		traceMetadata.saveMetadata();
	}
	
	private void parseRawTrace(IdManager producerIdManager) throws SoCTraceException {
		
		IdManager eIdManager = new IdManager();
		IdManager epIdManager = new IdManager();
		IdManager etIdManager = new IdManager();
		IdManager eptIdManager = new IdManager();
		
		try {
			startFrameEventTypeId = -1;
			numberOfFrames = 0;
			numberOfEvents = 0;
			int page = 0;
			int thisPageEvents = 0;
			Event last = null;
			List<Event> currentPageEvents = new LinkedList<Event>();
			List<Event> frameStartEvents = new LinkedList<Event>();
			
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new DataInputStream(new FileInputStream(traceFile))));
			GStreamerRecord record = null;
			int c =0;
			while ( (record = getEventRecord(br)) != null) {
				logger.trace("line {}", c++);
				EventProducer prod = getProducer(record, producerIdManager);
				EventType et = getType(record, etIdManager, eptIdManager);
				
				Event e = new Event(eIdManager.getNextId());
				e.setCpu(record.cpu);
				e.setPage(page);
				e.setTimestamp(record.timestamp);				
				e.setEventProducer(prod);
				e.setType(et);
				if (e.getTimestamp() < minTimestamp)
					minTimestamp = e.getTimestamp();
				if (e.getTimestamp() > maxTimestamp) 
					maxTimestamp = e.getTimestamp();
				
				// duration for last event
				if (last != null) {
					EventParam ep = new EventParam(epIdManager.getNextId());
					ep.setEvent(last);
					ep.setEventParamType(getDurationEventParamType(last.getType()));
					ep.setValue(String.valueOf(e.getTimestamp() - last.getTimestamp()));										
				}
				
				// DRO 10/06/2013 - GStreamer generalization
				// For every key 
				for (String attributeName : record.attributesValue.keySet()) {
					EventParam eventParam = new EventParam(epIdManager.getNextId());
					eventParam.setEvent(e);
					
					// Set the event parameter type and its value
					eventParam.setEventParamType(getEventParamType(record, e.getType(), attributeName));
					eventParam.setValue(record.attributesValue.get(attributeName));
				}

				
				if (!FRAMES_OVERLAPPING) {
					if (record.typeName.equals(FRAME_START_TYPE)) {
						numberOfFrames++;
						startFrameEventTypeId = et.getId();
						frameStartEvents.add(e);
						if (canChangePage(thisPageEvents)) {
							saveEvents(currentPageEvents);
							currentPageEvents.clear();
							thisPageEvents = 0;
							page++;
						}
					} else if (mustChangePage(thisPageEvents)) {
						throw new SoCTraceException("Error: wrong trace -> overlapping frame");
					}
				} else if (mustChangePage(thisPageEvents)){
					saveEvents(currentPageEvents);
					currentPageEvents.clear();
					thisPageEvents = 0;
					page++;					
				}
				
				// only here totEvents and thisPageEvents consider the last one read
				numberOfEvents++;
				thisPageEvents++;
				currentPageEvents.add(e);
				last = e;
			}

			if (thisPageEvents>0) {
				// duration for last event is set to 0 because not known
				EventParam ep = new EventParam(epIdManager.getNextId());
				ep.setEvent(last);
				ep.setEventParamType(getDurationEventParamType(last.getType()));
				ep.setValue("0");
				// save last page
				saveEvents(currentPageEvents);
			}
			
			logger.debug("Saved {} events on {} pages", numberOfEvents, (page+1));
			
		} catch (Exception e) {
			throw new SoCTraceException(e);
		}
		
	}
	
	/** 
	 * Save the events of a page in the trace DB.
	 * 
	 * @param events events list
	 * @throws SoCTraceException
	 */
	private void saveEvents(List<Event> events) throws SoCTraceException {		
		for ( Event e: events ) {
			traceDB.save(e);
			for (EventParam ep: e.getEventParams()) {
				traceDB.save(ep);
			}
		}
		traceDB.commit(); // committing each page is faster
	}

	private EventParamType getDurationEventParamType(EventType et) {
		for (EventParamType ept: et.getEventParamTypes()) {
			if (ept.getName().equals(GStreamerConstants.DURATION_NAME))
				return ept;
		}
		return null;
	}

	/**
	 * DRO 10/06/2013 - GStreamer generalization
	 * This method returns an event parameter type according to its name
	 * @param et
	 * @param attributeName
	 * @return
	 */
	private EventParamType getEventParamType(GStreamerRecord record, EventType et, String attributeName) {
		// The event parameter type to return
		EventParamType eventParamType = null; 
		
		// For every event parameter types 
		for (int j = 0; j < et.getEventParamTypes().size(); j++) {
			// Get the current event parameter type
			eventParamType = et.getEventParamTypes().get(j);
			
			// If we find the event parameter type "attributeName" 
			if(eventParamType.getName().equals(attributeName)) {
				return eventParamType;
			}
		}
		
		return eventParamType;
	}
		
	private EventType getType(GStreamerRecord record, IdManager etIdManager, IdManager eptIdManager) {
		if (!types.containsKey(record.typeName)) {
			EventType et = new EventType(etIdManager.getNextId(), EventCategory.PUNCTUAL_EVENT);
			et.setName(record.typeName);
			EventParamType ept = new EventParamType(eptIdManager.getNextId());
			ept.setEventType(et);
			ept.setName(GStreamerConstants.DURATION_NAME);
			ept.setType(GStreamerConstants.DURATION_TYPE);
			
			// DRO 10/06/2013 - Attributes generation
			// For every key 
			for (String attributeName : record.attributesValue.keySet()) {
				EventParamType eventParamType = new EventParamType(eptIdManager.getNextId());
				eventParamType.setEventType(et);
				eventParamType.setName(attributeName);
				eventParamType.setType("STRING");
			}
			
			types.put(record.typeName, et);
		}
		return types.get(record.typeName);
	}

	private EventProducer getProducer(GStreamerRecord record, IdManager epIdManager) {
		if (!producersMap.containsKey(record.pid)) {
			producersMap.put(record.pid, new HashMap<String, EventProducer>());
		} 
		Map<String, EventProducer> threads = producersMap.get(record.pid);
		if (!threads.containsKey(record.threadId)) {
			EventProducer p = new EventProducer(epIdManager.getNextId());
			p.setLocalId(record.threadId);
			p.setName(record.threadId);
			p.setType(GStreamerConstants.GStreamerEventProducer.THREAD.toString());
			threads.put(record.threadId, p);
		}
		return threads.get(record.threadId);
	}

	private boolean canChangePage(int currentPageEvents) {
		if ( currentPageEvents > GStreamerConstants.PAGE_EXPECTED_SIZE )
			return true;
		if ( GStreamerConstants.PAGE_EXPECTED_SIZE - currentPageEvents < GStreamerConstants.PAGE_SIZE_DELTA)
			return true;
		return false;
	}
	
	private boolean mustChangePage(int currentPageEvents) {
		if ( currentPageEvents >= GStreamerConstants.PAGE_EXPECTED_SIZE )
			return true;
		return false;
	}

	/**
	 * Get an event record from the given reader.
	 * 
	 * @param br reader
	 * @return the record or null if the file is finished
	 * @throws IOException 
	 */
	private GStreamerRecord getEventRecord(BufferedReader br) throws IOException {
		String strLine;
		GStreamerRecord record = null;
		while (record == null) {
			if ((strLine = br.readLine()) == null)
				return null;

			strLine = strLine.trim();
			if( strLine.equals("") ) continue;
			
			if(strLine.startsWith("#")) continue;
				
			// A GStreamer record use a trace header
			record = new GStreamerRecord(GStreamerConstants.DEFAULT_HEADER, strLine);
			
		}
		return record;	
	}


	private boolean checkArgs(ArgumentsManager argsm) {
		if (argsm.getTokens().size() != 1)
			return false;
		return true;
	}
	
	/**
	 * Create the parent producer objects, set their ids in sons,
	 * and save all. Note: only sons are actual event producers.
	 * 
	 * @param producerIdManager
	 * @throws SoCTraceException
	 */
	private void saveProducers(IdManager producerIdManager) throws SoCTraceException {
		Iterator<Entry<Integer, Map<String, EventProducer>>> pit = producersMap.entrySet().iterator();
		while (pit.hasNext()) {
			Entry<Integer, Map<String, EventProducer>> entry = pit.next();
			Integer pid = entry.getKey();
			int parentId = producerIdManager.getNextId();
			EventProducer ep = new EventProducer(parentId);
			ep.setType(GStreamerConstants.GStreamerEventProducer.PROCESS.toString());
			ep.setName(String.valueOf(pid));
			ep.setLocalId(String.valueOf(pid));
			traceDB.save(ep);
			Map<String, EventProducer> threadsMap = entry.getValue();
			for (EventProducer son: threadsMap.values()) {
				son.setParentId(parentId);
				traceDB.save(son);
			}				
		}
	}
	
	/**
	 * Save the event types 
	 * @throws SoCTraceException
	 */
	private void saveTypes() throws SoCTraceException {
		for (EventType et: types.values()) {
			traceDB.save(et);
			for (EventParamType ept: et.getEventParamTypes()) {
				traceDB.save(ept);
			}
		}
	}
}
