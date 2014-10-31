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
package fr.inria.soctrace.tools.importer.otf2.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import fr.inria.soctrace.framesoc.core.tools.management.ILineProcessor;

public class Otf2PrintWrapperTests {

	public static void testPush(String filename, IProgressMonitor monitor) {
		System.out.println("Executing the wrapper");
		List<String> cargs = new ArrayList<String>();
		cargs.add("-G");
		cargs.add(filename);
		Otf2PrintWrapper wrapper = new Otf2PrintWrapper(cargs);
		wrapper.execute(monitor, new ILineProcessor() {
			@Override
			public void process(String line) {
				System.out.println("processing : " + line);
			}
		});
		System.out.println("Monitor cancelled: " + monitor.isCanceled());
		System.out.println("End of test");
	}
	
	public static void testPop(String filename, IProgressMonitor monitor) {
		try {
			System.out.println("Executing the wrapper");
			List<String> cargs = new ArrayList<String>();
			cargs.add("-G");
			cargs.add(filename);
			Otf2PrintWrapper wrapper = new Otf2PrintWrapper(cargs);
			BufferedReader br = wrapper.execute(monitor);
			String line = "";
			while ((line = br.readLine()) != null && !monitor.isCanceled()) {
				System.out.println("processing : " + line);
			}
			System.out.println("Monitor cancelled: " + monitor.isCanceled());
			br.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
