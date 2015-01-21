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

import java.sql.Timestamp;

/**
 * Class containing utility methods 
 *
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class SoctraceUtils {
	
	/**
	 * Conversion setting to 0 the nanoseconds,
	 * to deal with a mysql driver bug:
	 * nanoseconds value is not correct.
	 * e.g. if the string does not contains nanoseconds
	 * a non deterministic value is used for nanoseconds.
	 * 
	 * @param ts the string
	 * @return the timestamp
	 */
	public static Timestamp stringToTimestamp(String ts) {
		Timestamp t = Timestamp.valueOf(ts);
		t.setNanos(0);
		return t;
	}
	
	/**
	 * Conversion setting to 0 the nanoseconds,
	 * to deal with the mysql driver bug affecting
	 * the reverse translation (see stringToTimestamp()).
	 * 
	 * @param ts timestamp
	 * @return the string 
	 */
	public static String timestampToString(Timestamp ts) {
		ts.setNanos(0);
		return ts.toString();
	}
	
}
