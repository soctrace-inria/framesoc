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

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.tools.management.ArgumentsManager;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.tools.importer.gstreamer.core.GStreamerParser;

/**
 * Test class providing a main to test the GStreamer parser.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class GStreamerTestMain {

	public static void main(String[] args) {
		DeltaManager delta = new DeltaManager();
		
		System.out.println("Args: ");
		for (String s: args) {	
			System.out.println(s);
		}
			
		if (args.length < 1) {
			System.err.println("Too few arguments");
			return;
		}
		
		String sysDbName = Configuration.getInstance().get(SoCTraceProperty.soctrace_db_name);
		String traceDbName = FramesocManager.getInstance().getTraceDBName("GSTREAMER");
		
		ArgumentsManager argsm = new ArgumentsManager();
		argsm.parseArgs(args);
		argsm.printArgs();
		
		SystemDBObject sysDB = null;
		TraceDBObject traceDB = null;

		try {
			
			// open system DB	
			sysDB = new SystemDBObject(sysDbName, DBMode.DB_OPEN);
			// create new trace DB
			traceDB = new TraceDBObject(traceDbName, DBMode.DB_CREATE);
			
			// parsing	
			GStreamerParser parser = new GStreamerParser(sysDB, traceDB, argsm);
			delta.start();
			parser.parseTrace(null);
			delta.end("Parse trace");
			
			// close the trace DB and the system DB (commit)
			traceDB.close();
			sysDB.close();
						
		} catch ( SoCTraceException ex ) {
			System.err.println(ex.getMessage());
			ex.printStackTrace();
			System.err.println("Import failure. Trying to rollback modifications.");
			if (sysDB!=null)
				try {
					sysDB.rollback();
					sysDB.close();
				} catch (SoCTraceException e) {
					e.printStackTrace();
				}
			if (traceDB!=null)
				try {
					traceDB.dropDatabase();
				} catch (SoCTraceException e) {
					e.printStackTrace();
				}				
		} 		
	}
}
