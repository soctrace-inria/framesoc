package fr.inria.soctrace.tools.importer.otf2.core;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.State;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.IdManager;

public class Otf2StateParser {
	String eventFile;
	Otf2Parser theParser;
	private IdManager eIdManager = new IdManager();
	
	HashMap<EventProducer, State> stateMaps = new HashMap<EventProducer, State>();

	public Otf2StateParser(Otf2Parser aParser) {
		theParser = aParser;
		eventFile = "/home/youenn/Documents/traces/ex_otf2/mpi_only/rennes_64_cg.C.64/print.txt";
	}

	public void parseState(IProgressMonitor monitor) {

		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new DataInputStream(
					new FileInputStream(eventFile))));

			String line;
			while ((line = br.readLine()) != null) {
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
	
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
