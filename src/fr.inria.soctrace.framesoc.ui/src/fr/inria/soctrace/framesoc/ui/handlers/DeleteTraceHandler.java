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

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.lib.model.Trace;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Handler for delete trace command.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class DeleteTraceHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		if (!HandlerCommons.checkSystemDB(event))
			return null;

		try {
			List<Trace> traces = HandlerCommons.getSelectedTraces(event);

			StringBuilder sb = new StringBuilder("The following traces and the corresponding DBs will be deleted. ");
			sb.append("Are you sure?");
			sb.append("\n\n");
			for (Trace t: traces) {
				sb.append("* " + t.getAlias() + "\n");
			}
			
			boolean del = MessageDialog.openConfirm(window.getShell(), "Delete Traces", sb.toString());
			if (!del)
				return null;
			
			// delete the trace
			for (Trace t: traces)
				FramesocManager.getInstance().deleteTrace(t);

		} catch (SoCTraceException e) {
			MessageDialog.openError(window.getShell(), "Error deleting the trace", e.getMessage());
		}

		return null;
	}

}
