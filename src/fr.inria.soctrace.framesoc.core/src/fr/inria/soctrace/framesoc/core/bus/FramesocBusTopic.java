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
package fr.inria.soctrace.framesoc.core.bus;

/**
 * Constants for Framesoc topics.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public enum FramesocBusTopic {
	
	/**
	 * Event fired when a view gets focus (e.g. event table) to update the focused trace
	 * in shared views (e.g. TraceTreeView)
	 * The event body is the Trace object corresponding to the trace shown in the 
	 * currently focused view.
	 * Note that, when the handle method is called in the bus listener after this event
	 * is fired, the value of the bus variables containing the selected trace is NOT
	 * valid, since it is one of the handle (the TraceTreeView.handle()) that set it.
	 */
	TOPIC_UI_FOCUSED_TRACE,
	
	/**
	 * Event fired when TraceTreeView must be synchronized from DB.
	 * The event body is a boolean stating if the event must be processed or not.
	 */
	TOPIC_UI_SYNCH_TRACES_NEEDED,

	/**
	 * Event fired when TraceTreeView has been synchronized from DB.
	 * The event body is a map of  trace lists (ADD, DELETE, UPDATE)
	 */
	TOPIC_UI_TRACES_SYNCHRONIZED,

	/**
	 * Event fired when TraceTreeView must be simply refreshed (DB is not used)
	 * using the current trace objects stored in the TraceNodes.
	 * The event body is null.
	 */
	TOPIC_UI_REFRESH_TRACES_NEEDED,

	/**
	 * Event fired after the system has been initialized.
	 * The event body is null.
	 */
	TOPIC_UI_SYSTEM_INITIALIZED,
	
	/**
	 * Event fired when we want to display a time interval in a trace table view.
	 * The event body is a TraceIntervalDescriptor.
	 */
	TOPIC_UI_TABLE_DISPLAY_TIME_INTERVAL,
		
	/**
	 * Event fired when we want to display a time interval in a Gantt chart.
	 * The event body is a TraceIntervalDescriptor.
	 */
	TOPIC_UI_GANTT_DISPLAY_TIME_INTERVAL,

	/**
	 * Event fired by the FramesocColorsDialog when the user saves a new set of colors.
	 * The event body is a ColorsChangeDescriptor.
	 */
	TOPIC_UI_COLORS_CHANGED,

	/**
	 * Event fired when we want to display a trace in a density histogram.
	 * The event body is a TraceIntervalDescriptor.
	 */
	TOPIC_UI_HISTOGRAM_DISPLAY_TIME_INTERVAL,

	/**
	 * Event fired when we want to switch the highlight state of some traces.
	 * The event body is a list of traces.
	 */
	TOPIC_UI_HIGHLIGHT_TRACES,
	
	/**
	 * Event fired when we want to display a time interval in a Pie Chart.
	 * The event body is a TraceIntervalDescriptor.
	 * The Pie Chart should handle the statistics operator selection in 
	 * a convenient way (e.g., proposing a dialog to the user).
	 */	
	TOPIC_UI_PIE_DISPLAY_TIME_INTERVAL,
	
	/**
	 * Event fired by the user to synchronize all the open views of a given
	 * trace group. The synchronization will occur on the time interval and,
	 * depending on the enabled options, the event type and event producer
	 * filters.
	 */
	TOPIC_UI_SYNCHRONIZE_TIME_AND_FILTER,
	
	/**
	 * Event fired when the user is requesting the launching of a tool.
	 * The event body should be the current active trace.
	 * 
	 * The event body should be of the type ToolDescriptor
	 */
	TOPIC_UI_LAUNCH_TOOL,

	/**
	 * Event fired when a tool required synchronization with another tool.
	 * Synchronization can occur on the trace, the time interval and/or the
	 * event type/producer filters.
	 * 
	 * The event body should be an instance of
	 * {@link TraceConfigurationDescriptor} containing the concerned tool (given
	 * as the tool activator ID) and the elements needed for synchronization
	 */
	TOPIC_UI_SYNCH_TOOL;
	
}
