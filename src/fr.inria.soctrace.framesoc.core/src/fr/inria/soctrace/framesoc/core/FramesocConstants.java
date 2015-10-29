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
package fr.inria.soctrace.framesoc.core;

/**
 * Management constants for Framesoc.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FramesocConstants {
			
	/**
	 * Framesoc tool types 
	 */
	public static enum FramesocToolType {		

		IMPORT("IMPORT"),
		ANALYSIS("ANALYSIS"),
		EXPORT("EXPORT");
		
		private String name;
		
		private FramesocToolType(String name){
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
		
		/**
		 * Return the type corresponding to a string.
		 * If no predefined type is found, the default is ANALYSIS.
		 * 
		 * @param type string containing the type name
		 * @return the corresponding type, or ANALYSIS if no match is found
		 */
		public static FramesocToolType getType(String type) {
			for (FramesocToolType t: FramesocToolType.values()) {
				if (type.equals(t.toString()))
					return t;
			}
			return ANALYSIS; // default
		}
	}	
	
	/**
	 * CSV Separator
	 */
	public static final String CSV_SEPARATOR = ",";

}
