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
package fr.inria.soctrace.tools.importer.otf2.core;

/**
 * Constants for Otf2 parser
 * 
 * TODO: add more
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class Otf2Constants {
	
	/**
	 * Amount of work for the progress monitor
	 */
	public static final int WORK = Integer.MAX_VALUE;
	
	/**
	 * Page size
	 */
	public static final int PAGE_SIZE = 200000;

	/**
	 * Trace Type name
	 */
	public static final String TRACE_TYPE = "otf2";
	public static final String TRACE_EXT = ".otf2";
		
	/**
	 * Category labels
	 */
	public static final String EVENT = "Event";
	public static final String STATE = "State";
	public static final String LINK = "Link";
	public static final String VARIABLE = "Variable";
	
	/**
	 * Global definition parsing constants
	 */
	final static String PROPERTY_SEPARATOR = ",";
	final static String PARAMETER_SEPARATOR = ":";

	final static String CLOCK_PROPERTIES = "CLOCK_PROPERTIES";
	final static String LOCATION_GROUP = "LOCATION_GROUP";
	final static String REGION = "REGION";
	final static String SYSTEM_TREE_NODE = "SYSTEM_TREE_NODE";
	final static String CLOCK_TIME_OFFSET = "Global Offset";

	final static String GROUP_NAME = "Name";
	final static String GROUP_TYPE = "Type";
	final static String GROUP_PARENT = "Parent";

	final static String NODE_NAME = "Name";
	final static String NODE_TYPE = "Class";
	final static String NODE_PARENT = "Parent";
	final static String NODE_UNKNOWN_PARENT = "UNDEFINED";

	final static String REGION_NAME = "Name";
	
	/**
	 * Event parsing constants
	 */
	final static String ENTER_STATE = "ENTER";
	final static String LEAVE_STATE = "LEAVE";
	
	final static String METRIC = "METRIC";
	final static String MPI_RECV = "MPI_RECV";
	final static String MPI_SEND = "MPI_SEND";
	final static String MPI_IRECV = "MPI_IRECV";
	final static String MPI_ISEND = "MPI_ISEND";
	final static String MPI_IRECV_REQUEST = "MPI_IRECV_REQUEST";
	final static String MPI_ISEND_COMPLETE = "MPI_ISEND_COMPLETE";
	final static String MPI_COLLECTIVE_BEGIN = "MPI_COLLECTIVE_BEGIN";
	final static String MPI_COLLECTIVE_END = "MPI_COLLECTIVE_END";
	
	/**
	 * Artificial event type
	 */
	final static String MPI_COMM = "MPI_COMM";
	final static String MPI_COLLECTIVE = "MPI_COLLECTIVE";
	final static String MPI_RECEIVE_REQUEST = "MPI_RECEIVE_REQUEST";
	final static String MPI_SEND_COMPLETE = "MPI_SEND_COMPLETE";
	final static String MPI_METRIC = "MPI_METRIC";
	
}
