/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Generoso Pagano - initial API and implementation
 ******************************************************************************/
/**
 * 
 */
package fr.inria.soctrace.tools.framesoc.exporter.input;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.lib.model.Trace;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ExporterInput implements IFramesocToolInput {
	
	/**
	 * The trace we want to export
	 */
	public List<Trace> traces = new ArrayList<Trace>();
		
	/**
	 * Export directory
	 */
	public String directory;

	/**
	 * Check parameters 
	 * @return true if all is OK
	 */
	public boolean check() {
		if (traces == null || traces.isEmpty())
			return false;
		File dir = new File(directory);
		if (directory == null || !dir.isDirectory() || !dir.canWrite())
			return false;
		return true;
	}

	/**
	 * Debug print
	 */
	public void print() {
		System.out.println("Trace:");
		for (Trace trace : traces) {
			trace.print();
		}
		System.out.println("Directory");
		System.out.println(directory);
	}

	@Override
	public String getCommand() {
		return "";
	}
	
}
