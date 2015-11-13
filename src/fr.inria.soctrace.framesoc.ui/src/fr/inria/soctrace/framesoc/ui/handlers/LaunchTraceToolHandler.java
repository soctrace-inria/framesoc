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
import fr.inria.soctrace.framesoc.core.bus.FramesocBus;
import fr.inria.soctrace.framesoc.core.bus.FramesocBusTopic;
import fr.inria.soctrace.framesoc.ui.model.ToolDescriptor;
import fr.inria.soctrace.framesoc.ui.model.TraceConfigurationDescriptor;
import fr.inria.soctrace.framesoc.ui.utils.TraceSelection;
import fr.inria.soctrace.lib.model.Tool;
import fr.inria.soctrace.lib.model.utils.SoCTraceException;

/**
 * Handler for launch analysis tool command.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public class LaunchTraceToolHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Do nothing
		return null;
	}
	
	/**
	 * Display the launch tool dialog.
	 * 
	 * @param tools
	 *            analysis tools list
	 * @throws SoCTraceException
	 */
	public static void launchTool(List<Tool> tools) throws SoCTraceException {
		if (tools.isEmpty())
			return;

		Tool tool = tools.get(0);

		ToolDescriptor tDes = new ToolDescriptor();
		tDes.setTool(tool);
	
		TraceConfigurationDescriptor des = new TraceConfigurationDescriptor();
		// Remove prefix "plugin:"
		des.setToolID(tool.getCommand().substring(tool.getCommand().indexOf(":")+1));
		des.setTrace(TraceSelection.getCurrentSelectedTrace());

		// Launch (or set focus on) the tool
		FramesocBus.getInstance().send(
				FramesocBusTopic.TOPIC_UI_LAUNCH_TOOL, tDes);
		
		// Synchronize if necessary
		FramesocBus.getInstance().send(
				FramesocBusTopic.TOPIC_UI_SYNCH_TOOL, des);
	}
	
}
