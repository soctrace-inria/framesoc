package fr.inria.soctrace.tools.importer.otf2.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.State;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.tools.importer.otf2.reader.Otf2PrintWrapper;

public class Otf2StateParser {
	
	private Otf2Parser theParser;
	private IdManager eIdManager = new IdManager();
	
	private HashMap<EventProducer, State> stateMaps = new HashMap<EventProducer, State>();

	public Otf2StateParser(Otf2Parser aParser) {
		theParser = aParser;
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
				if (keyword.equals(Otf2Constants.ENTER_STATE)) {
					parseEnteringState(line);
				}
				if (keyword.equals(Otf2Constants.LEAVE_STATE)) {
					parseLeavingState(line);
				}
				
				if (theParser.elist.size() == Otf2Constants.PAGE_SIZE)
					theParser.page++;

				if (theParser.elist.size() >= Otf2Constants.PAGE_SIZE) {
					theParser.saveEvents(theParser.elist);
					
					theParser.numberOfEvents += theParser.elist.size();
					theParser.elist.clear();
				}
			}

			if (theParser.elist.size() > 0) {
				theParser.saveEvents(theParser.elist);
				theParser.numberOfEvents += theParser.elist.size();
				theParser.elist.clear();
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
		String[] groupProperty = conf.split(Otf2Constants.PARAMETER_SEPARATOR);
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
		
		if(theParser.minTimestamp == -1)
			theParser.minTimestamp = timeStamp;
		
		stateMaps.put(anEp, aState);
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
		String[] groupProperty = conf.split(Otf2Constants.PARAMETER_SEPARATOR);
		eventName = groupProperty[1].trim();
		int indexOfFirstQuote = eventName.indexOf("\"") + 1;
		eventName = eventName.substring(indexOfFirstQuote,
				eventName.indexOf("\"", indexOfFirstQuote));
		
		EventProducer anEp = theParser.idProducersMap.get(epId);
		State aState = stateMaps.get(anEp);
		if(!aState.getType().getName().equals(eventName))
			System.out.println("Error while creating state!!!!!!");
		
		if(theParser.maxTimestamp < timeStamp)
			theParser.maxTimestamp = timeStamp;
		
		aState.setEndTimestamp(timeStamp);
		aState.setPage(theParser.page);
		theParser.elist.add(aState);
	}

}
