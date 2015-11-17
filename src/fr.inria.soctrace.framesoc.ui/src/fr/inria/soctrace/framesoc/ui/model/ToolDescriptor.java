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
 * This class allows to launch a tool if it is present in the database.
 * Currently it is made to be use only with the message TOPIC_UI_LAUNCH_TOOL,
 * which is only handled by the {@link FramesocToolManager}.
 * 
 * It is possible to specify the tool to launch in several ways: 
 *  - Specify the tool as an instance of the tool itself (setTool()) 
 *  - Specify the toolname (setToolName())
 * 
 * It also provides the possibility to specify some input parameters by
 * providing an instance of IFramesocToolInput. It is the responsibility of the
 * sender to make sure that the correct version of the input is provided. If no
 * input is specified, {@link EmptyInput} will be used.
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
