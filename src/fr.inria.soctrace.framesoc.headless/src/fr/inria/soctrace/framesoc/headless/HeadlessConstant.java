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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.inria.soctrace.framesoc.headless.launcher.HeadlessPluginLauncher;
import fr.inria.soctrace.framesoc.headless.launcher.PrintTracesLauncher;

/**
 * This class define the constants for the headless mode of Framesoc
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class HeadlessConstant {

	// Available programs
	// Utils
	public final static String TRACE_DETAILS = "print_traces";

	/**
	 * Create the map use to make the correspondence between the command line
	 * and the Framesoc programs
	 */
	public static final Map<String, HeadlessPluginLauncher> programs;
	static {
		Map<String, HeadlessPluginLauncher> aMap = new HashMap<String, HeadlessPluginLauncher>();
		aMap.put(TRACE_DETAILS, new PrintTracesLauncher());
		programs = Collections.unmodifiableMap(aMap);
	}
}
