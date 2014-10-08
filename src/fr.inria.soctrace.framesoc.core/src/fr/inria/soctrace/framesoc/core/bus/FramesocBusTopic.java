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
package fr.inria.soctrace.framesoc.core.bus;

/**
 * Constants for Framesoc topics.
 * 
 * TODO use enum 
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class FramesocBusTopic {
	
	/**
	 * Event fired when a view gets focus (e.g. event table) to update the focused trace
	 * in shared views (e.g. TraceTreeView)
	 * The event body is the Trace object corresponding to the trace shown in the 
	 * currently focused view.
	 * Note that, when the handle method is called in the bus listener after this event
	 * is fired, the value of the bus variables containing the selected trace is NOT
	 * valid, since is one of the handle (the TraceTreeView.handl()) that set it.
	 */
	public static final String TOPIC_UI_FOCUSED_TRACE = "TOPIC_UI_FOCUSED_TRACE"; //$NON-NLS-1$
	
	/**
	 * Event fired when TraceTreeView must be synchronized from DB.
	 * The event body is a boolean stating if the event must be processed or not.
	 */
	public static final String TOPIC_UI_SYNCH_TRACES_NEEDED = "TOPIC_UI_SYNCH_TRACES_NEEDED"; //$NON-NLS-1$

	/**
	 * Event fired when TraceTreeView has been synchronized from DB.
	 * The event body is a map of  trace lists (ADD, DELETE, UPDATE)
	 */
	public static final String TOPIC_UI_TRACES_SYNCHRONIZED = "TOPIC_UI_TRACES_SYNCHRONIZED"; //$NON-NLS-1$

	/**
	 * Event fired when TraceTreeView must be simply refreshed (DB is not used)
	 * using the current trace objects stored in the TraceNodes.
	 * The event body is null.
	 */
	public static final String TOPIC_UI_REFRESH_TRACES_NEEDED = "TOPIC_UI_REFRESH_TRACES_NEEDED"; //$NON-NLS-1$

	/**
	 * Event fired after the system has been initialized.
	 * The event body is null.
	 */
	public static final String TOPIC_UI_SYSTEM_INITIALIZED = "TOPIC_UI_SYSTEM_INITIALIZED"; //$NON-NLS-1$
	
	/**
	 * Event fired when we want to display a time interval in a trace table view.
	 * The event body is a TraceIntervalDescriptor.
	 */
	public static final String TOPIC_UI_TABLE_DISPLAY_TIME_INTERVAL = "TOPIC_UI_TABLE_DISPLAY_TIME_INTERVAL"; //$NON-NLS-1$
		
	/**
	 * Event fired when we want to display a time interval in a Gantt chart.
	 * The event body is a TraceIntervalDescriptor.
	 */
	public static final String TOPIC_UI_GANTT_DISPLAY_TIME_INTERVAL = "TOPIC_UI_GANTT_DISPLAY_TIME_INTERVAL"; //$NON-NLS-1$

	/**
	 * Event fired by the FramesocColorsDialog when the user saves a new set of colors.
	 * The event body is a ColorsChangeDescriptor.
	 */
	public static final String TOPIC_UI_COLORS_CHANGED = "TOPIC_UI_COLORS_CHANGED"; //$NON-NLS-1$

	/**
	 * Event fired when we want to display a trace in a density histogram.
	 * The event body is a TraceIntervalDescriptor.
	 * XXX: at the moment, the whole trace is always displayed, even if a different
	 * time interval is specified.
	 */
	public static final String TOPIC_UI_HISTOGRAM_DISPLAY = "TOPIC_UI_HISTOGRAM_DISPLAY"; //$NON-NLS-1$

	/**
	 * Event fired when we want to switch the highlight state of some traces.
	 * The event body is a list of traces.
	 */
	public static final String TOPIC_UI_HIGHLIGHT_TRACES = "TOPIC_UI_HIGHLIGHT_TRACES"; //$NON-NLS-1$

}
