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
package fr.inria.soctrace.framesoc.ui.handlers;

import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusVariable;
import fr.inria.soctrace.framesoc.ui.utils.TraceSelection;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Class providing functionalities used by handlers
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class HandlerCommons {

	/**
	 * Check if the system DB is existing: to be called
	 * before each operation potentially dealing with the 
	 * System DB.
	 * 
	 * <p>
	 * If the DB does not exist, print a dialog.
	 * 
	 * @param event the handler event
	 * @return true if the SystemDB exists
	 * @throws ExecutionException 
	 */
	public static boolean checkSystemDB(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		try {
			if (!FramesocManager.getInstance().isSystemDBExisting()) {
				MessageDialog.openWarning(window.getShell(), "System DB instance not found", 
						"Initialize the system before performing any operation.");
				return false;
			}
		} catch (SoCTraceException e) {
			MessageDialog.openError(window.getShell(), "Exception", e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * Get the currently selected trace.
	 * 
	 * @param event the handler event
	 * @return the selected trace or null if we are unable to get a trace selection
	 * @throws ExecutionException
	 */
	public static Trace getSelectedTrace(ExecutionEvent event) throws ExecutionException {
		Trace selection = (Trace) FramesocBus.getInstance().getVariable(FramesocBusVariable.TRACE_VIEW_SELECTED_TRACE);
		if (selection == null) {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			MessageDialog.openError(window.getShell(), "Exception", "Unable to get the selected trace. Try selecting the trace again.");
			return null;
		}
		return selection;
	}
	
	/**
	 * Get the currently selected traces.
	 * 
	 * @param event the handler event
	 * @return the selected traces or an empty list
	 * @throws ExecutionException
	 */
	public static List<Trace> getSelectedTraces(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) FramesocBus.getInstance().getVariable(FramesocBusVariable.TRACE_VIEW_CURRENT_TRACE_SELECTION);
		if (selection == null) {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
			MessageDialog.openError(window.getShell(), "Exception", "Unable to get the selected traces. Try selecting the traces again.");
			return null;
		}
		return TraceSelection.getTracesFromSelection(selection);
	}


}
