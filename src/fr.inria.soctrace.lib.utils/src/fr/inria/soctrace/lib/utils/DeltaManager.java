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
package fr.inria.soctrace.lib.utils;

/**
 * Utility to manage time intervals.
 * Time is in milliseconds.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class DeltaManager {

	long t1 = 0;
	long t2 = 0;
	
	/**
	 * Store start time
	 */
	public void start() {
		t1 = System.currentTimeMillis();
	}
	
	/**
	 * Store end time
	 */
	public void end() {
		t2 = System.currentTimeMillis();
	}

	/**
	 * Store end time and print delta message
	 * 
	 * @param s String to print before delta
	 */
	public void end(String s) {
		end();
		System.out.println(getMessage(s));
	}

	/**
	 * Store end time and return delta message
	 * 
	 * @param s String to print before delta
	 */
	public String endMessage(String s) {
		end();
		return getMessage(s);		
	}
	
	/**
	 * Get the delta 
	 * @return The delta in milliseconds
	 */
	public long getDelta() {
		return t2-t1;
	}
	
	/**
	 * Get the delta message
	 * @param s String to print before delta
	 * @return the delta message
	 */
	private String getMessage(String s) {
		return "[" + s + "] Delta: " + ( t2 -t1 ) + " ms";
	}
	
}
