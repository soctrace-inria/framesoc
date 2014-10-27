package fr.inria.soctrace.tools.importer.otf2.core;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.utils.IdManager;

public class Otf2PreParser {

	String defFile;
	Otf2Parser theParser;
	private IdManager etIdManager = new IdManager();
	private IdManager epIdManager = new IdManager();
	
	public Otf2PreParser(Otf2Parser aParser) {
		theParser = aParser;
		defFile = "/home/youenn/Documents/traces/ex_otf2/mpi_only/rennes_64_cg.C.64/printG.txt";
	}

	public void parseDef() {

		BufferedReader br;
		try {
			br = new BufferedReader(new InputStreamReader(new DataInputStream(
					new FileInputStream(defFile))));

			String line;
			while ((line = br.readLine()) != null) {
				if (line.isEmpty() || !line.contains(" "))
					continue;

				String keyword = line.substring(0, line.indexOf(" "));
				if (keyword.equals(Otf2Constants.CLOCK_PROPERTIES)) {
					parseClockProperties(line);
				}
				if (keyword.equals(Otf2Constants.REGION)) {
					parseRegion(line);
				}
				if (keyword.equals(Otf2Constants.LOCATION_GROUP)) {
					parseLocationGroup(line);
				}
				if (keyword.equals(Otf2Constants.SYSTEM_TREE_NODE)) {
					parseTreeNode(line);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void parseClockProperties(String aLine) {
		String conf = aLine.substring(Otf2Constants.CLOCK_PROPERTIES.length());
		conf = conf.trim();
		String[] clockInfo = conf.split(Otf2Constants.PROPERTY_SEPARATOR);
		for (int i = 0; i < clockInfo.length; i++) {
			String[] clockProperty = clockInfo[i].split(Otf2Constants.PARAMETER_SEPARATOR);
			if (clockProperty[0].trim().equals(Otf2Constants.CLOCK_TIME_OFFSET)) {
				theParser.timeOffset = Long.parseLong(clockProperty[1].trim());
			}
		}
	}

	// LOCATION_GROUP
	// 35 Name: "MPI Rank 35" <258>, Type: PROCESS, Parent:
	// "parapide-9.rennes.grid5000.fr" <5>
	public void parseLocationGroup(String aLine) {
		int id;
		String name = "";
		String type = "";
		String parentName = "";
		int parentId = EventProducer.NO_PARENT_ID;

		String conf = aLine.substring(Otf2Constants.LOCATION_GROUP.length());
		conf = conf.trim();

		String idString = conf.substring(0, conf.indexOf(" "));
		id = Integer.valueOf(idString);
		conf = conf.substring(idString.length());
		conf.trim();

		String[] groupInfo = conf.split(Otf2Constants.PROPERTY_SEPARATOR);
		for (int i = 0; i < groupInfo.length; i++) {
			String[] groupProperty = groupInfo[i].split(Otf2Constants.PARAMETER_SEPARATOR);

			if (groupProperty[0].trim().equals(Otf2Constants.GROUP_NAME)) {
				name = groupProperty[1].trim();
				int indexOfFirstQuote = name.indexOf("\"") + 1;
				name = name.substring(indexOfFirstQuote,
						name.indexOf("\"", indexOfFirstQuote));
			}
			if (groupProperty[0].trim().equals(Otf2Constants.GROUP_TYPE)) {
				type = groupProperty[1].trim();
				;
			}
			if (groupProperty[0].trim().equals(Otf2Constants.GROUP_PARENT)) {
				parentName = groupProperty[1].trim();

				if (!parentName.equals(Otf2Constants.NODE_UNKNOWN_PARENT)) {
					int indexOfFirstQuote = parentName.indexOf("\"") + 1;
					parentName = parentName.substring(indexOfFirstQuote,
							parentName.indexOf("\"", indexOfFirstQuote));
					parentId = getParentId(parentName);
				}
			}
		}

		theParser.producersMap.put(name, createProducer(name, id, type, parentId));
		theParser.idProducersMap.put(id, theParser.producersMap.get(name));
	}

	// SYSTEM_TREE_NODE
	// 2 Name: "paradent-22.rennes.grid5000.fr" <227>, Class: "node" <5>,
	// Parent: "Linux" <0>
	public void parseTreeNode(String aLine) {
		int id;
		String name = "";
		String type = "";
		String parentName = "";
		int parentId = EventProducer.NO_PARENT_ID;

		String conf = aLine.substring(Otf2Constants.SYSTEM_TREE_NODE.length());
		conf = conf.trim();

		String idString = conf.substring(0, conf.indexOf(" "));
		id = Integer.valueOf(idString);
		conf = conf.substring(idString.length());
		conf.trim();

		String[] nodeInfo = conf.split(Otf2Constants.PROPERTY_SEPARATOR);
		for (int i = 0; i < nodeInfo.length; i++) {
			String[] nodeProperty = nodeInfo[i].split(Otf2Constants.PARAMETER_SEPARATOR);

			if (nodeProperty[0].trim().equals(Otf2Constants.NODE_NAME)) {
				name = nodeProperty[1].trim();
				int indexOfFirstQuote = name.indexOf("\"") + 1;
				name = name.substring(indexOfFirstQuote,
						name.indexOf("\"", indexOfFirstQuote));
			}
			if (nodeProperty[0].trim().equals(Otf2Constants.NODE_TYPE)) {
				type = nodeProperty[1].trim();
				int indexOfFirstQuote = type.indexOf("\"") + 1;
				type = type.substring(indexOfFirstQuote,
						type.indexOf("\"", indexOfFirstQuote));
			}
			if (nodeProperty[0].trim().equals(Otf2Constants.NODE_PARENT)) {

				parentName = nodeProperty[1].trim();
				if (!parentName.equals(Otf2Constants.NODE_UNKNOWN_PARENT)) {
					int indexOfFirstQuote = parentName.indexOf("\"") + 1;
					parentName = parentName.substring(indexOfFirstQuote,
							parentName.indexOf("\"", indexOfFirstQuote));
					parentId = getParentId(parentName);
				}
			}
		}

		theParser.producersMap.put(name, createProducer(name, id, type, parentId));
		theParser.idProducersMap.put(id, theParser.producersMap.get(name));
	}

	// REGION
	// 135 Name: "MPI_Init" <143> (Aka. "MPI_Init" <143>), Descr.: "" <0>, Role:
	// FUNCTION, Paradigm: MPI, Flags: NONE, File: "MPI" <8>, Begin: 0, End: 0
	public void parseRegion(String aLine) {
		int id;
		String name = "";

		String conf = aLine.substring(Otf2Constants.REGION.length());
		conf = conf.trim();

		String idString = conf.substring(0, conf.indexOf(" "));
		id = Integer.valueOf(idString);
		conf = conf.substring(idString.length());

		conf = conf.trim();
		String[] regionInfo = conf.split(Otf2Constants.PROPERTY_SEPARATOR);
		for (int i = 0; i < regionInfo.length; i++) {
			String[] regionProperty = regionInfo[i].split(Otf2Constants.PARAMETER_SEPARATOR);

			if (regionProperty[0].trim().equals(Otf2Constants.REGION_NAME)) {
				name = regionProperty[1].trim();
				int indexOfFirstQuote = name.indexOf("\"") + 1;
				name = name.substring(indexOfFirstQuote,
						name.indexOf("\"", indexOfFirstQuote));
			}
		}

		theParser.types.put(name, createEventType(name, id, EventCategory.STATE));
	}

	public int getParentId(String aParent) {
		int parentId = EventProducer.NO_PARENT_ID;

		if (theParser.producersMap.containsKey(aParent))
			return theParser.producersMap.get(aParent).getId();

		return parentId;
	}

	public EventProducer createProducer(String name, int id, String type,
			int pid) {
		EventProducer anEP = new EventProducer(epIdManager.getNextId());

		anEP.setName(name);
		anEP.setParentId(pid);
		anEP.setType(type);
		anEP.setLocalId(String.valueOf(id));

		return anEP;
	}

	public EventType createEventType(String name, int id, int aCat) {
		EventType anET = new EventType(etIdManager.getNextId(), aCat);

		anET.setName(name);

		return anET;
	}

}
