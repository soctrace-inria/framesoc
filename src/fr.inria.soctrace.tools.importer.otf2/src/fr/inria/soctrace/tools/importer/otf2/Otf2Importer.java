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
package fr.inria.soctrace.tools.importer.otf2;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.tools.management.ArgumentsManager;
import fr.inria.soctrace.framesoc.core.tools.management.PluginImporterJob;
import fr.inria.soctrace.framesoc.core.tools.model.FramesocTool;
import fr.inria.soctrace.framesoc.core.tools.model.IPluginToolJobBody;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.storage.DBObject;
import fr.inria.soctrace.lib.storage.DBObject.DBMode;
import fr.inria.soctrace.lib.storage.SystemDBObject;
import fr.inria.soctrace.lib.storage.TraceDBObject;
import fr.inria.soctrace.lib.utils.DeltaManager;
import fr.inria.soctrace.tools.importer.otf2.core.Otf2Constants;
import fr.inria.soctrace.tools.importer.otf2.core.Otf2Parser;

/**
 * Otf2 importer tool.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class Otf2Importer extends FramesocTool {

	private final static Logger logger = LoggerFactory.getLogger(Otf2Importer.class);

	/**
	 * Plugin Tool Job body: we use a Job since we have to perform a long operation and we don't
	 * want to freeze the UI.
	 */
	public class Otf2ImporterPluginJobBody implements IPluginToolJobBody {

		private String args[];

		public Otf2ImporterPluginJobBody(String[] args) {
			this.args = args;
		}

		@Override
		public void run(IProgressMonitor monitor) {
			DeltaManager delta = new DeltaManager();
			delta.start();

			logger.debug("Args: ");
			for (String s : args) {
				logger.debug(s);
			}

			ArgumentsManager argsm = new ArgumentsManager();
			argsm.parseArgs(args);
			argsm.printArgs();

			boolean novar = argsm.getFlags().contains(Otf2Constants.OPT_NO_VAR);

			Assert.isTrue(argsm.getTokens().size() == 1);
			String traceFile = argsm.getTokens().get(0);
			if (monitor.isCanceled())
				return;

			String traceDbName = getNewTraceDBName(traceFile);

			SystemDBObject sysDB = null;
			TraceDBObject traceDB = null;

			try {
				// open system DB
				sysDB = SystemDBObject.openNewIstance();
				// create new trace DB
				traceDB = new TraceDBObject(traceDbName, DBMode.DB_CREATE);

				// parsing
				Otf2Parser parser = new Otf2Parser(sysDB, traceDB, traceFile, novar);
				parser.parseTrace(monitor);

			} catch (SoCTraceException ex) {
				System.err.println(ex.getMessage());
				ex.printStackTrace();
				System.err.println("Import failure. Trying to rollback modifications in DB.");
				if (sysDB != null) {
					try {
						sysDB.rollback();
					} catch (SoCTraceException e) {
						e.printStackTrace();
					}
				}
				if (traceDB != null) {
					try {
						traceDB.dropDatabase();
					} catch (SoCTraceException e) {
						e.printStackTrace();
					}
				}
			} finally {
				// close the trace DB and the system DB (commit)
				DBObject.finalClose(traceDB);
				DBObject.finalClose(sysDB);
				delta.end("Import trace");
			}
		}
	}

	private String getNewTraceDBName(String traceFile) {
		String basename = FilenameUtils.getBaseName(traceFile);
		String extension = FilenameUtils.getExtension(traceFile);
		if (extension.equals(Otf2Constants.TRACE_EXT)) {
			basename = basename.replace(Otf2Constants.TRACE_EXT, "");
		}
		return FramesocManager.getInstance().getTraceDBName(basename);
	}

	@Override
	public void launch(String[] args) {
		PluginImporterJob job = new PluginImporterJob("Otf2 Importer",
				new Otf2ImporterPluginJobBody(args));
		job.setUser(true);
		job.schedule();
	}

	@Override
	public boolean canLaunch(String[] args) {

		ArgumentsManager argsm = new ArgumentsManager();
		try {
			// do this in a try block, since the method is called also for
			// invalid input (it is called each time input changes)
			argsm.parseArgs(args);
		} catch (IllegalArgumentException e) {
			return false;
		}

		// check if the (single) file is specified
		if (argsm.getTokens().size() != 1)
			return false;

		// check if the file exists
		String file = argsm.getTokens().get(0);
		File f = new File(file);
		if (!f.isFile())
			return false;

		return true;
	}

}
