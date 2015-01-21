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
/**
 * 
 */
package fr.inria.soctrace.framesoc.ui.handlers;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.soctrace.framesoc.core.FramesocConstants.FramesocToolType;
import fr.inria.soctrace.framesoc.core.FramesocManager;
import fr.inria.soctrace.framesoc.ui.toolbar.AbstractMenuContribution;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Handler for commands launchable from the drop down toolbar menu.
 * 
 * <p>
 * These commands accept a parameter (the tool name) as specified in the
 * MANIFEST file of this plugin.
 * The toolbar commands, containing the parameter, are dynamically created
 * by the concrete subclasses of {@link AbstractMenuContribution}.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class DropDownLaunchHandler extends AbstractHandler {

	/**
	 * Logger
	 */
	private final static Logger logger = LoggerFactory.getLogger(DropDownLaunchHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		String par = event.getParameter("fr.inria.soctrace.framesoc.ui.commands.dropdown.toolName");
		logger.debug("Parameter value: {}", par);
		if (par == null)
			return null;
				
		try {
			
			Tool tool = FramesocManager.getInstance().getTool(par);
			logger.debug("Tool: {}", tool);
			if (tool == null)
				return null;	
			
			List<Tool> tools = new LinkedList<Tool>();
			tools.add(tool);
			if (tool.getType().equals(FramesocToolType.IMPORT.toString())) {
				IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
				ImportTraceHandler.launchImporter(window.getShell(), tools);
			} else {
				IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
				LaunchAnalysisToolHandler.launchTool(window.getShell(), tools);
			}
			
		} catch (SoCTraceException e) {
			e.printStackTrace();
		}
				
		return null;
	}

}
