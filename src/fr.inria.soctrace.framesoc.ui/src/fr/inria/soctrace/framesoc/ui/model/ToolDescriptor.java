/*******************************************************************************
 * Copyright (c) 2012-2015 INRIA.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Youenn Corre - initial API and implementation
 ******************************************************************************/
package fr.inria.soctrace.framesoc.ui.model;

import fr.inria.soctrace.framesoc.core.tools.model.IFramesocToolInput;
import fr.inria.soctrace.lib.model.Tool;

/**
 * This class extends TraceIntervalDescriptor in order to transmit more
 * information for synchronization between views.
 * 
 * This include the list of filtered event producers, or event types and if
 * a particular event should be focused on.
 * 
 * @author "Youenn Corre <youenn.corre@inria.fr>"
 */
public class ToolDescriptor extends TraceIntervalDescriptor {

	/**
	 * Can be used to specify the ID of the tool that is concerned by the
	 * message
	 */
	private String toolName = "";
	
	private Tool tool = null;
	
	private IFramesocToolInput toolInput = null;


	public String getToolName() {
		return toolName;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

	public IFramesocToolInput getToolInput() {
		return toolInput;
	}

	public void setToolInput(IFramesocToolInput toolInput) {
		this.toolInput = toolInput;
	}

	public Tool getTool() {
		return tool;
	}

	public void setTool(Tool tool) {
		this.tool = tool;
	}
	
}
