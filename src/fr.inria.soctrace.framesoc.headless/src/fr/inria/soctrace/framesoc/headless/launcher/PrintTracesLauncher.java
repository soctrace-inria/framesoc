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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import fr.inria.soctrace.lib.model.Trace;

/**
 * This class prints details about the traces that are stored in the database
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class PrintTracesLauncher extends HeadlessPluginLauncher {

	@Override
	public void launch(String[] args) {
		options = new Options();
		defineOptions();

		CommandLineParser parser = new DefaultParser();
		CommandLine line;
		try {
			line = parser.parse(options, args);

			// Check if asking for help
			if (line.hasOption("h")) {
				printUsage();
				return;
			}

			printTraceDetail();
			
		} catch (ParseException e) {
			System.out
					.println("Error while parsing the arguments of the command line: "
							+ args.toString());
			e.printStackTrace();
			printUsage();
		}

	}

	private void printTraceDetail() {
		loadTraces();
		System.out.format("%-5s%-25s%-35s\n", "ID", "DATE", "NAME");
		System.out.format("-----------------------------------------------------------------\n");
		for(Trace trace: traces){
			System.out.format("%-5s%-25s%-35s\n", trace.getId(), trace.getTracingDate(), trace.getAlias());
		}
		
	}

	@Override
	public void printUsage() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("print_traces [OPTION]\n"
				+ "Print details about the traces contained in the database.",
				options);
	}
	
}
