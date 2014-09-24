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
package fr.inria.soctrace.framesoc.core.tools.model;

import fr.inria.soctrace.framesoc.core.tools.management.ArgumentsManager;


/**
 * Framesoc Tool Interface.
 * 
 * <p>
 * Each Framesoc Tool implemented as an Eclipse plugin
 * must provide a class implementing the following interface.
 * 
 * <p>
 * The tool has whole freedom in defining his arguments list,
 * with a single exception:
 * 
 * <blockquote>
 * IMPORT tools (parsers) should consider as trace files path
 * all the plain strings at the end of arguments list.
 * <p>
 * Example of arguments list:
 *   
 *   <pre>-p=2 -x -log=test.log file1.dat file2.dat</pre> 
 *   
 * In this command line the strings "file1.dat" and "file2.dat"
 * are trace files.<br>
 * The same applies for IMPORT tools written as external programs.
 * </blockquote>
 *   
 * <p>
 * A Framesoc tool can take advantage of the {@link ArgumentsManager}
 * class to parse the arguments list.
 *
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public interface IFramesocTool {
	
	/**
	 * Launch method for Framesoc tools.
	 * 
	 * @param args launching arguments
	 */
	void launch(String args[]);
	
	/**
	 * Framesoc is allowed to launch this tool only if
	 * the method returns true.
	 * Used to allow tools to validate user input in 
	 * Framesoc launch tool dialogs.
	 *
	 * @param args launching arguments that would be used
	 * @return true if the tool can be launched, false otherwise.
	 */
	boolean canLaunch(String[] args);

}
