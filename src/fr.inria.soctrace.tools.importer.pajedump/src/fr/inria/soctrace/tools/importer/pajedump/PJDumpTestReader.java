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
package fr.inria.soctrace.tools.importer.pajedump;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.tools.management.ArgumentsManager;
import fr.inria.soctrace.lib.model.Event;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.EventQuery;
import fr.inria.soctrace.lib.query.conditions.ConditionsConstants.ComparisonOperation;
import fr.inria.soctrace.lib.query.conditions.SimpleCondition;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;

public class PJDumpTestReader {

	private final static Logger logger = LoggerFactory.getLogger(PJDumpTestReader.class);
	
	/**
	 * -db=dbname 
	 * 
	 * @param args
	 * @throws SoCTraceException 
	 */
	public static void main(String[] args) throws SoCTraceException {

		logger.debug("Args: ");
		for (String s: args) {	
			logger.debug(s);
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
		read(traceDbName);
		
	}

	private static void read(String traceDbName) {
		try {	

			TraceDBObject traceDB = TraceDBObject.openNewIstance(traceDbName);
			
			long min = traceDB.getMinTimestamp();
			long max = traceDB.getMaxTimestamp();
			List<Event> elist = null;

			DeltaManager delta = new DeltaManager();

			long a = min;
			long b = Math.min(max, min+100000000000L);

			EventQuery query = new EventQuery(traceDB);
			query.setLoadParameters(false);
			query.setElementWhere(new SimpleCondition("TIMESTAMP", ComparisonOperation.BETWEEN, a + " AND " + b));

			delta.start();
			elist = query.getList();
			delta.end("read interval: (" + a + ", " + b + ")" );					

			logger.debug("size: {}", elist.size());
			
			traceDB.close();

		} catch (SoCTraceException e) {
			e.printStackTrace();
		}	

	}
	
	private static boolean checkArgs(ArgumentsManager argsm) {
		if (!argsm.getOptions().containsKey("db"))
			return false;
		return true;
	}

}
