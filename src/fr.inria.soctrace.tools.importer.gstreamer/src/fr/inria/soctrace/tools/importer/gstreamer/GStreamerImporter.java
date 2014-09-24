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

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.tools.management.ArgumentsManager;
import fr.inria.soctrace.framesoc.core.tools.management.PluginImporterJob;
import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IPluginToolJobBody;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.tools.importer.gstreamer.core.GStreamerParser;

/**
 * GStreamer Parser Tool
 *  
 * Arguments list: a single trace file
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class GStreamerImporter extends FramesocTool {

	/**
	 * Plugin Tool Job body: we use a Job since we have to
	 * perform a long operation and we don't want to freeze the UI.
	 */
	public class GStreamerImporterPluginJobBody implements IPluginToolJobBody {

		private String args[];
		
		public GStreamerImporterPluginJobBody(String[] args) {
			this.args = args;
		}

		@Override
		public void run(IProgressMonitor monitor) {
			
			DeltaManager delta = new DeltaManager();
			delta.start();
							
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
				parser.parseTrace(monitor);
								
			} catch ( SoCTraceException ex ) {
				System.err.println(ex.getMessage());
				ex.printStackTrace();
				System.err.println("Import failure. Trying to rollback modifications in DB.");
				if (sysDB!=null)
					try {
						sysDB.rollback();
					} catch (SoCTraceException e) {
						e.printStackTrace();
					}
				if (traceDB!=null)
					try {
						traceDB.dropDatabase();
					} catch (SoCTraceException e) {
						e.printStackTrace();
					}				
			} finally {
				// close the trace DB and the system DB (commit)
				DBObject.finalClose(traceDB);
				DBObject.finalClose(sysDB);
				delta.end("Import trace");
			}
		}
	}
	
	@Override
	public void launch(String[] args) {
		PluginImporterJob job = new PluginImporterJob("GStreamer Importer", new GStreamerImporterPluginJobBody(args));
		job.setUser(true);
		job.schedule();
	}
	
	@Override
	public boolean canLaunch(String[] args) {
		if (args.length<1)
			return false;
		
		String file = args[0];
				
		File f = new File(file);
		if (!f.isFile())
			return false;
		
		return true;
	}


}
