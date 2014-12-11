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
package fr.inria.soctrace.framesoc.core.tools.model;

import fr.inria.soctrace.lib.model.Tool;

/**
 * Base abstract class for Framesoc plugin tools.
 * 
 * @author "Generoso Pagano <generoso.pagano@inria.fr>"
 */
public abstract class FramesocTool implements IFramesocTool {

	/**
	 * The Tool object related to this plugin tool
	 */
	private Tool tool = null;
	
	@Override
	public abstract void launch(String[] args);

	@Override
	public ParameterCheckStatus canLaunch(String[] args) {
		return new ParameterCheckStatus(true, "");
	}

	/**
	 * Set the Tool object related to this plugin tool
	 * @param tool The Tool object related to this plugin tool
	 */
	public void setTool(Tool tool) {
		this.tool = tool;
	}
	
	/**
	 * Return the Tool object related to this plugin tool
	 */
	public Tool getTool() {
		return tool;
	}
	
}
