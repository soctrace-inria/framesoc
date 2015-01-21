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
 * Constants for Framesoc variables
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public enum FramesocBusVariable {

	/*
	 * Important note for the following two variables:
	 * TRACE_VIEW_SELECTED_TRACE
	 * TRACE_VIEW_CURRENT_TRACE_SELECTION
	 * 
	 * They are *always* set together in order to ensure consistency.
	 * Currently they are set:
	 * - by the Initializer (to null)
	 * - by the TraceTreeView
	 *   - in the handle of TOPIC_UI_FOCUSED_TRACE
	 *   - in the selection changed handler
	 * 
	 * TODO: maybe merge in the same variable (define the appropriate class...)
	 */
	
	/**
	 * Trace object corresponding to the selected trace.
	 * If more than one trace is selected, this variable stores the first selected trace.
	 */
	TRACE_VIEW_SELECTED_TRACE,

	/**
	 * Selection object corresponding to the current selection in the Traces view.
	 * It is updated each time one or more trace node are selected in this view.
	 * Therefore the ISelectionObject contains a list of TraceNode (only).
	 */
	TRACE_VIEW_CURRENT_TRACE_SELECTION;

}
