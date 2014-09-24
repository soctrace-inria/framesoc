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

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.regex.Pattern;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.tools.management.ArgumentsManager;
import fr.inria.soctrace.framesoc.core.tools.management.PluginImporterJob;
import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IPluginToolJobBody;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.Configuration;
import fr.inria.soctrace.lib.utils.Configuration.SoCTraceProperty;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.importer.pajedump.core.PJDumpConstants;
import fr.inria.soctrace.tools.importer.pajedump.core.PJDumpParser;

/**
 * Paje dump importer tool.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PajeDumpImporter extends FramesocTool {

	private final static Logger logger = LoggerFactory.getLogger(PajeDumpImporter.class);

	/**
	 * Plugin Tool Job body: we use a Job since we have to perform a long
	 * operation and we don't want to freeze the UI.
	 */
	public class PJDumpImporterPluginJobBody implements IPluginToolJobBody {

		private String args[];

		public PJDumpImporterPluginJobBody(String[] args) {
			this.args = args;
		}

		@Override
		public void run(IProgressMonitor monitor) {
			DeltaManager delta = new DeltaManager();

			logger.debug("Args: ");

			for (String s : args) {
				logger.debug(s);
			}

			ArgumentsManager argsm = new ArgumentsManager();
			argsm.parseArgs(args);
			argsm.printArgs();

			String pattern = Pattern.quote(System.getProperty("file.separator"));
			int numberOfTraces = argsm.getTokens().size();
			int currentTrace = 1;
			for (String traceFile : argsm.getTokens()) {

				if (monitor.isCanceled())
					break;
				
				delta.start();

				String sysDbName = Configuration.getInstance().get(
						SoCTraceProperty.soctrace_db_name);

				String t[] = traceFile.split(pattern);
				String t2 = t[t.length - 1];
				if (t2.endsWith(PJDumpConstants.TRACE_EXT))
					t2 = t2.replace(PJDumpConstants.TRACE_EXT, "");
				String traceDbName = FramesocManager.getInstance().getTraceDBName(t2);

				SystemDBObject sysDB = null;
				TraceDBObject traceDB = null;

				try {

					// open system DB
					sysDB = new SystemDBObject(sysDbName, DBMode.DB_OPEN);
					// create new trace DB
					traceDB = new TraceDBObject(traceDbName, DBMode.DB_CREATE);

					// parsing
					PJDumpParser parser = new PJDumpParser(sysDB, traceDB, traceFile);
					parser.parseTrace(monitor, currentTrace, numberOfTraces);

				} catch (SoCTraceException ex) {
					System.err.println(ex.getMessage());
					ex.printStackTrace();
					System.err.println("Import failure. Trying to rollback modifications in DB.");
					if (sysDB != null)
						try {
							sysDB.rollback();
						} catch (SoCTraceException e) {
							e.printStackTrace();
						}
					if (traceDB != null)
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
					currentTrace++;
				}
			}
			delta.end("All trace imported");
		}

	}

	@SuppressWarnings("unused")
	private boolean checkArgs(ArgumentsManager argsm) {
		if (argsm.getTokens().size() != 1)
			return false;
		return true;
	}

	@Override
	public void launch(String[] args) {
		PluginImporterJob job = new PluginImporterJob("Paje Dump Importer",
				new PJDumpImporterPluginJobBody(args));
		job.setUser(true);
		job.schedule();
	}

	@Override
	public boolean canLaunch(String[] args) {
		if (args.length < 1)
			return false;

		for (String file : args) {
			File f = new File(file);
			if (!f.isFile())
				return false;
		}

		return true;
	}

}
