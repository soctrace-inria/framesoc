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
/**
 * 
 */
package fr.inria.soctrace.tools.framesoc.exporter.dbexporter;

import java.io.File;

import fr.inria.soctrace.lib.model.Trace;

/**
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class ExporterInput {
	
	/**
	 * The trace we want to export
	 */
	public Trace trace;
		
	/**
	 * Export directory
	 */
	public String directory;

	/**
	 * Check parameters 
	 * @return true if all is OK
	 */
	public boolean check() {
		if (trace==null)
			return false;
		File dir = new File(directory);
		if (directory==null || !dir.isDirectory())
			return false;
		return true;
	}

	/**
	 * Debug print
	 */
	public void print() {
		System.out.println("Trace");
		trace.print();
		System.out.println("Directory");
		System.out.println(directory);
	}
	
}
