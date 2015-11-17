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
package fr.inria.soctrace.framesoc.headless;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import fr.inria.soctrace.framesoc.headless.launcher.HeadlessPluginLauncher;
import fr.inria.soctrace.framesoc.ui.FramesocUiStartup;

/**
 * This class allows to launch Framesoc in command line.
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class FramesocApplication implements IApplication {


	// Scanner for user input
	private Scanner scanner;

	// Manage headless programs available through extension points
	private CommandManager commandManager;

	// List of the available commands
	private List<String> commands;

	@Override
	public Object start(IApplicationContext context) throws Exception {
		System.out.println("Launching Framesoc in headless mode.");
		scanner = new Scanner(System.in);
		
		initialize();
		
		parseArgument(context.getArguments());

		scanner.close();
		return EXIT_OK;
	}

	
	private void initialize() {
		performBasicCheck();
		
		commands = new ArrayList<String>();
		commandManager = new CommandManager();
		commands.addAll(commandManager.getCommands().keySet());
		commands.addAll(HeadlessConstant.programs.keySet());
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}

	/**
	 * Print the currently supported Framesoc programs
	 */
	public void printCommands() {
		System.out.println("The currently supported commands are:");
		for (String command : commands) {
			System.out.println("\t" + command);
		}

		System.out
				.println("\nFor more info on a particular command, type '<COMMAND> --help'.\n");
	}

	/**
	 * Parse the provided arguments and launch the corresponding application
	 * 
	 * @param arguments
	 *            a map of arguments as provided by
	 *            IApplicationContext.getArguments())
	 */
	public void parseArgument(Map<?, ?> arguments) {
		String[] args = (String[]) arguments.get("application.args");

		// Check that arguments were provided
		if (args.length <= 0) {
			System.out.print("No command provided. ");
			printCommands();
			return;
		}

		if (commands.contains(args[0])) {
			// Remove the first argument since it is the program name
			List<String> argList = new ArrayList<String>(Arrays.asList(args));
			argList.remove(0);

			// Convert back to array
			String[] programArgs = new String[argList.size()];
			programArgs = argList.toArray(programArgs);

			getLauncher(args[0]).launch(programArgs);
		} else {
			System.out.println("Error: The command " + args[0]
					+ " is not supported in headless version.");
			printCommands();
			return;
		}
	}

	/**
	 * Check that the database is setup, and if not create one
	 */
	private boolean performBasicCheck() {
		FramesocUiStartup startup = new FramesocUiStartup();
		HeadlessSetup setup = new HeadlessSetup(scanner);

		// Check that database is correct
		while (!startup.validateConfFile(false)) {
			System.out.println("Invalid path to database system provided.");
			setup.launchSetup();
		}

		return true;
	}
	
	private HeadlessPluginLauncher getLauncher(String command) {
		if(HeadlessConstant.programs.containsKey(command))
			return HeadlessConstant.programs.get(command);
		if(commandManager.getCommands().containsKey(command)) 
			return commandManager.instantiateLauncher(command);
		
		return null;
	}
}
