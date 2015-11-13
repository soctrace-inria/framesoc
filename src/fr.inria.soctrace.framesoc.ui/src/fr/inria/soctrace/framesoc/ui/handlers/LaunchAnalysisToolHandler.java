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
package fr.inria.soctrace.framesoc.ui.handlers;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

import fr.inria.soctrace.framesoc.core.FramesocConstants.FramesocToolType;
import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.ui.dialogs.LaunchToolDialog;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;
import fr.inria.soctrace.lib.search.ITraceSearch;
import fr.inria.soctrace.lib.search.TraceSearch;

/**
 * Handler for launch analysis tool command.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class LaunchAnalysisToolHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		
		if (!HandlerCommons.checkSystemDB(event))
			return null;

		ITraceSearch traceSearch = null;
		try {
			traceSearch = new TraceSearch().initialize();
			List<Tool> tools = traceSearch.getToolByType(FramesocToolType.ANALYSIS.toString());
			traceSearch.uninitialize();
			
			if (tools.size() == 0) {
				MessageDialog.openInformation(window.getShell(), "No analysis tool registered", "There is no analysis tool registered to the system");
				return null;
			}
			
			launchTool(window.getShell(), tools);
		} catch (SoCTraceException e) {
			MessageDialog.openError(window.getShell(), "Error during the analysis", e.getMessage());
		} finally {
			TraceSearch.finalUninitialize(traceSearch);
		}

		return null;
	}
	
	/**
	 * Display the launch tool dialog.
	 * 
	 * @param shell shell
	 * @param tools analysis tools list
	 * @throws SoCTraceException
	 */
	public static void launchTool(Shell shell, List<Tool> tools) throws SoCTraceException {
		
		LaunchToolDialog dlg = new LaunchToolDialog(shell, tools);
		
		if (dlg.open() != Window.OK)
			return;
		
		// launch tool
		FramesocManager.getInstance().launchTool(dlg.getTool(), dlg.getInput());
	}

}
