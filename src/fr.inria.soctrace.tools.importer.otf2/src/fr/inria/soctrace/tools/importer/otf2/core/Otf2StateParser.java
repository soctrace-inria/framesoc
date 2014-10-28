package fr.inria.soctrace.tools.importer.otf2.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.Link;
import fr.inria.soctrace.lib.model.State;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.tools.importer.otf2.reader.Otf2PrintWrapper;

public class Otf2StateParser {

	private static final Logger logger = LoggerFactory
			.getLogger(Otf2StateParser.class);

	private Otf2Parser theParser;
	private IdManager eIdManager = new IdManager();

	// Keep the current states for each event producer
	// It must be able to hold several states since it is possible to have state
	// embedding
	private HashMap<EventProducer, List<State>> stateMaps = new HashMap<EventProducer, List<State>>();
	private List<String> uniqueKeyword = new ArrayList<String>();

	private HashMap<EventProducer, List<Link>> linkMaps = new HashMap<EventProducer, List<Link>>();

	private Map<String, Otf2LineParser> parserMap = new HashMap<String, Otf2LineParser>();
	private Map<String, String> eventCategory = new HashMap<String, String>();

	public Otf2StateParser(Otf2Parser aParser) {
		theParser = aParser;

		parserMap.put(Otf2Constants.EVENT, new EventParser());
		parserMap.put(Otf2Constants.LINK, new LinkParser());
		parserMap.put(Otf2Constants.STATE, new StateParser());
		// parserMap.put(Otf2Constants.VARIABLE, new VariableParser());

		eventCategory.put(Otf2Constants.MPI_IRECV_REQUEST, Otf2Constants.EVENT);
		eventCategory
		.put(Otf2Constants.MPI_ISEND_COMPLETE, Otf2Constants.EVENT);
		eventCategory.put(Otf2Constants.MPI_COLLECTIVE_BEGIN,
				Otf2Constants.EVENT);
		eventCategory
				.put(Otf2Constants.MPI_COLLECTIVE_END, Otf2Constants.EVENT);
		eventCategory
		.put(Otf2Constants.METRIC, Otf2Constants.EVENT);
	
	

		eventCategory.put(Otf2Constants.ENTER_STATE, Otf2Constants.STATE);
		eventCategory.put(Otf2Constants.LEAVE_STATE, Otf2Constants.STATE);

		eventCategory.put(Otf2Constants.MPI_RECV, Otf2Constants.LINK);
		eventCategory.put(Otf2Constants.MPI_IRECV, Otf2Constants.LINK);
		eventCategory.put(Otf2Constants.MPI_SEND, Otf2Constants.LINK);
		eventCategory.put(Otf2Constants.MPI_ISEND, Otf2Constants.LINK);
	}

	public void parseState(IProgressMonitor monitor) {

		try {
			List<String> args = new ArrayList<String>();
			args.add(theParser.getTraceFile());
			Otf2PrintWrapper wrapper = new Otf2PrintWrapper(args);
			BufferedReader br = wrapper.execute(monitor);

			String line;
			while ((line = br.readLine()) != null && !monitor.isCanceled()) {
				if (line.isEmpty() || !line.contains(" "))
					continue;

				String keyword = line.substring(0, line.indexOf(" "));
				if (!eventCategory.containsKey(keyword)) {
					 logger.debug("Unknown keyword encountered: " + keyword);
					if (!uniqueKeyword.contains(keyword))
						uniqueKeyword.add(keyword);
					continue;
				}

				parserMap.get(eventCategory.get(keyword)).parseLine(keyword,
						line);

				if (!uniqueKeyword.contains(keyword))
					uniqueKeyword.add(keyword);

				if (theParser.eventList.size() == Otf2Constants.PAGE_SIZE)
					theParser.page++;

				if (theParser.eventList.size() >= Otf2Constants.PAGE_SIZE) {
					theParser.saveEvents(theParser.eventList);

					theParser.numberOfEvents += theParser.eventList.size();
					theParser.eventList.clear();
				}
			}

			if (theParser.eventList.size() > 0) {
				theParser.saveEvents(theParser.eventList);
				theParser.numberOfEvents += theParser.eventList.size();
				theParser.eventList.clear();
			}

			for (String kWord : uniqueKeyword) {
				System.out.println(kWord);
			}

			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private class EventParser implements Otf2LineParser {
		public void parseLine(String keyword, String line)
				throws SoCTraceException {

				parseEvent(keyword, line);

		}

		public void parseEvent(String keyword, String aLine) {
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
			if (conf.contains(" "))
				timeStampString = conf.substring(0, conf.indexOf(" "));
			else
				timeStampString = conf;
			timeStamp = Long.valueOf(timeStampString) - theParser.timeOffset;
			conf = conf.substring(timeStampString.length());
			conf = conf.trim();

			EventProducer anEp = theParser.idProducersMap.get(epId);
			anEvent.setEventProducer(anEp);
			anEvent.setTimestamp(timeStamp);
			try {
				anEvent.setType(theParser.types.get(eventName));
			} catch (SoCTraceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (theParser.minTimestamp == -1)
				theParser.minTimestamp = timeStamp;

			theParser.eventList.add(anEvent);
		}

	}
	
	private class StateParser implements Otf2LineParser {
		public void parseLine(String keyword, String line)
				throws SoCTraceException {
			if (keyword.equals(Otf2Constants.ENTER_STATE)) {
				parseEnteringState(line);
			} else if (keyword.equals(Otf2Constants.LEAVE_STATE)) {
				parseLeavingState(line);
			}
		}

		public void parseEnteringState(String aLine) {
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
			timeStamp = Long.valueOf(timeStampString) - theParser.timeOffset;
			conf = conf.substring(timeStampString.length());
			conf = conf.trim();

			// Get event type
			String[] groupProperty = conf
					.split(Otf2Constants.PARAMETER_SEPARATOR);
			eventName = groupProperty[1].trim();
			int indexOfFirstQuote = eventName.indexOf("\"") + 1;
			eventName = eventName.substring(indexOfFirstQuote,
					eventName.indexOf("\"", indexOfFirstQuote));

			EventProducer anEp = theParser.idProducersMap.get(epId);
			aState.setEventProducer(anEp);
			aState.setTimestamp(timeStamp);
			try {
				aState.setType(theParser.types.get(eventName));
			} catch (SoCTraceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (theParser.minTimestamp == -1)
				theParser.minTimestamp = timeStamp;

			if (!stateMaps.containsKey(anEp))
				stateMaps.put(anEp, new ArrayList<State>());

			stateMaps.get(anEp).add(aState);

			aState.setImbricationLevel(stateMaps.get(anEp).indexOf(aState));
		}


		public void parseLeavingState(String aLine) {
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
			timeStamp = Long.valueOf(timeStampString) - theParser.timeOffset;
			conf = conf.substring(timeStampString.length());
			conf = conf.trim();

			// Get event type
			String[] groupProperty = conf
					.split(Otf2Constants.PARAMETER_SEPARATOR);
			eventName = groupProperty[1].trim();
			int indexOfFirstQuote = eventName.indexOf("\"") + 1;
			eventName = eventName.substring(indexOfFirstQuote,
					eventName.indexOf("\"", indexOfFirstQuote));

			EventProducer anEp = theParser.idProducersMap.get(epId);
			State aState = null;
			for (State state : stateMaps.get(anEp)) {
				if (state.getType() == theParser.types.get(eventName)) {
					if (aState != null) {
						System.err
								.println("Warning: Several states of the same type are embedded.");
					}
					aState = state;
				}
			}

			if (theParser.maxTimestamp < timeStamp)
				theParser.maxTimestamp = timeStamp;

			stateMaps.get(anEp).remove(aState);

			aState.setEndTimestamp(timeStamp);
			aState.setPage(theParser.page);
			theParser.eventList.add(aState);
		}

	}

	private class LinkParser implements Otf2LineParser {
		public void parseLine(String keyword, String line)
				throws SoCTraceException {
			if (keyword.equals(Otf2Constants.MPI_ISEND)
					|| keyword.equals(Otf2Constants.MPI_SEND))
				parseSend(keyword, line);

			if (keyword.equals(Otf2Constants.MPI_IRECV)
					|| keyword.equals(Otf2Constants.MPI_RECV))
				parseReceive(keyword, line);
		}

		public void parseSend(String keyword, String aLine) {
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
			timeStamp = Long.valueOf(timeStampString) - theParser.timeOffset;
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

			EventProducer anEp = theParser.idProducersMap.get(epId);
			aLink.setEventProducer(anEp);
			aLink.setTimestamp(timeStamp);
			aLink.setEndProducer(theParser.idProducersMap.get(Integer
					.valueOf(receiverIdString)));

			try {
				aLink.setType(theParser.types.get(eventName));
			} catch (SoCTraceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (theParser.minTimestamp == -1)
				theParser.minTimestamp = timeStamp;

			if (!linkMaps.containsKey(anEp))
				linkMaps.put(anEp, new ArrayList<Link>());

			linkMaps.get(anEp).add(aLink);
		}

		public void parseReceive(String keyword, String aLine) {
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
			timeStamp = Long.valueOf(timeStampString) - theParser.timeOffset;
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

			EventProducer anEp = theParser.idProducersMap.get(epId);
			Link aLink = null;

			if (!linkMaps.containsKey(theParser.idProducersMap.get(senderId))) {
				System.err.println(keyword + ": Unknown link with senderId "
						+ senderId);
				return;
			}

			for (Link link : linkMaps.get(theParser.idProducersMap
					.get(senderId))) {
				if (Integer.valueOf(link.getEndProducer().getLocalId()) == epId) {
					if (aLink != null) {
						// System.err
						// /
						// .println("Warning: Several links of the same type are embedded.");
					}
					aLink = link;
				}
			}

			if (aLink == null) {
				System.err.println(keyword + ": Null link.");
				return;
			}

			if (theParser.maxTimestamp < timeStamp)
				theParser.maxTimestamp = timeStamp;

			linkMaps.get(theParser.idProducersMap.get(senderId)).remove(aLink);

			aLink.setEndTimestamp(timeStamp);
			aLink.setPage(theParser.page);
			theParser.eventList.add(aLink);
		}
	}

}
