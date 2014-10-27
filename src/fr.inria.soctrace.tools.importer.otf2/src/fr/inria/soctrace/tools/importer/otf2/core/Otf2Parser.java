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
package fr.inria.soctrace.tools.importer.otf2.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventParam;
import fr.inria.soctrace.lib.model.EventParamType;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;

/**
 * Otf2 Parser core class.
 * 
 * TODO
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class Otf2Parser {

	private static final Logger logger = LoggerFactory.getLogger(Otf2Parser.class);

	private SystemDBObject sysDB;
	private TraceDBObject traceDB;
	private String traceFile;

	public Map<String, EventProducer> producersMap = new HashMap<String, EventProducer>();
	public Map<Integer, EventProducer> idProducersMap = new HashMap<Integer, EventProducer>();
	public Map<String, EventType> types = new HashMap<String, EventType>();
	public List<Event> elist = new LinkedList<Event>();
	public int numberOfEvents = 0;
	public long minTimestamp = -1;
	public long maxTimestamp = -1;
	// Start of the time stamp so that we avoid having big timestamp
	public long timeOffset = 0;
	public int page = 0;
	
	public Otf2Parser(SystemDBObject sysDB, TraceDBObject traceDB, String traceFile) {
		this.traceFile = traceFile;
		this.sysDB = sysDB;
		this.traceDB = traceDB;
	}

	/**
	 * Parser entry point.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @throws SoCTraceException
	 */
	public void parseTrace(IProgressMonitor monitor) throws SoCTraceException {

		logger.debug("Trace file: {}", traceFile);

		try {
			monitor.beginTask("Import trace " + traceFile, Otf2Constants.WORK);
			// Trace Events, EventTypes and Producers
			boolean complete = parseRawTrace(monitor);
			saveProducers();
			saveTypes();
			saveTraceMetadata(!complete);
		} finally {
			monitor.done();
		}

	}

	/**
	 * Main parsing method
	 * 
	 * @param monitor
	 *            progress monitor
	 * @return true if the trace was completely imported
	 * @throws SoCTraceException
	 */
	private boolean parseRawTrace(IProgressMonitor monitor) throws SoCTraceException {
		Otf2PreParser aPreParse = new Otf2PreParser(this);
		aPreParse.parseDef();
		
		Otf2StateParser aStateParser = new  Otf2StateParser(this);
		aStateParser.parseState(monitor);
		return true;
	}

	public void saveEvents(List<Event> events) throws SoCTraceException {
		for (Event e : events) {
			try {
				e.check();
			} catch (SoCTraceException ex) {
				logger.debug(ex.getMessage());
				throw new SoCTraceException(ex);
			}
			traceDB.save(e);
			for (EventParam ep : e.getEventParams()) {
				traceDB.save(ep);
			}
		}
		traceDB.commit(); // committing each page is faster
	}
	
	private void saveProducers() throws SoCTraceException {
		Collection<EventProducer> eps = producersMap.values();
		for (EventProducer ep : eps) {
			traceDB.save(ep);
		}
		traceDB.commit();
	}

	private void saveTypes() throws SoCTraceException {
		for (EventType et : types.values()) {
			traceDB.save(et);
			for (EventParamType ept : et.getEventParamTypes()) {
				traceDB.save(ept);
			}
		}
	}

	private void saveTraceMetadata(boolean partialImport) throws SoCTraceException {
		String alias = FilenameUtils.getBaseName(traceFile);
		String realAlias = (partialImport) ? (alias + " [part]") : alias;
		Otf2TraceMetadata metadata = new Otf2TraceMetadata(sysDB, traceDB.getDBName(), realAlias);
		metadata.setNumberOfEvents(numberOfEvents);
		metadata.setMinTimestamp(minTimestamp);
		metadata.setMaxTimestamp(maxTimestamp);
		metadata.createMetadata();
		metadata.saveMetadata();
	}

}
