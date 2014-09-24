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
package fr.inria.soctrace.framesoc.ui.utils;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;

import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusVariable;
import fr.inria.soctrace.framesoc.ui.model.TraceNode;
import fr.inria.soctrace.lib.model.Trace;

/**
 * Utility class used to store currently selected and currently
 * shown Trace in several view (e.g. Histogram, Events table)
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public final class TraceSelection {

	/**
	 * Get the current selected trace from the bus
	 * @return the currentSelected trace, or null if no trace is selected
	 */
	public static Trace getCurrentSelectedTrace() {
		return (Trace) FramesocBus.getInstance().getVariable(FramesocBusVariable.TRACE_VIEW_SELECTED_TRACE);
	}

	/**
	 * Given a structured selection containing a TraceNode,
	 * it returns the first contained Trace object.
	 * 
	 * @param selection structured selection containing a TraceNode
	 * @return the first Trace
	 */
	public static Trace getTraceFromSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			if (sel.getFirstElement() instanceof TraceNode)
				return ((TraceNode) sel.getFirstElement()).getTrace();
		}
		return null;
	}

	/**
	 * Given a structured selection containing one or more TraceNode,
	 * it returns all the contained traces.
	 * If no TraceNode is found, an empty list is returned.
	 * 
	 * @param selection structured selection containing one or more TraceNode
	 * @return a list of Traces found, or an empty list if no trace is found
	 */
	public static List<Trace> getTracesFromSelection(ISelection selection) {
		List<Trace> traces = new LinkedList<Trace>();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;
			for (Object o: sel.toList()) {
				if (o instanceof TraceNode)
					traces.add(((TraceNode) o).getTrace());
			}
		}
		return traces;
	}

	/**
	 * Check if the selection is valid.
	 * A selection is considered valid if it is a not empty structured 
	 * selection containing only TraceNode objects.
	 * 
	 * @param selection structured selection object
	 * @return true if the selection is valid, false otherwise
	 */
	public static boolean isSelectionValid(ISelection selection) {
		if (!(selection instanceof IStructuredSelection))
			return false;
		IStructuredSelection ss = (IStructuredSelection) selection;
		if (ss.isEmpty())
			return false;
		for (Object o: ss.toList()) {
			if ( !(o instanceof TraceNode ) ) {
				return false;
			}
		}
		return true;
	}

}
