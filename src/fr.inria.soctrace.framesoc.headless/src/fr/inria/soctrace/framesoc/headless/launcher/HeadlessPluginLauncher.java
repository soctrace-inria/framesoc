/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Youenn Corre - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.headless.launcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.cli.Options;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.query.TraceQuery;
import fr.inria.soctrace.lib.storage.SystemDBObject;

/**
 * This abstract class defines a set of methods required to launch a Framesoc
 * tool in command line
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public abstract class HeadlessPluginLauncher {

	// List of options for the program as defined by the CLI library
	protected Options options;
	
	protected List<Trace> traces;

	/**
	 * Function called to launch the program
	 * 
	 * @param args
	 *            arguments provided as input for the program
	 */
	public abstract void launch(String[] args);

	/**
	 * Print the help of the program
	 */
	public abstract void printUsage();
	
	public void defineOptions() {
		options.addOption("h", "help", false, "Print this help");
	}
	
	/**
	 * Load the traces present in the database 
	 * 
	 * @throws SoCTraceException
	 */
	protected void loadTraces() {
		traces = new ArrayList<Trace>();
		SystemDBObject sysDB;
		try {
			sysDB = FramesocManager.getInstance().getSystemDB();

			final TraceQuery tQuery = new TraceQuery(sysDB);
			traces = tQuery.getList();
			sysDB.close();
		} catch (SoCTraceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Sort alphabetically
		Collections.sort(traces, new Comparator<Trace>() {
			@Override
			public int compare(final Trace arg0, final Trace arg1) {
				return arg0.getId() - arg1.getId();
			}
		});
	}
}
