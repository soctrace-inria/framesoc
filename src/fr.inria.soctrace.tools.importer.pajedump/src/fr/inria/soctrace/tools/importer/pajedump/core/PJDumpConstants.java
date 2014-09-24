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
package fr.inria.soctrace.tools.importer.pajedump.core;

/**
 * Constants for PJDump parser
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class PJDumpConstants {
	
	/**
	 * Amount of work for the progress monitor
	 */
	public static final int WORK = Integer.MAX_VALUE;
	
	/**
	 * Field separator
	 */
	public static final String SEPARATOR = ",\\s*";
		
	/**
	 * Page size
	 */
	public static final int PAGE_SIZE = 200000;

	/**
	 * Trace Type name
	 */
	public static final String TRACE_TYPE = "pjdump";
	public static final String TRACE_EXT = ".pjdump";
	
	/**
	 * Entity labels
	 */
	public static final String CONTAINER = "Container";
	public static final String EVENT = "Event";
	public static final String STATE = "State";
	public static final String LINK = "Link";
	public static final String VARIABLE = "Variable";
	
	/** Line positions for interesting columns */
	
	public static final int ENTITY = 0;
	
	//	Container, parentContainer, containerType, startTime, endTime, duration, name
	public static final int C_PARENT_CONTAINER = 1;
	public static final int C_TYPE = 2;
	public static final int C_NAME = 6;
	//	State, container, stateType, startTime, endTime, duration, imbrication, value
	public static final int S_CONTAINER = 1;
	public static final int S_TYPE = 2;
	public static final int S_START_TIME = 3;
	public static final int S_END_TIME = 4;
	public static final int S_IMBRICATION = 6;
	public static final int S_VALUE = 7;
	//	Variable, container, variableType, startTime, endTime, duration, value
	public static final int V_CONTAINER = 1;
	public static final int V_TYPE = 2;
	public static final int V_START_TIME = 3;
	public static final int V_VALUE = 6;
	//	Event, container, eventType, time, value
	public static final int E_CONTAINER = 1;
	public static final int E_TYPE = 2;
	public static final int E_TIME = 3;
	public static final int E_VALUE = 4;
	//	Link, container, linkType, startTime, endTime, duration, value, startContainer, endContainer
	public static final int L_TYPE = 2;
	public static final int L_START_TIME = 3;
	public static final int L_END_TIME = 4;
	public static final int L_VALUE = 6;
	public static final int L_START_CONTAINER = 7;
	public static final int L_END_CONTAINER = 8;

	/**
	 * Time shift exponent (nanoseconds)
	 */
	public static final int TIME_SHIFT = 9;
	
}
