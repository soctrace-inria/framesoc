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
package fr.inria.soctrace.lib.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Utility class used to log events in a thread-safe way.
 * Useful for tests.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class History {
	
	private static final boolean DEBUG = true;
    private static LinkedList<String> records = new LinkedList<String>();

    /**
     * Add a string to the log.
     * 
     * @param record string to log
     */
    public static synchronized void add(String record) {
    	records.add(record);
    	if (DEBUG)
    		System.out.println(record);
    }

    /**
     * Print the log on the standard output.
     */
    public static synchronized void print() {
    	System.out.println("#######################################################");
        System.out.println("# EVENT LOG");
        System.out.println("");
        for (String s: records) {
            System.out.println(s);
        }
    	System.out.println("#######################################################");
    }
    
    /**
     * Print the log on a given file.
     * 
     * @param filePath file path
     */
    public static synchronized void trace(String filePath) {
    	try {
    		FileWriter fstream = new FileWriter(filePath);
			BufferedWriter out = new BufferedWriter(fstream);
	    	out.write("#######################################################\n");
	        out.write("# EVENT LOG\n\n");
	        for (String s: records) {
	            out.write(s + "\n");
	        }
	    	out.write("#######################################################\n");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    /**
     * Clear the log.
     */
    public static synchronized void clear() {
    	records.clear();
    }
       
}
