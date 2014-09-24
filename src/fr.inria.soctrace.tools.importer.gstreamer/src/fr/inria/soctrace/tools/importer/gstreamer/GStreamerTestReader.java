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
package fr.inria.soctrace.tools.importer.gstreamer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.inria.soctrace.framesoc.core.tools.management.ArgumentsManager;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.EventType;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventTypeQuery;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;
import fr.inria.soctrace.lib.search.utils.IntervalDesc;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;

/**
 * Test class for DB read access.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class GStreamerTestReader {

	/**
	 * -db=dbname 
	 * 
	 * @param args
	 * @throws SoCTraceException 
	 */
	public static void main(String[] args) throws SoCTraceException {

		System.out.println("Args: ");
		for (String s: args) {	
			System.out.println(s);
		}

		if (args.length < 1) {
			System.err.println("Too few arguments");
			return;
		}

		ArgumentsManager argsm = new ArgumentsManager();
		argsm.parseArgs(args);
		argsm.printArgs();
		if (!checkArgs(argsm)) {
			System.err.println("wrong args");
			return;
		}
		String traceDbName = argsm.getOptions().get("db");
		readByPage(traceDbName);

		Map<String, Integer> name2id = buildTypeMap(traceDbName);
		Iterator<Entry<String, Integer>> it = name2id.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Integer> e = it.next();
			System.out.println("NAME: " + e.getKey() + ", ID: " + e.getValue());
		}

	}

	private static void readByPage(String traceDbName) {
		ITraceSearch search = null;
		try {	

			search = new TraceSearch();
			search.initialize();

			Trace trace = search.getTraceByDBName(traceDbName);
			long min = search.getMinTimestamp(trace);
			long max = search.getMaxTimestamp(trace);

			boolean GETBYPAGE = true;
			int page = 0;
			long D = 1000000000L;
			List<Event> elist = null;

			DeltaManager delta = new DeltaManager();
			DeltaManager all = new DeltaManager();
			all.start();
			do {

				long a = min + page*D;
				long b = a + D;

				delta.start();

				if (GETBYPAGE) {
					elist = search.getEventsByPage(trace, page);
					delta.end("read page: " + page);
				} else {
					elist = search.getEventsByInterval(trace, new IntervalDesc(a, b));
					delta.end("read page: " + page + " (" + a + ", " + b + ")" );					
				}				

				System.out.println("size: " + elist.size());
				page++;

				if (elist.size()>0) {
					if (elist.get(elist.size()-1).getTimestamp()>=max)
						break;
				}

			} while(true);
			all.end("all trace");

			search.uninitialize();

		} catch (SoCTraceException e) {
			e.printStackTrace();
		} finally {
			TraceSearch.finalUninitialize(search);
		}

	}

	private static Map<String, Integer> buildTypeMap(String traceDbName) throws SoCTraceException {
		TraceDBObject traceDB = null;
		try{
			traceDB = TraceDBObject.openNewIstance(traceDbName);
			
			// get the types list
			EventTypeQuery etq = new EventTypeQuery(traceDB);
			List<EventType> etlist = etq.getList();
			etq.clear();
			traceDB.close();

			// create the map
			Map<String, Integer> name2id = new HashMap<String, Integer>();
			for (EventType et: etlist) {
				name2id.put(et.getName(), et.getId());
			}
			return name2id;
			
		} finally {
			DBObject.finalClose(traceDB);
		}
	}

	private static boolean checkArgs(ArgumentsManager argsm) {
		if (!argsm.getOptions().containsKey("db"))
			return false;
		return true;
	}

}
