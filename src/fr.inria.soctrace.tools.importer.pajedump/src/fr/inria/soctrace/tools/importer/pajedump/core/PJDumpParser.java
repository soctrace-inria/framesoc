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
package fr.inria.soctrace.tools.importer.pajedump.core;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
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
import fr.inria.soctrace.lib.model.Link;
import fr.inria.soctrace.lib.model.PunctualEvent;
import fr.inria.soctrace.lib.model.State;
import fr.inria.soctrace.lib.model.Variable;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.IdManager;

/**
 * PJDump Parser core class.
 * 
 * Warning: the current implementation of this parser works under the hypothesis that a producer may
 * be in a single state at a given time.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PJDumpParser {

	private static final Logger logger = LoggerFactory.getLogger(PJDumpParser.class);

	protected SystemDBObject sysDB;
	protected TraceDBObject traceDB;
	protected String traceFile;
	protected int numberOfEvents = 0;
	protected long minTimestamp;
	protected long maxTimestamp;

	private Map<String, PJDumpLineParser> parserMap = new HashMap<String, PJDumpLineParser>();

	private Map<String, EventProducer> producersMap = new HashMap<String, EventProducer>();
	private Map<String, EventType> types = new HashMap<String, EventType>();
	private int page = 0;
	private IdManager eIdManager = new IdManager();
	private IdManager etIdManager = new IdManager();
	private IdManager epIdManager = new IdManager();
	private List<Event> elist = new LinkedList<Event>();
	private Map<String, List<Link>> endPendingLinks = new HashMap<String, List<Link>>();
	private Map<String, List<Link>> startPendingLinks = new HashMap<String, List<Link>>();
	private long byteRead = 0; // byte read corresponding to events not saved yet
	private boolean doublePrecision = true;

	public PJDumpParser(SystemDBObject sysDB, TraceDBObject traceDB, String traceFile,
			boolean doublePrecision) {

		this.traceFile = traceFile;
		this.sysDB = sysDB;
		this.traceDB = traceDB;

		parserMap.put(PJDumpConstants.CONTAINER, new ContainerParser());
		parserMap.put(PJDumpConstants.EVENT, new EventParser());
		parserMap.put(PJDumpConstants.LINK, new LinkParser());
		parserMap.put(PJDumpConstants.STATE, new StateParser());
		parserMap.put(PJDumpConstants.VARIABLE, new VariableParser());
		this.doublePrecision = doublePrecision;
	}

	/**
	 * 
	 * @param monitor
	 *            progress monitor
	 * @param numberOfTraces
	 * @param currentTrace
	 * @throws SoCTraceException
	 */
	public void parseTrace(IProgressMonitor monitor, int currentTrace, int numberOfTraces)
			throws SoCTraceException {

		logger.debug("Trace file: {}", traceFile);

		try {
			monitor.beginTask("Import trace (" + currentTrace + "/" + numberOfTraces + ")",
					PJDumpConstants.WORK);
			monitor.subTask("Trace file: " + traceFile);
			// Trace Events, EventTypes and Producers
			boolean part = parseRawTrace(monitor);
			saveProducers();
			saveTypes();
			saveTraceMetadata(part);
		} finally {
			monitor.done();
		}

	}

	private long getFileSize(String filename) {
		File file = new File(filename);
		return file.length();
	}

	private boolean parseRawTrace(IProgressMonitor monitor) throws SoCTraceException {

		try {
			boolean partialImport = false;
			numberOfEvents = 0;
			minTimestamp = Long.MAX_VALUE;
			maxTimestamp = Long.MIN_VALUE;
			page = 0;
			elist.clear();

			// we add +1 to file size to avoid dividing by 0
			double scale = ((double) PJDumpConstants.WORK) / (getFileSize(traceFile) + 1);
			// add +1 to the byte read too to compensate
			byteRead = 1;

			BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(
					new FileInputStream(traceFile))));
			String[] line;
			while ((line = getLine(br)) != null) {

				logger.debug(Arrays.toString(line));
				parserMap.get(line[PJDumpConstants.ENTITY]).parseLine(line);

				if (elist.size() == PJDumpConstants.PAGE_SIZE)
					page++;

				if (elist.size() >= PJDumpConstants.PAGE_SIZE && endPendingLinks.isEmpty()
						&& startPendingLinks.isEmpty()) {
					saveEvents(elist);
					monitor.worked(getWorked(scale));
					byteRead = 0;
					numberOfEvents += elist.size();
					elist.clear();
					if (monitor.isCanceled()) {
						if (getLine(br) != null) {
							// there were other lines
							partialImport = true;
						}
						break;
					}
				}
			}

			if (elist.size() > 0) {
				saveEvents(elist);
				monitor.worked(getWorked(scale));
				byteRead = 0;
				numberOfEvents += elist.size();
				elist.clear();
			}

			logger.debug("Saved {} events on {} pages", numberOfEvents, (page + 1));

			return partialImport;

		} catch (Exception e) {
			throw new SoCTraceException(e);
		}

	}

	int getWorked(double scale) {
		return (int) (scale * byteRead);
	}

	/**
	 * Save the events of a page in the trace DB.
	 * 
	 * @param events
	 *            events list
	 * @throws SoCTraceException
	 */
	private void saveEvents(List<Event> events) throws SoCTraceException {
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

	/**
	 * Get an event record from the given reader.
	 * 
	 * @param br
	 *            reader
	 * @return the record or null if the file is finished
	 * @throws IOException
	 */
	private String[] getLine(BufferedReader br) throws IOException {
		String strLine = null;
		String[] args = null;
		while (args == null) {
			if ((strLine = br.readLine()) == null)
				return null;

			byteRead += strLine.length() + 1;

			strLine = strLine.trim();
			if (strLine.equals(""))
				continue;
			if (strLine.startsWith("#"))
				continue;

			args = strLine.split(PJDumpConstants.SEPARATOR);
		}
		return args;
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

	protected void saveTraceMetadata(boolean partialImport) throws SoCTraceException {
		String alias = FilenameUtils.getBaseName(traceFile);
		String realAlias = (partialImport) ? (alias + " [part]") : alias;
		PJDumpTraceMetadata metadata = new PJDumpTraceMetadata(sysDB, traceDB.getDBName(),
				realAlias, numberOfEvents, minTimestamp, maxTimestamp);
		metadata.createMetadata();
		metadata.saveMetadata();
	}

	private void updateMinMax(long timestamp) {
		if (timestamp < minTimestamp)
			minTimestamp = timestamp;
		if (timestamp > maxTimestamp)
			maxTimestamp = timestamp;
	}

	private EventType getType(String name, int category) {
		if (!types.containsKey(name)) {
			EventType et = new EventType(etIdManager.getNextId(), category);
			et.setName(name);
			types.put(name, et);
		}
		return types.get(name);
	}

	private int getEventProducerId(String name) {
		if (!producersMap.containsKey(name)) {
			return -1;
		}
		return producersMap.get(name).getId();
	}

	private long getTimestamp(String ts) {
		if (doublePrecision) {
			Double timestamp = Double.parseDouble(ts);
			timestamp = Math.pow(10, PJDumpConstants.TIME_SHIFT) * timestamp;
			return timestamp.longValue();
		} else {
			return Long.parseLong(ts);
		}
	}

	// entity parsers

	private class EventParser implements PJDumpLineParser {
		public void parseLine(String[] fields) throws SoCTraceException {
			PunctualEvent e = new PunctualEvent(eIdManager.getNextId());
			e.setEventProducer(producersMap.get(fields[PJDumpConstants.E_CONTAINER]));
			e.setPage(page);
			e.setTimestamp(getTimestamp(fields[PJDumpConstants.E_TIME]));
			e.setType(getType(fields[PJDumpConstants.E_VALUE], EventCategory.PUNCTUAL_EVENT));
			elist.add(e);
			updateMinMax(e.getTimestamp());
		}
	}

	private class LinkParser implements PJDumpLineParser {
		public void parseLine(String[] fields) throws SoCTraceException {
			Link l = new Link(eIdManager.getNextId());
			l.setPage(page);
			l.setTimestamp(getTimestamp(fields[PJDumpConstants.L_START_TIME]));
			l.setType(getType(fields[PJDumpConstants.L_VALUE], EventCategory.LINK));
			l.setEndTimestamp(getTimestamp(fields[PJDumpConstants.L_END_TIME]));
			elist.add(l);

			// start producer
			if (producersMap.containsKey(fields[PJDumpConstants.L_START_CONTAINER])) {
				l.setEventProducer(producersMap.get(fields[PJDumpConstants.L_START_CONTAINER]));
			} else {
				if (!startPendingLinks.containsKey(fields[PJDumpConstants.L_START_CONTAINER])) {
					startPendingLinks.put(fields[PJDumpConstants.L_START_CONTAINER],
							new LinkedList<Link>());
				}
				startPendingLinks.get(fields[PJDumpConstants.L_START_CONTAINER]).add(l);
			}

			// end producer
			if (producersMap.containsKey(fields[PJDumpConstants.L_END_CONTAINER])) {
				l.setEndProducer(producersMap.get(fields[PJDumpConstants.L_END_CONTAINER]));
			} else {
				if (!endPendingLinks.containsKey(fields[PJDumpConstants.L_END_CONTAINER])) {
					endPendingLinks.put(fields[PJDumpConstants.L_END_CONTAINER],
							new LinkedList<Link>());
				}
				endPendingLinks.get(fields[PJDumpConstants.L_END_CONTAINER]).add(l);
			}
			updateMinMax(l.getTimestamp());
			updateMinMax(l.getEndTimestamp());
		}
	}

	private class StateParser implements PJDumpLineParser {
		public void parseLine(String[] fields) throws SoCTraceException {
			State s = new State(eIdManager.getNextId());
			s.setEventProducer(producersMap.get(fields[PJDumpConstants.S_CONTAINER]));
			s.setPage(page);
			s.setTimestamp(getTimestamp(fields[PJDumpConstants.S_START_TIME]));
			s.setType(getType(fields[PJDumpConstants.S_VALUE], EventCategory.STATE));
			s.setEndTimestamp(getTimestamp(fields[PJDumpConstants.S_END_TIME]));
			s.setImbricationLevel(Integer.valueOf(fields[PJDumpConstants.S_IMBRICATION]));
			elist.add(s);
			updateMinMax(s.getTimestamp());
			updateMinMax(s.getEndTimestamp());
		}
	}

	private class VariableParser implements PJDumpLineParser {
		public void parseLine(String[] fields) throws SoCTraceException {
			Variable v = new Variable(eIdManager.getNextId());
			v.setEventProducer(producersMap.get(fields[PJDumpConstants.V_CONTAINER]));
			v.setPage(page);
			v.setTimestamp(getTimestamp(fields[PJDumpConstants.V_START_TIME]));
			v.setType(getType(fields[PJDumpConstants.V_TYPE], EventCategory.VARIABLE));
			v.setValue(Double.valueOf(fields[PJDumpConstants.V_VALUE]));
			v.setEndTimestamp(0);
			elist.add(v);
			updateMinMax(v.getTimestamp());
		}
	}

	private class ContainerParser implements PJDumpLineParser {
		public void parseLine(String[] fields) {
			if (producersMap.containsKey(fields[PJDumpConstants.C_NAME]))
				return;
			EventProducer ep = new EventProducer(epIdManager.getNextId());
			ep.setName(fields[PJDumpConstants.C_NAME]);
			ep.setParentId(getEventProducerId(fields[PJDumpConstants.C_PARENT_CONTAINER]));
			ep.setType(fields[PJDumpConstants.C_TYPE]);
			ep.setLocalId(String.valueOf(ep.getId()));
			producersMap.put(ep.getName(), ep);
			if (endPendingLinks.containsKey(ep.getName())) {
				for (Link l : endPendingLinks.get(ep.getName())) {
					l.setEndProducer(ep);
				}
				endPendingLinks.remove(ep.getName());
			}
			if (startPendingLinks.containsKey(ep.getName())) {
				for (Link l : startPendingLinks.get(ep.getName())) {
					l.setEventProducer(ep);
				}
				startPendingLinks.remove(ep.getName());
			}
		}
	}

}
