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

import java.io.Serializable;

/**
 * Enumeration for DBMS.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public enum DBMS implements Serializable {
	
	MYSQL("mysql"),
	SQLITE("sqlite"),
	UNKNOWN("UNKNOWN_DBMS");
	
	private String name;
	
	private DBMS(String name){
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
	
	/**
	 * Convert a DBMS name in a DBMS object.
	 * 
	 * @param s name 
	 * @return the corresponding DBMS or {@link DBMS#UNKNOWN} if a wrong name is passed.
	 */
	public static DBMS toDbms(String s) {
		if (s.equals(MYSQL.toString()))
			return MYSQL;
		if (s.equals(SQLITE.toString()))
			return SQLITE;
		return UNKNOWN;
	}

}
