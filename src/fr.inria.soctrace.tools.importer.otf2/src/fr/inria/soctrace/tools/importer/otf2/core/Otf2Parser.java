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

import java.io.BufferedReader;
import java.util.ArrayList;
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
import fr.inria.soctrace.lib.model.State;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.tools.importer.otf2.reader.Otf2PrintWrapper;

/**
 * Otf2 Parser core class.
 * 
 * TODO
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class Otf2Parser {

	private static final Logger logger = LoggerFactory
			.getLogger(Otf2Parser.class);

	private SystemDBObject sysDB;
	private TraceDBObject traceDB;
	private String traceFile;

	private Map<String, EventProducer> producersMap = new HashMap<String, EventProducer>();
	/**
	 * Event producers indexed by their in-trace id for easier access during
	 * parsing
	 */
	private Map<Integer, EventProducer> idProducersMap = new HashMap<Integer, EventProducer>();
	private Map<String, EventType> types = new HashMap<String, EventType>();
	private List<Event> eventList = new LinkedList<Event>();
	private int numberOfEvents = 0;
	private long minTimestamp = -1;
	private long maxTimestamp = -1;
	private int page = 0;

	// Starting time of the time stamp to avoid having huge timestamps
	private long timeOffset = 0;
	private long timeGranularity = -1;

	private Map<String, Otf2LineParser> parserMap = new HashMap<String, Otf2LineParser>();
	private Map<String, String> eventCategory = new HashMap<String, String>();

	/**
	 * Keep the current states for each event producer It must be able to hold
	 * several states since it is possible to have embedded states
	 */
	private HashMap<EventProducer, List<State>> stateMaps = new HashMap<EventProducer, List<State>>();
	/**
	 * Keep the communication links for each sending event producer
	 */
	private HashMap<EventProducer, List<Link>> linkMaps = new HashMap<EventProducer, List<Link>>();

	private IdManager eIdManager = new IdManager();

	public Otf2Parser(SystemDBObject sysDB, TraceDBObject traceDB,
			String traceFile) {
		this.traceFile = traceFile;
		this.sysDB = sysDB;
		this.traceDB = traceDB;

		parserMap.put(Otf2Constants.EVENT, new EventParser());
		parserMap.put(Otf2Constants.LINK, new LinkParser());
		parserMap.put(Otf2Constants.STATE, new StateParser());
		// TODO Does OTF2 contains variables (METRIC ?)
		// parserMap.put(Otf2Constants.VARIABLE, new VariableParser());

		eventCategory.put(Otf2Constants.MPI_IRECV_REQUEST, Otf2Constants.EVENT);
		eventCategory
				.put(Otf2Constants.MPI_ISEND_COMPLETE, Otf2Constants.EVENT);
		eventCategory.put(Otf2Constants.MPI_COLLECTIVE_BEGIN,
				Otf2Constants.EVENT);
		eventCategory
				.put(Otf2Constants.MPI_COLLECTIVE_END, Otf2Constants.EVENT);
		eventCategory.put(Otf2Constants.METRIC, Otf2Constants.EVENT);

		eventCategory.put(Otf2Constants.ENTER_STATE, Otf2Constants.STATE);
		eventCategory.put(Otf2Constants.LEAVE_STATE, Otf2Constants.STATE);

		eventCategory.put(Otf2Constants.MPI_RECV, Otf2Constants.LINK);
		eventCategory.put(Otf2Constants.MPI_IRECV, Otf2Constants.LINK);
		eventCategory.put(Otf2Constants.MPI_SEND, Otf2Constants.LINK);
		eventCategory.put(Otf2Constants.MPI_ISEND, Otf2Constants.LINK);
	}

	public String getTraceFile() {
		return traceFile;
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

			// preparse
			if (!preparse(monitor))
				return;

			// parse
			boolean complete = parse(monitor);

			saveProducers();
			saveTypes();
			saveTraceMetadata(!complete);
		} finally {
			monitor.done();
		}
	}

	/**
	 * Provide event producers and event types
	 * 
	 * @param monitor
	 * @return true if the process was not cancelled, false otherwise
	 * @throws SoCTraceException
	 */
	private boolean preparse(IProgressMonitor monitor) throws SoCTraceException {
		Otf2PreParser aPreParser = new Otf2PreParser(this);
		aPreParser.parseDefinitons(monitor);
		return !monitor.isCanceled();
	}

	/**
	 * Parse and build the actual events
	 * 
	 * @param monitor
	 * @return true if the process was not cancelled, false otherwise
	 * @throws SoCTraceException
	 */
	private boolean parse(IProgressMonitor monitor) throws SoCTraceException {
		try {
			monitor.subTask("Getting events");
			List<String> args = new ArrayList<String>();
			args.add(getTraceFile());
			Otf2PrintWrapper wrapper = new Otf2PrintWrapper(args);
			BufferedReader br = wrapper.execute(monitor);

			String line;
			while ((line = br.readLine()) != null && !monitor.isCanceled()) {
				if (line.isEmpty() || !line.contains(" "))
					continue;

				String keyword = line.substring(0, line.indexOf(" "));

				// Is the keyword supported by our parser ?
				if (!eventCategory.containsKey(keyword)) {
					logger.debug("Warning: unsupported keyword encountered: "
							+ keyword);
					continue;
				}

				// Build an event
				parserMap.get(eventCategory.get(keyword)).parseLine(keyword,
						line);

				if (eventList.size() == Otf2Constants.PAGE_SIZE)
					page++;

				if (eventList.size() >= Otf2Constants.PAGE_SIZE) {
					saveEvents(eventList);

					numberOfEvents += eventList.size();
					eventList.clear();
				}
			}

			// Are there non saved events after we have finished parsing the
			// trace?
			if (eventList.size() > 0) {
				saveEvents(eventList);
				numberOfEvents += eventList.size();
				eventList.clear();
			}

			br.close();
		} catch (Exception e) {
			throw new SoCTraceException(e);
		}
		return !monitor.isCanceled();
	}

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

	private void saveProducers() throws SoCTraceException {
		Collection<EventProducer> eps = getProducersMap().values();
		for (EventProducer ep : eps) {
			traceDB.save(ep);
		}
		traceDB.commit();
	}

	private void saveTypes() throws SoCTraceException {
		for (EventType et : getTypes().values()) {
			traceDB.save(et);
			for (EventParamType ept : et.getEventParamTypes()) {
				traceDB.save(ept);
			}
		}
	}

	private void saveTraceMetadata(boolean partialImport)
			throws SoCTraceException {
		String alias = FilenameUtils.getBaseName(traceFile);
		String realAlias = (partialImport) ? (alias + " [part]") : alias;
		Otf2TraceMetadata metadata = new Otf2TraceMetadata(sysDB,
				traceDB.getDBName(), realAlias);
		metadata.setNumberOfEvents(numberOfEvents);
		metadata.setMinTimestamp(minTimestamp);
		metadata.setMaxTimestamp(maxTimestamp);
		metadata.setTimeUnit(metadata.getTimeUnit(timeGranularity));
		metadata.createMetadata();
		metadata.saveMetadata();
	}

	public Map<String, EventType> getTypes() {
		return types;
	}

	public void setTypes(Map<String, EventType> types) {
		this.types = types;
	}

	public Map<Integer, EventProducer> getIdProducersMap() {
		return idProducersMap;
	}

	public void setIdProducersMap(Map<Integer, EventProducer> idProducersMap) {
		this.idProducersMap = idProducersMap;
	}

	public Map<String, EventProducer> getProducersMap() {
		return producersMap;
	}

	public void setProducersMap(Map<String, EventProducer> producersMap) {
		this.producersMap = producersMap;
	}

	public long getTimeOffset() {
		return timeOffset;
	}

	public void setTimeOffset(long timeOffset) {
		this.timeOffset = timeOffset;
	}

	public long getTimeGranularity() {
		return timeGranularity;
	}

	public void setTimeGranularity(long timeGranularity) {
		this.timeGranularity = timeGranularity;
	}

	private class EventParser implements Otf2LineParser {
		public void parseLine(String keyword, String line)
				throws SoCTraceException {
			parseEvent(keyword, line);
		}

		private void parseEvent(String keyword, String aLine)
				throws SoCTraceException {
			Event anEvent = new Event(eIdManager.getNextId());

			long timeStamp;
			int epId;
			String eventName = "";
			if (keyword.equals(Otf2Constants.MPI_COLLECTIVE_BEGIN)
					|| keyword.equals(Otf2Constants.MPI_COLLECTIVE_END))
				eventName = Otf2Constants.MPI_COLLECTIVE;

			if (keyword.equals(Otf2Constants.MPI_IRECV_REQUEST))
				eventName = Otf2Constants.MPI_RECEIVE_REQUEST;

			if (keyword.equals(Otf2Constants.MPI_ISEND_COMPLETE))
				eventName = Otf2Constants.MPI_SEND_COMPLETE;

			if (keyword.equals(Otf2Constants.METRIC))
				eventName = Otf2Constants.MPI_METRIC;

			String conf = aLine.substring(keyword.length());
			conf = conf.trim();

			// Get producer id
			String epIdString = conf.substring(0, conf.indexOf(" "));
			epId = Integer.valueOf(epIdString);
			conf = conf.substring(epIdString.length());
			conf = conf.trim();

			// Get timestamp
			String timeStampString;
			// Are there more parameters in the line?
			if (conf.contains(" "))
				timeStampString = conf.substring(0, conf.indexOf(" "));
			else
				timeStampString = conf;
			timeStamp = Long.valueOf(timeStampString) - timeOffset;
			conf = conf.substring(timeStampString.length());
			conf = conf.trim();

			EventProducer anEp = getIdProducersMap().get(epId);
			anEvent.setEventProducer(anEp);
			anEvent.setTimestamp(timeStamp);
			anEvent.setType(getTypes().get(eventName));

			if (minTimestamp == -1)
				minTimestamp = timeStamp;

			eventList.add(anEvent);
		}

	}

	private class StateParser implements Otf2LineParser {
		public void parseLine(String keyword, String line)
				throws SoCTraceException {
			// Getting in a new state
			if (keyword.equals(Otf2Constants.ENTER_STATE)) {
				parseEnteringState(line);
			}
			// Getting out of a state
			if (keyword.equals(Otf2Constants.LEAVE_STATE)) {
				parseLeavingState(line);
			}
		}

		private void parseEnteringState(String aLine) throws SoCTraceException {
			State aState = new State(eIdManager.getNextId());

			long timeStamp;
			int epId;
			String eventName = "";

			String conf = aLine.substring(Otf2Constants.ENTER_STATE.length());
			conf = conf.trim();

			// Get producer id
			String epIdString = conf.substring(0, conf.indexOf(" "));
			epId = Integer.valueOf(epIdString);
			conf = conf.substring(epIdString.length());
			conf = conf.trim();

			// Get timestamp
			String timeStampString = conf.substring(0, conf.indexOf(" "));
			timeStamp = Long.valueOf(timeStampString) - timeOffset;
			conf = conf.substring(timeStampString.length());
			conf = conf.trim();

			// Get event type
			String[] groupProperty = conf
					.split(Otf2Constants.PARAMETER_SEPARATOR);
			eventName = groupProperty[1].trim();
			int indexOfFirstQuote = eventName.indexOf("\"") + 1;
			eventName = eventName.substring(indexOfFirstQuote,
					eventName.indexOf("\"", indexOfFirstQuote));

			EventProducer anEp = getIdProducersMap().get(epId);
			aState.setEventProducer(anEp);
			aState.setTimestamp(timeStamp);
			aState.setType(getTypes().get(eventName));

			if (minTimestamp == -1)
				minTimestamp = timeStamp;

			if (!stateMaps.containsKey(anEp))
				stateMaps.put(anEp, new ArrayList<State>());

			// Add the state to the current states of the event producers
			stateMaps.get(anEp).add(aState);

			aState.setImbricationLevel(stateMaps.get(anEp).indexOf(aState));
		}

		private void parseLeavingState(String aLine) {
			long timeStamp;
			int epId;
			String eventName = "";

			String conf = aLine.substring(Otf2Constants.ENTER_STATE.length());
			conf = conf.trim();

			// Get producer id
			String epIdString = conf.substring(0, conf.indexOf(" "));
			epId = Integer.valueOf(epIdString);
			conf = conf.substring(epIdString.length());
			conf = conf.trim();

			// Get timestamp
			String timeStampString = conf.substring(0, conf.indexOf(" "));
			timeStamp = Long.valueOf(timeStampString) - timeOffset;
			conf = conf.substring(timeStampString.length());
			conf = conf.trim();

			// Get event type
			String[] groupProperty = conf
					.split(Otf2Constants.PARAMETER_SEPARATOR);
			eventName = groupProperty[1].trim();
			int indexOfFirstQuote = eventName.indexOf("\"") + 1;
			eventName = eventName.substring(indexOfFirstQuote,
					eventName.indexOf("\"", indexOfFirstQuote));

			EventProducer anEp = getIdProducersMap().get(epId);
			State aState = null;
			// Retrieve the state based on event type
			for (State state : stateMaps.get(anEp)) {
				if (state.getType() == getTypes().get(eventName)) {
					// Is there more than one state of the same type embedded?
					if (aState != null) {
						logger.warn("Warning: Several states of the same type are embedded.");
					}
					aState = state;
				}
			}

			if (maxTimestamp < timeStamp)
				maxTimestamp = timeStamp;

			// Remove state of the current states
			stateMaps.get(anEp).remove(aState);

			aState.setEndTimestamp(timeStamp);
			aState.setPage(page);
			eventList.add(aState);
		}
	}

	private class LinkParser implements Otf2LineParser {
		public void parseLine(String keyword, String line)
				throws SoCTraceException {
			// Starting a communication
			if (keyword.equals(Otf2Constants.MPI_ISEND)
					|| keyword.equals(Otf2Constants.MPI_SEND))
				parseSend(keyword, line);

			// Finishing a communication
			if (keyword.equals(Otf2Constants.MPI_IRECV)
					|| keyword.equals(Otf2Constants.MPI_RECV))
				parseReceive(keyword, line);
		}

		private void parseSend(String keyword, String aLine)
				throws SoCTraceException {
			Link aLink = new Link(eIdManager.getNextId());

			long timeStamp;
			int epId;
			String eventName = Otf2Constants.MPI_COMM;

			String conf = aLine.substring(keyword.length());
			conf = conf.trim();

			// Get producer id
			String epIdString = conf.substring(0, conf.indexOf(" "));
			epId = Integer.valueOf(epIdString);
			conf = conf.substring(epIdString.length());
			conf = conf.trim();

			// Get timestamp
			String timeStampString = conf.substring(0, conf.indexOf(" "));
			timeStamp = Long.valueOf(timeStampString) - timeOffset;
			conf = conf.substring(timeStampString.length());
			conf = conf.trim();

			// Get receiver id
			String[] groupProperty = conf
					.split(Otf2Constants.PROPERTY_SEPARATOR);
			String[] groupParameter = groupProperty[0]
					.split(Otf2Constants.PARAMETER_SEPARATOR);
			groupParameter[1] = groupParameter[1].trim();
			String receiverIdString = groupParameter[1].substring(0,
					groupParameter[1].indexOf(" "));

			EventProducer anEp = getIdProducersMap().get(epId);
			aLink.setEventProducer(anEp);
			aLink.setTimestamp(timeStamp);
			aLink.setEndProducer(getIdProducersMap().get(
					Integer.valueOf(receiverIdString)));
			aLink.setType(getTypes().get(eventName));

			if (minTimestamp == -1)
				minTimestamp = timeStamp;

			if (!linkMaps.containsKey(anEp))
				linkMaps.put(anEp, new ArrayList<Link>());

			// Add the link to the unfinished communication links of the sending
			// producer
			linkMaps.get(anEp).add(aLink);
		}

		private void parseReceive(String keyword, String aLine) {
			long timeStamp;
			int epId;

			String conf = aLine.substring(keyword.length());
			conf = conf.trim();

			// Get producer id
			String epIdString = conf.substring(0, conf.indexOf(" "));
			epId = Integer.valueOf(epIdString);
			conf = conf.substring(epIdString.length());
			conf = conf.trim();

			// Get timestamp
			String timeStampString = conf.substring(0, conf.indexOf(" "));
			timeStamp = Long.valueOf(timeStampString) - timeOffset;
			conf = conf.substring(timeStampString.length());
			conf = conf.trim();

			// Get sender id
			String[] groupProperty = conf
					.split(Otf2Constants.PROPERTY_SEPARATOR);
			String[] groupParameter = groupProperty[0]
					.split(Otf2Constants.PARAMETER_SEPARATOR);
			groupParameter[1] = groupParameter[1].trim();
			String senderIdString = groupParameter[1].substring(0,
					groupParameter[1].indexOf(" "));
			int senderId = Integer.valueOf(senderIdString);

			Link aLink = null;

			// If we receive a communication but did not recorded the sending
			if (!linkMaps.containsKey(getIdProducersMap().get(senderId))) {
				// State an error and discard the event
				logger.error(keyword + ": Unknown link with senderId "
						+ senderId);
				return;
			}

			// Retrieve the link based on the sender and receiver IDs
			for (Link link : linkMaps.get(getIdProducersMap().get(senderId))) {
				if (Integer.valueOf(link.getEndProducer().getLocalId()) == epId) {
					// Do we have more than one link with the same sender and
					// receiver IDs ?
					if (aLink != null) {
						logger.warn("Warning: Several links of the same type are embedded.");
					}
					aLink = link;
				}
			}

			// No link was found (i.e. the sending was not recorded)
			if (aLink == null) {
				// State an error and discard the event
				logger.error(keyword + ": Null link.");
				return;
			}

			if (maxTimestamp < timeStamp)
				maxTimestamp = timeStamp;

			// Remove the link
			linkMaps.get(getIdProducersMap().get(senderId)).remove(aLink);

			aLink.setEndTimestamp(timeStamp);
			aLink.setPage(page);
			eventList.add(aLink);
		}
	}

}
