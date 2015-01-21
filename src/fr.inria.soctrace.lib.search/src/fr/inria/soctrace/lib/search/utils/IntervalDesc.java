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
package fr.inria.soctrace.lib.search.utils;

/**
 * Utility class used to represent a time interval.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class IntervalDesc {
	
	public long t1;
	public long t2;
	
	/**
	 * @param t1 start timestamp
	 * @param t2 end timestamp
	 */
	public IntervalDesc(long t1, long t2) {
		this.t1 = t1;
		this.t2 = t2;
	}
	
}
