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
 * <pre>
 * 
 * WARNING: The following types of events are not currently supported (i.e., they are ignored) 
 * in the importer. This list is based on the list of all event records in the OTF2 
 * documentation:
 * 
 * - BUFFER_FLUSH
 * - MEASUREMENT_ON_OFF
 * - MPI_REQUEST_TEST MPI_REQUEST_CANCEL 
 * - All the OMP related events (OmpFork, OmpJoin, OmpAcquireLock, OmpReleaseLock,
 *   OmpTaskCreate, OmpTaskSwitch, OmpTaskComplete, etc.)
 * 
 * - PARAMETER_STRING, PARAMETER_INT, PARAMETER_UNSIGNED_INT
 * 
 * - All the RMA events (RMA_WIN_CREATE, RMA_WIN_DESTROY RMA_COLLECTIVE_BEGIN,
 *   RMA_COLLECTIVE_END, RMA_GROUP_SYNC, RMA_REQUEST_LOCK, RMA_ACQUIRE_LOCK,
 *   RMA_TRY_LOCK, RMA_RELEASE_LOCK, RMA_SYNC RMA_WAIT_CHANGE, RMA_PUT, RMA_GET,
 *   RMA_ATOMIC, etc.)
 * 
 * - All the thread events (THREAD_FORK, THREAD_JOIN, etc.)
 *
 * Also, among the supported types of events, some of the event type parameters are also ignored: 
 * - the parameters of the event types given in the REGION definitions 
 * - some of the parameters of the metric variable
 * 
 * </pre>
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
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
	 * Parser options
	 */
	public static final String OPT_NO_VAR = "novar";

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
	final static String VALUE_SEPARATOR = ";";

	final static String CLOCK_PROPERTIES = "CLOCK_PROPERTIES";
	final static String LOCATION = "LOCATION";
	final static String LOCATION_GROUP = "LOCATION_GROUP";
	final static String REGION = "REGION";
	final static String SYSTEM_TREE_NODE = "SYSTEM_TREE_NODE";
	final static String METRIC_MEMBER = "METRIC_MEMBER";

	/**
	 * Specific definition properties
	 */
	final static String CLOCK_TIME_OFFSET = "Global Offset";
	final static String CLOCK_GRANULARITY = "Ticks per Seconds";

	final static String GROUP_NAME = "Name";
	final static String GROUP_TYPE = "Type";
	final static String GROUP_PARENT = "Parent";

	final static String NODE_NAME = "Name";
	final static String NODE_TYPE = "Class";
	final static String NODE_PARENT = "Parent";
	final static String NODE_UNKNOWN_PARENT = "UNDEFINED";

	final static String REGION_NAME = "Name";

	final static String METRIC_NAME = "Name";
	final static String METRIC_DESCR = "Descr.";
	final static String METRIC_TYPE = "Type";
	final static String METRIC_MODE = "Mode";

	final static String METRIC_VALUE = "METRIC_VALUE";

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
