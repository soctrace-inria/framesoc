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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import fr.inria.soctrace.lib.model.EventProducer;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.utils.ModelConstants.EventCategory;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.utils.IdManager;
import fr.inria.soctrace.tools.importer.otf2.reader.Otf2PrintWrapper;

/**
 * Otf2 pre-parser.
 * 
 * Parse the output of "otf2-print -G" to retrieve: the event producer hierarchy, the event types
 * and other meta-data.
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
class Otf2PreParser {

	private Otf2Parser theParser;
	private int totNumberOfLines = 0;
	private IdManager etIdManager = new IdManager();
	private IdManager epIdManager = new IdManager();

	public Otf2PreParser(Otf2Parser aParser) {
		theParser = aParser;
	}

	/**
	 * Check that the otf2-print is working and parse the otf2 definitions (created with otf2-print
	 * with the -G option) and get all sort of info (event producers, event types, etc.)
	 * 
	 * @param monitor
	 *            progress monitor
	 * @throws SoCTraceException
	 */
	public void parseDefinitons(IProgressMonitor monitor) throws SoCTraceException {
		
		if (!checkExternalProgram()) {
			monitor.setCanceled(true);
			return;
		}
		
		try {
			monitor.subTask("Getting event producers and event types");
			List<String> args = new ArrayList<String>();
			args.add("-G");
			args.add(theParser.getTraceFile());
			Otf2PrintWrapper wrapper = new Otf2PrintWrapper(args);

			BufferedReader br = wrapper.execute(monitor);

			String line;

			while ((line = br.readLine()) != null && !monitor.isCanceled()) {
				if (line.isEmpty() || !line.contains(" "))
					continue;

				String keyword = line.substring(0, line.indexOf(" "));
				if (keyword.equals(Otf2Constants.CLOCK_PROPERTIES)) {
					parseClockProperties(line);
				} else if (keyword.equals(Otf2Constants.REGION)) {
					parseRegion(line);
				} else if (keyword.equals(Otf2Constants.LOCATION_GROUP)) {
					parseLocationGroup(line);
				} else if (keyword.equals(Otf2Constants.SYSTEM_TREE_NODE)) {
					parseTreeNode(line);
				} else if (keyword.equals(Otf2Constants.METRIC_MEMBER)) {
					parseMetricMember(line);
				} else if (keyword.equals(Otf2Constants.LOCATION)) {
					parseLocation(line);
				}
			}
			br.close();

			createStaticTypes();
			theParser.setNumberOfLines(totNumberOfLines);

		} catch (Exception e) {
			throw new SoCTraceException(e);
		}
	}

	private boolean checkExternalProgram() {
		List<String> args = new ArrayList<>(1);
		args.add("--version");
		Otf2PrintWrapper wrapper = new Otf2PrintWrapper(args);
		BufferedReader br = wrapper.execute(new NullProgressMonitor());
		try {
			String line;
			if ((line = br.readLine()) != null) {
				Pattern p = Pattern.compile("otf2-print: version (\\d\\.\\d)");
				Matcher m = p.matcher(line);
				return m.find();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Parse a location line to get the number of events.
	 * 
	 * @param aLine
	 *            a location line
	 */
	private void parseLocation(String aLine) {
		Pattern pattern = Pattern.compile("# Events: (\\d+)");
		Matcher m = pattern.matcher(aLine);
		int val = 0;
		if (m.find()) {
			val = Integer.parseInt(m.group(1));
		}
		totNumberOfLines += val;
	}

	/**
	 * Get the starting time offset for the timestamps
	 * 
	 * @param aLine
	 *            clock properties line
	 */
	private void parseClockProperties(String aLine) {
		String conf = aLine.substring(Otf2Constants.CLOCK_PROPERTIES.length());
		conf = conf.trim();
		String[] clockInfo = conf.split(Otf2Constants.PROPERTY_SEPARATOR);
		for (int i = 0; i < clockInfo.length; i++) {
			String[] clockProperty = clockInfo[i].split(Otf2Constants.PARAMETER_SEPARATOR);
			if (clockProperty[0].trim().equals(Otf2Constants.CLOCK_TIME_OFFSET)) {
				theParser.setTimeOffset(Long.parseLong(clockProperty[1].trim()));
			}
			if (clockProperty[0].trim().equals(Otf2Constants.CLOCK_GRANULARITY)) {
				theParser.setTimeGranularity(Long.parseLong(clockProperty[1].trim()));
			}
		}
	}

	/**
	 * Parse a node of the MPI cluster, and create the corresponding event producer
	 * 
	 * @param aLine
	 *            string with the info about the producer
	 */
	private void parseLocationGroup(String aLine) {
		int id;
		String name = "";
		String type = "";
		String parentName = "";
		int parentId = EventProducer.NO_PARENT_ID;

		String conf = aLine.substring(Otf2Constants.LOCATION_GROUP.length());
		conf = conf.trim();

		// Get the "in-trace" id of the producer
		String idString = conf.substring(0, conf.indexOf(" "));
		id = Integer.valueOf(idString);
		conf = conf.substring(idString.length());
		conf.trim();

		String[] groupInfo = conf.split(Otf2Constants.PROPERTY_SEPARATOR);
		for (int i = 0; i < groupInfo.length; i++) {
			String[] groupProperty = groupInfo[i].split(Otf2Constants.PARAMETER_SEPARATOR);

			// Get the name
			if (groupProperty[0].trim().equals(Otf2Constants.GROUP_NAME)) {
				name = groupProperty[1].trim();
				int indexOfFirstQuote = name.indexOf("\"") + 1;
				name = name.substring(indexOfFirstQuote, name.indexOf("\"", indexOfFirstQuote));
			}
			// Get the type
			if (groupProperty[0].trim().equals(Otf2Constants.GROUP_TYPE)) {
				type = groupProperty[1].trim();
			}
			// Get the parent event producer
			if (groupProperty[0].trim().equals(Otf2Constants.GROUP_PARENT)) {
				parentName = groupProperty[1].trim();

				// If it has a parent
				if (!parentName.equals(Otf2Constants.NODE_UNKNOWN_PARENT)) {
					// Get the parent ID
					int indexOfFirstQuote = parentName.indexOf("\"") + 1;
					parentName = parentName.substring(indexOfFirstQuote,
							parentName.indexOf("\"", indexOfFirstQuote));
					parentId = getParentId(parentName);
				}
			}
		}

		theParser.getProducersMap().put(name, createProducer(name, id, type, parentId));
		theParser.getIdProducersMap().put(id, theParser.getProducersMap().get(name));
	}

	/**
	 * Parse the non-leaf producer of the producer hierarchy tree and create the corresponding event
	 * producer
	 * 
	 * @param aLine
	 *            system tree node line
	 */
	private void parseTreeNode(String aLine) {
		int id;
		String name = "";
		String type = "";
		String parentName = "";
		int parentId = EventProducer.NO_PARENT_ID;

		String conf = aLine.substring(Otf2Constants.SYSTEM_TREE_NODE.length());
		conf = conf.trim();

		// Get the in-trace ID
		String idString = conf.substring(0, conf.indexOf(" "));
		id = Integer.valueOf(idString);
		conf = conf.substring(idString.length());
		conf.trim();

		String[] nodeInfo = conf.split(Otf2Constants.PROPERTY_SEPARATOR);
		for (int i = 0; i < nodeInfo.length; i++) {
			String[] nodeProperty = nodeInfo[i].split(Otf2Constants.PARAMETER_SEPARATOR);

			// Get the name
			if (nodeProperty[0].trim().equals(Otf2Constants.NODE_NAME)) {
				name = nodeProperty[1].trim();
				int indexOfFirstQuote = name.indexOf("\"") + 1;
				name = name.substring(indexOfFirstQuote, name.indexOf("\"", indexOfFirstQuote));
			}
			// Get the type
			if (nodeProperty[0].trim().equals(Otf2Constants.NODE_TYPE)) {
				type = nodeProperty[1].trim();
				int indexOfFirstQuote = type.indexOf("\"") + 1;
				type = type.substring(indexOfFirstQuote, type.indexOf("\"", indexOfFirstQuote));
			}
			// Get the parent
			if (nodeProperty[0].trim().equals(Otf2Constants.NODE_PARENT)) {

				parentName = nodeProperty[1].trim();
				// If it has a parent
				if (!parentName.equals(Otf2Constants.NODE_UNKNOWN_PARENT)) {
					int indexOfFirstQuote = parentName.indexOf("\"") + 1;
					parentName = parentName.substring(indexOfFirstQuote,
							parentName.indexOf("\"", indexOfFirstQuote));
					parentId = getParentId(parentName);
				}
			}
		}

		theParser.getProducersMap().put(name, createProducer(name, id, type, parentId));
		theParser.getIdProducersMap().put(id, theParser.getProducersMap().get(name));
	}

	/**
	 * Create an event type
	 * 
	 * @param aLine
	 *            a region line
	 */
	private void parseRegion(String aLine) {
		String name = "";

		String conf = aLine.substring(Otf2Constants.REGION.length());
		conf = conf.trim();

		// Parse the id but ignore it since we have no use for it
		String idString = conf.substring(0, conf.indexOf(" "));
		conf = conf.substring(idString.length());

		conf = conf.trim();
		String[] regionInfo = conf.split(Otf2Constants.PROPERTY_SEPARATOR);
		for (int i = 0; i < regionInfo.length; i++) {
			String[] regionProperty = regionInfo[i].split(Otf2Constants.PARAMETER_SEPARATOR);

			// Get the name
			if (regionProperty[0].trim().equals(Otf2Constants.REGION_NAME)) {
				name = regionProperty[1].trim();
				int indexOfFirstQuote = name.indexOf("\"") + 1;
				name = name.substring(indexOfFirstQuote, name.indexOf("\"", indexOfFirstQuote));
			}
		}

		theParser.getTypes().put(name, createEventType(name, EventCategory.STATE));
	}

	/**
	 * Create the event type for a metric
	 * 
	 * @param aLine
	 *            a metric line
	 */
	private void parseMetricMember(String aLine) {

		if (theParser.ignoreVariables())
			return;

		String name = "";

		String conf = aLine.substring(Otf2Constants.METRIC_MEMBER.length());
		conf = conf.trim();

		// Parse the id
		String idString = conf.substring(0, conf.indexOf(" "));
		conf = conf.substring(idString.length());

		conf = conf.trim();
		String[] regionInfo = conf.split(Otf2Constants.PROPERTY_SEPARATOR);
		for (int i = 0; i < regionInfo.length; i++) {
			String[] regionProperty = regionInfo[i].split(Otf2Constants.PARAMETER_SEPARATOR);

			// Get the name
			if (regionProperty[0].trim().equals(Otf2Constants.REGION_NAME)) {
				name = regionProperty[1].trim();
				int indexOfFirstQuote = name.indexOf("\"") + 1;
				name = name.substring(indexOfFirstQuote, name.indexOf("\"", indexOfFirstQuote));
			}
		}

		theParser.getTypes().put(name, createEventType(name, EventCategory.VARIABLE));
	}

	/**
	 * Given a producer name, get the corresponding ID
	 * 
	 * @param aParent
	 *            the parent name
	 * @return the parent id
	 */
	private int getParentId(String aParent) {
		if (theParser.getProducersMap().containsKey(aParent))
			return theParser.getProducersMap().get(aParent).getId();
		return EventProducer.NO_PARENT_ID;
	}

	/**
	 * Create an event producer based on the given parameters
	 * 
	 * @param name
	 *            event producer name
	 * @param id
	 *            "In-trace" ID of the producer (set as localID)
	 * @param type
	 *            event producer type
	 * @param pid
	 *            parent ID
	 * @return The created event producer
	 */
	private EventProducer createProducer(String name, int id, String type, int pid) {
		EventProducer anEP = new EventProducer(epIdManager.getNextId());

		anEP.setName(name);
		anEP.setParentId(pid);
		anEP.setType(type);
		anEP.setLocalId(String.valueOf(id));

		return anEP;
	}

	/**
	 * Create an event type based on the given parameters
	 * 
	 * @param name
	 *            type name
	 * @param cat
	 *            soctrace category of the event
	 * @return The created event type
	 */
	private EventType createEventType(String name, int cat) {
		EventType anET = new EventType(etIdManager.getNextId(), cat);
		anET.setName(name);
		return anET;
	}

	/**
	 * Create some event types that are not present in the definition file but that corresponds to
	 * keywords in the OTF2 specifications
	 */
	private void createStaticTypes() {
		theParser.getTypes().put(Otf2Constants.MPI_COMM,
				createEventType(Otf2Constants.MPI_COMM, EventCategory.LINK));
		theParser.getTypes().put(Otf2Constants.MPI_COLLECTIVE,
				createEventType(Otf2Constants.MPI_COLLECTIVE, EventCategory.PUNCTUAL_EVENT));
		theParser.getTypes().put(Otf2Constants.MPI_RECEIVE_REQUEST,
				createEventType(Otf2Constants.MPI_RECEIVE_REQUEST, EventCategory.PUNCTUAL_EVENT));
		theParser.getTypes().put(Otf2Constants.MPI_SEND_COMPLETE,
				createEventType(Otf2Constants.MPI_SEND_COMPLETE, EventCategory.PUNCTUAL_EVENT));
		theParser.getTypes().put(Otf2Constants.MPI_METRIC,
				createEventType(Otf2Constants.MPI_METRIC, EventCategory.VARIABLE));
	}

}
