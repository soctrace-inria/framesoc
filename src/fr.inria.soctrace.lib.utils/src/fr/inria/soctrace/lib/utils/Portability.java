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
 * Class containing different utilities for OS portability.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 *
 */
public class Portability {
	
	/**
	 * Enumerate for OS types
	 */
	public enum OSTYPE {
		UNIX,
		WIN,
		UNKNOWN
	}
	
	/**
	 * Cache variable for OS type
	 */
	private final static OSTYPE osType = init();
	
	/* public methods */
	
	/** 
	 * @return The user home
	 */
	public static String getUserHome() {
        return System.getProperty("user.home");
	}
	
	/**
	 * @return The OS name in lower case
	 */
	public static String getOSName() {
        return System.getProperty("os.name").toLowerCase();
	}
	
	/**
	 * @return The OS Type (OSTYPE enumerate)
	 */
	public static OSTYPE getOSType() {
        return osType;
	}

	/**
	 * Normalize a path according to the OS.
	 * More precisely use / or \ in paths, depending on the OS. 
	 * @param path path to normalize
	 * @return a normalized path
	 */
	public static String normalize(String path) {
        if (osType == OSTYPE.UNIX) {
            path = path.replace("\\", "/");
        } 
        else if (osType == OSTYPE.WIN) {
            path = path.replace("/", "\\");                    
        }
		return path;
	}
	
	/**
	 * Get the OS path separator.
	 * If the OS type is not known, the unix path separator 
	 * is returned.
	 * 
	 * @return the OS path separator
	 */
	public static String getPathSeparator() {
        if (osType == OSTYPE.UNIX) {
        	return "/";
        } 
        else if (osType == OSTYPE.WIN) {
        	return "\\";                    
        }
        else 
        	return "/";
	}
	
	/* utilities */
	
	private static OSTYPE init() {
		String osName = getOSName();
        if (osName.indexOf("nux") >= 0)
            return OSTYPE.UNIX;
        if (osName.indexOf("win") >= 0)
            return OSTYPE.WIN;
		return OSTYPE.UNKNOWN;                    
	}
}
